package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winni on 2/18/14.
 */
public class BusinessPhoto {

    private int mCountValue;
    private String mNextLink;
    private String mPreviousLink;
    private List<Image> mImages;

    public BusinessPhoto() {

    }

    public BusinessPhoto(int countValue, String nextLink, String previousLink, ArrayList<Image> images) {
        mCountValue = countValue;
        mNextLink = nextLink;
        mPreviousLink = previousLink;
        mImages = images;
    }

    public BusinessPhoto(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mCountValue = jsonResponse.getInt("count");
            mNextLink = jsonResponse.getString("next");
            mPreviousLink = jsonResponse.getString("previous");
            mImages = Image.generateImageListFromJSONObject(jsonResponse.getJSONArray("results"));
        }
    }

    public int getCountValue() {
        return mCountValue;
    }

    public void setCountValue(int countValue) {
        mCountValue = countValue;
    }

    public String getNextLink() {
        return mNextLink;
    }

    public void setNextLink(String nextLink) {
        mNextLink = nextLink;
    }

    public String getPreviousLink() {
        return mPreviousLink;
    }

    public void setPreviousLink(String previousLink) {
        mPreviousLink = previousLink;
    }

    public List<Image> getImages() {
        return mImages;
    }

    public void setImages(ArrayList<Image> images) {
        mImages = images;
    }
}
