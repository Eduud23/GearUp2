package com.example.gearup;

public class Order {
    private String orderId;
    private String userId;
    private double price;
    private String productName;
    private String productBrand;
    private String productYearModel; // Combined year and model
    private String productDescription;
    private int productQuantity;

    // Constructor
    public Order(String orderId, String userId, double price,
                 String productName, String productBrand,
                 String productYearModel, // Updated to reflect combined year and model
                 String productDescription, int productQuantity) {
        this.orderId = orderId;
        this.userId = userId;
        this.price = price;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productYearModel = productYearModel; // Updated
        this.productDescription = productDescription;
        this.productQuantity = productQuantity;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public double getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public String getProductYearModel() {
        return productYearModel; // Updated to reflect combined year and model
    }

    public String getProductDescription() {
        return productDescription;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public void setProductYearModel(String productYearModel) { // Updated
        this.productYearModel = productYearModel;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    // Optional toString method for easy debugging
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", price=" + price +
                ", productName='" + productName + '\'' +
                ", productBrand='" + productBrand + '\'' +
                ", productYearModel='" + productYearModel + '\'' + // Updated
                ", productDescription='" + productDescription + '\'' +
                ", productQuantity=" + productQuantity +
                '}';
    }
}
