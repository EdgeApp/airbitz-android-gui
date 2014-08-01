package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment{

    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressSpinner mFromSpinner;
    private HighlightOnPressSpinner mToSpinner;

    private TextView mTitleTextView;
    private TextView mAccountTextView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private Button mThisWeek;
    private Button mThisMonth;
    private Button mThisYear;

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

    private List<Button> mTimeButtons;
    private List<Wallet> mWalletList;
    private List<String> mWalletNameList;
    private CoreAPI mCoreApi;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        mCoreApi = CoreAPI.getApi();
        mWalletList = new ArrayList<Wallet>();
        mWalletNameList = new ArrayList<String>();
        for(Wallet w: mCoreApi.loadWallets()){
            if(!w.isArchiveHeader() && !w.isHeader() &&!w.isArchived()) {
                mWalletList.add(w);
                mWalletNameList.add(w.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_export_saving_options, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_help);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_account_spinner);
        mFromSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

        mAccountTextView = (TextView) mView.findViewById(R.id.textview_account);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);

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

        mThisMonth = (Button) mView.findViewById(R.id.button_this_month);
        mThisWeek = (Button) mView.findViewById(R.id.button_this_week);
        mThisYear = (Button) mView.findViewById(R.id.button_this_year);

        mTimeButtons = new ArrayList<Button>();
        mTimeButtons.add(mThisWeek);
        mTimeButtons.add(mThisMonth);
        mTimeButtons.add(mThisYear);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),R.layout.item_request_wallet_spinner,mWalletNameList);
        dataAdapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletSpinner.setAdapter(dataAdapter);
        mWalletSpinner.setSelection(0);

        showExportButtons();

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

        mThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(0);//TODO
            }
        });
        mThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(1);//todo
            }
        });
        mThisYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(2);
            }
        });


        return mView;
    }

    private void showExportButtons(){
        String source = bundle.getString("button_clicked");
        if(source.equals("CSV")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("Quicken")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("Quickbooks")){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(source.equals("PDF")){

        }else if(source.equals("Wallet")){
            mGoogleDriveButton.setVisibility(View.GONE);
            mGoogleDriveImage.setVisibility(View.GONE);
            mDropBoxButton.setVisibility(View.GONE);
            mDropBoxImage.setVisibility(View.GONE);
        }
    }

    private void showTimeButton(int pos){
        for(int i = 0;i < mTimeButtons.size();i++){
            if(i==pos){
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.btn_cancel));
            }else{
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.emboss_down));
            }
        }
    }
}
