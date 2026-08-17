package com.zomato.restaurant_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.restaurant_service.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExternalRestaurantServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExternalRestaurantService externalRestaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // By default, externalRestaurantService will use the injected real objectMapper in production,
        // but since we Mock ObjectMapper here, we need to inject a real one to make reading JSON easy,
        // or just mock it. Let's initialize a real ObjectMapper to use instead of Mock.
        externalRestaurantService = new ExternalRestaurantService(restTemplate, new ObjectMapper());
    }

    @Test
    void testFetchRestaurantsByCity_Success() {
        // Arrange
        String city = "Hyderabad";

        // Mock Nominatim
        String geoResponseStr = "[{\"lat\": \"17.3850\", \"lon\": \"78.4867\"}]";
        ResponseEntity<String> geoEntity = new ResponseEntity<>(geoResponseStr, HttpStatus.OK);
        when(restTemplate.exchange(contains("nominatim"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(geoEntity);

        // Mock Overpass
        String overpassResponseStr = "{\"elements\": [{\"id\": 12345, \"lat\": 17.3850, \"lon\": 78.4867, \"tags\": {\"name\": \"Test Overpass Restaurant\", \"cuisine\": \"Indian\"}}]}";
        ResponseEntity<String> overpassEntity = new ResponseEntity<>(overpassResponseStr, HttpStatus.OK);
        when(restTemplate.exchange(contains("overpass-api"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class), anyString()))
            .thenReturn(overpassEntity);

        // Act
        List<Restaurant> results = externalRestaurantService.fetchRestaurantsByCity(city);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Test Overpass Restaurant", results.get(0).getName());
        assertTrue(results.get(0).getCuisines().contains("Indian"));
        assertEquals("Hyderabad", results.get(0).getCity());
    }

    @Test
    void testFetchRestaurantsByCity_GeocodingFails() {
        // Arrange
        String city = "UnknownCity";
        
        String geoResponseStr = "[]";
        ResponseEntity<String> geoEntity = new ResponseEntity<>(geoResponseStr, HttpStatus.OK);
        when(restTemplate.exchange(contains("nominatim"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(geoEntity);

        // Act
        List<Restaurant> results = externalRestaurantService.fetchRestaurantsByCity(city);

        // Assert
        assertTrue(results.isEmpty());
    }
}
