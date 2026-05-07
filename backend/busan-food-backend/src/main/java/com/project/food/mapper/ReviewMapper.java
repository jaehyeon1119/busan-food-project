package com.project.food.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.project.food.dto.ReviewDto;

@Mapper
public interface ReviewMapper {

    List<ReviewDto> findByRestaurantId(int restaurantId);

    void insertReview(ReviewDto reviewDto);

    void updateReview(ReviewDto reviewDto);

    void deleteReview(int reviewId);
}