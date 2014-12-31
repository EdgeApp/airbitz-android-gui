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

import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.airbitz.api.AirbitzAPI;
import com.airbitz.shared.utils.SpannableUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by winni on 2/18/14.
 */
public class BusinessDetail {

    private String mBizId;
    private String mBizName;
    private String mBizDescription;
    private String mBizWebsite;
    private String mBizPhone;
    private String mBizAddress;
    private String mBizCity;
    private String mBizCounty;
    private String mBizState;
    private String mBizPostalCode;
    private String mBizCountry;
    private String mBizDistance;
    private String mSquareImage;
    private List<Category> mCategory;
    private List<Social> mSocialArray;
    private List<Hour> mHourArray;
    private List<Image> mImageArray;
    private boolean mHasPhysicalBusiness;
    private boolean mHasOnlineBusiness;
    private String mHasBitconDiscount;
    private Location mLocation;

    public BusinessDetail(String mBizId, String mBizName, String mBizDescription,
                          String mBizWebsite, String mBizPhone, String mBizAddress,
                          String mBizCity, String mBizCounty, String mBizState,
                          String mBizPostalCode, String mBizCountry,
                          List<Category> mCategory, List<Social> mSocialArray,
                          List<Hour> mHourArray, List<Image> mImageArray,
                          boolean mHasPhysicalBusiness, boolean mHasOnlineBusiness,
                          String mHasBitconDiscount, Location mLocation) {
        this.mBizId = mBizId;
        this.mBizName = mBizName;
        this.mBizDescription = mBizDescription;
        this.mBizWebsite = mBizWebsite;
        this.mBizPhone = mBizPhone;
        this.mBizAddress = mBizAddress;
        this.mBizCity = mBizCity;
        this.mBizCounty = mBizCounty;
        this.mBizState = mBizState;
        this.mBizPostalCode = mBizPostalCode;
        this.mBizCountry = mBizCountry;
        this.mCategory = mCategory;
        this.mSocialArray = mSocialArray;
        this.mHourArray = mHourArray;
        this.mImageArray = mImageArray;
        this.mHasPhysicalBusiness = mHasPhysicalBusiness;
        this.mHasOnlineBusiness = mHasOnlineBusiness;
        this.mHasBitconDiscount = mHasBitconDiscount;
        this.mLocation = mLocation;
    }

    public BusinessDetail(JSONObject jsonResponse) throws JSONException {
        if (jsonResponse != null) {
            mBizId = jsonResponse.getString("bizId");
            mBizName = jsonResponse.getString("name");
            mBizDistance = jsonResponse.getString("distance");
            mBizDescription = jsonResponse.getString("description");
            mBizWebsite = jsonResponse.getString("website");
            mBizPhone = jsonResponse.getString("phone");
            mBizAddress = jsonResponse.getString("address");
            mBizCity = jsonResponse.getString("city");
            mBizCounty = jsonResponse.getString("county");
            mBizState = jsonResponse.getString("state");
            mBizPostalCode = jsonResponse.getString("postalcode");
            mBizCountry = jsonResponse.getString("country");
            mSquareImage = jsonResponse.getString("square_image");
            mSocialArray = Social.generateSocialListFromJSONObject(jsonResponse.getJSONArray("social"));
            mHourArray = Hour.generateHourListFromJSONObject(jsonResponse.getJSONArray("hours"));
            mCategory = Category.generateCategoryListFromJSONObject(jsonResponse.getJSONArray("categories"));
            mImageArray = Image.generateImageListFromJSONObject(jsonResponse.getJSONArray("images"));
            mHasPhysicalBusiness = jsonResponse.getBoolean("has_physical_business");
            mHasOnlineBusiness = jsonResponse.getBoolean("has_online_business");
            mHasBitconDiscount = jsonResponse.getString("has_bitcoin_discount");
            if (!jsonResponse.isNull("location")) {
                mLocation = new Location(jsonResponse.getJSONObject("location"));
            } else {
                mLocation = new Location(0, 0);
            }

        }
    }

    public String getDistance() {
        return mBizDistance;
    }

    public String getName() {
        return mBizName;
    }

    public void setName(String name) {
        mBizName = name;
    }

    public String getId() {
        return mBizId;
    }

    public void setId(String id) {
        mBizId = id;
    }

    public List<Category> getCategoryObject() {
        return mCategory;
    }

    public void setCategoryObject(List<Category> category) {
        mCategory = category;
    }

    public List<Social> getSocialObjectArray() {
        return mSocialArray;
    }

    public void setSocialObjectArray(ArrayList<Social> socialArray) {
        mSocialArray = socialArray;
    }

    public List<Image> getImages() {
        return mImageArray;
    }

    public void setImages(ArrayList<Image> imageArray) {
        mImageArray = imageArray;
    }

    public String getDescription() {
        return mBizDescription;
    }

    public void setDescription(String description) {
        mBizDescription = description;
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

    public List<Hour> getHourObjectArray() {
        return mHourArray;
    }

    public void setHourObjectArray(ArrayList<Hour> hourArray) {
        mHourArray = hourArray;
    }

    public void setPhysicalBusiness(boolean flag) {
        mHasPhysicalBusiness = flag;
    }

    public boolean getFlagPhysicalBusiness() {
        return mHasPhysicalBusiness;
    }

    public void setOnlineBusiness(boolean flag) {
        mHasOnlineBusiness = flag;
    }

    public boolean getFlagOnlineBusiness() {
        return mHasOnlineBusiness;
    }

    public void setHasBitconDiscount(String flag) {
        mHasBitconDiscount = flag;
    }

    public String getFlagBitcoinDiscount() {
        return mHasBitconDiscount;
    }

    public void setLocationObject(Location location) {
        mLocation = location;
    }

    public Location getLocationObjectArray() {
        return mLocation;
    }

    public Image getPrimaryImage() {

        // Try to find image with tag "primary"
        Image primaryImage = null;
        for (Image image : mImageArray) {
            if (image.getTags().contains("Primary")) {
                primaryImage = image;
                break;
            }
        }

        // Otherwise, just use the first image
        if (primaryImage == null) {
            primaryImage = mImageArray.get(0);
        }

        return primaryImage;
    }

    public String getSquareImageLink() {
        JSONObject temp = null;
        String url = null;
        try {
            temp = new JSONObject(mSquareImage);
            url = AirbitzAPI.getServerRoot() + temp.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return url;
    }

    public Spannable getPrettyAddressString() {
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(mBizAddress)
                .append("\n")
                .append(mBizCity)
                .append(", ")
                .append(mBizState)
                .append(" ")
                .append(mBizPostalCode);
        SpannableUtil.setBoldSpan(ssb, mBizAddress);
        return ssb;
    }

}
