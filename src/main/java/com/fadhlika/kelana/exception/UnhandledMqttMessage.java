package com.fadhlika.kelana.exception;

public class UnhandledMqttMessage extends RuntimeException {

    public UnhandledMqttMessage(String message) {
        super(message);
    }
}
