package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment{

    private EditText mAccountEdittext;
    private EditText mFromEdittext;
    private EditText mToEdittext;

    private TextView mTitleTextView;
    private TextView mAccountTextView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private HighlightOnPressButton mPrintButton;
    private ImageView mPrintImage;
    private HighlightOnPressButton mSDCardButton;
    private ImageView mSDCardImage;
    private HighlightOnPressButton mEmailButton;
    private ImageView mEmailImage;
    private HighlightOnPressButton mGoogleDriveButton;
    private ImageView mGoogleDriveImage;
    private HighlightOnPressButton mDropBoxButton;
    private ImageView mDropBoxImage;
    private HighlightOnPressButton mViewButton;
    private ImageView mViewImage;

    private ScrollView mScrollView;

    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;

    private Bundle bundle;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_export_saving_options, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_help);

        mAccountEdittext = (EditText) mView.findViewById(R.id.edittext_account);
        mFromEdittext = (EditText) mView.findViewById(R.id.edittext_from);
        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mAccountTextView = (TextView) mView.findViewById(R.id.textview_account);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);

        mAccountEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mToEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mAccountTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mToTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mPrintButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_print);
        mPrintImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_print);
        mSDCardButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_sd_card);
        mSDCardImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_sd_card);
        mEmailButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_email);
        mEmailImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_email);
        mGoogleDriveButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_google_drive);
        mGoogleDriveImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_google_drive);
        mDropBoxButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_dropbox);
        mDropBoxImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_dropbox);
        mViewButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_button_view);
        mViewImage = (ImageView) mView.findViewById(R.id.fragment_exportsaving_image_view);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        showButtons();

        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        mSDCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mGoogleDriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mDropBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
            }
        });

        mViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO
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
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        return mView;
    }

    private void showButtons(){//TODO SD CARD?
        String source = bundle.getString("button_clicked");
        if(source.equals("CSV")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mEmailButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mEmailButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("Quicken")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mEmailButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mEmailButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("Quickbooks")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mEmailButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mEmailButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("PDF")){
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
        }else if(source.equals("Wallet")){
            mSDCardButton.setVisibility(View.GONE);
            mSDCardImage.setVisibility(View.GONE);
            mGoogleDriveButton.setVisibility(View.GONE);
            mGoogleDriveImage.setVisibility(View.GONE);
            mDropBoxButton.setVisibility(View.GONE);
            mDropBoxImage.setVisibility(View.GONE);
        }
    }
}
