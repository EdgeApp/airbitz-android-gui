package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final int UPPER = 0;
    private static final int LOWER = 1;

    private Button mBitCoinBalanceButton;
    private Button mDollarBalanceButton;
    private Button mButtonMover;
    private SeekBar mSeekBar;


    private ListView mLatestWalletListView;

    private ImageButton mHelpButton;
    private ImageButton mAddButton;

    private TextView mTitleTextView;

    private WalletAdapter mLatestWalletAdapter;

    private boolean mSwitchWordOne = true;
    private boolean mOnBitcoinMode = true;

    private List<Wallet> mLatestWalletList;
    private AirbitzAPI mAPI;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAPI = AirbitzAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallets, container, false);

        mLatestWalletList = mAPI.getWallets();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mLatestWalletAdapter = new WalletAdapter(getActivity(), mLatestWalletList);

        mBitCoinBalanceButton = (Button) view.findViewById(R.id.back_button_top);
        mDollarBalanceButton = (Button) view.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);

        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mAddButton = (ImageButton) view.findViewById(R.id.button_add);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWalletType();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Wallet Info", "Wallet info description");
            }
        });

        mBitCoinBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBitCoinBalanceButton.setBackgroundResource(R.drawable.btn_green);
                mDollarBalanceButton.setBackgroundResource(getResources().getColor(android.R.color.transparent));

                mBitCoinBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_btc, 0, R.drawable.ico_btc, 0);
                mDollarBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_usd, 0, R.drawable.ico_usd, 0);

                mBitCoinBalanceButton.setTextColor(getResources().getColor(android.R.color.white));
                mDollarBalanceButton.setTextColor(getResources().getColor(android.R.color.black));

                mBitCoinBalanceButton.setTypeface(NavigationActivity.montserratBoldTypeFace);
                mDollarBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);

                mBitCoinBalanceButton.setPadding(15,10,15,10);
                mDollarBalanceButton.setPadding(15,10,15,10);


                mLatestWalletListView.setAdapter(mLatestWalletAdapter);

                if(!mOnBitcoinMode){

                    double conv = 8.7544;
                    for(Wallet trans: mLatestWalletList){
                        try{
                            double item = Double.parseDouble(trans.getAmount().substring(1))*conv;
                            String amount = String.format("B%.3f", item);
                            trans.setAmount(amount);
                        } catch (Exception e){
                            trans.setAmount("0");
                            e.printStackTrace();
                        }
                    }

                    mLatestWalletAdapter.notifyDataSetChanged();
                }
                mOnBitcoinMode = true;

            }
        });
        mDollarBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBitCoinBalanceButton.setBackgroundResource(getResources().getColor(android.R.color.transparent));
                mDollarBalanceButton.setBackgroundResource(R.drawable.btn_green);

                mBitCoinBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_btc_dark, 0, R.drawable.ico_btc, 0);
                mDollarBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_dollar_white, 0, R.drawable.ico_usd_white, 0);

                mBitCoinBalanceButton.setTextColor(getResources().getColor(android.R.color.black));
                mDollarBalanceButton.setTextColor(getResources().getColor(android.R.color.white));

                mBitCoinBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
                mDollarBalanceButton.setTypeface(NavigationActivity.montserratBoldTypeFace);

                mBitCoinBalanceButton.setPadding(15,10,15,10);
                mDollarBalanceButton.setPadding(15,10,15,10);

                mLatestWalletListView.setAdapter(mLatestWalletAdapter);

                if(mOnBitcoinMode){

                    double conv = 0.1145;
                    for(Wallet trans: mLatestWalletList){
                        try{
                            double item = Double.parseDouble(trans.getAmount().substring(1))*conv;
                            String amount = String.format("$%.3f", item);
                            trans.setAmount(amount);
                        } catch (Exception e){
                            trans.setAmount("0");
                            e.printStackTrace();
                        }
                    }

                    mLatestWalletAdapter.notifyDataSetChanged();
                }
                mOnBitcoinMode = false;

            }
        });

        mOnBitcoinMode = true;

//        mBitCoinBalanceButton.setBackgroundResource(R.drawable.btn_green);
//        mDollarBalanceButton.setBackgroundResource(getResources().getColor(android.R.color.transparent));
//
//        mBitCoinBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_btc, 0, R.drawable.ico_btc, 0);
//        mDollarBalanceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_coin_usd, 0, R.drawable.ico_usd, 0);
//
//        mBitCoinBalanceButton.setTextColor(getResources().getColor(android.R.color.white));
//        mDollarBalanceButton.setTextColor(getResources().getColor(android.R.color.black));
//        mBitCoinBalanceButton.setPadding(15, 10, 15, 10);
//        mDollarBalanceButton.setPadding(15, 10, 15, 10);
//
//        mBitCoinBalanceButton.setTypeface(NavigationActivity.montserratBoldTypeFace);
//        mDollarBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
//

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mLatestWalletListView = (ListView) view.findViewById(R.id.layout_listview);

        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mDollarBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mButtonMover.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mLatestWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                showWalletFragment(a.getList().get(i).getName(), a.getList().get(i).getAmount());
            }
        });

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar_slider);
        mSeekBar.setOnSeekBarChangeListener(this);

        return view;
    }

    private void showWalletFragment(String name, String amount) {
        Bundle bundle = new Bundle();
        bundle.putString(Wallet.WALLET_NAME, name);
        bundle.putString(Wallet.WALLET_AMOUNT, amount);
        Fragment fragment = new WalletFragment();
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }

//    @Override
//    public void onBackPressed() {
//        if(mRequestClass == null){
//            finish();
//        }
//        else{
//            super.onBackPressed();
//        }
//
//    }
//
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        float heightDiff = mBitCoinBalanceButton.getHeight();
        float moveX = heightDiff*progress/seekBar.getMax();
        RelativeLayout.LayoutParams absParams =
                (RelativeLayout.LayoutParams)mButtonMover.getLayoutParams();

        absParams.leftMargin = 0;
        absParams.topMargin = (int) -moveX;

        mButtonMover.setLayoutParams(absParams);
    }

    public void onStartTrackingTouch(SeekBar seekBar) { }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar.getProgress() > seekBar.getMax()/2) {
            seekBar.setProgress(seekBar.getMax());
            setSwitchSelection(UPPER);
        } else {
            seekBar.setProgress(0);
            setSwitchSelection(LOWER);
        }
        onProgressChanged(seekBar, seekBar.getProgress(), true);
    }

    private void setSwitchSelection(int selection) {
        Drawable[] drawables;
        switch(selection) {
            case UPPER:
                mButtonMover.setText(mBitCoinBalanceButton.getText());
                drawables = mBitCoinBalanceButton.getCompoundDrawables();
                mButtonMover.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
               break;
            case LOWER:
                mButtonMover.setText(mDollarBalanceButton.getText());
                drawables = mDollarBalanceButton.getCompoundDrawables();
                mButtonMover.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                break;
            default:
                break;
        }
    }

    public void addItemToLatestTransactionList(String name, String amount, List<Wallet> mTransactionList){

        if(mOnBitcoinMode){
            double conv = 8.7544;
            double item = Double.parseDouble(amount.substring(1))*conv;
            amount = String.format("B%.3f", item);
        }
        else{
            double conv = 0.1145;
            double item = Double.parseDouble(amount.substring(1))*conv;
            amount = String.format("$%.3f", item);
        }

        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mTransactionList.add(new Wallet(name, amount));
        mLatestWalletAdapter.notifyDataSetChanged();
        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());

    }


    public void showDialogWalletType(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle("Wallet Type");

        alertDialogBuilder
                .setMessage("Do you want to create online or offline wallet?")
                .setCancelable(false)
                .setPositiveButton("Online",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if(mSwitchWordOne){
                            addItemToLatestTransactionList("Baseball Team", "B15.000", mLatestWalletList);
                            mSwitchWordOne = false;
                        }
                        else{
                            addItemToLatestTransactionList("Fantasy Football", "B10.000", mLatestWalletList);
                            mSwitchWordOne = true;
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton("Offline",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment());
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}