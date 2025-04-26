package com.example.gearup;

public class ForecastCategoryModel {
    private String categoryTitle;
    private int forecastedQuantity;

    public ForecastCategoryModel(String categoryTitle, int forecastedQuantity) {
        this.categoryTitle = categoryTitle;
        this.forecastedQuantity = forecastedQuantity;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public int getForecastedQuantity() {
        return forecastedQuantity;
    }
}
