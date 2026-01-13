package com.fadhlika.kelana.dto.owntracks;

public record Waypoint(
                String desc,
                Double lat,
                Double lon,
                Integer rad,
                int tst,
                String uuid,
                Integer major,
                Integer minor,
                String rid) implements Message {

}
