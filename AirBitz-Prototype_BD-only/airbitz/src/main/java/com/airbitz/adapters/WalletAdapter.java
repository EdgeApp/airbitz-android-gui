package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.Wallet;

import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletAdapter extends ArrayAdapter<Wallet> {

    private Context mContext;
    private List<Wallet> mWalletList;

    public WalletAdapter(Context context, List<Wallet> walletList){
        super(context, R.layout.item_listview_wallets, walletList);
        mContext = context;
        mWalletList = walletList;
    }

    @Override
    public Wallet getItem(int position) {
        return super.getItem(position);
    }

    public List<Wallet> getList(){
        return mWalletList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_wallets, parent, false);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textview_title);
        TextView amountTextView = (TextView) convertView.findViewById(R.id.textview_amount);
        titleTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        amountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace, Typeface.ITALIC);
        titleTextView.setText(mWalletList.get(position).getName());
        amountTextView.setText(mWalletList.get(position).getAmount()
                + mContext.getResources().getString(R.string.no_break_space_character));
        return convertView;
    }

}
