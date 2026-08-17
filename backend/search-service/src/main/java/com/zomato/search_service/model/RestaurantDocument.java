package com.zomato.search_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "restaurants")
public class RestaurantDocument {

    @Id
    private String id;

    private String name;

    private String description;

    private String city;

    private Double locationLat;
    private Double locationLon;

    @Field(type = FieldType.Keyword)
    private List<String> cuisines;

    @Field(type = FieldType.Keyword)
    private List<String> ambiences;

    private Double averageRating;

    private BigDecimal costForTwo;

    private Boolean isFamilyFriendly;

    public RestaurantDocument() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    public Double getLocationLon() {
        return locationLon;
    }

    public void setLocationLon(Double locationLon) {
        this.locationLon = locationLon;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public void setCuisines(List<String> cuisines) {
        this.cuisines = cuisines;
    }

    public List<String> getAmbiences() {
        return ambiences;
    }

    public void setAmbiences(List<String> ambiences) {
        this.ambiences = ambiences;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public BigDecimal getCostForTwo() {
        return costForTwo;
    }

    public void setCostForTwo(BigDecimal costForTwo) {
        this.costForTwo = costForTwo;
    }

    public Boolean getIsFamilyFriendly() {
        return isFamilyFriendly;
    }

    public void setIsFamilyFriendly(Boolean isFamilyFriendly) {
        this.isFamilyFriendly = isFamilyFriendly;
    }
}
