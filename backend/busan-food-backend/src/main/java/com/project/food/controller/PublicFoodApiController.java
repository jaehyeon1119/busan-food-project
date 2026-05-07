package com.project.food.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.food.service.PublicFoodApiService;

@RestController
public class PublicFoodApiController {

    private final PublicFoodApiService publicFoodApiService;

    public PublicFoodApiController(PublicFoodApiService publicFoodApiService) {
        this.publicFoodApiService = publicFoodApiService;
    }

    @GetMapping("/api/public-food/import")
    public String importFoodData() {
        return publicFoodApiService.importBusanFoodData();
    }
}