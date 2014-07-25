package com.airbitz.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.utils.Common;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created on 2/13/14.
 */
public class VenueAdapter extends BaseAdapter {

    public static final String TAG = VenueAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<BusinessSearchResult> mVenues;
    private int mCurrentPosition = 0;
    private boolean firstPlace = false;
    private double mLat;
    private double mLon;
    private final Picasso p;

    public VenueAdapter(Context context, List<BusinessSearchResult> venues) {
        firstPlace = false;
        mContext = context;
        mVenues = venues;
        mLat = getLatFromSharedPreference();
        mLon = getLonFromSharedPreference();
        p =  new Picasso.Builder(context).build();
    }

    static class VenueViewHolderItem {
        RelativeLayout relativeLayoutItem;
        ImageView venueBackgroundItem;
        View blankViewVenueItem;
        LinearLayout linearLayoutGradientVenueItem;
        LinearLayout linearLayoutInfoContainerVenueItem;
        TextView textViewBusinessNameItem;
        LinearLayout linearLayoutBottomContainerVenueItem;
        TextView textViewAddressItem;
        TextView textViewDiscountItem;
        TextView textViewDistanceItem;
        int position;
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
        VenueViewHolderItem viewHolder;

        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_venues, parent, false);
            TextView venueNameTextView = (TextView) convertView.findViewById(R.id.textview_business_name);
            TextView distanceTextView = (TextView) convertView.findViewById(R.id.textview_distance);
            TextView addressTextView = (TextView) convertView.findViewById(R.id.textview_address);
            TextView discountTextView = (TextView) convertView.findViewById(R.id.textview_discount);

            venueNameTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
            distanceTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
            addressTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
            discountTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);

            final BusinessSearchResult result = mVenues.get(position);

            // Address
            if (!TextUtils.isEmpty(result.getAddress())) {
                addressTextView.setText(result.getAddress());
                addressTextView.setVisibility(View.VISIBLE);
            } else {
                addressTextView.setVisibility(View.INVISIBLE);
            }

            // Discount
            String discount = result.getBizDiscount();
            double discountDouble = 0;
            try {
                discountDouble = Double.parseDouble(discount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (discountDouble != 0) {
                discountTextView.setText("BTC Discount " + (int) (discountDouble * 100) + "%");
                discountTextView.setVisibility(View.VISIBLE);
            } else {
                discountTextView.setVisibility(View.GONE);
            }

            // Distance
            try{
                double distance = Double.parseDouble(mVenues.get(position).getDistance());
                distance = Common.metersToMiles(distance);
                if (distance < 1.0){
                    double distFeet = Common.milesToFeet(distance);
                    if(distFeet<=1000){
                        int intDist = (int) Math.floor(distFeet);
                        String distanceString = ""+intDist;
                        distanceTextView.setText(distanceString+" feet");
                    }else{
                        distance = Math.ceil(distance*10)/10;
                        String distanceString = ""+distance;
                        distanceString = distanceString.substring(1,distanceString.length());
                        distanceTextView.setText(distanceString+" miles");
                    }
                } else if (distance >= 1000){
                    int distanceInInt = (int) distance;
                    distanceTextView.setText(String.valueOf(distanceInInt)+" miles");
                } else {
                    distance = Math.ceil(distance*10)/10;
                    distanceTextView.setText(String.valueOf(distance)+" miles");
                }
                distanceTextView.setVisibility(View.VISIBLE);
            } catch (Exception e){
                distanceTextView.setVisibility(View.INVISIBLE);
                e.printStackTrace();
            }
            ImageView backgroundView = (ImageView) convertView.findViewById(R.id.venueBackground);
            venueNameTextView.setText(mVenues.get(position).getName());

            if(position==0){
                RelativeLayout mainLayout = (RelativeLayout)convertView.findViewById(R.id.mainLayout);
                int height = (int) mContext.getResources().getDimension(R.dimen.new_height);
                RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(mainLayout.getLayoutParams().width, height);
                mainLayout.setLayoutParams(ilp);
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(distanceTextView.getLayoutParams().width, distanceTextView.getLayoutParams().height);
                llp.setMargins(0, (int) mContext.getResources().getDimension(R.dimen.offset_height), 0, 0); // llp.setMargins(left, top, right, bottom);
                distanceTextView.setLayoutParams(llp);
            }else{
                RelativeLayout mainLayout = (RelativeLayout)convertView.findViewById(R.id.mainLayout);
                mainLayout.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.venue_list_small_height_175);
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(distanceTextView.getLayoutParams().width, distanceTextView.getLayoutParams().height);
                llp.setMargins(0, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                distanceTextView.setLayoutParams(llp);
            }

            p.load(mVenues.get(position).getProfileImage().getImageThumbnail()).noFade().into(backgroundView);

            viewHolder = new VenueViewHolderItem();
            viewHolder.relativeLayoutItem = (RelativeLayout) convertView.findViewById(R.id.mainLayout);
            viewHolder.venueBackgroundItem = (ImageView) convertView.findViewById(R.id.venueBackground);
            viewHolder.blankViewVenueItem = convertView.findViewById(R.id.blank_view_venue);
            viewHolder.linearLayoutGradientVenueItem = (LinearLayout) convertView.findViewById(R.id.linear_layout_gradient_venue);
            viewHolder.linearLayoutInfoContainerVenueItem = (LinearLayout) convertView.findViewById(R.id.linear_layout_info_container_venue);
            viewHolder.textViewBusinessNameItem = (TextView) convertView.findViewById(R.id.textview_business_name);
            viewHolder.linearLayoutBottomContainerVenueItem = (LinearLayout) convertView.findViewById(R.id.linear_layout_bottom_container_venue);
            viewHolder.textViewAddressItem = (TextView) convertView.findViewById(R.id.textview_address);
            viewHolder.textViewDiscountItem = (TextView) convertView.findViewById(R.id.textview_discount);
            viewHolder.textViewDistanceItem = (TextView) convertView.findViewById(R.id.textview_distance);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (VenueViewHolderItem) convertView.getTag();
        }

        final BusinessSearchResult result = mVenues.get(position);

        if(position==0){
            int height = (int) mContext.getResources().getDimension(R.dimen.new_height);
            RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(viewHolder.relativeLayoutItem.getLayoutParams().width, height);
            viewHolder.relativeLayoutItem.setLayoutParams(ilp);
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(viewHolder.textViewDistanceItem.getLayoutParams().width, viewHolder.textViewDistanceItem.getLayoutParams().height);
            llp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llp.topMargin = (int) mContext.getResources().getDimension(R.dimen.offset_height); // llp.setMargins(left, top, right, bottom);
            viewHolder.textViewDistanceItem.setLayoutParams(llp);
        }else{
            viewHolder.relativeLayoutItem.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.venue_list_small_height_175);
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(viewHolder.textViewDistanceItem.getLayoutParams().width, viewHolder.textViewDistanceItem.getLayoutParams().height);
            llp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llp.topMargin = 0;
            viewHolder.textViewDistanceItem.setLayoutParams(llp);
        }

        p.load(mVenues.get(position).getProfileImage().getImageThumbnail()).noFade().into(viewHolder.venueBackgroundItem);

        viewHolder.textViewBusinessNameItem.setText(mVenues.get(position).getName());
        // Address
        if (!TextUtils.isEmpty(result.getAddress())) {
            viewHolder.textViewAddressItem.setText(result.getAddress());
            viewHolder.textViewAddressItem.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewAddressItem.setVisibility(View.INVISIBLE);
        }
        // Discount
        String discount = result.getBizDiscount();
        double discountDouble = 0;
        try {
            discountDouble = Double.parseDouble(discount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (discountDouble != 0) {
            viewHolder.textViewDiscountItem.setText("BTC Discount " + (int) (discountDouble * 100) + "%");
            viewHolder.textViewDiscountItem.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewDiscountItem.setVisibility(View.GONE);
        }
        // Distance
        try{
            double distance = Double.parseDouble(mVenues.get(position).getDistance());
            distance = Common.metersToMiles(distance);
            if (distance < 1.0){
                double distFeet = Common.milesToFeet(distance);
                if(distFeet<=1000){
                    int intDist = (int) Math.floor(distFeet);
                    String distanceString = ""+intDist;
                    viewHolder.textViewDistanceItem.setText(distanceString+" feet");
                }else{
                    distance = Math.ceil(distance*10)/10;
                    String distanceString = ""+distance;
                    distanceString = distanceString.substring(1,distanceString.length());
                    viewHolder.textViewDistanceItem.setText(distanceString+" miles");
                }
            } else if (distance >= 1000){
                int distanceInInt = (int) distance;
                viewHolder.textViewDistanceItem.setText(String.valueOf(distanceInInt)+" miles");
            } else {
                distance = Math.ceil(distance*10)/10;
                viewHolder.textViewDistanceItem.setText(String.valueOf(distance)+" miles");
            }
            viewHolder.textViewDistanceItem.setVisibility(View.VISIBLE);
        } catch (Exception e){
            viewHolder.textViewDistanceItem.setVisibility(View.INVISIBLE);
            e.printStackTrace();
        }
        return convertView;
    }

    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = mContext.getSharedPreferences(BusinessDirectoryFragment.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference(){
        double lat = (double)getStateFromSharedPreferences(BusinessDirectoryFragment.LAT_KEY);
        return lat;
    }

    private double getLonFromSharedPreference(){
        double lon = (double)getStateFromSharedPreferences(BusinessDirectoryFragment.LON_KEY);
        return lon;
    }


}
