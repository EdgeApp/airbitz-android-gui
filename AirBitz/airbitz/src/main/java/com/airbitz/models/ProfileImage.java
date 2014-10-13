package com.airbitz.models;

import com.airbitz.api.AirbitzAPI;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2/18/14.
 */
public class ProfileImage {

    private int mWidth;
    private int mHeight;
    private String mImageLink;
    private String mImageThumbnail;
    private BoundingBox mBoundingBox;

    public ProfileImage(){

    }

    public ProfileImage(int width, int height, String imageLink, String imageThumbnail){
        mWidth = width;
        mHeight = height;
        mImageLink = imageLink;
        mImageThumbnail = imageThumbnail;
    }

    public ProfileImage(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
            mWidth = jsonResponse.getInt("width");
            mHeight = jsonResponse.getInt("height");
//            mImageLink = "http://107.170.22.83"+jsonResponse.getString("image");
//            mImageThumbnail = "http://107.170.22.83"+jsonResponse.getString("thumbnail");

            String serverRoot = AirbitzAPI.getServerRoot();
            serverRoot = serverRoot.substring(0,serverRoot.length()-1);

            mImageLink = serverRoot+jsonResponse.getString("image");
            mImageThumbnail = serverRoot+jsonResponse.getString("thumbnail");
            mBoundingBox = new BoundingBox(jsonResponse.getJSONObject("bounding_box"));
        }
    }

    public void setWidth(int width){
        mWidth = width;
    }

    public void setHeight(int height){
        mHeight = height;
    }

    public void setImageLink(String imageLink){
        mImageLink = imageLink;
    }

    public void setImageThumbnail(String imageThumbnail){
        mImageThumbnail = imageThumbnail;
    }

    public void setBoundingBoxObject(BoundingBox boundingBox){
        mBoundingBox = boundingBox;
    }

    public BoundingBox getBoundingBoxObject(){
        return mBoundingBox;
    }

    public int getWidth(){
        return mWidth;
    }

    public int getHeight(){
        return mHeight;
    }

    public String getImageLink(){
        return mImageLink;
    }

    public String getImageThumbnail(){
        return mImageThumbnail;
    }



}
