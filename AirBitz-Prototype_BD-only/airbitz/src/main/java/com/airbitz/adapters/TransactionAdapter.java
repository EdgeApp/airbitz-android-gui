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
import com.airbitz.models.AccountTransaction;

import java.util.List;

/**
 * Created on 2/13/14.
 */
public class TransactionAdapter extends ArrayAdapter<AccountTransaction> {

    private Context mContext;
    private boolean search;

    private List<AccountTransaction> mListAccountTransaction;

    public TransactionAdapter(Context context, List<AccountTransaction> listTransaction){
        super(context, R.layout.item_listview_transaction, listTransaction);
        mContext = context;
        mListAccountTransaction = listTransaction;
    }

    public void setSearch(boolean isSearch){
        search = isSearch;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_listview_transaction, parent, false);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.textview_date);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.textview_name);
        TextView debitAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_debit);
        TextView creditAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_kredit);
        TextView confirmationsTextView = (TextView) convertView.findViewById(R.id.textview_confirmations);
        dateTextView.setText(mListAccountTransaction.get(position).getDate());
        nameTextView.setText(mListAccountTransaction.get(position).getName());
        creditAmountTextView.setText(mListAccountTransaction.get(position).getCreditAmount());
        if(search){
            debitAmountTextView.setText("$0.00");
            confirmationsTextView.setText("None");
        }else {
            debitAmountTextView.setText(mListAccountTransaction.get(position).getDebitAmount()
                    + mContext.getResources().getString(R.string.no_break_space_character));
            confirmationsTextView.setText("2 confirmations");
        }
        dateTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
        nameTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);
        debitAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        creditAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        return convertView;
    }
}
