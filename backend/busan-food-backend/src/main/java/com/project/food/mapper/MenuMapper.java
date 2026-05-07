package com.project.food.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.project.food.dto.MenuDto;

@Mapper
public interface MenuMapper {

    List<MenuDto> findByRestaurantId(int restaurantId);
}