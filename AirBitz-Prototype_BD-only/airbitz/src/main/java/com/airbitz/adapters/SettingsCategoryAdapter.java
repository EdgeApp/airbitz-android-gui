package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.airbitz.R;

import java.util.List;

/**
 * Created by matt on 6/24/14.
 */
public class SettingsCategoryAdapter extends ArrayAdapter<String> {

    private List<String> mCategories;
    private Context mContext;

    public SettingsCategoryAdapter(Context context, List<String> categories) {
        super(context, R.layout.item_listview_settings_categories,categories);
        mCategories = categories;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_settings_categories, parent, false);

        return convertView;
    }
}
