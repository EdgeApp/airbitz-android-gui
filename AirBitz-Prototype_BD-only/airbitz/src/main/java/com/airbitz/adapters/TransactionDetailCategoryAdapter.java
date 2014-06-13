package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;

import java.util.List;

/**
 * Created by matt on 6/12/14.
 */
public class TransactionDetailCategoryAdapter extends ArrayAdapter {
    private Context mContext;
    private List<String> mCategories;

    public TransactionDetailCategoryAdapter(Context context, List<String> categories){
        super(context, R.layout.item_listview_transaction_detail, categories);
        mContext = context;
        mCategories = categories;
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public String getItem(int position) {
        return mCategories.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final String category = (String) mCategories.get(position);
        convertView = inflater.inflate(R.layout.item_listview_transaction_detail, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item_name);
        textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        textView.setText(category);



        return convertView;
    }
}
