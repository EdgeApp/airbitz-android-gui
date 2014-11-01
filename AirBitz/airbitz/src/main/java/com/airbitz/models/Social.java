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


    public Social() {

    }

    public Social(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mSocialType = jsonResponse.getString("social_type");
            mSocialId = jsonResponse.getString("social_id");
            mSocialUrl = jsonResponse.getString("social_url");
        }
    }

    public Social(String socialType, String socialId, String socialUrl) {
        mSocialType = socialType;
        mSocialId = socialId;
        mSocialUrl = socialUrl;
    }

    public static List<Social> generateSocialListFromJSONObject(JSONArray objectArray) {
        List<Social> resultList = new ArrayList<Social>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new Social(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    public String getSocialType() {
        return mSocialType;
    }

    public void setSocialType(String socialType) {
        mSocialType = socialType;
    }

    public String getSocialId() {
        return mSocialId;
    }

    public void setSocialId(String socialId) {
        mSocialId = socialId;
    }

    public String getSocialUrl() {
        return mSocialUrl;
    }

    public void setSocialUrl(String socialUrl) {
        mSocialUrl = socialUrl;
    }
}
