package com.airbitz.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/18/14.
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

    public BusinessDetail(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
            mBizId = jsonResponse.getString("bizId");
            mBizName = jsonResponse.getString("name");
            mBizDescription = jsonResponse.getString("description");
            mBizWebsite = jsonResponse.getString("website");
            mBizPhone = jsonResponse.getString("phone");
            mBizAddress = jsonResponse.getString("address");
            mBizCity = jsonResponse.getString("city");
            mBizCounty = jsonResponse.getString("county");
            mBizState = jsonResponse.getString("state");
            mBizPostalCode = jsonResponse.getString("postalcode");
            mBizCountry = jsonResponse.getString("country");
            mSocialArray = Social.generateSocialListFromJSONObject(jsonResponse.getJSONArray("social"));
            mHourArray = Hour.generateHourListFromJSONObject(jsonResponse.getJSONArray("hours"));
            mCategory = Category.generateCategoryListFromJSONObject(jsonResponse.getJSONArray("categories"));
            mImageArray = Image.generateImageListFromJSONObject(jsonResponse.getJSONArray("images"));
            mHasPhysicalBusiness = jsonResponse.getBoolean("has_physical_business");
            mHasOnlineBusiness = jsonResponse.getBoolean("has_online_business");
            mHasBitconDiscount = jsonResponse.getString("has_bitcoin_discount");
            mLocation = new Location(jsonResponse.getJSONObject("location"));
        }
    }

    public void setName(String name){
        mBizName = name;
    }

    public String getName(){
        return mBizName;
    }

    public void setId(String id){
        mBizId = id;
    }

    public String getId(){
        return mBizId;
    }

    public void setCategoryObject(List<Category> category){
        mCategory = category;
    }

    public List<Category> getCategoryObject(){
        return mCategory;
    }

    public void setSocialObjectArray(ArrayList<Social> socialArray){
        mSocialArray = socialArray;
    }

    public List<Social> getSocialObjectArray(){
        return mSocialArray;
    }

    public void setImages(ArrayList<Image> imageArray){
        mImageArray = imageArray;
    }

    public List<Image> getImages(){
        return mImageArray;
    }

    public void setDescription(String description){
        mBizDescription = description;
    }

    public String getDescription(){
        return mBizDescription;
    }

    public void setWebsite(String website){
        mBizWebsite = website;
    }

    public String getWebsite(){
        return mBizWebsite;
    }

    public void setPhone(String phone){
        mBizPhone = phone;
    }

    public String getPhone(){
        return mBizPhone;
    }

    public void setAddress(String address){
        mBizAddress = address;
    }

    public String getAddress(){
        return mBizAddress;
    }

    public void setCity(String city){
        mBizCity = city;
    }

    public String getCity(){
        return mBizCity;
    }

    public void setCounty(String county){
        mBizCounty = county;
    }

    public String getCounty(){
        return mBizCounty;
    }

    public void setState(String state){
        mBizState = state;
    }

    public String getState(){
        return mBizState;
    }

    public void setPostalCode(String postalCode){
        mBizPostalCode = postalCode;
    }

    public String getPostalCode(){
        return mBizPostalCode;
    }

    public void setCountry(String country){
        mBizCountry = country;
    }

    public String getCountry(){
        return mBizCountry;
    }

    public void setHourObjectArray(ArrayList<Hour> hourArray){
        mHourArray = hourArray;
    }

    public List<Hour> getHourObjectArray(){
        return mHourArray;
    }

    public void setPhysicalBusiness(boolean flag){
        mHasPhysicalBusiness = flag;
    }

    public boolean getFlagPhysicalBusiness(){
        return mHasPhysicalBusiness;
    }

    public void setOnlineBusiness(boolean flag){
        mHasOnlineBusiness = flag;
    }

    public boolean getFlagOnlineBusiness(){
        return mHasOnlineBusiness;
    }

    public void setHasBitconDiscount(String flag){
        mHasBitconDiscount = flag;
    }

    public String getFlagBitcoinDiscount(){
        return mHasBitconDiscount;
    }

    public void setLocationObject(Location location){
        mLocation = location;
    }

    public Location getLocationObjectArray(){
        return mLocation;
    }

}
