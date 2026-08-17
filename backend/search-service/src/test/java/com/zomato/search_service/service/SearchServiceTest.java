package com.zomato.search_service.service;

import com.zomato.search_service.model.RestaurantDocument;
import com.zomato.search_service.repository.RestaurantSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @Mock
    private RestaurantSearchRepository searchRepository;

    @InjectMocks
    private SearchService searchService;

    private RestaurantDocument testDoc1;
    private RestaurantDocument testDoc2;

    @BeforeEach
    void setUp() {
        testDoc1 = new RestaurantDocument();
        testDoc1.setId("1");
        testDoc1.setName("Pasta Palace");
        testDoc1.setDescription("Authentic Italian");
        testDoc1.setCuisines(Arrays.asList("Italian", "Pasta"));

        testDoc2 = new RestaurantDocument();
        testDoc2.setId("2");
        testDoc2.setName("Taco Town");
        testDoc2.setDescription("Spicy Mexican");
        testDoc2.setCuisines(Collections.singletonList("Mexican"));
    }

    @Test
    void saveRestaurant_Success() {
        when(searchRepository.save(any(RestaurantDocument.class))).thenReturn(testDoc1);
        RestaurantDocument saved = searchService.saveRestaurant(testDoc1);
        assertNotNull(saved);
        assertEquals("Pasta Palace", saved.getName());
        verify(searchRepository, times(1)).save(testDoc1);
    }

    @Test
    void searchRestaurants_Success() {
        when(searchRepository.findByNameOrDescription("Pasta", "Pasta")).thenReturn(Collections.singletonList(testDoc1));
        List<RestaurantDocument> results = searchService.searchRestaurants("Pasta");
        assertEquals(1, results.size());
        assertEquals("Pasta Palace", results.get(0).getName());
        verify(searchRepository, times(1)).findByNameOrDescription("Pasta", "Pasta");
    }

    @Test
    void getByCuisine_Success() {
        when(searchRepository.findByCuisinesContaining("Italian")).thenReturn(Collections.singletonList(testDoc1));
        List<RestaurantDocument> results = searchService.getByCuisine("Italian");
        assertEquals(1, results.size());
        assertEquals("Italian", results.get(0).getCuisines().get(0));
        verify(searchRepository, times(1)).findByCuisinesContaining("Italian");
    }

    @Test
    void getAll_Success() {
        when(searchRepository.findAll()).thenReturn(Arrays.asList(testDoc1, testDoc2));
        List<RestaurantDocument> results = searchService.getAll();
        assertEquals(2, results.size());
        verify(searchRepository, times(1)).findAll();
    }
}
