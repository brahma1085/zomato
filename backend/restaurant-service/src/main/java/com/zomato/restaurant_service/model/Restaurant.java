package com.zomato.restaurant_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // Location components (in a real system, these would also be indexed geographically)
    private String address;
    private Double latitude;
    private Double longitude;
    private String city;

    // E.g., Indian, Chinese, Italian
    @ElementCollection
    @CollectionTable(name = "restaurant_cuisines", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "cuisine")
    private List<String> cuisines;

    // E.g., Family, Romantic, Casual
    @ElementCollection
    @CollectionTable(name = "restaurant_ambience", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "ambience")
    private List<String> ambiences;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "cost_for_two")
    private BigDecimal costForTwo;

    // Features like Parking, Family Friendly, Delivery
    private Boolean hasParking;
    private Boolean isFamilyFriendly;
    private Boolean offersDelivery;

    public Restaurant() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public BigDecimal getCostForTwo() {
        return costForTwo;
    }

    public void setCostForTwo(BigDecimal costForTwo) {
        this.costForTwo = costForTwo;
    }

    public Boolean getHasParking() {
        return hasParking;
    }

    public void setHasParking(Boolean hasParking) {
        this.hasParking = hasParking;
    }

    public Boolean getIsFamilyFriendly() {
        return isFamilyFriendly;
    }

    public void setIsFamilyFriendly(Boolean isFamilyFriendly) {
        this.isFamilyFriendly = isFamilyFriendly;
    }

    public Boolean getOffersDelivery() {
        return offersDelivery;
    }

    public void setOffersDelivery(Boolean offersDelivery) {
        this.offersDelivery = offersDelivery;
    }
}
