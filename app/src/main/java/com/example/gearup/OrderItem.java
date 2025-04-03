package com.example.gearup;

public class OrderItem {
    private String orderId;
    private String productName;
    private Long quantity;  // Change to Long to match Firestore data type
    private double totalPrice;
    private String customerName;
    private String shippingAddress;
    private String paymentMethod;
    private String orderStatus;
    private String imageUrl;

    // No-argument constructor for Firestore deserialization
    public OrderItem() {
        // Default constructor
    }

    // Constructor to initialize all fields
    public OrderItem(String orderId, String productName, Long quantity, double totalPrice,
                     String customerName, String shippingAddress, String paymentMethod, String orderStatus, String imageUrl) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for each field
    public String getOrderId() { return orderId; }
    public String getProductName() { return productName; }
    public Long getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderStatus() { return orderStatus; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}