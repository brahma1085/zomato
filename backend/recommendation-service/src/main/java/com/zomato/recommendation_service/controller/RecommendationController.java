package com.zomato.recommendation_service.controller;

import com.zomato.recommendation_service.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "") String context,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, context, location, lat, lng));
    }
}
