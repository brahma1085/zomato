package com.zomato.restaurant_service.controller;

import com.zomato.restaurant_service.model.Restaurant;
import com.zomato.restaurant_service.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        logger.info("Received request to create restaurant: {}", restaurant.getName());
        return ResponseEntity.ok(restaurantService.createRestaurant(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        logger.info("Received request to fetch restaurants (city: {}, lat: {}, lng: {})", city, lat, lng);
        if (lat != null && lng != null) {
            return ResponseEntity.ok(restaurantService.getRestaurantsByLocation(lat, lng));
        } else if (city != null && !city.isEmpty()) {
            return ResponseEntity.ok(restaurantService.getRestaurantsByCity(city));
        }
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        logger.info("Received request to fetch restaurant by id: {}", id);
        return restaurantService.getRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant details) {
        logger.info("Received request to update restaurant with id: {}", id);
        try {
            return ResponseEntity.ok(restaurantService.updateRestaurant(id, details));
        } catch (RuntimeException e) {
            logger.warn("Failed to update restaurant with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        logger.info("Received request to delete restaurant with id: {}", id);
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
