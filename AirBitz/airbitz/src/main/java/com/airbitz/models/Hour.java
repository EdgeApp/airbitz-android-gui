package com.airbitz.models;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created on 2/18/14.
 */
public class Hour {

    public static final String TAG = Hour.class.getSimpleName();
    private static SimpleDateFormat sMilitaryFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sAmPmFormat = new SimpleDateFormat("hh:mm a");
    private String mDayOfWeek;
    private String mHourStart;
    private String mHourEnd;

    public Hour() {

    }

    public Hour(String dayOfWeek, String hourStart, String hourEnd) {
        mDayOfWeek = dayOfWeek;
        mHourStart = hourStart;
        mHourEnd = mHourEnd;
    }

    public Hour(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mDayOfWeek = jsonResponse.getString("dayOfWeek");
            mHourStart = jsonResponse.getString("hourStart");
            mHourEnd = jsonResponse.getString("hourEnd");
        }
    }

    public static List<Hour> generateHourListFromJSONObject(JSONArray objectArray) {
        List<Hour> resultList = new ArrayList<Hour>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new Hour(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    public String getDayOfWeek() {
        return mDayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        mDayOfWeek = dayOfWeek;
    }

    public String getHourStart() {
        return mHourStart;
    }

    public void setHourStart(String hourStart) {
        mHourStart = hourStart;
    }

    public String getHourEnd() {
        return mHourEnd;
    }

    public void setHourEnd(String hourEnd) {
        mHourEnd = hourEnd;
    }

    public String setHourEnd() {
        return mHourEnd;
    }

    public String getPrettyStartEndHour() {

        if (TextUtils.isEmpty(mHourEnd) || "null".equals(mHourEnd)) {
            return "Open 24 Hours";
        }

        String startHour = "";
        String endHour = "";

        try {
            Date hourStartDateMil = sMilitaryFormat.parse(mHourStart);
            startHour = sAmPmFormat.format(hourStartDateMil);
            Date hourEndDateMil = sMilitaryFormat.parse(mHourEnd);
            endHour = sAmPmFormat.format(hourEndDateMil);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return startHour + " - " + endHour;
    }
}
