package com.project.food.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.project.food.dto.ReviewDto;
import com.project.food.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{restaurantId}")
    public List<ReviewDto> getReviews(@PathVariable int restaurantId) {
        return reviewService.findByRestaurantId(restaurantId);
    }

    @PostMapping
    public void write(@RequestBody ReviewDto reviewDto) {
        reviewService.write(reviewDto);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable int reviewId) {
        reviewService.delete(reviewId);
    }
    
    @PutMapping("/{reviewId}")
    public void update(@PathVariable int reviewId, @RequestBody ReviewDto reviewDto) {
        reviewDto.setReviewId(reviewId);
        reviewService.update(reviewDto);
    }
}