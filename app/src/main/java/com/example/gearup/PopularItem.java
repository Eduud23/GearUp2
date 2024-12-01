package com.example.gearup;

public class PopularItem {
    private String address;

    public PopularItem() {
        // Empty constructor for Firestore
    }

    public PopularItem(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
