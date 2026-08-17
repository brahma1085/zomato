package com.zomato.review_service.controller;

import com.zomato.review_service.model.Review;
import com.zomato.review_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.submitReview(review));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Review>> getReviewsForRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsForRestaurant(restaurantId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}
