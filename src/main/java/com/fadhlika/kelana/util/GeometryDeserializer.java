package com.fadhlika.kelana.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.io.IOException;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {
    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        GeoJsonReader reader = new GeoJsonReader();
        try {
            JsonNode node = p.getCodec().readTree(p);
            return reader.read(node.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
