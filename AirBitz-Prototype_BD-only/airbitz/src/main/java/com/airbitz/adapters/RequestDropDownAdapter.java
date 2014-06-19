package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Wallet;

import java.util.List;

/**
 * Created by matt on 6/17/14.
 */
public class RequestDropDownAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> mWalletList;

    public RequestDropDownAdapter(Context context, List<String> walletList) {
        super(context, R.layout.item_listview_request_drop_down, walletList);
        mWalletList = walletList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_request_drop_down, parent, false);
        ((TextView) convertView).setText(mWalletList.get(position));
        return convertView;
    }
}
