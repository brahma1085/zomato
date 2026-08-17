package com.zomato.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    Map<String, Object> getUserProfile(@PathVariable("userId") String userId);
}
