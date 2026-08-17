package com.zomato.user_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This could map to the Keycloak user ID (UUID)
    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    // e.g., Vegetarian, Vegan, Jain
    @ElementCollection
    @CollectionTable(name = "user_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    private List<String> dietaryPreferences;

    @ElementCollection
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "restaurant_id")
    private List<Long> favoriteRestaurants;

    @ElementCollection
    @CollectionTable(name = "user_rejections", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "restaurant_id")
    private List<Long> rejectedRestaurants;

    @ElementCollection
    @CollectionTable(name = "user_friends", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "friend_keycloak_id")
    private List<String> socialConnections;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Default constructor
    public User() {}

    public User(String keycloakId, String name, String email, String phoneNumber, List<String> dietaryPreferences) {
        this.keycloakId = keycloakId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dietaryPreferences = dietaryPreferences;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(List<String> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public List<Long> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    public void setFavoriteRestaurants(List<Long> favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }

    public List<Long> getRejectedRestaurants() {
        return rejectedRestaurants;
    }

    public void setRejectedRestaurants(List<Long> rejectedRestaurants) {
        this.rejectedRestaurants = rejectedRestaurants;
    }

    public List<String> getSocialConnections() {
        return socialConnections;
    }

    public void setSocialConnections(List<String> socialConnections) {
        this.socialConnections = socialConnections;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
