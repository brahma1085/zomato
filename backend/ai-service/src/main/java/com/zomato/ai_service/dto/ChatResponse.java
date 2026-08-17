package com.zomato.ai_service.dto;

import java.util.List;
import java.util.Map;

public class ChatResponse {
    private String message;
    private List<Map<String, Object>> restaurants;

    public ChatResponse() {}

    public ChatResponse(String message, List<Map<String, Object>> restaurants) {
        this.message = message;
        this.restaurants = restaurants;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, Object>> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Map<String, Object>> restaurants) {
        this.restaurants = restaurants;
    }
}
