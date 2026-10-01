package com.fadhlika.kelana.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record Stats(
        int totalPoints,
        int totalReverseGeocodedPoints,
        int totalCitiesVisited,
        int totalCountriesVisited,
        List<String> cities,
        List<String> countries,
        ZonedDateTime lastPointTimestamp) {

}
