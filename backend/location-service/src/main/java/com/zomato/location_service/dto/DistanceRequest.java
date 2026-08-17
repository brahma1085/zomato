package com.zomato.location_service.dto;

public class DistanceRequest {
    private Double sourceLat;
    private Double sourceLon;
    private Double destLat;
    private Double destLon;

    // Getters and Setters
    public Double getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(Double sourceLat) {
        this.sourceLat = sourceLat;
    }

    public Double getSourceLon() {
        return sourceLon;
    }

    public void setSourceLon(Double sourceLon) {
        this.sourceLon = sourceLon;
    }

    public Double getDestLat() {
        return destLat;
    }

    public void setDestLat(Double destLat) {
        this.destLat = destLat;
    }

    public Double getDestLon() {
        return destLon;
    }

    public void setDestLon(Double destLon) {
        this.destLon = destLon;
    }
}
