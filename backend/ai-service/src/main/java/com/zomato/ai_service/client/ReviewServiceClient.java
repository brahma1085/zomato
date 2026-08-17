package com.zomato.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "review-service")
public interface ReviewServiceClient {

    @GetMapping("/api/reviews/restaurant/{restaurantId}")
    List<Map<String, Object>> getReviewsForRestaurant(@PathVariable("restaurantId") Long restaurantId);
}
