package com.zomato.location_service.controller;

import com.zomato.location_service.dto.DistanceRequest;
import com.zomato.location_service.dto.DistanceResponse;
import com.zomato.location_service.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(@RequestBody DistanceRequest request) {
        return ResponseEntity.ok(locationService.calculateDistance(request));
    }
}
