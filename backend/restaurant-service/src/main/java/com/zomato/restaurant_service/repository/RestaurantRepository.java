package com.zomato.restaurant_service.repository;

import com.zomato.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCity(String city);
    List<Restaurant> findByCuisinesContaining(String cuisine);
}
