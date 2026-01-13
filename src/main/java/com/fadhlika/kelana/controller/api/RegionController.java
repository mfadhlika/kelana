package com.fadhlika.kelana.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadhlika.kelana.dto.Feature;
import com.fadhlika.kelana.dto.FeatureCollection;
import com.fadhlika.kelana.dto.RegionProperties;
import com.fadhlika.kelana.dto.Response;
import com.fadhlika.kelana.model.User;
import com.fadhlika.kelana.service.RegionService;

@RestController
@RequestMapping("/api/v1/regions")
public class RegionController {
    @Autowired
    private RegionService regionService;

    @GetMapping
    public Response<FeatureCollection> getRegions() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Feature> features = regionService.fetchRegions(user.getId()).stream().map(curr -> {
            RegionProperties props = new RegionProperties(
                    curr.getDesc(),
                    curr.getBeaconUUID(),
                    curr.getBeaconMajor(),
                    curr.getBeaconMinor(),
                    curr.getRid(),
                    curr.getGeocode(),
                    curr.getCreatedAt());

            return new Feature(curr.getGeometry(), props);
        }).toList();

        return new Response<>(new FeatureCollection(features));
    }
}
