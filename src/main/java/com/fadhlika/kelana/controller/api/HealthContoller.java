package com.fadhlika.kelana.controller.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadhlika.kelana.dto.Response;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/health")
public class HealthContoller {
    @GetMapping
    public Response<Void> healthCheck() {
        return new Response<>("healthy");
    }
}
