package com.zomato.restaurant_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.restaurant_service.model.Restaurant;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExternalRestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRestaurantService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private static final List<String> IMAGE_URLS = List.of(
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
            "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800",
            "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800",
            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800",
            "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
            "https://images.unsplash.com/photo-1544148103-0773bf10d330?w=800",
            "https://images.unsplash.com/photo-1537047902294-62a40c20a6ae?w=800",
            "https://images.unsplash.com/photo-1424847651672-bf20a4b0982b?w=800"
    );

    public ExternalRestaurantService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Restaurant> fetchRestaurantsByCity(String city) {
        List<Restaurant> restaurants = new ArrayList<>();
        try {
            // 1. Get Lat/Lng from Nominatim
            String nominatimUrl = "https://nominatim.openstreetmap.org/search?q=" + city + "&format=json&limit=1";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Zomato-UC-App/1.0 (contact@zomato-uc.com)");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            String geocodeResponse = restTemplate.exchange(nominatimUrl, org.springframework.http.HttpMethod.GET, entity, String.class).getBody();
            JsonNode geocodeRoot = objectMapper.readTree(geocodeResponse);

            if (geocodeRoot.isArray() && geocodeRoot.size() > 0) {
                JsonNode location = geocodeRoot.get(0);
                double lat = location.get("lat").asDouble();
                double lon = location.get("lon").asDouble();

                // 2. Fetch restaurants using Overpass API
                // Finds amenities=restaurant within 2000m radius with 10s timeout
                String overpassQuery = "[out:json][timeout:10];node[\"amenity\"=\"restaurant\"](around:2000," + lat + "," + lon + ");out 10;";
                String overpassUrl = "https://overpass-api.de/api/interpreter?data={query}";
                String overpassResponse = null;
                try {
                    overpassResponse = restTemplate.exchange(overpassUrl, org.springframework.http.HttpMethod.GET, entity, String.class, overpassQuery).getBody();
                } catch (Exception ex) {
                    logger.warn("Primary Overpass API failed: {}. Trying fallback...", ex.getMessage());
                    String fallbackUrl = "https://lz4.overpass-api.de/api/interpreter?data={query}";
                    overpassResponse = restTemplate.exchange(fallbackUrl, org.springframework.http.HttpMethod.GET, entity, String.class, overpassQuery).getBody();
                }
                JsonNode overpassRoot = objectMapper.readTree(overpassResponse);

                JsonNode elements = overpassRoot.get("elements");
                if (elements != null && elements.isArray()) {
                    for (JsonNode element : elements) {
                        JsonNode tags = element.get("tags");
                        if (tags != null && tags.has("name")) {
                            Restaurant r = new Restaurant();
                            r.setName(tags.get("name").asText());
                            
                            String address = tags.has("addr:street") ? tags.get("addr:street").asText() : city + " Center";
                            if (tags.has("addr:housenumber")) {
                                address = tags.get("addr:housenumber").asText() + " " + address;
                            }
                            r.setAddress(address);
                            r.setCity(city);
                            
                            r.setLatitude(element.get("lat").asDouble());
                            r.setLongitude(element.get("lon").asDouble());
                            
                            String cuisine = tags.has("cuisine") ? tags.get("cuisine").asText() : "Local";
                            // Basic parsing if multiple cuisines exist
                            if (cuisine.contains(";")) {
                                cuisine = cuisine.split(";")[0];
                            }
                            // Capitalize cuisine
                            cuisine = cuisine.substring(0, 1).toUpperCase() + cuisine.substring(1);
                            
                            r.setCuisines(List.of(cuisine));
                            r.setAmbiences(List.of("Casual"));
                            
                            // Mocking some ratings for realism
                            r.setAverageRating(3.5 + Math.random() * 1.5);
                            r.setTotalReviews((int) (Math.random() * 500) + 10);
                            r.setCostForTwo(java.math.BigDecimal.valueOf((int) (Math.random() * 50) + 20));
                            
                            r.setHasParking(Math.random() > 0.5);
                            r.setIsFamilyFriendly(Math.random() > 0.2);
                            r.setOffersDelivery(Math.random() > 0.3);
                            
                            r.setDescription(r.getName() + " is a lovely restaurant in " + city + " serving " + cuisine + " food.");
                            r.setImageUrl(IMAGE_URLS.get(random.nextInt(IMAGE_URLS.size())));
                            
                            restaurants.add(r);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching external restaurants for city {}: {}", city, e.getMessage());
        }
        return restaurants;
    }

    public List<Restaurant> fetchRestaurantsByLocation(Double lat, Double lon) {
        List<Restaurant> restaurants = new ArrayList<>();
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Zomato-UC-App/1.0 (contact@zomato-uc.com)");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // Fetch restaurants using Overpass API
            String overpassQuery = "[out:json][timeout:10];node[\"amenity\"=\"restaurant\"](around:2000," + lat + "," + lon + ");out 10;";
            String overpassUrl = "https://overpass-api.de/api/interpreter?data={query}";
            String overpassResponse = null;
            try {
                overpassResponse = restTemplate.exchange(overpassUrl, org.springframework.http.HttpMethod.GET, entity, String.class, overpassQuery).getBody();
            } catch (Exception ex) {
                logger.warn("Primary Overpass API failed: {}. Trying fallback...", ex.getMessage());
                String fallbackUrl = "https://lz4.overpass-api.de/api/interpreter?data={query}";
                overpassResponse = restTemplate.exchange(fallbackUrl, org.springframework.http.HttpMethod.GET, entity, String.class, overpassQuery).getBody();
            }
            JsonNode overpassRoot = objectMapper.readTree(overpassResponse);

            JsonNode elements = overpassRoot.get("elements");
            if (elements != null && elements.isArray()) {
                for (JsonNode element : elements) {
                    JsonNode tags = element.get("tags");
                    if (tags != null && tags.has("name")) {
                        Restaurant r = new Restaurant();
                        r.setName(tags.get("name").asText());
                        
                        String address = tags.has("addr:street") ? tags.get("addr:street").asText() : "Nearby Center";
                        if (tags.has("addr:housenumber")) {
                            address = tags.get("addr:housenumber").asText() + " " + address;
                        }
                        r.setAddress(address);
                        r.setCity("Current Location");
                        
                        r.setLatitude(element.get("lat").asDouble());
                        r.setLongitude(element.get("lon").asDouble());
                        
                        String cuisine = tags.has("cuisine") ? tags.get("cuisine").asText() : "Local";
                        if (cuisine.contains(";")) {
                            cuisine = cuisine.split(";")[0];
                        }
                        cuisine = cuisine.substring(0, 1).toUpperCase() + cuisine.substring(1);
                        
                        r.setCuisines(List.of(cuisine));
                        r.setAmbiences(List.of("Casual"));
                        
                        r.setAverageRating(3.5 + Math.random() * 1.5);
                        r.setTotalReviews((int) (Math.random() * 500) + 10);
                        r.setCostForTwo(java.math.BigDecimal.valueOf((int) (Math.random() * 50) + 20));
                        
                        r.setHasParking(Math.random() > 0.5);
                        r.setIsFamilyFriendly(Math.random() > 0.2);
                        r.setOffersDelivery(Math.random() > 0.3);
                        
                        r.setDescription(r.getName() + " is a lovely restaurant serving " + cuisine + " food.");
                        r.setImageUrl(IMAGE_URLS.get(random.nextInt(IMAGE_URLS.size())));
                        
                        restaurants.add(r);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching external restaurants for location {},{}: {}", lat, lon, e.getMessage());
        }
        return restaurants;
    }
}
