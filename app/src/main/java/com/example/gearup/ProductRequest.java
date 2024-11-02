package com.example.gearup;

public class ProductRequest {
    private String product_name;
    private String brand;
    private int year_model;
    private double price;

    public ProductRequest(String product_name, String brand, int year_model, double price) {
        this.product_name = product_name;
        this.brand = brand;
        this.year_model = year_model;
        this.price = price;
    }
}

