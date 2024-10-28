package com.example.gearup;

public class Order {
    private String orderId;
    private String userId;
    private double price;
    private String productName;
    private String productBrand;
    private String productYearModel;
    private String productDescription;
    private int quantity;
    private String productImageUrl; // Holds the product image URL

    // Constructor
    public Order(String orderId, String userId, double price, String productName,
                 String productBrand, String productYearModel, String productDescription,
                 int quantity, String productImageUrl) {
        this.orderId = orderId;
        this.userId = userId;
        this.price = price;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productYearModel = productYearModel;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.productImageUrl = productImageUrl; // Initialize the image URL
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
        return productYearModel;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductImageUrl() {
        return productImageUrl; // Getter for product image URL
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

    public void setProductYearModel(String productYearModel) {
        this.productYearModel = productYearModel;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl; // Setter for product image URL
    }
}
