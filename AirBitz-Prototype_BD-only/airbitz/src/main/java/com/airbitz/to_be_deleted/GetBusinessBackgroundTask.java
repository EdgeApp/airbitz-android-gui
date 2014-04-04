package com.airbitz.to_be_deleted;

import android.os.AsyncTask;
import android.widget.ImageView;

import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.VenueFragment;
import com.airbitz.models.BusinessDetail;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created on 2/25/14.
 */
public class GetBusinessBackgroundTask extends AsyncTask<String, Void, String> {

    public static final String TAG = GetBusinessBackgroundTask.class.getSimpleName();

    ImageView mTargetView;
    VenueFragment mVenueFragment;
    String mBizId;
    VenueAdapter mVenueAdapter;
    int mPosition;


    public GetBusinessBackgroundTask(ImageView targetView, VenueFragment venueFragment, VenueAdapter venueAdapter, int position){
        mVenueFragment = venueFragment;
        mTargetView = targetView;
        mVenueAdapter = venueAdapter;
        mPosition = position;
    }

    @Override
    protected String doInBackground(String... params) {
        mBizId = params[0];
        AirbitzAPI api = AirbitzAPI.getApi();
        return api.getBusinessById(params[0]);
    }

    @Override
    protected void onPostExecute(String responseString) {
        try{
            BusinessDetail bizDetail = new BusinessDetail(new JSONObject(responseString));
            //GetImageTask task = new GetImageTask(mTargetView, mVenueFragment, mVenueAdapter, mPosition);
            //task.execute(bizDetail.getImages().get(0).getPhotoLink(), mBizId);

        }catch (JSONException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
