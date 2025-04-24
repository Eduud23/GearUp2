package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class LocalShop implements Parcelable {
    private String shopName, image, kindOfRepair, timeSchedule, place, contactNumber, website;
    private double ratings, latitude, longitude, distance;

    public LocalShop(String shopName, String image, String kindOfRepair, String timeSchedule, String place, String contactNumber, double ratings, String website, double latitude, double longitude, double distance) {
        this.shopName = shopName;
        this.image = image;
        this.kindOfRepair = kindOfRepair;
        this.timeSchedule = timeSchedule;
        this.place = place;
        this.contactNumber = contactNumber;
        this.ratings = ratings;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    // Getter methods
    public String getShopName() { return shopName; }
    public String getImage() { return image; }
    public String getKindOfRepair() { return kindOfRepair; }
    public String getTimeSchedule() { return timeSchedule; }
    public String getPlace() { return place; }
    public String getContactNumber() { return contactNumber; }
    public double getRatings() { return ratings; }
    public String getWebsite() { return website; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getDistance() { return distance; }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    // Parcelable implementation
    protected LocalShop(Parcel in) {
        shopName = in.readString();
        image = in.readString();
        kindOfRepair = in.readString();
        timeSchedule = in.readString();
        place = in.readString();
        contactNumber = in.readString();
        ratings = in.readDouble();
        website = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distance = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shopName);
        dest.writeString(image);
        dest.writeString(kindOfRepair);
        dest.writeString(timeSchedule);
        dest.writeString(place);
        dest.writeString(contactNumber);
        dest.writeDouble(ratings);
        dest.writeString(website);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distance);
    }

    public static final Creator<LocalShop> CREATOR = new Creator<LocalShop>() {
        @Override
        public LocalShop createFromParcel(Parcel in) {
            return new LocalShop(in);
        }

        @Override
        public LocalShop[] newArray(int size) {
            return new LocalShop[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
