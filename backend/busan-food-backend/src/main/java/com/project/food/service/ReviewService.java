package com.project.food.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.food.dto.ReviewDto;
import com.project.food.mapper.ReviewMapper;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    public List<ReviewDto> findByRestaurantId(int restaurantId) {
        return reviewMapper.findByRestaurantId(restaurantId);
    }

    public void write(ReviewDto reviewDto) {
        reviewMapper.insertReview(reviewDto);
    }

    public void delete(int reviewId) {
        reviewMapper.deleteReview(reviewId);
    }
    
    
    public void update(ReviewDto reviewDto) {
        reviewMapper.updateReview(reviewDto);
    }
}