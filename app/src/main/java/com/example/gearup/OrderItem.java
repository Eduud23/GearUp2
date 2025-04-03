package com.example.gearup;

public class OrderItem {
    private String productName;
    private String quantity;
    private double totalPrice;
    private String customerName;
    private String shippingAddress;
    private String paymentMethod;
    private String orderStatus;
    private String orderId;

    public OrderItem(String orderId, String productName, String quantity, double totalPrice,
                     String customerName, String shippingAddress, String paymentMethod, String orderStatus) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
    }

    // Getters and setters for each field
    public String getOrderId() { return orderId; }
    public String getProductName() { return productName; }
    public String getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderStatus() { return orderStatus; }
}
