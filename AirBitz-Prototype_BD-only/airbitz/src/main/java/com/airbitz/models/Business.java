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
public class Business {

    public static final String TAG = Business.class.getSimpleName();
    private String mName;
    private String mType;
    private String mId;
    private boolean mIsCached;

    public Business(){
    }

    public boolean isCached() {
        return mIsCached;
    }

    public void setIsCached(boolean isCached) {
        mIsCached = isCached;
    }

    public Business(String name, String type, String id){
        mName = name;
        mType = type;
        mId = id;
    }

    public Business(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse !=null){
            mName = jsonResponse.getString("text");
            mType = jsonResponse.getString("type");

            if(mType.equalsIgnoreCase("business")){
                mId = jsonResponse.getString("bizId");
            }
            else if(mType.equalsIgnoreCase("category")){
                mId = "";
            }
        }
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Business) {
            final Business otherBusiness = (Business) o;
            return mId.equals(otherBusiness.getId());
        }
        return false;
    }

    public static List<Business> generateBusinessObjectListFromJSON(JSONArray objectArray){
        JSONArray temp = objectArray;
        List<Business> resultList = new ArrayList<Business>();
        for(int counter=0; counter < objectArray.length(); counter++){
            try{
                resultList.add(new Business(objectArray.getJSONObject(counter)));
            }catch (JSONException e){
                Log.d(TAG, ""+e.getMessage());
            }catch (Exception e){
                Log.d(TAG, ""+e.getMessage());
            }
        }
        return resultList;
    }

    public void setName(String name){
        mName = name;
    }

    public void setType(String type){
        mType = type;
    }

    public void setId(String id){
        mId = mId;
    }

    public String getName(){
        return mName;
    }

    public String getType(){
        return mType;
    }

    public String getId(){
        return mId;
    }

}
