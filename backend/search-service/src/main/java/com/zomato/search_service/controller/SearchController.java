package com.zomato.search_service.controller;

import com.zomato.search_service.model.RestaurantDocument;
import com.zomato.search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/index")
    public ResponseEntity<RestaurantDocument> indexRestaurant(@RequestBody RestaurantDocument restaurant) {
        return ResponseEntity.ok(searchService.saveRestaurant(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDocument>> search(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchRestaurants(query));
    }

    @GetMapping("/cuisine")
    public ResponseEntity<List<RestaurantDocument>> searchByCuisine(@RequestParam String cuisine) {
        return ResponseEntity.ok(searchService.getByCuisine(cuisine));
    }
}
