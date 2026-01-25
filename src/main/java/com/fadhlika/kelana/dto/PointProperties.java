package com.fadhlika.kelana.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record PointProperties(
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") ZonedDateTime timestamp,
                Integer altitude,
                Double speed,
                Integer course,
                Integer courseAccuracy,
                Integer accuracy,
                Integer verticalAccuracy,
                List<String> motions,
                String batteryState,
                Double batteryLevel,
                Double pressure,
                String deviceId,
                String ssid,
                FeatureCollection geocode,
                String rawData) {

}
