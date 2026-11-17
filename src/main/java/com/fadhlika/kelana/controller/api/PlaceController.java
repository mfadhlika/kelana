package com.fadhlika.kelana.controller.api;

import com.fadhlika.kelana.dto.Feature;
import com.fadhlika.kelana.dto.FeatureCollection;
import com.fadhlika.kelana.dto.PlaceProperties;
import com.fadhlika.kelana.dto.Response;
import com.fadhlika.kelana.model.Place;
import com.fadhlika.kelana.service.PlaceService;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;

    PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    public Response<FeatureCollection> findPlaces(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam Double rad) throws Exception {
        List<Place> places = placeService.fetchPlaces(lon, lat, rad);

        List<Feature> features = new ArrayList<>();
        for (Place place : places) {
            PlaceProperties props = new PlaceProperties(place.type(), place.postcode(), place.countryCode(),
                    place.name(), place.countryCode(), place.city(), place.district(), place.locality(), place.street(),
                    place.state());
            features.add(new Feature(place.lonlat(), props));
        }

        return new Response<>(new FeatureCollection(features));
    }
}
