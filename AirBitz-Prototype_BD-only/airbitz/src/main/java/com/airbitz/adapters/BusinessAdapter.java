package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbitz.R;

/**
 * Created on 2/11/14.
 */
public class BusinessAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private String[] mBusinessValue;

    public BusinessAdapter(Context context, String[] businessValue){
        super(context, R.layout.item_listview_business, businessValue);
        mContext = context;
        mBusinessValue = businessValue;
    }

    public String getBusinessValue(int position){
        return mBusinessValue[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_business, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.textview_business_title);
        textView.setText(mBusinessValue[position]);
        Typeface latoBlackTypeFace=Typeface.createFromAsset(mContext.getAssets(), "font/Lato-Bla.ttf");
        textView.setTypeface(latoBlackTypeFace);
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.imageview_icon);

        if(position == 0){
            iconImage.setImageResource(R.drawable.ico_restaurant);
        }
        else if(position == 1){
            iconImage.setImageResource(R.drawable.ico_bar);
        }
        else if(position == 2){
            iconImage.setImageResource(R.drawable.ico_coffee);
        }
        else if(position == 3){
            iconImage.setImageResource(R.drawable.ico_more);
        }

        return convertView;
    }
}
