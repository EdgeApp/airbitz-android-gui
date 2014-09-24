package com.airbitz.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ExportFragment extends Fragment{

    private HighlightOnPressButton mCSVButton;
    private HighlightOnPressButton mQuickenButton;
    private HighlightOnPressButton mQuickBooksButton;
    private HighlightOnPressButton mPdfbutton;
    private HighlightOnPressButton mWalletPrivateSeed;


    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mBackButton;
    private TextView mTitleTextView;

    private Bundle bundle;

    View mView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_export, container, false);
        } else {

            return mView;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mCSVButton = (HighlightOnPressButton) mView.findViewById(R.id.button_csv);
        mQuickenButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quicken);
        mQuickBooksButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quickbooks);
        mPdfbutton = (HighlightOnPressButton) mView.findViewById(R.id.button_pdf);
        mWalletPrivateSeed = (HighlightOnPressButton) mView.findViewById(R.id.button_wallet);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_export_back_button);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_export_help_button);
        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        bundle = new Bundle();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.EXPORT_WALLET), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = getArguments();
                bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, ExportSavingOptionFragment.ExportTypes.CSV.ordinal());
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });
        mQuickenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = getArguments();
                bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, ExportSavingOptionFragment.ExportTypes.Quicken.ordinal());
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });
        mQuickBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = getArguments();
                bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, ExportSavingOptionFragment.ExportTypes.Quickbooks.ordinal());
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });
        mPdfbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = getArguments();
                bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, ExportSavingOptionFragment.ExportTypes.PDF.ordinal());
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });
        mWalletPrivateSeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = getArguments();
                bundle.putInt(ExportSavingOptionFragment.EXPORT_TYPE, ExportSavingOptionFragment.ExportTypes.PrivateSeed.ordinal());
                frag.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        return mView;
    }
}
