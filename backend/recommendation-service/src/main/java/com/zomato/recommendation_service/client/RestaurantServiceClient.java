package com.zomato.recommendation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {
    @GetMapping("/api/restaurants")
    List<Map<String, Object>> getAllRestaurants(@org.springframework.web.bind.annotation.RequestParam(value = "city", required = false) String city, @org.springframework.web.bind.annotation.RequestParam(value = "lat", required = false) Double lat, @org.springframework.web.bind.annotation.RequestParam(value = "lng", required = false) Double lng);
}
