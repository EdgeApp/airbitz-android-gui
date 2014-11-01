/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

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
