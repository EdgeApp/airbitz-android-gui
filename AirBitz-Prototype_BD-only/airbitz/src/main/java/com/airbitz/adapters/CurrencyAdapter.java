package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;

import java.util.List;

/**
 * Created by matt on 7/4/14.
 */
public class CurrencyAdapter extends ArrayAdapter<String> implements Filterable {

    private Context mContext;
    private List<String> mCurrencies;
    public CurrencyAdapter(Context context, List<String> currencies){
        super(context, R.layout.item_currency_spinner, currencies);
        mContext = context;
        mCurrencies = currencies;
    }

    @Override
    public String getItem(int position) {
        return mCurrencies.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.item_currency_spinner_dropdown, parent, false);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_currency);
        textView.setText(mCurrencies.get(position));
        textView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        textView.setSingleLine(false);
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.item_currency_spinner, parent, false);

        TextView textView = (TextView) convertView.findViewById(R.id.textview_currency);
        textView.setText(mCurrencies.get(position));
        textView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        textView.setSingleLine(false);
        return convertView;
    }
}
