package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private Product product;
    private int quantity;

    // Constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Parcelable implementation
    protected CartItem(Parcel in) {
        product = in.readParcelable(Product.class.getClassLoader());
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // Getters
    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setter for quantity if needed
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Describe contents
    @Override
    public int describeContents() {
        return 0;
    }

    // Write to parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
    }
}
