package com.fadhlika.kelana.controller.api;

import com.fadhlika.kelana.dto.Response;
import com.fadhlika.kelana.model.Place;
import com.fadhlika.kelana.service.PlaceService;
import java.util.List;
import java.util.Optional;
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
    public Response<List<Place>> fetchPlaces(
            @RequestParam Optional<String> city,
            @RequestParam Optional<String> country,
            @RequestParam Optional<Integer> limit,
            @RequestParam Optional<Integer> offset) {
        List<Place> places = placeService.fetchPlaces(
                city,
                country,
                limit,
                offset);

        return new Response<List<Place>>(places);
    }

    @GetMapping("/search")
    public Response<List<Place>> findPlaces(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam Double rad) throws Exception {
        List<Place> places = placeService.fetchPlaces(lon, lat, rad);

        return new Response<List<Place>>(places);
    }
}
