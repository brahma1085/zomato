package com.zomato.location_service.controller;

import com.zomato.location_service.dto.DistanceRequest;
import com.zomato.location_service.dto.DistanceResponse;
import com.zomato.location_service.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(@RequestBody DistanceRequest request) {
        logger.info("Received distance calculation request between ({}, {}) and ({}, {})",
                request.getSourceLat(), request.getSourceLon(), request.getDestLat(), request.getDestLon());
        return ResponseEntity.ok(locationService.calculateDistance(request));
    }
}
