package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;

/**
 * Created on 2/14/14.
 */
public class BusinessDetailAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private String[] mBusinessDetailValue;

    public BusinessDetailAdapter(Context context, String[] businessDetailValue) {
        super(context, R.layout.item_listview_business, businessDetailValue);
        mContext = context;
        mBusinessDetailValue = businessDetailValue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_business, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.textview_business_title);
        textView.setText(mBusinessDetailValue[position]);
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.imageview_icon);

        if (position == 0) {
            iconImage.setImageResource(R.drawable.ico_bitcoin_loc);
        }

        return convertView;
    }
}
