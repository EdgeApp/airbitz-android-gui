package com.airbitz.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.fragments.VenueFragment;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.tasks.GetImageTask;
import com.airbitz.utils.Common;

import java.util.List;

/**
 * Created on 2/13/14.
 */
public class VenueAdapter extends BaseAdapter {

    public static final String TAG = VenueAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<BusinessSearchResult> mVenues;
    private LayoutInflater mInflater;
    private int mCurrentPosition = 0;
    private boolean firstPlace = false;
    private VenueFragment mVenueFragment;
    private double mLat;
    private double mLon;

    public VenueAdapter(Context context, List<BusinessSearchResult> venues, VenueFragment venueFragment) {
        firstPlace = false;
        mContext = context;
        mVenues = venues;
        mInflater = LayoutInflater.from(mContext);
        mVenueFragment = venueFragment;
        mLat = getLatFromSharedPreference();
        mLon = getLonFromSharedPreference();
    }

    @Override
    public int getCount() {
        return mVenues.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mCurrentPosition = position;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_venues, parent, false);
        TextView venueNameTextView = (TextView) convertView.findViewById(R.id.textview_business_name);
        TextView distanceTextView = (TextView) convertView.findViewById(R.id.textview_distance);
        TextView discountTextView = (TextView) convertView.findViewById(R.id.textview_discount);

        venueNameTextView.setTypeface(BusinessDirectoryActivity.latoBlackTypeFace);
        distanceTextView.setTypeface(BusinessDirectoryActivity.latoBlackTypeFace);
        discountTextView.setTypeface(BusinessDirectoryActivity.helveticaNeueTypeFace);

        String type = mVenues.get(position).getCategoryObject().get(0).getCategoryName();
        String discount = mVenues.get(position).getBizDiscount();
        double discountDouble = 0;
        try{
            discountDouble = Double.parseDouble(discount);
        } catch (Exception e){
            e.printStackTrace();
        }
        int discountInt = (int)(discountDouble*100);

        if(discountInt==0){
            discountTextView.setText(type);
        } else {
            discountTextView.setText(type+" | Disc. "+discountInt+"%");
        }

        try{
            double distance = Double.parseDouble(mVenues.get(position).getDistance());
            distance = Common.metersToMiles(distance);
            if (distance < 1.0){
                distance = Math.ceil(distance*10)/10;
                String distanceString = ""+distance;
                distanceString = distanceString.substring(1,distanceString.length());
                distanceTextView.setText(distanceString+" miles");
            } else if (distance >= 1000){
                int distanceInInt = (int) distance;
                distanceTextView.setText(String.valueOf(distanceInInt)+" miles");
            } else {
                distance = Math.ceil(distance*10)/10;
                distanceTextView.setText(String.valueOf(distance)+" miles");
            }
        } catch (Exception e){
            distanceTextView.setText("-");
            e.printStackTrace();
        }
        ImageView backgroundView = (ImageView) convertView.findViewById(R.id.venueBackground);

        venueNameTextView.setText(mVenues.get(position).getName());


        if(position==0){
            RelativeLayout mainLayout = (RelativeLayout)convertView.findViewById(R.id.mainLayout);
            int height = (int) mContext.getResources().getDimension(R.dimen.venue_list_full_height_275);
            mainLayout.getLayoutParams().height = height;
        }

        final Bitmap bitmap = mVenueFragment.getBitmapFromMemCache(mVenues.get(position).getId());
        if (bitmap != null) {
            backgroundView.setImageBitmap(bitmap);
        } else {
            GetImageTask getImageTask = new GetImageTask(backgroundView, mVenueFragment, this, position);
            getImageTask.execute(mVenues.get(position).getProfileImage().getImageThumbnail(),mVenues.get(position).getId());
        }
        return convertView;
    }

    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = mContext.getSharedPreferences(BusinessDirectoryActivity.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference(){
        double lat = (double)getStateFromSharedPreferences(BusinessDirectoryActivity.LAT_KEY);
        return lat;
    }

    private double getLonFromSharedPreference(){
        double lon = (double)getStateFromSharedPreferences(BusinessDirectoryActivity.LON_KEY);
        return lon;
    }


}
