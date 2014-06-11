package com.airbitz.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.StringBusinessTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by matt on 6/10/14.
 */
public class TransactionDetailSearchAdapter extends ArrayAdapter {
    private Context mContext;
    private List<BusinessSearchResult> mBusinesses;
    private List<String> mContactNames;
    private List<Object> mCombined;

    private static int sGrayText;
    private static int sGreenText;

    public TransactionDetailSearchAdapter(Context context, List<BusinessSearchResult> businesses, List<String> contactNames, List<Object> combined){
        super(context, R.layout.item_listview_transaction_detail, combined);
        mContext = context;
        mBusinesses = businesses;
        mContactNames = contactNames;
        mCombined = combined;
    }

    @Override
    public int getCount() {
        return mCombined.size();
    }

    @Override
    public Object getItem(int position) {
        return mCombined.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_transaction_detail, parent, false);

        final BusinessSearchResult business = (BusinessSearchResult)mCombined.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.transaction_detail_item);
        textView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        textView.setText(business.getName());

        return convertView;
    }

}
