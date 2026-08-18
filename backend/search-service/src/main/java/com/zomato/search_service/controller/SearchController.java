package com.zomato.search_service.controller;

import com.zomato.search_service.model.RestaurantDocument;
import com.zomato.search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/index")
    public ResponseEntity<RestaurantDocument> indexRestaurant(@RequestBody RestaurantDocument restaurant) {
        logger.info("Received request to index restaurant: {}", restaurant.getName());
        return ResponseEntity.ok(searchService.saveRestaurant(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDocument>> search(@RequestParam String query) {
        logger.info("Received search request for query: {}", query);
        return ResponseEntity.ok(searchService.searchRestaurants(query));
    }

    @GetMapping("/cuisine")
    public ResponseEntity<List<RestaurantDocument>> searchByCuisine(@RequestParam String cuisine) {
        logger.info("Received search request by cuisine: {}", cuisine);
        return ResponseEntity.ok(searchService.getByCuisine(cuisine));
    }
}
