package com.project.food.dto;

public class RestaurantDto {

    private int restaurantId;
    private String name;
    private String category;
    private String address;
    private String region;
    private String mainMenu;
    private String mainImage;

    private double rating;
    private int reviewCount;

    private double lat;
    private double lng;

    
    private String tel;
    private String openTime;
    private String homepage;
    private String description;

    
    
    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getMainMenu() { return mainMenu; }
    public void setMainMenu(String mainMenu) { this.mainMenu = mainMenu; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    
}