package com.zomato.location_service.service;

import com.zomato.location_service.dto.DistanceRequest;
import com.zomato.location_service.dto.DistanceResponse;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    public DistanceResponse calculateDistance(DistanceRequest request) {
        // We calculate the direct Haversine distance
        // and estimate a travel time (e.g., 3 mins per km).

        double distance = haversine(request.getSourceLat(), request.getSourceLon(),
                request.getDestLat(), request.getDestLon());
        
        int estimatedTime = (int) Math.round(distance * 3); // 3 mins per km

        return new DistanceResponse(Math.round(distance * 100.0) / 100.0, estimatedTime);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in km
    }
}
