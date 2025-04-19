package com.example.gearup;

public class ForecastProductItem {
    private String category;         // Previously 'product_line'
    private String vehicle_type;     // Previously 'category'
    private String component;
    private String description;
    private String image_url;

    public ForecastProductItem() {
        // Firestore requires a public no-argument constructor
    }

    public String getCategory() {
        return category;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public String getComponent() {
        return component;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }
}
