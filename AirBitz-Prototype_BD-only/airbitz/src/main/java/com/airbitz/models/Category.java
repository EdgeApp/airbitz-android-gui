package com.airbitz.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/18/14.
 */
public class Category {

    public static final String TAG = Category.class.getSimpleName();
    private String mCategoryName;
    private String mCategoryLevel;

    public Category() {
    }

    public Category(String name, String level) {
        mCategoryName = name;
        mCategoryLevel = level;
    }

    public Category(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mCategoryName = jsonResponse.getString("name");
            mCategoryLevel = jsonResponse.getString("level");
        }
    }

    public static List<Category> generateCategoryListFromJSONObject(JSONArray objectArray) {
        List<Category> resultList = new ArrayList<Category>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new Category(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public void setCategoryName(String name) {
        mCategoryName = name;
    }

    public String getCategoryLevel() {
        return mCategoryLevel;
    }

    public void setCategoryLevel(String level) {
        mCategoryLevel = level;
    }

}
