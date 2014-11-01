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
 * Created on 2/19/14.
 */
public class BusinessSearchResult {

    private static final String TAG = BusinessSearchResult.class.getSimpleName();
    private String mBizId;
    private String mBizName;
    private String mBizWebsite;
    private String mBizPhone;
    private String mBizAddress;
    private String mBizCity;
    private String mBizCounty;
    private String mBizState;
    private String mBizPostalCode;
    private String mBizCountry;
    private List<Category> mCategoryArray;
    private List<Social> mSocialArray;
    private ProfileImage mProfileImage;
    private ProfileImage mSquareProfileImage;
    private Location mLocation;
    private String mBizDiscount;
    private String mDistance;

    public BusinessSearchResult(String bizId, String bizName, String bizWebsite, String bizPhone, String bizAddress,
                                String bizCity, String bizCounty, String bizState, String bizPostalCode,
                                String bizCountry, ArrayList<Category> categoryArray,
                                ArrayList<Social> socialArray, ProfileImage profileImage, ProfileImage squareProfileImage, Location location,
                                String discount, String distance) {
        mBizId = bizId;
        mBizName = bizName;
        mBizWebsite = bizWebsite;
        mBizPhone = bizPhone;
        mBizAddress = bizAddress;
        mBizCity = bizCity;
        mBizCounty = bizCounty;
        mBizState = bizState;
        mBizPostalCode = bizPostalCode;
        mBizCountry = bizCountry;
        mCategoryArray = categoryArray;
        mSocialArray = socialArray;
        mProfileImage = profileImage;
        mSquareProfileImage = squareProfileImage;
        mLocation = location;
        setBizDiscount(discount);
        mDistance = distance;
    }

    public BusinessSearchResult(String bizId, String bizName) {
        mBizId = bizId;
        mBizName = bizName;
    }

    public BusinessSearchResult(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            if (jsonResponse.has("bizId")) {
                mBizId = jsonResponse.getString("bizId");
            }
            mBizName = jsonResponse.getString("name");
            mBizWebsite = jsonResponse.getString("website");
            mBizPhone = jsonResponse.getString("phone");
            mBizAddress = jsonResponse.getString("address");
            mBizCity = jsonResponse.getString("city");
            mBizCounty = jsonResponse.getString("county");
            mBizState = jsonResponse.getString("state");
            mBizPostalCode = jsonResponse.getString("postalcode");
            mBizCountry = jsonResponse.getString("country");
            mCategoryArray = Category.generateCategoryListFromJSONObject(jsonResponse.getJSONArray("categories"));
            mSocialArray = Social.generateSocialListFromJSONObject(jsonResponse.getJSONArray("social"));
            mProfileImage = new ProfileImage(jsonResponse.getJSONObject("profile_image"));
            mSquareProfileImage = new ProfileImage(jsonResponse.getJSONObject("square_image"));
            mLocation = new Location(jsonResponse.getJSONObject("location"));
            mBizDiscount = jsonResponse.getString("has_bitcoin_discount");
            mDistance = jsonResponse.getString("distance");
        }
    }

    public static List<BusinessSearchResult> generateBusinessObjectListFromJSON(JSONArray objectArray) {
        List<BusinessSearchResult> resultList = new ArrayList<BusinessSearchResult>();
        for (int counter = 0; counter < objectArray.length(); counter++) {
            try {
                resultList.add(new BusinessSearchResult(objectArray.getJSONObject(counter)));
            } catch (JSONException e) {
                Log.d(TAG, "" + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
            }
        }
        return resultList;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BusinessSearchResult) {
            BusinessSearchResult toCompare = (BusinessSearchResult) o;
            return this.mBizId.equals(toCompare.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mBizId.hashCode();
    }

    public String getId() {
        return mBizId;
    }

    public void setId(String id) {
        mBizId = id;
    }

    public String getName() {
        return mBizName;
    }

    public void setName(String name) {
        mBizName = name;
    }

    public void setCategoryObjectArray(List<Category> categoryArray) {
        mCategoryArray = categoryArray;
    }

    public List<Category> getCategoryObject() {
        return mCategoryArray;
    }

    public List<Social> getSocialObjectArray() {
        return mSocialArray;
    }

    public void setSocialObjectArray(List<Social> socialArray) {
        mSocialArray = socialArray;
    }

    public void setProfileImageObjectArray(ProfileImage profileImage) {
        mProfileImage = profileImage;
    }

    public ProfileImage getProfileImage() {
        return mProfileImage;
    }

    public ProfileImage getSquareProfileImage() {
        return mSquareProfileImage;
    }

    public String getWebsite() {
        return mBizWebsite;
    }

    public void setWebsite(String website) {
        mBizWebsite = website;
    }

    public String getPhone() {
        return mBizPhone;
    }

    public void setPhone(String phone) {
        mBizPhone = phone;
    }

    public String getAddress() {
        return mBizAddress;
    }

    public void setAddress(String address) {
        mBizAddress = address;
    }

    public String getCity() {
        return mBizCity;
    }

    public void setCity(String city) {
        mBizCity = city;
    }

    public String getCounty() {
        return mBizCounty;
    }

    public void setCounty(String county) {
        mBizCounty = county;
    }

    public String getState() {
        return mBizState;
    }

    public void setState(String state) {
        mBizState = state;
    }

    public String getPostalCode() {
        return mBizPostalCode;
    }

    public void setPostalCode(String postalCode) {
        mBizPostalCode = postalCode;
    }

    public String getCountry() {
        return mBizCountry;
    }

    public void setCountry(String country) {
        mBizCountry = country;
    }

    public Location getLocationObject() {
        return mLocation;
    }

    public void setLocationObject(Location location) {
        mLocation = location;
    }

    public String getBizDiscount() {
        return mBizDiscount;
    }

    public void setBizDiscount(String bizDiscount) {
        this.mBizDiscount = bizDiscount;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        this.mDistance = distance;
    }
}
