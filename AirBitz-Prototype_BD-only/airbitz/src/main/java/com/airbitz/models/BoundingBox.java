package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by winni on 2/18/14.
 */
public class BoundingBox {

    private double mX;
    private double mY;
    private double mWidth;
    private double mHeight;

    public BoundingBox(){

    }

    public BoundingBox(double y, double x, double width, double height){
        mY = y;
        mX = x;
        mWidth = width;
        mHeight = height;
    }

    public BoundingBox(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
            mX = jsonResponse.getDouble("x");
            mY = jsonResponse.getDouble("y");
            mWidth = jsonResponse.getDouble("width");
            mHeight = jsonResponse.getDouble("height");
        }
    }

    public void setY(double y){
        mY = y;
    }

    public double getY(){
        return mY;
    }

    public void setX(double x){
        mX = x;
    }

    public double getX(){
        return mX;
    }

    public void setWidth(double width){
        mWidth = width;
    }

    public double getWidth(){
        return mWidth;
    }

    public void setHeight(double height){
        mHeight = height;
    }

    public double setHeight(){
        return mHeight;
    }

}
