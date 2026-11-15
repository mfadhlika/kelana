package com.fadhlika.kelana.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.locationtech.jts.geom.Geometry;

import com.fadhlika.kelana.dto.Feature;
import com.fadhlika.kelana.util.GeometryDeserializer;
import com.fadhlika.kelana.util.GeometrySerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

public record Place(
        int id,
        String provider,
        @JsonSerialize(using = GeometrySerializer.class) @JsonDeserialize(using = GeometryDeserializer.class) Geometry lonlat,
        String type,
        String postcode,
        String countryCode,
        String name,
        String country,
        String city,
        String district,
        String locality,
        String street,
        String state,
        Feature geodata,
        ZonedDateTime createdAt) {
    public Place(
            String provider,
            @JsonSerialize(using = GeometrySerializer.class) @JsonDeserialize(using = GeometryDeserializer.class) Geometry lonlat,
            String type,
            String postcode,
            String countryCode,
            String name,
            String country,
            String city,
            String district,
            String locality,
            String street,
            String state,
            Feature geodata) {
        this(0, provider, lonlat, type, postcode, countryCode, name, country, city, district, locality, street, state,
                geodata, ZonedDateTime.now(ZoneOffset.UTC));
    }
}
