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
    private boolean mSearch;
    private boolean mIsBitcoin = true;
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

        if(0 == position && mListTransaction.size() == 1){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_solo));
        }else if(0 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_top));
        }else if(mListTransaction.size()-1 == position){
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_bottom));
        }else{
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.wallet_list_standard));
        }

        String dateString = new SimpleDateFormat("MMM dd yyyy, kk:mm aa").format(mListTransaction.get(position).getDate()*1000);
        dateTextView.setText(dateString);

        Transaction transaction = mListTransaction.get(position);
        Wallet wallet = mCoreAPI.getWallet(transaction.getWalletUUID());

        nameTextView.setText(transaction.getName());
        long transactionSatoshis = transaction.getAmountSatoshi();
        long transactionFees = transaction.getMinerFees() + transaction.getABFees();
        if(mSearch){
            String btcCurrency = mCoreAPI.FormatDefaultCurrency(transactionSatoshis, true, false);
            creditAmountTextView.setText(mCoreAPI.getUserBTCSymbol()+" "+btcCurrency);
            String fiatCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, wallet.getCurrencyNum(), false, true);
            debitAmountTextView.setText(fiatCurrency);
            if(transactionSatoshis >= 0){
                debitAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
                creditAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
            }else{
                debitAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                creditAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
            }
        }else {
            if(transactionSatoshis >= 0){
                creditAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
            }else{
                creditAmountTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
            }
            if (mIsBitcoin) {
                String walletCurrency = mCoreAPI.FormatDefaultCurrency(transactionSatoshis, true, false);
                long totalSatoshisSoFar = 0;
                for(int i = position; i < mListTransaction.size();i++){
                    totalSatoshisSoFar+=mListTransaction.get(i).getAmountSatoshi();
                }
                String totalCurrency = mCoreAPI.FormatDefaultCurrency(totalSatoshisSoFar, true, false);

                creditAmountTextView.setText(mCoreAPI.getUserBTCSymbol() + " " + walletCurrency);
                debitAmountTextView.setText(mCoreAPI.getUserBTCSymbol() + " " + totalCurrency);
            } else {
                String walletCurrency = mCoreAPI.FormatCurrency(transactionSatoshis, wallet.getCurrencyNum(), false, true);
                long totalSatoshisSoFar = 0;
                for(int i = position; i < mListTransaction.size();i++){
                    totalSatoshisSoFar+=mListTransaction.get(i).getAmountSatoshi();
                }
                String totalCurrency = mCoreAPI.FormatCurrency(totalSatoshisSoFar,wallet.getCurrencyNum(), false, true);

                creditAmountTextView.setText(walletCurrency);
                debitAmountTextView.setText(totalCurrency);
            }
        }
        if(mSearch){
//            debitAmountTextView.setText("$0.00");
            confirmationsTextView.setText(transaction.getCategory());
        }else {
            confirmationsTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
            if(transaction.getConfirmations() == 0){
                confirmationsTextView.setText("Unconfirmed");
                confirmationsTextView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
            }else if(transaction.getConfirmations() >= 6){
                confirmationsTextView.setText("Confirmed");
            }else{
                confirmationsTextView.setText(transaction.getConfirmations()+" confirmations");
            }
        }
        dateTextView.setTypeface(BusinessDirectoryFragment.latoBlackTypeFace);
        nameTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);
        debitAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        creditAmountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        return convertView;
    }
}
