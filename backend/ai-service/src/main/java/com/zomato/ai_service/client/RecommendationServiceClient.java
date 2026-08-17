package com.zomato.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "recommendation-service")
public interface RecommendationServiceClient {

    @GetMapping("/api/recommendations")
    List<Map<String, Object>> getRecommendations(@RequestParam("userId") String userId, @RequestParam("context") String context, @RequestParam(value = "location", required = false) String location, @RequestParam(value = "lat", required = false) Double lat, @RequestParam(value = "lng", required = false) Double lng);
}
