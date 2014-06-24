package com.airbitz.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.PasswordRecoveryActivity;
import com.airbitz.activities.SignUpActivity;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private Button mBitCoinBalanceButton;
    private Button mDollarBalanceButton;
    private Button mButtonMover;
    //private SeekBar mSeekBar;

    private TextView walletsHeader;
    private TextView archiveHeader;

    private LinearLayout newWalletLayout;
    private EditText nameEditText;
    private TextView onlineTextView;
    private TextView offlineTextView;
    private Switch newWalletSwitch;
    private Button cancelButton;
    private Button doneButton;
    private Spinner newWalletSpinner;
    private LinearLayout spinnerLayout;

    private boolean archiveClosed = false;

    private RelativeLayout topSwitch;
    private RelativeLayout bottomSwitch;
    private RelativeLayout switchable;
    private RelativeLayout switchContainer;

    private DynamicListView mLatestWalletListView;

    private ImageButton mHelpButton;
    private ImageButton mAddButton;

    private ImageView moverCoin;
    private ImageView moverType;


    private TextView mTitleTextView;

    private WalletAdapter mLatestWalletAdapter;

    private boolean mSwitchWordOne = true;
    private boolean mOnBitcoinMode = true;

    private List<Wallet> mLatestWalletList;
    private List<Wallet> archivedWalletList;

    private List<String> currencyList;
    private CoreAPI mAPI;
    private AddWalletTask mAddWalletTask;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAPI = CoreAPI.getApi();
        mLatestWalletList = mAPI.loadWallets();

//        //*******************TESTING
//        mAPI.setWalletOrder(mLatestWalletList);
//
//        String temp = mAPI.conversion(100000000, false);
//        temp = mAPI.conversion(2147483647, false);
//        double big = 2147483647*10.0;
//        temp = mAPI.conversion((long) big, false);
//        temp = mAPI.conversion(1073741823, false);
//        String other = temp+"";

//        String temp = mAPI.formatSatoshi(2147483647, false, 3);
//        String temp2 = mAPI.SatoshiToCurrencyString((long) 1E8);

        archivedWalletList = new ArrayList<Wallet>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallets, container, false);

        currencyList = new ArrayList<String>();
        currencyList.add("CAD");
        currencyList.add("USD");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mLatestWalletAdapter = new WalletAdapter(getActivity(), mLatestWalletList);

        newWalletLayout = (LinearLayout) view.findViewById(R.id.layout_new_wallet);
        nameEditText = (EditText) view.findViewById(R.id.name_edittext);
        onlineTextView = (TextView) view.findViewById(R.id.online_textview);
        offlineTextView = (TextView) view.findViewById(R.id.offline_textview);
        newWalletSwitch = (Switch) view.findViewById(R.id.new_wallet_switch);
        cancelButton = (Button) view.findViewById(R.id.button_cancel);
        doneButton = (Button) view.findViewById(R.id.button_done);
        newWalletSpinner = (Spinner) view.findViewById(R.id.new_wallet_spinner);
        spinnerLayout = (LinearLayout) view.findViewById(R.id.spinner_layout);

        mBitCoinBalanceButton = (Button) view.findViewById(R.id.back_button_top);
        mDollarBalanceButton = (Button) view.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);

        topSwitch = (RelativeLayout) view.findViewById(R.id.top_switch);
        bottomSwitch = (RelativeLayout) view.findViewById(R.id.bottom_switch);
        switchable = (RelativeLayout) view.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) view.findViewById(R.id.layout_balance);

        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mAddButton = (ImageButton) view.findViewById(R.id.button_add);

        moverCoin = (ImageView) view.findViewById(R.id.button_mover_coin);
        moverType = (ImageView) view.findViewById(R.id.button_mover_type);

        walletsHeader = (TextView) view.findViewById(R.id.wallets_header);
        archiveHeader = (TextView) view.findViewById(R.id.archive_header);

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
                mBitCoinBalanceButton.setEnabled(false);
                mDollarBalanceButton.setEnabled(false);
                mButtonMover.setEnabled(false);
                mBitCoinBalanceButton.setClickable(false);
                mDollarBalanceButton.setClickable(false);
                mButtonMover.setClickable(false);
                mBitCoinBalanceButton.setFocusable(false);
                mDollarBalanceButton.setFocusable(false);
                mButtonMover.setFocusable(false);
            }
        });

        mBitCoinBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(true);
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                mOnBitcoinMode = true;
            }
        });
        mDollarBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(false);
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                mOnBitcoinMode = false;
            }
        });
        mButtonMover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                if(mOnBitcoinMode){
                    switchBarInfo(false);
                    mOnBitcoinMode = false;
                }else{
                    switchBarInfo(true);
                    mOnBitcoinMode = true;
                }
            }
        });

        mOnBitcoinMode = true;
        //switchBarInfo(true);

        /*nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });*/

        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    goDoneCancel(0);
                    return true;
                }
                return false;
            }
        });

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, currencyList);
        newWalletSpinner.setSelection(1);

        newWalletSpinner.setAdapter(dataAdapter);

        newWalletSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    onlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    offlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    nameEditText.setVisibility(View.INVISIBLE);
                }else{
                    onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    spinnerLayout.setVisibility(View.VISIBLE);
                    nameEditText.setVisibility(View.VISIBLE);
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDoneCancel(0);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDoneCancel(1);
            }
        });


        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mLatestWalletListView = (DynamicListView) view.findViewById(R.id.layout_listview);

        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mLatestWalletListView.setWalletList(mLatestWalletList);
        mLatestWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mLatestWalletListView.setHeaders(walletsHeader,archiveHeader);
        mLatestWalletListView.setArchivedList(archivedWalletList);
        mLatestWalletListView.setArchiveClosed(archiveClosed);

        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mDollarBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mButtonMover.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        archiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mLatestWalletAdapter.getArchivePos();
                //a.switchCloseAfterArchive(pos);
                System.out.println("Map Sizes before: " +mLatestWalletAdapter.getMapSize()+" vs " + mLatestWalletList.size());
                if(archiveClosed){
                    while(!archivedWalletList.isEmpty()){
                        mLatestWalletList.add(archivedWalletList.get(0));
                        mLatestWalletAdapter.addWallet(archivedWalletList.get(0));
                        archivedWalletList.remove(0);
                    }
                }else {
                    pos++;
                    while(pos<mLatestWalletList.size()){
                        archivedWalletList.add(mLatestWalletList.get(pos));
                        mLatestWalletAdapter.removeWallet(mLatestWalletList.get(pos));
                        mLatestWalletList.remove(pos);
                    }
                }
                System.out.println("Map Sizes after: " +mLatestWalletAdapter.getMapSize()+" vs " + mLatestWalletList.size());
                mLatestWalletAdapter.notifyDataSetChanged();
                ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
                archiveClosed = !archiveClosed;
                mLatestWalletListView.setArchiveClosed(archiveClosed);
            }
        });

        mLatestWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                if(a.getList().get(i).getName() != "xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL" && a.getList().get(i).getName() != "SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd") {//TODO ALERT
                    showWalletFragment(a.getList().get(i).getName(), a.getList().get(i).getAmount());
                }else if(a.getList().get(i).getName() == "SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd"){
                    int pos = a.getPosition(a.getList().get(i));
                    //a.switchCloseAfterArchive(pos);
                    System.out.println("Map Sizes before: " +a.getMapSize()+" vs " + mLatestWalletList.size());
                    if(archiveClosed){
                        while(!archivedWalletList.isEmpty()){
                            mLatestWalletList.add(archivedWalletList.get(0));
                            mLatestWalletAdapter.addWallet(archivedWalletList.get(0));
                            archivedWalletList.remove(0);
                        }
                    }else {
                        pos++;
                        while(pos<mLatestWalletList.size()){
                            archivedWalletList.add(mLatestWalletList.get(pos));
                            mLatestWalletAdapter.removeWallet(mLatestWalletList.get(pos));
                            mLatestWalletList.remove(pos);
                        }
                    }
                    System.out.println("Map Sizes after: " +a.getMapSize()+" vs " + mLatestWalletList.size());
                    mLatestWalletAdapter.notifyDataSetChanged();
                    ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
                    archiveClosed = !archiveClosed;
                    mLatestWalletListView.setArchiveClosed(archiveClosed);
                }
            }
        });
        /*RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams((int)getActivity().getResources().getDimension(R.dimen.spinner_width_password),(int)getActivity().getResources().getDimension(R.dimen.drop_down_height));
        rLP.addRule(RelativeLayout.ABOVE, R.id.bottom_switch);
        switchable.setLayoutParams(rLP);*/
        /*if(switchable.getVisibility()==View.VISIBLE){
            System.out.println("I Should be visible, Height: "+switchable.getHeight() +", Width: "+switchable.getWidth());
        }*/
        return view;
    }

    private void switchBarInfo(boolean isBitcoin){
        RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(switchContainer.getWidth(), switchContainer.getHeight()/2);
        if(isBitcoin) {
            rLP.addRule(RelativeLayout.ABOVE, R.id.bottom_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mBitCoinBalanceButton.getText());
            moverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            moverType.setImageResource(R.drawable.ico_btc_white);
            double conv = 8.7544;
            for(Wallet trans: mLatestWalletList){
                if(trans.getName() != "xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL" && trans.getName() != "SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd") {//TODO ALERT
                    try {
                        double item = Double.parseDouble(trans.getAmount().substring(1)) * conv;
                        String amount = String.format("B%.3f", item);
                        trans.setAmount(amount);
                    } catch (Exception e) {
                        trans.setAmount("0");
                        e.printStackTrace();
                    }
                }
            }
            mLatestWalletAdapter.notifyDataSetChanged();
        }else{
            rLP.addRule(RelativeLayout.BELOW, R.id.top_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mDollarBalanceButton.getText());
            moverCoin.setImageResource(R.drawable.ico_coin_usd_white);
            moverType.setImageResource(R.drawable.ico_usd_white);
            double conv = 0.1145;
            for(Wallet wallet: mLatestWalletList){
                if(wallet.isRealWallet()) {//TODO ALERT
                    try {
                        double item = wallet.getBalance(); //Double.parseDouble(wallet.getAmount().substring(1)) * conv;
                        String amount = String.format("$%.3f", item);
                        wallet.setAmount(amount);
                    } catch (Exception e) {
                        wallet.setAmount("0");
                        e.printStackTrace();
                    }
                }
            }
        }
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
        float moveX = heightDiff*(seekBar.getMax()-progress)/seekBar.getMax();
        RelativeLayout.LayoutParams absParams =
                (RelativeLayout.LayoutParams)mButtonMover.getLayoutParams();

        absParams.leftMargin = 0;
        absParams.topMargin = (int) moveX;

        mButtonMover.setLayoutParams(absParams);
    }

    public void onStartTrackingTouch(SeekBar seekBar) { }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar.getProgress() > seekBar.getMax()/2) {
            seekBar.setProgress(seekBar.getMax());
            setSwitchSelection(BTC);
        } else {
            seekBar.setProgress(0);
            setSwitchSelection(CURRENCY);
        }
        onProgressChanged(seekBar, seekBar.getProgress(), true);
    }

    private void setSwitchSelection(int selection) {

        switch(selection) {
            case BTC:
                //onProgressChanged(mSeekBar, 100, true);
                mButtonMover.setText(mBitCoinBalanceButton.getText());
                mButtonMover.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ico_coin_btc_white), null,
                        getResources().getDrawable(R.drawable.ico_btc_white), null);
               break;
            case CURRENCY:
                //onProgressChanged(mSeekBar, 0, true);
                mButtonMover.setText(mDollarBalanceButton.getText());
                String left = "ico_coin_"+mCurrencyResourceString+"_white";
                String right = "ico_"+mCurrencyResourceString+"_white";
                int leftID = getResources().getIdentifier(left,"drawable",getActivity().getPackageName());
                int rightID = getResources().getIdentifier(right,"drawable",getActivity().getPackageName());
                Drawable leftD = getResources().getDrawable(leftID);
                Drawable rightD = getResources().getDrawable(rightID);
                mButtonMover.setCompoundDrawablesWithIntrinsicBounds(leftD, null, rightD, null);
                break;
            default:
                break;
        }
    }

    public void addNewWallet(String name){
        if(AirbitzApplication.isLoggedIn()) {
            mAddWalletTask = new AddWalletTask(name, AirbitzApplication.getUsername(), AirbitzApplication.getPassword());
            mAddWalletTask.execute((Void) null);
        } else {
            Log.d("WalletsFragment", "not logged in");
        }
    }

    /**
     * Represents an asynchronous creation of a new wallet
     */
    public class AddWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final String mWalletName, mUsername, mPassword;

        AddWalletTask(String walletName, String username, String password) {
            mWalletName = walletName;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
//            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mAPI.createWallet(mWalletName, mUsername, mPassword, SignUpActivity.DOLLAR_CURRENCY_NUMBER);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddWalletTask = null;
//            showProgress(false);
            if (!success) {
                Log.d("WalletsFragment", "AddWalletTask failed");
            } else {
                mLatestWalletList.clear();
                mLatestWalletList.addAll(mAPI.loadWallets());
                mLatestWalletAdapter.swapWallets();
                mLatestWalletAdapter.notifyDataSetChanged();
                ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());

            }
        }

        @Override
        protected void onCancelled() {
            mAddWalletTask = null;
        }
    }


    public void showDialogWalletType(){

        newWalletLayout.setVisibility(View.VISIBLE);
        nameEditText.requestFocus();

        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
        alertDialog.show();*/
    }

    private void goDoneCancel(int choice){
        if(choice == 0){
            if(!nameEditText.getText().toString().isEmpty()) {
                if (!newWalletSwitch.isChecked()) {
                    if (mOnBitcoinMode) {
                        addNewWallet(nameEditText.getText().toString());
                    } else {
                        addNewWallet(nameEditText.getText().toString());
                    }
                } else {
                    ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment());
                }
                nameEditText.setText("");
                newWalletSpinner.setSelection(1);
                onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                nameEditText.setHint("Name your wallet");
                nameEditText.setHintTextColor(getResources().getColor(R.color.text_hint));
                newWalletSwitch.setChecked(false);
                newWalletLayout.setVisibility(View.GONE);
                mBitCoinBalanceButton.setEnabled(true);
                mDollarBalanceButton.setEnabled(true);
                mButtonMover.setEnabled(true);
                mBitCoinBalanceButton.setClickable(true);
                mDollarBalanceButton.setClickable(true);
                mButtonMover.setClickable(true);
                mBitCoinBalanceButton.setFocusable(true);
                mDollarBalanceButton.setFocusable(true);
                mButtonMover.setFocusable(true);
                if(nameEditText.hasFocus()){
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }else{
                nameEditText.setHint("Wallet Name Required");
                nameEditText.setHintTextColor(getResources().getColor(R.color.red));
            }
        }else if(choice == 1){
            nameEditText.setText("");
            newWalletSpinner.setSelection(1);
            onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
            offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
            nameEditText.setHint("Name your wallet");
            newWalletSwitch.setChecked(false);
            newWalletLayout.setVisibility(View.GONE);
            mBitCoinBalanceButton.setEnabled(true);
            mDollarBalanceButton.setEnabled(true);
            mButtonMover.setEnabled(true);
            mBitCoinBalanceButton.setClickable(true);
            mDollarBalanceButton.setClickable(true);
            mButtonMover.setClickable(true);
            mBitCoinBalanceButton.setFocusable(true);
            mDollarBalanceButton.setFocusable(true);
            mButtonMover.setFocusable(true);
            if(nameEditText.hasFocus()){
                final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mLatestWalletListView.setHeaderVisibilityOnReturn();
    }


}