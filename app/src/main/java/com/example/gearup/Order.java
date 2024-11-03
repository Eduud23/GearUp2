package com.example.gearup;

public class Order {
    private String orderId;
    private String userId; // User ID for the current user
    private double productPrice;
    private String productName;
    private String productBrand;
    private String productYearModel;
    private String productDescription;
    private int productQuantity;
    private String productImageUrl;

    // New fields for delivery info
    private String userName;
    private String deliveryAddress;
    private String contactNumber;
    private String zipCode;

    // New field for order status
    private String orderStatus;
    private String sellerId;

    // Updated constructor
    public Order(String orderId, String userId, double productPrice, String productName,
                 String productBrand, String productYearModel, String productDescription,
                 int productQuantity, String productImageUrl, String userName,
                 String deliveryAddress, String contactNumber, String zipCode,
                 String orderStatus, String sellerId) {
        this.orderId = orderId;
        this.userId = userId;
        this.productPrice = productPrice;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productYearModel = productYearModel;
        this.productDescription = productDescription;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.userName = userName;
        this.deliveryAddress = deliveryAddress;
        this.contactNumber = contactNumber;
        this.zipCode = zipCode;
        this.orderStatus = orderStatus; // Set the order status
        this.sellerId = sellerId; // Initialize sellerId
    }

    // Getters and Setters
    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductYearModel() {
        return productYearModel;
    }

    public void setProductYearModel(String productYearModel) {
        this.productYearModel = productYearModel;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
