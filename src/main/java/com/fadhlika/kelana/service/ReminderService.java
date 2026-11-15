package com.fadhlika.kelana.service;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import com.fadhlika.kelana.model.Location;
import com.fadhlika.kelana.repository.LocationRepository;

public class ReminderService {
    @Value("${reminder.lookup_radius}")
    private int lookupRadius;

    private final LocationRepository locationRepository;

    ReminderService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Async
    public synchronized void processReminder(int userId, Location location) {
        GeometricShapeFactory shape = new GeometricShapeFactory();
        Coordinate coordinate = location.getLonlat().getCoordinate();
        double diameter = 2d * (double) lookupRadius;
        double widthInDegrees = diameter / (40075000d * Math.cos(Math.toRadians(coordinate.y)) /
                360d);
        double heightInDegrees = diameter / 111320d;
        shape.setWidth(widthInDegrees);
        shape.setHeight(heightInDegrees);
        shape.setNumPoints(35);

        shape.setCentre(coordinate);

        Geometry bounds = shape.createCircle();

        List<Location> locations = locationRepository.findLocations(Optional.of(userId), null, null, null, null, null,
                null, null, Optional.of(bounds));

        if (locations.isEmpty()) {
            return;
        }

    }
}
