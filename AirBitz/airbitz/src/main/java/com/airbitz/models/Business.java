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

import android.util.Log;

import com.airbitz.api.AirbitzAPI;

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
    private String mSquareLink;
    private boolean mIsCached;

    public Business() {
    }

    public Business(String name, String type, String id) {
        mName = name;
        mType = type;
        mId = id;
    }

    public Business(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mName = jsonResponse.getString("text");
            mType = jsonResponse.getString("type");

            if (mType.equalsIgnoreCase("business")) {
                mId = jsonResponse.getString("bizId");
                mSquareLink = AirbitzAPI.getServerRoot() + jsonResponse.getString("square_image");
            } else if (mType.equalsIgnoreCase("category")) {
                mId = "";
            }
        }
    }

    public static List<Business> generateBusinessObjectListFromJSON(JSONArray objectArray) {
        JSONArray temp = objectArray;
        List<Business> resultList = new ArrayList<Business>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new Business(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    public boolean isCached() {
        return mIsCached;
    }

    public void setIsCached(boolean isCached) {
        mIsCached = isCached;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Business) {
            final Business otherBusiness = (Business) o;
            return mName.equals(otherBusiness.getName());
        }
        return false;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = mId;
    }

    public void setSquareLink(String link) {
        mSquareLink = link;
    }

    public String getSquareImageLink() {
        return mSquareLink;
    }

}
