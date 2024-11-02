package com.example.gearup;

public class PriceRequest {
    private String product_name;
    private String brand;
    private int year_model;

    public PriceRequest(String product_name, String brand, int year_model) {
        this.product_name = product_name;
        this.brand = brand;
        this.year_model = year_model;
    }
}
