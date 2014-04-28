package com.airbitz.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.DisplayActivity;
import com.airbitz.activities.LandingActivity;
import com.airbitz.activities.SecurityActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends Fragment {

    private Button mDisplayButton;
    private Button mSecurityButton;
    private Button mLanguageButton;
    private Button mExchangeRateButton;
    private Button mCategoriesButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mTitleTextView;
    private TextView mLanguageTextView;
    private TextView mExchangeTextView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);

        mDisplayButton = (Button) view.findViewById(R.id.button_display);
        mSecurityButton = (Button) view.findViewById(R.id.button_security);
        mLanguageButton = (Button) view.findViewById(R.id.button_language);
        mExchangeRateButton = (Button) view.findViewById(R.id.button_exchange);
        mCategoriesButton = (Button) view.findViewById(R.id.button_categories);

        mLanguageTextView = (TextView) view.findViewById(R.id.language_chosen);
        mExchangeTextView = (TextView) view.findViewById(R.id.exchange_rate_chosen);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mDisplayButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mSecurityButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mLanguageButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mExchangeRateButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mCategoriesButton.setTypeface(LandingActivity.latoBlackTypeFace);

        mLanguageTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.ITALIC);
        mExchangeTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.ITALIC);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        mDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DisplayActivity.class);
                startActivity(intent);
            }
        });

        mSecurityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SecurityActivity.class);
                startActivity(intent);
            }
        });

        mLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mExchangeRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO load state here
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO save state here
    }

}
