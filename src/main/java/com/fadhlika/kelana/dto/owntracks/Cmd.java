package com.fadhlika.kelana.dto.owntracks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Cmd(
                String _type,
                String action,
                String request,
                Integer status,
                Tour tour,
                List<Tour> tours,
                Integer ntours,
                Waypoints waypoints) implements Message {
        public static Cmd responseTours(List<Tour> tours) {
                return new Cmd("cmd", "response", "tours", null, null, tours, tours.size(), null);
        }

        public static Cmd responseTour(Tour tour) {
                return new Cmd("cmd", "response", "tour", 200, tour, null, null, null);
        }

        public static Cmd setWaypoints(
                        Waypoints waypoints) {
                return new Cmd("cmd", "setWaypoints", "tour", null, null, null, null, waypoints);
        }
}
