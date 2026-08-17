package com.zomato.review_service.service;

import com.zomato.review_service.model.Review;
import com.zomato.review_service.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review submitReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    public List<Review> getReviewsByUser(String userId) {
        return reviewRepository.findByUserId(userId);
    }
}
