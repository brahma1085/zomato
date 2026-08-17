package com.zomato.location_service.service;

import com.zomato.location_service.dto.DistanceRequest;
import com.zomato.location_service.dto.DistanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();
    }

    @Test
    void testCalculateDistance() {
        // Arrange
        DistanceRequest request = new DistanceRequest();
        request.setSourceLat(17.3850);
        request.setSourceLon(78.4867);
        request.setDestLat(17.4401);
        request.setDestLon(78.3489);

        // Act
        DistanceResponse response = locationService.calculateDistance(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.getDistanceInKm() > 0);
        assertTrue(response.getEstimatedTimeInMinutes() > 0);
    }
}
