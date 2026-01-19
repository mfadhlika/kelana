package com.fadhlika.kelana.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.io.IOException;

public class GeometrySerializer extends JsonSerializer<Geometry> {

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        GeoJsonWriter writer = new GeoJsonWriter();
        writer.setEncodeCRS(false);
        String str = writer.write(value);
        gen.writeRawValue(str);
    }
}
