package com.project.food.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.food.dto.RestaurantDto;
import com.project.food.mapper.RestaurantMapper;

@Service
public class RestaurantService {

    private final RestaurantMapper mapper;

    public RestaurantService(RestaurantMapper mapper) {
        this.mapper = mapper;
    }

    public List<RestaurantDto> findAll() {
        return mapper.findAll();
    }

    public RestaurantDto findById(int id) {
        return mapper.findById(id);
    }

    public Map<String, Object> getReviewStats(int restaurantId) {
        return mapper.getReviewStats(restaurantId);
    }
}