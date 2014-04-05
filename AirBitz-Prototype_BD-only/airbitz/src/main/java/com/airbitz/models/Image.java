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
    private double mPhotoHeight;
    private double mPhotoWidth;
    private BoundingBox mBoundingBox;
    private List<String> mTags;

    public Image(){
    }

    public Image(String photoLink, double photoHeight, double mPhotoWidth){
        mPhotoLink = photoLink;
        mPhotoHeight = photoHeight;
        mPhotoWidth = mPhotoWidth;
    }

    public Image(JSONObject jsonResponse) throws JSONException{
        if(jsonResponse != null){
//            mPhotoLink = "http://107.170.22.83"+jsonResponse.getString("image");
            String serverRoot = AirbitzAPI.getServerRoot();
            serverRoot = serverRoot.substring(0,serverRoot.length()-1);
            mPhotoLink = serverRoot+jsonResponse.getString("image");
            mPhotoHeight = jsonResponse.getDouble("width");
            mPhotoWidth = jsonResponse.getDouble("height");
            mBoundingBox = new BoundingBox(jsonResponse.getJSONObject("bounding_box"));

            mTags = new ArrayList<String>();
            final JSONArray tags = jsonResponse.getJSONArray("tags");
            final int len = tags.length();
            for (int i = 0; i < len; i++) {
                final String tag = tags.getString(i);
                mTags.add(tag);
            }
        }
    }

    public static List<Image> generateImageListFromJSONObject(JSONArray objectArray){
        List<Image> resultList = new ArrayList<Image>();
        for(int counter=0; counter < objectArray.length(); counter++){
            try{
                resultList.add(new Image(objectArray.getJSONObject(counter)));
            }catch (JSONException e){
                Log.d(TAG, ""+e.getMessage());
            }catch (Exception e){
                Log.d(TAG, ""+e.getMessage());
            }
        }
        return resultList;
    }


    public void setPhotoLink(String photoLink){
        mPhotoLink = photoLink;
    }

    public String getPhotoLink(){
        return mPhotoLink;
    }

    public void setPhotoWidth(double photoWidth){
        mPhotoWidth = photoWidth;
    }

    public double getPhotoWidth(){
        return mPhotoWidth;
    }

    public void setPhotoHeight(double photoHeight){
        mPhotoHeight = photoHeight;
    }

    public double getPhotoHeight(){
        return mPhotoHeight;
    }

    public void setBoundingBoxObject(BoundingBox boundingBox){
        mBoundingBox = boundingBox;
    }

    public BoundingBox getBoundingBoxObject(){
        return mBoundingBox;
    }

    public List<String> getTags() {
        return mTags;
    }

}
