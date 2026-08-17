package com.zomato.restaurant_service.service;

import com.zomato.restaurant_service.model.Restaurant;
import com.zomato.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ExternalRestaurantService externalRestaurantService;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRestaurantsByCity_enoughLocalData() {
        // Arrange
        String city = "Hyderabad";
        List<Restaurant> mockList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Restaurant r = new Restaurant();
            r.setId((long) i);
            r.setCity(city);
            mockList.add(r);
        }
        when(restaurantRepository.findByCity(city)).thenReturn(mockList);

        // Act
        List<Restaurant> results = restaurantService.getRestaurantsByCity(city);

        // Assert
        assertEquals(5, results.size());
        verify(restaurantRepository, times(1)).findByCity(city);
        verify(externalRestaurantService, never()).fetchRestaurantsByCity(anyString());
    }

    @Test
    void testGetRestaurantsByCity_notEnoughLocalData_fetchesExternal() {
        // Arrange
        String city = "Hyderabad";
        List<Restaurant> mockList = new ArrayList<>(); // Empty local
        when(restaurantRepository.findByCity(city)).thenReturn(mockList);

        List<Restaurant> externalList = new ArrayList<>();
        Restaurant r1 = new Restaurant();
        r1.setId(1L);
        r1.setCity(city);
        externalList.add(r1);

        when(externalRestaurantService.fetchRestaurantsByCity(city)).thenReturn(externalList);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(r1);

        // Act
        List<Restaurant> results = restaurantService.getRestaurantsByCity(city);

        // Assert
        assertEquals(1, results.size());
        verify(restaurantRepository, times(1)).findByCity(city);
        verify(externalRestaurantService, times(1)).fetchRestaurantsByCity(city);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testUpdateRestaurant() {
        // Arrange
        Long id = 1L;
        Restaurant existing = new Restaurant();
        existing.setId(id);
        existing.setName("Old Name");

        Restaurant details = new Restaurant();
        details.setName("New Name");
        details.setCity("Hyderabad");

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Restaurant updated = restaurantService.updateRestaurant(id, details);

        // Assert
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("Hyderabad", updated.getCity());
        verify(restaurantRepository, times(1)).findById(id);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testUpdateRestaurant_notFound() {
        // Arrange
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            restaurantService.updateRestaurant(id, new Restaurant());
        });
        assertEquals("Restaurant not found with id 1", exception.getMessage());
    }
}
