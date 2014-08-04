
package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.LocationSearchResult;

import java.util.List;

/**
 * Created on 2/11/14.
 */
public class LocationAdapter extends ArrayAdapter<LocationSearchResult> implements Filterable {

    private Context mContext;
    private List<LocationSearchResult> mLocationValue;

    private static int sGrayText;
    private static int sGreenText;
    private static int sBlueText;

    private static String sCurrentLocation;
    private static String sOnTheWeb;

    public LocationAdapter(Context context, List<LocationSearchResult> locationValue) {
        super(context, R.layout.item_listview_location, locationValue);
        mContext = context;
        mLocationValue = locationValue;

        // Text color
        sGrayText = context.getResources().getColor(R.color.gray_text);
        sGreenText = context.getResources().getColor(R.color.green_text);
        sBlueText = context.getResources().getColor(R.color.blue_text);

        sCurrentLocation = context.getString(R.string.current_location);
        sOnTheWeb = context.getString(R.string.on_the_web);
    }

    @Override public int getCount() {
        return mLocationValue.size();
    }

    @Override public LocationSearchResult getItem(int position) {
        return mLocationValue.get(position);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_location, parent, false);

        final LocationSearchResult location = mLocationValue.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.fragment_category_textview_title);
        textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        textView.setText(location.getLocationName());

        if (sCurrentLocation.equals(location.getLocationName()) ||
            sOnTheWeb.equals(location.getLocationName())) {
            textView.setTextColor(sBlueText);
        } else {
            textView.setTextColor(location.isCached() ? sGreenText : sGrayText);
        }

        return convertView;
    }
}
