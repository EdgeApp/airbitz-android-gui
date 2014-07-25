package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;

import java.util.List;

/**
 * Created by matt on 7/22/14.
 */
public class WalletPickerAdapter extends ArrayAdapter {

    private Context mContext;
    private List<Wallet> mWalletList;
    private WalletPickerEnum mSource;

    public WalletPickerAdapter(Context context,List<Wallet> walletList, WalletPickerEnum source){
        super(context,R.layout.item_request_wallet_spinner,walletList);
        mContext = context;
        mWalletList = walletList;
        mSource = source;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mSource == WalletPickerEnum.SendFrom || mSource == WalletPickerEnum.Request){
            convertView = inflater.inflate(R.layout.item_request_wallet_spinner_dropdown, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_request_wallet_spinner_dropdown_textview);
            textView.setText(mWalletList.get(position).getName() + " ("+mWalletList.get(position).getBalanceFormatted()+")");
        }
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mSource == WalletPickerEnum.SendTo){
            convertView = inflater.inflate(R.layout.item_send_listing_spinner,parent,false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_send_listing_spinner_textview);
            textView.setText(mWalletList.get(position).getName() + " ("+mWalletList.get(position).getBalanceFormatted()+")");
        }else {
            convertView = inflater.inflate(R.layout.item_request_wallet_spinner, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.item_request_wallet_spinner_textview);
            textView.setText(mWalletList.get(position).getName());
        }
        return convertView;
    }
}
