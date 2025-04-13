package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String orderId;
    private String productId;        // ✅ New field
    private String productName;
    private Long quantity;
    private double totalPrice;
    private String customerName;
    private String shippingAddress;
    private String paymentMethod;
    private String orderStatus;
    private String deliveryOption;
    private String imageUrl;
    private String sellerId;
    private String paymentIntentId;

    // ✅ No-argument constructor for Firestore
    public OrderItem() {
        // Default constructor
    }

    // ✅ Full constructor with productId
    public OrderItem(String orderId, String productName, Long quantity, double totalPrice,
                     String customerName, String shippingAddress, String paymentMethod, String orderStatus,
                     String deliveryOption, String imageUrl, String sellerId, String paymentIntentId,
                     String productId) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
        this.deliveryOption = deliveryOption;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.paymentIntentId = paymentIntentId;
        this.productId = productId;
    }

    // ✅ Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderStatus() { return orderStatus; }
    public String getDeliveryOption() { return deliveryOption; }
    public String getImageUrl() { return imageUrl; }
    public String getSellerId() { return sellerId; }
    public String getPaymentIntentId() { return paymentIntentId; }

    // ✅ Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setDeliveryOption(String deliveryOption) { this.deliveryOption = deliveryOption; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    // ✅ Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(productName);
        dest.writeLong(quantity != null ? quantity : 0L);
        dest.writeDouble(totalPrice);
        dest.writeString(customerName);
        dest.writeString(shippingAddress);
        dest.writeString(paymentMethod);
        dest.writeString(orderStatus);
        dest.writeString(deliveryOption);
        dest.writeString(imageUrl);
        dest.writeString(sellerId);
        dest.writeString(paymentIntentId);
        dest.writeString(productId); // ✅ Write productId
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            OrderItem item = new OrderItem();
            item.setOrderId(in.readString());
            item.setProductName(in.readString());
            item.setQuantity(in.readLong());
            item.setTotalPrice(in.readDouble());
            item.setCustomerName(in.readString());
            item.setShippingAddress(in.readString());
            item.setPaymentMethod(in.readString());
            item.setOrderStatus(in.readString());
            item.setDeliveryOption(in.readString());
            item.setImageUrl(in.readString());
            item.setSellerId(in.readString());
            item.setPaymentIntentId(in.readString());
            item.setProductId(in.readString()); // ✅ Read productId
            return item;
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
}
