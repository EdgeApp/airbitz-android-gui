package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/18/14.
 */
public class Categories {
    public static final String TAG = Categories.class.getSimpleName();
    private int mCountValue;
    private String mNextLink;
    private String mPreviousLink;
    private String mLastUpdate;
    private List<Category> mCategoryArray;

    public Categories(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mCountValue = jsonResponse.getInt("count");
            mNextLink = jsonResponse.getString("next");
            mPreviousLink = jsonResponse.getString("previous");
            mLastUpdate = jsonResponse.getString("last_updated");
            mCategoryArray = Category.generateCategoryListFromJSONObject(jsonResponse.getJSONArray("results"));
        }
    }

    public void addCategories(Categories categories) {
        mCategoryArray.addAll(categories.mCategoryArray);
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

    public String getLastUpdate() {
        return mLastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    public List<Category> getBusinessCategoryArray() {
        return mCategoryArray;
    }

    public void setBusinessCategoryArray(ArrayList<Category> categoryArray) {
        mCategoryArray = categoryArray;
    }

    public void removeBusinessCategoryArray() {
        if (mCategoryArray != null) {
            mCategoryArray.clear();
        }
    }

}
