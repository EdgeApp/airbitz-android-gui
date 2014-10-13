package com.airbitz.objects;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created on 2/14/14.
 */
public class BusinessVenue {

    public static final String TAG = BusinessVenue.class.getSimpleName();
    private String mName;
    private String mType;
    private String mAddress;
    private String mImageLocation;
    private String mWebLocation;
    private String mBusinessHour;
    private String mAbout;
    private LatLng mLocation;
    private double mBitCoinDiscount;
    private double mDistanceInMiles;
    private int mPhoneNumber;

    public BusinessVenue() {

    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getImageLocation() {
        return mImageLocation;
    }

    public void setImageLocation(String mImageLocation) {
        this.mImageLocation = mImageLocation;
    }

    public String getWebLocation() {
        return mWebLocation;
    }

    public void setWebLocation(String mWebLocation) {
        this.mWebLocation = mWebLocation;
    }

    public String getBusinessHour() {
        return mBusinessHour;
    }

    public void setBusinessHour(String mBusinessHour) {
        this.mBusinessHour = mBusinessHour;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String mAbout) {
        this.mAbout = mAbout;
    }

    public double getBitCoinDiscount() {
        return mBitCoinDiscount;
    }

    public void setBitCoinDiscount(double mBitCoinDiscount) {
        this.mBitCoinDiscount = mBitCoinDiscount;
    }

    public double getDistanceInMiles() {
        return mDistanceInMiles;
    }

    public void setDistanceInMiles(double mDistanceInMiles) {
        this.mDistanceInMiles = mDistanceInMiles;
    }

    public int getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(int mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng mLocation) {
        this.mLocation = mLocation;
    }
}
