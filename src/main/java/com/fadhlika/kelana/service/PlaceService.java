package com.fadhlika.kelana.service;

import com.fadhlika.kelana.dto.FeatureCollection;
import com.fadhlika.kelana.dto.GeocodeProperties;
import com.fadhlika.kelana.model.Place;
import com.fadhlika.kelana.repository.PhotonRepository;
import com.fadhlika.kelana.repository.PlaceRepository;

import tools.jackson.core.type.TypeReference;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    private final PhotonRepository photonRepository;

    PlaceService(PlaceRepository placeRepository, PhotonRepository photonRepository) {
        this.placeRepository = placeRepository;
        this.photonRepository = photonRepository;
    }

    public List<Place> fetchPlaces(Double lon, Double lat, Double rad) throws Exception {
        try {
            List<Place> places = placeRepository.fetchPlaces(lon, lat, rad);

            if (places.isEmpty()) {
                FeatureCollection res = photonRepository.reverseGeocode(lat, lon, (int) Math.round(rad));
                places = res.features().stream().map(feature -> {
                    GeocodeProperties props = feature.convertProperties(
                            new TypeReference<GeocodeProperties>() {
                            });

                    return new Place(
                            "photon",
                            feature.getGeometry(),
                            props.type(),
                            props.postcode(),
                            props.countrycode(),
                            props.name(),
                            props.country(),
                            props.country(),
                            props.district(),
                            props.locality(),
                            props.street(),
                            props.state(),
                            feature);
                }).toList();
            }

            return places;
        } catch (Exception e) {
            throw e;
        }
    }
}
