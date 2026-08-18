package com.zomato.review_service.controller;

import com.zomato.review_service.model.Review;
import com.zomato.review_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody Review review) {
        logger.info("Received request to submit review for restaurant: {}", review.getRestaurantId());
        return ResponseEntity.ok(reviewService.submitReview(review));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Review>> getReviewsForRestaurant(@PathVariable Long restaurantId) {
        logger.info("Received request to fetch reviews for restaurant id: {}", restaurantId);
        return ResponseEntity.ok(reviewService.getReviewsForRestaurant(restaurantId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable String userId) {
        logger.info("Received request to fetch reviews for user id: {}", userId);
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}
