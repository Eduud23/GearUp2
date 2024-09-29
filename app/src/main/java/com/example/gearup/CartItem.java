package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private Product product;
    private int quantity;

    // No-argument constructor required for Firestore
    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

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

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
    }
}
