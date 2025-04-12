package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String orderId;
    private String productName;
    private Long quantity;  // Change to Long to match Firestore data type
    private double totalPrice;
    private String customerName;
    private String shippingAddress;
    private String paymentMethod;
    private String orderStatus;
    private String deliveryOption;  // New field for delivery option
    private String imageUrl;
    private String sellerId; // Added sellerId field

    // No-argument constructor for Firestore deserialization
    public OrderItem() {
        // Default constructor
    }

    // Constructor to initialize all fields, including sellerId
    public OrderItem(String orderId, String productName, Long quantity, double totalPrice,
                     String customerName, String shippingAddress, String paymentMethod, String orderStatus,
                     String deliveryOption, String imageUrl, String sellerId) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus;
        this.deliveryOption = deliveryOption;  // Initialize delivery option
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;  // Initialize sellerId
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
    public String getDeliveryOption() { return deliveryOption; }  // Getter for delivery option
    public String getImageUrl() { return imageUrl; }
    public String getSellerId() { return sellerId; }  // Getter for sellerId

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setDeliveryOption(String deliveryOption) { this.deliveryOption = deliveryOption; }  // Setter for delivery option
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }  // Setter for sellerId

    // Parcelable methods

    @Override
    public int describeContents() {
        return 0; // No special objects are involved
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(productName);
        dest.writeLong(quantity != null ? quantity : 0L); // Ensure quantity is written as long (handle null values)
        dest.writeDouble(totalPrice);
        dest.writeString(customerName);
        dest.writeString(shippingAddress);
        dest.writeString(paymentMethod);
        dest.writeString(orderStatus);
        dest.writeString(deliveryOption);
        dest.writeString(imageUrl);
        dest.writeString(sellerId); // Write sellerId to the Parcel
    }

    // Creator field to recreate objects from a Parcel
    public static final Parcelable.Creator<OrderItem> CREATOR = new Parcelable.Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(
                    in.readString(), // orderId
                    in.readString(), // productName
                    in.readLong(),   // quantity
                    in.readDouble(), // totalPrice
                    in.readString(), // customerName
                    in.readString(), // shippingAddress
                    in.readString(), // paymentMethod
                    in.readString(), // orderStatus
                    in.readString(), // deliveryOption
                    in.readString(), // imageUrl
                    in.readString()  // sellerId
            );
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
}
