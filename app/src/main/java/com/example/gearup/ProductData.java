package com.example.gearup;

public class ProductData {
    private String name;
    private String brand;
    private String yearModel;
    private double price;

    public ProductData(String name, String brand, String yearModel, double price) {
        this.name = name;
        this.brand = brand;
        this.yearModel = yearModel;
        this.price = price;
    }

    // Getters and setters (if needed)
    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getYearModel() {
        return yearModel;
    }

    public double getPrice() {
        return price;
    }
}
