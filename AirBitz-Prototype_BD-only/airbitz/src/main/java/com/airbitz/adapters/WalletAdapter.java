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

import java.util.HashMap;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletAdapter extends ArrayAdapter<Wallet> {

    final int INVALID_ID = -1;

    private Context mContext;
    private List<Wallet> mWalletList;
    private int selectedViewPos = -1;
    private boolean hoverFirstHeader = false;
    private boolean hoverSecondHeader = false;
    private int nextId = 0;

    private boolean closeAfterArchive = false;
    private int  archivePos;


    HashMap<Wallet, Integer> mIdMap = new HashMap<Wallet, Integer>();
    HashMap<Wallet, Integer> mArchivedIdMap = new HashMap<Wallet, Integer>();

    public WalletAdapter(Context context, List<Wallet> walletList){
        super(context, R.layout.item_listview_wallets, walletList);
        mContext = context;
        mWalletList = walletList;
        for (int i = 0; i < walletList.size(); ++i) {
            if(walletList.get(i).getName() == "SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd") {
                archivePos = i;
            }
            mIdMap.put(walletList.get(i), i);
            nextId++;
        }
    }

    public void setFirstHeaderHover(boolean status){ hoverFirstHeader = status;}

    public void setSecondHeaderHover(boolean status){ hoverSecondHeader = status;}

    public void setSelectedViewPos(int position){
        selectedViewPos = position;
    }

    public void addWallet(Wallet wallet){
        if(mArchivedIdMap.containsKey(wallet)){
            mIdMap.put(wallet,mArchivedIdMap.get(wallet));
            mArchivedIdMap.remove(wallet);
        }else {
            mIdMap.put(wallet, nextId);
            nextId++;
        }
        //mWalletList.add(wallet);
    }

    public void removeWallet(Wallet wallet){
        mArchivedIdMap.put(wallet,mIdMap.get(wallet));
        mIdMap.remove(wallet);
    }

    public void switchCloseAfterArchive(int pos){
        archivePos = pos;
        closeAfterArchive = !closeAfterArchive;
    }

    @Override
    public Wallet getItem(int position) {
        return super.getItem(position);
    }

    public List<Wallet> getList(){
        return mWalletList;
    }

    public int getArchivePos(){ return archivePos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(mWalletList.get(position).getName()=="SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd" || mWalletList.get(position).getName()=="xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL"){//TODO ALERT
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets_header, parent, false);
            if(mWalletList.get(position).getName()=="SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd") {
                ((TextView) convertView).setText("ARCHIVE");
                archivePos = position;
                if(hoverSecondHeader){
                    convertView.setVisibility(View.INVISIBLE);
                }else {
                    convertView.setVisibility(View.VISIBLE);
                }
            }else{
                ((TextView) convertView).setText("WALLETS");
                if(hoverFirstHeader){
                    convertView.setVisibility(View.INVISIBLE);
                }else {
                    convertView.setVisibility(View.VISIBLE);
                }
            }
        }else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_wallets, parent, false);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.textview_title);
            TextView amountTextView = (TextView) convertView.findViewById(R.id.textview_amount);
            titleTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
            amountTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace, Typeface.ITALIC);
            titleTextView.setText(mWalletList.get(position).getName());
            amountTextView.setText(mWalletList.get(position).getAmount()
                    + mContext.getResources().getString(R.string.no_break_space_character));
        }
        if(archivePos < position && closeAfterArchive){
            //convertView.setVisibility(View.GONE);
        }else if(selectedViewPos == position){
            convertView.setVisibility(View.INVISIBLE);
        }else{
            convertView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public int getMapSize(){ return mIdMap.size();}

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Wallet item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
