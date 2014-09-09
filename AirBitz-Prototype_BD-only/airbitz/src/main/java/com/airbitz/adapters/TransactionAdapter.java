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
import com.airbitz.models.Wallet;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private Wallet mWallet;
    private boolean mSearch;
    private boolean mIsBitcoin = true;
    private int mCurrencyNum;
    private CoreAPI mCoreAPI;

    private List<Transaction> mListTransaction;
    private long[] mRunningSatoshi;

    public TransactionAdapter(Context context, Wallet wallet, List<Transaction> listTransaction){
        super(context, R.layout.item_listview_transaction, listTransaction);
        mContext = context;
        mWallet = wallet;
        mCurrencyNum = mWallet.getCurrencyNum();
        mListTransaction = listTransaction;
        createRunningSatoshi();
        mCoreAPI = CoreAPI.getApi();
    }

    public void createRunningSatoshi() {
        mRunningSatoshi = new long[mListTransaction.size()];

        long total = 0;
        for(int i=mListTransaction.size()-1; i>-1; i--) {
            total += mListTransaction.get(i).getAmountSatoshi();
            mRunningSatoshi[i] = total;
        }
    }

    public void setSearch(boolean isSearch){
        mSearch = isSearch;
    }

    public void setIsBitcoin(boolean isBitcoin) { mIsBitcoin = isBitcoin; }

    static class ViewHolderItem {
        TextView dateTextView;
        TextView nameTextView;
        TextView runningTotalTextView;
        TextView creditAmountTextView;
        TextView confirmationsTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if(convertView==null){
            // well set up the ViewHolder
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_transaction, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.textview_date);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.textview_name);
            viewHolder.runningTotalTextView = (TextView) convertView.findViewById(R.id.textview_amount_running_total);
            viewHolder.creditAmountTextView = (TextView) convertView.findViewById(R.id.textview_amount_kredit);
            viewHolder.confirmationsTextView = (TextView) convertView.findViewById(R.id.textview_confirmations);
            viewHolder.dateTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
            viewHolder.nameTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);
            viewHolder.runningTotalTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            viewHolder.creditAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            // store the holder with the view.
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }


        if(0 == position && mListTransaction.size() == 1){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_solo));
        }else if(0 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_top_archive));
        }else if(mListTransaction.size()-1 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_bottom));
        }else{
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard));
        }

        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(mListTransaction.get(position).getDate()*1000);
        viewHolder.dateTextView.setText(dateString);

        Transaction transaction = mListTransaction.get(position);

        viewHolder.nameTextView.setText(transaction.getName());

        long transactionSatoshis = transaction.getAmountSatoshi();
        long transactionSatoshisAbs = Math.abs(transactionSatoshis);

        String btcSymbol;
        String btcSymbolBalance = mCoreAPI.getUserBTCSymbol();
        Boolean bPositive;

        if (transactionSatoshis < 0)
        {
            btcSymbol = "-" + btcSymbolBalance;
            bPositive = false;
        }
        else
        {
            btcSymbol = btcSymbolBalance;
            bPositive = true;
        }

        if(mSearch){
            String btcCurrency = mCoreAPI.FormatDefaultCurrency(transactionSatoshisAbs, true, false);
            viewHolder.creditAmountTextView.setText(btcSymbol+" "+btcCurrency);

            String fiatCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, mCurrencyNum, false, true);
            viewHolder.runningTotalTextView.setText(fiatCurrency);

            if(bPositive){
                viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            }else{
                viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.red));
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            }
        }else {
            viewHolder.runningTotalTextView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
            if(bPositive){
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            }else{
                viewHolder.creditAmountTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            }
            if (mIsBitcoin) {
                String walletCurrency = mCoreAPI.FormatDefaultCurrency(transactionSatoshisAbs, true, false);
                String totalCurrency = mCoreAPI.FormatDefaultCurrency(mRunningSatoshi[position], true, false);

                viewHolder.creditAmountTextView.setText(btcSymbol + " " + walletCurrency);
                viewHolder.runningTotalTextView.setText(btcSymbolBalance + " " + totalCurrency);
            } else {
                String walletCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, mCurrencyNum, false, true);
                String totalCurrency = mCoreAPI.FormatCurrency(mRunningSatoshi[position], mCurrencyNum, false, true);

                viewHolder.creditAmountTextView.setText(walletCurrency);
                viewHolder.runningTotalTextView.setText(totalCurrency);
            }
        }
        if(mSearch){
            viewHolder.confirmationsTextView.setText(transaction.getCategory());
            viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
        }else {
            viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.green_text_dark));
            if(transaction.getConfirmations() == 0){
                viewHolder.confirmationsTextView.setText(mContext.getString(R.string.fragment_wallet_unconfirmed));
                viewHolder.confirmationsTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            }else if(transaction.getConfirmations() >= 6){
                viewHolder.confirmationsTextView.setText(mContext.getString(R.string.fragment_wallet_confirmed));
            }else{
                viewHolder.confirmationsTextView.setText(transaction.getConfirmations()+mContext.getString(R.string.fragment_wallet_confirmations));
            }
        }
        return convertView;
    }

    public void selectItem(View convertView, int position) {
        if(0 == position && mListTransaction.size() == 1){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_solo_selected));
        }else if(0 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_top_archive_selected));
        }else if(mListTransaction.size()-1 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_bottom_selected));
        }else{
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard_selected));
        }
    }
}
