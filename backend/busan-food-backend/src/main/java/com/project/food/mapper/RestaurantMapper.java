package com.project.food.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.project.food.dto.RestaurantDto;

@Mapper
public interface RestaurantMapper {

    List<RestaurantDto> findAll();

    RestaurantDto findById(int restaurantId);

    void insertRestaurant(RestaurantDto dto);

    void deleteAllRestaurants();

    Map<String, Object> getReviewStats(int restaurantId);
}