package com.zomato.review_service.service;

import com.zomato.review_service.model.Review;
import com.zomato.review_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitReview() {
        Review review = new Review();
        review.setRating(5.0);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review saved = reviewService.submitReview(review);

        assertNotNull(saved);
        assertEquals(5.0, saved.getRating());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void testGetReviewsForRestaurant() {
        Review review = new Review();
        review.setRestaurantId(1L);
        when(reviewRepository.findByRestaurantId(1L)).thenReturn(Collections.singletonList(review));

        List<Review> results = reviewService.getReviewsForRestaurant(1L);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getRestaurantId());
    }
}
