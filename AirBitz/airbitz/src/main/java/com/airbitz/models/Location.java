package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2/18/14.
 */
public class Location {

    private double mLatitude;
    private double mLongitude;

    public Location(double latitude, double longitude){
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Location(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
            mLatitude = jsonResponse.getDouble("latitude");
            mLongitude = jsonResponse.getDouble("longitude");
        }
    }

    public void setLatitude(double latitude){
        mLatitude = latitude;
    }

    public void setLongitude(double longitude){
        mLongitude = longitude;
    }

    public double getLatitude(){
        return mLatitude;
    }

    public double getLongitude(){
        return mLongitude;
    }
}
