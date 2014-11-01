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
public class Image {

    public static final String TAG = Image.class.getSimpleName();
    private String mPhotoLink;
    private String mPhotoThumbnailLink;
    private double mPhotoHeight;
    private double mPhotoWidth;
    private BoundingBox mBoundingBox;
    private List<String> mTags;

    public Image() {
    }

    public Image(String photoLink, double photoHeight, double mPhotoWidth) {
        mPhotoLink = photoLink;
        mPhotoHeight = photoHeight;
        mPhotoWidth = mPhotoWidth;
    }

    public Image(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
//            mPhotoLink = "http://107.170.22.83"+jsonResponse.getString("image");
            String serverRoot = AirbitzAPI.getServerRoot();
            serverRoot = serverRoot.substring(0, serverRoot.length() - 1);
            mPhotoLink = serverRoot + jsonResponse.getString("image");
            mPhotoHeight = jsonResponse.getDouble("width");
            mPhotoWidth = jsonResponse.getDouble("height");
            mBoundingBox = new BoundingBox(jsonResponse.getJSONObject("bounding_box"));
            mPhotoThumbnailLink = serverRoot + jsonResponse.getString("thumbnail");

            mTags = new ArrayList<String>();
            final JSONArray tags = jsonResponse.getJSONArray("tags");
            final int len = tags.length();
            for (int i = 0; i < len; i++) {
                final String tag = tags.getString(i);
                mTags.add(tag);
            }
        }
    }

    public static List<Image> generateImageListFromJSONObject(JSONArray objectArray) {
        List<Image> resultList = new ArrayList<Image>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new Image(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    public String getPhotoThumbnailLink() {
        return mPhotoThumbnailLink;
    }

    public String getPhotoLink() {
        return mPhotoLink;
    }

    public void setPhotoLink(String photoLink) {
        mPhotoLink = photoLink;
    }

    public double getPhotoWidth() {
        return mPhotoWidth;
    }

    public void setPhotoWidth(double photoWidth) {
        mPhotoWidth = photoWidth;
    }

    public double getPhotoHeight() {
        return mPhotoHeight;
    }

    public void setPhotoHeight(double photoHeight) {
        mPhotoHeight = photoHeight;
    }

    public BoundingBox getBoundingBoxObject() {
        return mBoundingBox;
    }

    public void setBoundingBoxObject(BoundingBox boundingBox) {
        mBoundingBox = boundingBox;
    }

    public List<String> getTags() {
        return mTags;
    }

}
