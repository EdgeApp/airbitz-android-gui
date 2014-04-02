package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.models.Business;

import java.util.List;

/**
 * Created on 2/11/14.
 */
public class BusinessSearchAdapter extends ArrayAdapter<Business> implements Filterable {

    private Context mContext;
    private List<Business> mLocationValue;

    private static int sGrayText;
    private static int sGreenText;

    public BusinessSearchAdapter(Context context, List<Business> locationValue){
        super(context, R.layout.item_listview_location, locationValue);
        mContext = context;
        mLocationValue = locationValue;
        sGrayText = context.getResources().getColor(R.color.gray_text);
        sGreenText = context.getResources().getColor(R.color.green_text);
    }

    @Override
    public int getCount() {
        return mLocationValue.size();
    }

    @Override
    public Business getItem(int position) {
        return mLocationValue.get(position);
    }

    public String getItemValue(int position){
        return mLocationValue.get(position).getName();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_location, parent, false);

        final Business business = mLocationValue.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_title);
        textView.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        textView.setText(business.getName());
        textView.setTextColor(business.isCached() ? sGreenText : sGrayText);

        return convertView;
    }
}



