package com.zomato.restaurant_service.service;

import com.zomato.restaurant_service.model.Restaurant;
import com.zomato.restaurant_service.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ExternalRestaurantService externalRestaurantService;

    public RestaurantService(RestaurantRepository restaurantRepository, ExternalRestaurantService externalRestaurantService) {
        this.restaurantRepository = restaurantRepository;
        this.externalRestaurantService = externalRestaurantService;
    }

    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public List<Restaurant> getRestaurantsByCity(String city) {
        List<Restaurant> localRestaurants = restaurantRepository.findByCity(city);
        if (localRestaurants.size() < 5) {
            // Fetch real-time data from external API
            List<Restaurant> externalRestaurants = externalRestaurantService.fetchRestaurantsByCity(city);
            if (!externalRestaurants.isEmpty()) {
                // Save to local DB and add to results
                for (Restaurant r : externalRestaurants) {
                    try {
                        Restaurant saved = restaurantRepository.save(r);
                        localRestaurants.add(saved);
                    } catch (Exception e) {
                        System.err.println("Failed to save external restaurant: " + e.getMessage());
                    }
                }
            }
        }
        return localRestaurants;
    }

    public List<Restaurant> getRestaurantsByLocation(Double lat, Double lng) {
        // Fetch real-time data from external API for given lat/lng
        List<Restaurant> externalRestaurants = externalRestaurantService.fetchRestaurantsByLocation(lat, lng);
        List<Restaurant> localRestaurants = new java.util.ArrayList<>();
        if (!externalRestaurants.isEmpty()) {
            // Save to local DB and add to results
            for (Restaurant r : externalRestaurants) {
                try {
                    Restaurant saved = restaurantRepository.save(r);
                    localRestaurants.add(saved);
                } catch (Exception e) {
                    System.err.println("Failed to save external restaurant: " + e.getMessage());
                }
            }
        }
        return localRestaurants;
    }

    public Restaurant updateRestaurant(Long id, Restaurant details) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurant.setName(details.getName());
            restaurant.setDescription(details.getDescription());
            restaurant.setAddress(details.getAddress());
            restaurant.setCity(details.getCity());
            restaurant.setLatitude(details.getLatitude());
            restaurant.setLongitude(details.getLongitude());
            restaurant.setCuisines(details.getCuisines());
            restaurant.setAmbiences(details.getAmbiences());
            restaurant.setAverageRating(details.getAverageRating());
            restaurant.setTotalReviews(details.getTotalReviews());
            restaurant.setCostForTwo(details.getCostForTwo());
            restaurant.setHasParking(details.getHasParking());
            restaurant.setIsFamilyFriendly(details.getIsFamilyFriendly());
            restaurant.setOffersDelivery(details.getOffersDelivery());
            return restaurantRepository.save(restaurant);
        }).orElseThrow(() -> new RuntimeException("Restaurant not found with id " + id));
    }

    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}
