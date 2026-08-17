package com.zomato.location_service.dto;

public class DistanceResponse {
    private Double distanceInKm;
    private Integer estimatedTimeInMinutes;

    public DistanceResponse(Double distanceInKm, Integer estimatedTimeInMinutes) {
        this.distanceInKm = distanceInKm;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
    }

    public Double getDistanceInKm() {
        return distanceInKm;
    }

    public void setDistanceInKm(Double distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    public Integer getEstimatedTimeInMinutes() {
        return estimatedTimeInMinutes;
    }

    public void setEstimatedTimeInMinutes(Integer estimatedTimeInMinutes) {
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
    }
}
