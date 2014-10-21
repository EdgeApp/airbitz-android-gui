package com.airbitz.adapters;

import android.content.Context;
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
import com.airbitz.fragments.SettingFragment;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.utils.Common;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Created on 2/13/14.
 */
public class VenueAdapter extends BaseAdapter {

    public static final String TAG = VenueAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<BusinessSearchResult> mVenues;

    public VenueAdapter(Context context, List<BusinessSearchResult> venues) {
        mContext = context;
        mVenues = venues;
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
        VenueViewHolderItem viewHolder;

        if (convertView == null) {
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
            viewHolder.addressTextView = addressTextView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VenueViewHolderItem) convertView.getTag();
        }

        String imgUrl = mVenues.get(position).getProfileImage().getImageThumbnail();
        Picasso.with(mContext).load(imgUrl).noFade().into(viewHolder.venueBackgroundItem);

        final BusinessSearchResult result = mVenues.get(position);
        // Address
        if (!TextUtils.isEmpty(result.getAddress())) {
            viewHolder.addressTextView.setText(result.getAddress());
            viewHolder.addressTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.addressTextView.setVisibility(View.INVISIBLE);
        }

        if (position == 0) {
            int height = (int) mContext.getResources().getDimension(R.dimen.new_height);
            RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(viewHolder.relativeLayoutItem.getLayoutParams().width, height);
            viewHolder.relativeLayoutItem.setLayoutParams(ilp);
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(viewHolder.textViewDistanceItem.getLayoutParams().width, viewHolder.textViewDistanceItem.getLayoutParams().height);
            llp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llp.topMargin = (int) mContext.getResources().getDimension(R.dimen.offset_height); // llp.setMargins(left, top, right, bottom);
            viewHolder.textViewDistanceItem.setLayoutParams(llp);
        } else {
            viewHolder.relativeLayoutItem.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.venue_list_small_height_175);
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(viewHolder.textViewDistanceItem.getLayoutParams().width, viewHolder.textViewDistanceItem.getLayoutParams().height);
            llp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llp.topMargin = 0;
            viewHolder.textViewDistanceItem.setLayoutParams(llp);
        }

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
        try {
            double meters = Double.parseDouble(mVenues.get(position).getDistance());
            viewHolder.textViewDistanceItem.setText(getDistanceString(meters));
            viewHolder.textViewDistanceItem.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            viewHolder.textViewDistanceItem.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public static String getDistanceString(double meters) {
        String distanceString="";

        if(isMeters()) {
            if(meters<1000) {
                distanceString = "" + (int) meters + " m";
            } else {
                distanceString = "" + BigDecimal.valueOf(meters*0.001).setScale(1, BigDecimal.ROUND_HALF_UP) + " km";
            }
        } else {
            double miles = Common.metersToMiles(meters);
            if (miles < 1.0) {
                double distFeet = Common.milesToFeet(miles);
                if (distFeet <= 1000) {
                    int intDist = (int) Math.floor(distFeet);
                    distanceString = "" + intDist + " feet";
                } else {
                    miles = Math.ceil(miles * 10) / 10;
                    distanceString = "" + miles;
                    distanceString = distanceString.substring(1, distanceString.length()) + " miles";
                }
            } else if (miles >= 1000) {
                int distanceInInt = (int) miles;
                distanceString = String.valueOf(distanceInInt) + " miles";
            } else {
                miles = Math.ceil(miles * 10) / 10;
                distanceString = String.valueOf(miles) + " miles";
            }
        }
        return distanceString;
    }

    public void warmupCache(List<BusinessSearchResult> venues) {
        for (BusinessSearchResult b : venues) {
            String url = b.getProfileImage().getImageThumbnail();
            Picasso.with(mContext).load(url).fetch();
        }
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
        TextView addressTextView;
        int position;
    }

    private static boolean isMeters() {
        int type = SettingFragment.getDistancePref();
        if(type == 1) {
            return true;
        } else if (type == 2) {
            return false;
        } else { // pull local and determine if en_US
            String locale = Locale.getDefault().toString();
            return !locale.equals("en_US");
        }
    }
}
