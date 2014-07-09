package com.airbitz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BusinessDirectoryFragment;
import com.airbitz.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private boolean mSearch;
    private boolean mIsBitcoin;
    private int mCurrencyNum;
    private CoreAPI mCoreAPI;

    private List<Transaction> mListTransaction;

    public TransactionAdapter(Context context, List<Transaction> listTransaction){
        super(context, R.layout.item_listview_transaction, listTransaction);
        mContext = context;
        mListTransaction = listTransaction;
        mCoreAPI = CoreAPI.getApi();
    }

    public void setSearch(boolean isSearch){
        mSearch = isSearch;
    }

    public void setIsBitcoin(boolean isBitcoin) { mIsBitcoin = isBitcoin; }

    public void setCurrencyNum(int num) { mCurrencyNum = num; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_transaction, parent, false);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.textview_date);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.textview_name);
        TextView debitAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_debit);
        TextView creditAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_kredit);
        TextView confirmationsTextView = (TextView) convertView.findViewById(R.id.textview_confirmations);

        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(mListTransaction.get(position).getDate()*1000);
        dateTextView.setText(dateString);

        nameTextView.setText(mListTransaction.get(position).getName());
        long transactionSatoshis = mListTransaction.get(position).getAmountSatoshi();
        if(mIsBitcoin) {
            creditAmountTextView.setText(mCoreAPI.formatSatoshi(transactionSatoshis));
        } else {
            creditAmountTextView.setText(mCoreAPI.FormatDefaultCurrency(transactionSatoshis, false, true));
        }
        if(mSearch){
//            debitAmountTextView.setText("$0.00");
            confirmationsTextView.setText("None");
        }else {
//            debitAmountTextView.setText("Debit amount"+mContext.getResources().getString(R.string.no_break_space_character));
            confirmationsTextView.setText("2 confirmations");
        }
        dateTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
        nameTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);
        debitAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        creditAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        return convertView;
    }
}
