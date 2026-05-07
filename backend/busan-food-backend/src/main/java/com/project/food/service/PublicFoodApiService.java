package com.project.food.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.food.dto.RestaurantDto;
import com.project.food.mapper.RestaurantMapper;

@Service
public class PublicFoodApiService {

    private final RestaurantMapper restaurantMapper;

    public PublicFoodApiService(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    public String importBusanFoodData() {
        try {
            String serviceKey = "829f9e0018ebb9dcf9b53922adc67260a4139cbade1a4df94d3c9869169cab4b";

            String url = "https://apis.data.go.kr/6260000/FoodService/getFoodKr"
                    + "?serviceKey=" + serviceKey
                    + "&pageNo=1"
                    + "&numOfRows=50"
                    + "&resultType=json";

            RestTemplate restTemplate = new RestTemplate();
            String jsonText = restTemplate.getForObject(url, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> root = objectMapper.readValue(jsonText, Map.class);

            Map<String, Object> getFoodKr = (Map<String, Object>) root.get("getFoodKr");

            if (getFoodKr == null) {
                return "실패: getFoodKr 데이터가 없습니다. API 응답을 확인하세요.";
            }

            Object itemObject = getFoodKr.get("item");

            if (itemObject == null) {
                return "실패: item 데이터가 없습니다. API 응답을 확인하세요.";
            }

            restaurantMapper.deleteAllRestaurants();

            int count = 0;

            if (itemObject instanceof List) {
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemObject;

                for (Map<String, Object> item : itemList) {
                    RestaurantDto dto = convert(item);
                    restaurantMapper.insertRestaurant(dto);
                    count++;
                }

            } else if (itemObject instanceof Map) {
                Map<String, Object> item = (Map<String, Object>) itemObject;

                RestaurantDto dto = convert(item);
                restaurantMapper.insertRestaurant(dto);
                count++;
            }

            return count + "개 음식점 데이터 저장 완료";

        } catch (Exception e) {
            e.printStackTrace();
            return "실패: " + e.getMessage();
        }
    }

    private RestaurantDto convert(Map<String, Object> item) {

        RestaurantDto dto = new RestaurantDto();

        dto.setName(getString(item, "MAIN_TITLE"));
        dto.setCategory("부산맛집");

        dto.setAddress(getString(item, "ADDR1"));
        dto.setRegion(getString(item, "GUGUN_NM"));

        dto.setMainMenu(getString(item, "RPRSNTV_MENU"));
        dto.setMainImage(getString(item, "MAIN_IMG_THUMB"));

        dto.setRating(4.5);
        dto.setReviewCount(0);

        dto.setLat(getDouble(item, "LAT"));
        dto.setLng(getDouble(item, "LNG"));

        dto.setTel(getString(item, "CNTCT_TEL"));
        dto.setOpenTime(getString(item, "USAGE_DAY_WEEK_AND_TIME"));
        dto.setHomepage(getString(item, "HOMEPAGE_URL"));
        dto.setDescription(getString(item, "ITEMCNTNTS"));

        return dto;
    }

    private String getString(Map<String, Object> item, String key) {
        Object value = item.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private double getDouble(Map<String, Object> item, String key) {
        try {
            Object value = item.get(key);

            if (value == null) {
                return 0;
            }

            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }
}