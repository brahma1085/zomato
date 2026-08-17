package com.zomato.search_service.service;

import com.zomato.search_service.model.RestaurantDocument;
import com.zomato.search_service.repository.RestaurantSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SearchService {

    private final RestaurantSearchRepository searchRepository;

    public SearchService(RestaurantSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public RestaurantDocument saveRestaurant(RestaurantDocument restaurant) {
        return searchRepository.save(restaurant);
    }

    public List<RestaurantDocument> searchRestaurants(String query) {
        return searchRepository.findByNameOrDescription(query, query);
    }

    public List<RestaurantDocument> getByCuisine(String cuisine) {
        return searchRepository.findByCuisinesContaining(cuisine);
    }

    public List<RestaurantDocument> getAll() {
        return StreamSupport.stream(searchRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }
}
