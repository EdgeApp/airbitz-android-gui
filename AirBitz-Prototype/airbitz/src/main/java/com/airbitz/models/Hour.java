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
public class Hour {

    public static final String TAG = Hour.class.getSimpleName();
    private String mDayOfWeek;
    private String mHourStart;
    private String mHourEnd;


    public Hour(){

    }

    public Hour(String dayOfWeek, String hourStart, String hourEnd){
        mDayOfWeek = dayOfWeek;
        mHourStart = hourStart;
        mHourEnd = mHourEnd;
    }

    public Hour(JSONObject jsonResponse) throws JSONException{
        if (jsonResponse != null){
            mDayOfWeek = jsonResponse.getString("dayOfWeek");
            mHourStart = jsonResponse.getString("hourStart");
            mHourEnd = jsonResponse.getString("hourEnd");
        }
    }

    public static List<Hour> generateHourListFromJSONObject(JSONArray objectArray){
        List<Hour> resultList = new ArrayList<Hour>();
        for(int counter=0; counter < objectArray.length(); counter++){
            try{
                resultList.add(new Hour(objectArray.getJSONObject(counter)));
            }catch (JSONException e){
                Log.d(TAG, ""+e.getMessage());
            }catch (Exception e){
                Log.d(TAG, ""+e.getMessage());
            }
        }
        return resultList;
    }

    public void setDayOfWeek(String dayOfWeek){
        mDayOfWeek = dayOfWeek;
    }

    public void setHourStart(String hourStart){
        mHourStart = hourStart;
    }

    public void setHourEnd(String hourEnd){
        mHourEnd = hourEnd;
    }

    public String getDayOfWeek(){
        return mDayOfWeek;
    }

    public String getHourStart(){
        return mHourStart;
    }

    public String getHourEnd(){
        return mHourEnd;
    }

    public String setHourEnd(){
        return mHourEnd;
    }
}
