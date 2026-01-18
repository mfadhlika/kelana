package com.fadhlika.kelana.controller.mqtt.owntracks;

import com.fadhlika.kelana.gateways.MqttGateway;
import com.fadhlika.kelana.model.MqttMessage;
import com.fadhlika.kelana.model.User;
import com.fadhlika.kelana.service.MqttService;
import com.fadhlika.kelana.service.OwntracksService;
import com.fadhlika.kelana.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "mqtt.enable", havingValue = "true")
public class OwntracksMqttController {
    private final Logger logger = LoggerFactory.getLogger(OwntracksMqttController.class);

    @Value("${kelana.base_url}")
    private String baseUrl;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OwntracksService owntracksService;

    @Autowired
    private UserService userService;

    @Autowired
    private MqttService mqttService;

    @Autowired
    private MqttGateway mqttGateway;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic,
            @Header(MqttHeaders.ID) String id) {
        logger.info("handle message {} from {}", id, topic);

        UUID messageSerial;
        try {
            messageSerial = mqttService.saveMessage(payload, topic);
        } catch (Exception e) {
            throw e;
        }

        try {
            String username;
            String deviceId;
            String command;
            Pattern pattern = Pattern.compile(
                    "owntracks/(?<username>[a-zA-Z0-9-_]+)/(?<deviceId>[a-zA-Z0-9-_]+)/?(?<command>[a-zA-Z0-9-_]*)");
            Matcher matcher = pattern.matcher(topic);

            matcher.find();

            username = matcher.group("username");
            deviceId = matcher.group("deviceId");
            command = matcher.group("command");

            User user = (User) userService.loadUserByUsername(username);

            com.fadhlika.kelana.dto.owntracks.Message message = mapper.readValue(payload,
                    com.fadhlika.kelana.dto.owntracks.Message.class);

            Optional<?> res = this.owntracksService.handleMessage(user, deviceId, message);

            if (res.isPresent()) {
                var value = res.get();
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<com.fadhlika.kelana.dto.owntracks.Message> messages = (List<com.fadhlika.kelana.dto.owntracks.Message>) value;
                    for (com.fadhlika.kelana.dto.owntracks.Message msg : messages) {
                        String resPayload = mapper.writeValueAsString(message);
                        switch (msg) {
                            case com.fadhlika.kelana.dto.owntracks.Cmd e:
                                mqttGateway.publish(String.format("owntracks/%s/%s/cmd", username, deviceId),
                                        resPayload);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    com.fadhlika.kelana.dto.owntracks.Message msg = (com.fadhlika.kelana.dto.owntracks.Message) value;
                    String resPayload = mapper.writeValueAsString(msg);
                    switch (msg) {
                        case com.fadhlika.kelana.dto.owntracks.Cmd e:
                            mqttGateway.publish(String.format("owntracks/%s/%s/cmd", username, deviceId),
                                    resPayload);
                            break;
                        default:
                            break;
                    }
                }
            }

            mqttService.updateMessageStatus(messageSerial, MqttMessage.Status.PROCESSED);
        } catch (Exception e) {
            logger.error("error processing message: ", e);
            mqttService.updateMessageStatus(messageSerial, MqttMessage.Status.ERROR, e.getMessage());
        }
    }

}
