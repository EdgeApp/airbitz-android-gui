package com.airbitz.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.models.Transaction;

import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private List<Transaction> mTransactionList;

    public WalletAdapter(Context context, List<Transaction> transactionList){
        super(context, R.layout.item_listview_wallets, transactionList);
        mContext = context;
        mTransactionList = transactionList;
    }

    @Override
    public Transaction getItem(int position) {
        return super.getItem(position);
    }

    public List<Transaction> getList(){
        return mTransactionList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_wallets, parent, false);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textview_title);
        TextView amountTextView = (TextView) convertView.findViewById(R.id.textview_amount);
        titleTextView.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        amountTextView.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace, Typeface.ITALIC);
        titleTextView.setText(mTransactionList.get(position).getName());
        amountTextView.setText(mTransactionList.get(position).getmAmount()
                + mContext.getResources().getString(R.string.no_break_space_character));
        return convertView;
    }

}
