package com.airbitz.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.ExportAdapter;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import org.apache.http.HeaderIterator;

import java.util.Calendar;

/**
 * Created on 2/22/14.
 */
public class ExportFragment extends Fragment{

    private HighlightOnPressButton mCSVButton;
    private HighlightOnPressButton mQuickenButton;
    private HighlightOnPressButton mQuickBooksButton;
    private HighlightOnPressButton mPdfbutton;
    private HighlightOnPressButton mWalletbutton;


    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mBackButton;
    private TextView mTitleTextView;

    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_export, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mCSVButton = (HighlightOnPressButton) mView.findViewById(R.id.button_csv);
        mQuickenButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quicken);
        mQuickBooksButton = (HighlightOnPressButton) mView.findViewById(R.id.button_quickbooks);
        mPdfbutton = (HighlightOnPressButton) mView.findViewById(R.id.button_pdf);
        mWalletbutton = (HighlightOnPressButton) mView.findViewById(R.id.button_wallet);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_export_back_button);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_export_help_button);
        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);
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
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        mCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = new Bundle();
                bundle.putString("button_clicked","CSV");
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        });
        mQuickenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = new Bundle();
                bundle.putString("button_clicked","Quicken");
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        });
        mQuickBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = new Bundle();
                bundle.putString("button_clicked","Quickbooks");
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        });
        mPdfbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = new Bundle();
                bundle.putString("button_clicked","PDF");
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        });
        mWalletbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new ExportSavingOptionFragment();
                bundle = new Bundle();
                bundle.putString("button_clicked","Wallet");
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(frag);
            }
        });

        return mView;
    }
}
