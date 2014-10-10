package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/18/14.
 */
public class SearchResult {

    public static final String TAG = SearchResult.class.getSimpleName();
    private int mCountValue;
    private String mNextLink;
    private String mPreviousLink;
    private List<BusinessSearchResult> mBusinessSearchResultObjectsArray;


    public SearchResult(int countValue, String nextLink, String previousLink) {
        mCountValue = countValue;
        mNextLink = nextLink;
        mPreviousLink = previousLink;
    }

    public SearchResult(JSONObject responseJson) throws JSONException {
        if (responseJson != null) {
            mCountValue = responseJson.getInt("count");
            mNextLink = responseJson.getString("next");
            mPreviousLink = responseJson.getString("previous");
            mBusinessSearchResultObjectsArray = BusinessSearchResult.generateBusinessObjectListFromJSON(responseJson.getJSONArray("results"));
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

    public List<BusinessSearchResult> getBusinessSearchObjectArray() {
        return mBusinessSearchResultObjectsArray;
    }

    public void setBusinessSearchObjectArray(ArrayList<BusinessSearchResult> businessSearchResultArray) {
        mBusinessSearchResultObjectsArray = businessSearchResultArray;
    }

}
