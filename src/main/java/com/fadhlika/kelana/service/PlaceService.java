package com.fadhlika.kelana.service;

import com.fadhlika.kelana.model.Place;
import com.fadhlika.kelana.repository.PlaceRepository;
import java.util.List;
import java.util.Optional;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> fetchPlaces(
            Optional<String> city,
            Optional<String> country,
            Optional<Integer> limit,
            Optional<Integer> offset) {
        try {
            return placeRepository.fetchPlaces(city, country, limit, offset);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Place> fetchPlaces(Double lat, Double lon, Double rad) throws Exception {
        try {
            GeometryFactory gf = new GeometryFactory();
            Point point = gf.createPoint(new Coordinate(lon, lat));

            int zone = (int) Math.floor((lon + 180) / 6) + 1;
            zone = Math.max(1, Math.min(zone, 60));
            int utmEpsg = (lat >= 0 ? 32600 : 32700) + zone;

            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
            CoordinateReferenceSystem utm = CRS.decode("EPSG:" + utmEpsg);

            MathTransform toUtm = CRS.findMathTransform(wgs84, utm);
            MathTransform toWgs84 = CRS.findMathTransform(utm, wgs84);

            Geometry pointIntMeters = JTS.transform(point, toUtm);
            Geometry bufferInMeters = pointIntMeters.buffer(rad);
            Geometry bufferWgs84 = JTS.transform(bufferInMeters, toWgs84);

            return placeRepository.fetchPlaces(bufferWgs84);
        } catch (Exception e) {
            throw e;
        }
    }
}
