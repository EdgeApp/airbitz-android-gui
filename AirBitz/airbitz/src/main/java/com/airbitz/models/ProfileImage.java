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

import com.airbitz.api.AirbitzAPI;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2/18/14.
 */
public class ProfileImage {

    private int mWidth;
    private int mHeight;
    private String mImageLink;
    private String mImageThumbnail;
    private BoundingBox mBoundingBox;

    public ProfileImage() {

    }

    public ProfileImage(int width, int height, String imageLink, String imageThumbnail) {
        mWidth = width;
        mHeight = height;
        mImageLink = imageLink;
        mImageThumbnail = imageThumbnail;
    }

    public ProfileImage(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mWidth = jsonResponse.getInt("width");
            mHeight = jsonResponse.getInt("height");
//            mImageLink = "http://107.170.22.83"+jsonResponse.getString("image");
//            mImageThumbnail = "http://107.170.22.83"+jsonResponse.getString("thumbnail");

            String serverRoot = AirbitzAPI.getServerRoot();
            serverRoot = serverRoot.substring(0, serverRoot.length() - 1);

            mImageLink = serverRoot + jsonResponse.getString("image");
            mImageThumbnail = serverRoot + jsonResponse.getString("thumbnail");
            mBoundingBox = new BoundingBox(jsonResponse.getJSONObject("bounding_box"));
        }
    }

    public BoundingBox getBoundingBoxObject() {
        return mBoundingBox;
    }

    public void setBoundingBoxObject(BoundingBox boundingBox) {
        mBoundingBox = boundingBox;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public void setImageLink(String imageLink) {
        mImageLink = imageLink;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        mImageThumbnail = imageThumbnail;
    }


}
