package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.AccountTransaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment  implements SeekBar.OnSeekBarChangeListener {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private ClearableEditText mSearchField;

    private ImageButton mExportButton;
    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private RelativeLayout mParentLayout;

    private ScrollView mScrollView;

    private Button mButtonBitcoinBalance;
    private Button mButtonDollarBalance;
    private Button mButtonMover;
    //private SeekBar mSeekBar;

    private Button mWalletNameButton;

    private ListView mListTransaction;

    private TransactionAdapter mTransactionAdapter;

    private GestureDetector mGestureDetector;

    private Intent mIntent;

    private List<AccountTransaction> mAccountTransactions;

    private String mWalletName;
    private Wallet mWallet;
    private AirbitzAPI mAPI;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mWalletName = getArguments().getString(Wallet.WALLET_NAME);
        mAPI = AirbitzAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mAccountTransactions = mAPI.getTransactions(mWalletName);
        mWallet = null;
        for(Wallet w: mAPI.getWallets()) {
            if(w.getName().contains(mWalletName))
                mWallet = w;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mTransactionAdapter = new TransactionAdapter(getActivity(), mAccountTransactions);

        mSearchField = (ClearableEditText) view.findViewById(R.id.edittext_search);

        mSendButton = (ResizableImageView) view.findViewById(R.id.button_send);
        mRequestButton = (ResizableImageView) view.findViewById(R.id.button_request);
        mWalletNameButton = (Button) view.findViewById(R.id.button_balance);

        mExportButton = (ImageButton) view.findViewById(R.id.button_export);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);
       // mSeekBar = (SeekBar) view.findViewById(R.id.seekbar_slider);
        //mSeekBar.setOnSeekBarChangeListener(this);

        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mButtonBitcoinBalance = (Button) view.findViewById(R.id.back_button_top);
        mButtonDollarBalance = (Button) view.findViewById(R.id.back_button_bottom);
        mListTransaction = (ListView) view.findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mAccountTransactions.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mWalletNameButton.setText(mWalletName);
        mButtonDollarBalance.setText(getWalletBalance());
        mWalletNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIntent = new Intent(getActivity(), WalletActivity.class);
//                mIntent.putExtra(RequestActivity.CLASSNAME, "TransactionActivity");
//                startActivity(mIntent);
                // TODO this should show a Wallet name picker?
            }
        });

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ExportFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment);
            }
        });
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal());
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
//                bundle.putString(WALLET_NAME, name);
//                bundle.putString(WALLET_AMOUNT, amount);
                Fragment fragment = new TransactionDetailFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Wallet info");
            }
        });

        return view;
    }

    // AirbitzAPI calls here
    private String getWalletBalance() {
        return mWallet.getAmount();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        float heightDiff = mButtonBitcoinBalance.getHeight();
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
                mButtonMover.setText(mButtonBitcoinBalance.getText());
                mButtonMover.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ico_coin_btc_white), null,
                        getResources().getDrawable(R.drawable.ico_btc_white), null);
                break;
            case CURRENCY:
                //onProgressChanged(mSeekBar, 0, true);
                mButtonMover.setText(mButtonDollarBalance.getText());
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
}
