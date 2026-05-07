package com.project.food.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.project.food.dto.MenuDto;
import com.project.food.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{restaurantId}")
    public List<MenuDto> getMenus(@PathVariable int restaurantId) {
        return menuService.findByRestaurantId(restaurantId);
    }
}