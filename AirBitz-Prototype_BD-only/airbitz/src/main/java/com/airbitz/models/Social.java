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
public class Social {

    public final static String TAG = Social.class.getSimpleName();
    private String mSocialType;
    private String mSocialId;
    private String mSocialUrl;


    public Social(){

    }

    public Social(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
            mSocialType = jsonResponse.getString("social_type");
            mSocialId = jsonResponse.getString("social_id");
            mSocialUrl = jsonResponse.getString("social_url");
        }
    }

    public Social(String socialType, String socialId, String socialUrl){
        mSocialType = socialType;
        mSocialId = socialId;
        mSocialUrl = socialUrl;
    }

    public static List<Social> generateSocialListFromJSONObject(JSONArray objectArray){
        List<Social> resultList = new ArrayList<Social>();
        for(int counter=0; counter < objectArray.length(); counter++){
            try{
                resultList.add(new Social(objectArray.getJSONObject(counter)));
            }catch (JSONException e){
                Log.d(TAG, ""+e.getMessage());
            }catch (Exception e){
                Log.d(TAG, ""+e.getMessage());
            }
        }
        return resultList;
    }

    public void setSocialType(String socialType){
        mSocialType = socialType;
    }

    public void setSocialId(String socialId){
        mSocialId = socialId;
    }

    public void setSocialUrl(String socialUrl){
        mSocialUrl = socialUrl;
    }

    public String getSocialType(){
        return mSocialType;
    }

    public String getSocialId(){
        return mSocialId;
    }

    public String getSocialUrl(){
        return mSocialUrl;
    }
}
