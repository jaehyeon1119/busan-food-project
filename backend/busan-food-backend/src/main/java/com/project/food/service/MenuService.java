package com.project.food.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.food.dto.MenuDto;
import com.project.food.mapper.MenuMapper;

@Service
public class MenuService {

    private final MenuMapper menuMapper;

    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<MenuDto> findByRestaurantId(int restaurantId) {
        return menuMapper.findByRestaurantId(restaurantId);
    }
}