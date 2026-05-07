package com.project.food.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.food.dto.RestaurantDto;
import com.project.food.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    @GetMapping
    public List<RestaurantDto> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RestaurantDto detail(@PathVariable int id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> stats(@PathVariable int id) {
        return service.getReviewStats(id);
    }
}