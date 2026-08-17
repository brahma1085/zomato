package com.zomato.search_service.repository;

import com.zomato.search_service.model.RestaurantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantDocument, String> {
    List<RestaurantDocument> findByNameOrDescription(String name, String description);
    List<RestaurantDocument> findByCuisinesContaining(String cuisine);
    List<RestaurantDocument> findByCity(String city);
}
