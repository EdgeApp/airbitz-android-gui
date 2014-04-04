package com.airbitz.to_be_deleted;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.airbitz.adapters.VenueAdapter;
import com.airbitz.fragments.VenueFragment;

import java.io.InputStream;
import java.net.URL;

/**
 * Created on 2/26/14.
 */
public class GetImageTask extends AsyncTask<String, Void, Bitmap> {

    public static final String TAG = "GetImageTask";

    ImageView mImageView;
    VenueFragment mVenueFragment;
    VenueAdapter mVenueAdapter;
    int mPosition;

    public GetImageTask(ImageView targetView, VenueFragment venueFragment, VenueAdapter venueAdapter, int position) {
        mVenueFragment = venueFragment;
        mImageView = targetView;
        mVenueAdapter = venueAdapter;
        mPosition = position;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap image = null;

        try {
            InputStream in = new URL(params[0]).openStream();
            image = BitmapFactory.decodeStream(in);
//            if(!mVenueFragment.isMemoryCacheFull(image.getByteCount())){
//
//                mVenueFragment.addBitmapToMemoryCache(params[1],image);
//            }
        } catch (Exception e) {
            Log.e(TAG, ""+e.getMessage());
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }


        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

}
