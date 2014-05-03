package com.airbitz.fragments;

import android.content.Intent;
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
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.LandingActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.models.AccountTransaction;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment {

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
    private Button mWalletNameButton;

    private ListView mListTransaction;

    private TransactionAdapter mTransactionAdapter;

    private GestureDetector mGestureDetector;

    private Intent mIntent;

    private List<AccountTransaction> mAccountTransaction;

    private String mWalletName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mWalletName = getArguments().getString(WalletsFragment.WALLET_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mAccountTransaction = getTransactions("nothing");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mTransactionAdapter = new TransactionAdapter(getActivity(), mAccountTransaction);

        mSearchField = (ClearableEditText) view.findViewById(R.id.edittext_search);

        mSendButton = (ResizableImageView) view.findViewById(R.id.button_send);
        mRequestButton = (ResizableImageView) view.findViewById(R.id.button_request);
        mWalletNameButton = (Button) view.findViewById(R.id.button_balance);

        mExportButton = (ImageButton) view.findViewById(R.id.button_export);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mButtonBitcoinBalance = (Button) view.findViewById(R.id.button_bitcoinbalance);
        mButtonDollarBalance = (Button) view.findViewById(R.id.button_dollarbalance);
        mListTransaction = (ListView) view.findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mAccountTransaction.size(), getActivity());

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mWalletNameButton.setText(mWalletName);
        mButtonDollarBalance.setText(getWalletBalance(mWalletName));
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
//                mIntent = new Intent(TransactionFragment.this, ExportActivity.class);
//                startActivity(mIntent);
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
//                Common.showHelpInfo(TransactionFragment.this, "Info", "Business directory info");
            }
        });

        return view;
    }

    // AirbitzAPI calls here
    private String getWalletBalance(String name) {
        //TODO get actual wallet balance
        return "120.015";
    }

    private List<AccountTransaction> getTransactions(String name) {
        //TODO replace with API call
        List<AccountTransaction> list = new ArrayList<AccountTransaction>();
        list.add(new AccountTransaction("Matt Kemp","DEC 10","B25.000", "-B5.000"));
        list.add(new AccountTransaction("John Madden","DEC 15","B30.000", "-B65.000"));
        list.add(new AccountTransaction("kelly@gmail.com", "NOV 1", "B95.000", "-B95.000"));

        return list;
    }
}
