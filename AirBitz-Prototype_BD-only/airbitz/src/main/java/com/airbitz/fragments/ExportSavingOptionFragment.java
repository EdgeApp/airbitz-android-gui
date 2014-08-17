package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public enum ExportTypes {PrivateSeed, CSV, Quicken, Quickbooks, PDF }
    public static final String EXPORT_TYPE = "com.airbitz.fragments.exportsavingoption.export_type";

    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressButton mFromButton;
    private HighlightOnPressButton mToButton;

    private RelativeLayout mDatesLayout;
    private LinearLayout mLastPeriodLayout;
    private LinearLayout mThisPeriodLayout;

    private TextView mTitleTextView;
    private TextView mAccountTextView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private Button mThisWeek;
    private Button mLastWeek;
    private Button mThisMonth;
    private Button mLastMonth;
    private Button mToday;
    private Button mYesterday;

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
    private Calendar today;

    private String mPrivateSeed; // for private seed type
    private String mFilepath; // for filetypes
    private int mExportType;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        mExportType = bundle.getInt(EXPORT_TYPE);

        mCoreApi = CoreAPI.getApi();
        mWalletList = mCoreApi.getCoreWallets();
        mWalletNameList = new ArrayList<String>();
        for(Wallet w: mWalletList){
            if(!w.isArchived())
                mWalletNameList.add(w.getName());
        }
        setupData(mExportType, mWalletList.get(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_export_saving_options, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        today = Calendar.getInstance();

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_help);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_account_spinner);
        mFromButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

        mAccountTextView = (TextView) mView.findViewById(R.id.textview_account);
        mDatesLayout = (RelativeLayout) mView.findViewById(R.id.fragment_export_date_entries);
        mLastPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_last_period);
        mThisPeriodLayout = (LinearLayout) mView.findViewById(R.id.layout_export_this_period);
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
        mToday = (Button) mView.findViewById(R.id.button_today);
        mYesterday = (Button) mView.findViewById(R.id.button_yesterday);
        mLastMonth = (Button) mView.findViewById(R.id.button_last_month);
        mLastWeek = (Button) mView.findViewById(R.id.button_last_week);

        mTimeButtons = new ArrayList<Button>();
        mTimeButtons.add(mYesterday);
        mTimeButtons.add(mLastWeek);
        mTimeButtons.add(mLastMonth);
        mTimeButtons.add(mToday);
        mTimeButtons.add(mThisWeek);
        mTimeButtons.add(mThisMonth);

        goSetFromToText();

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
                Wallet w = mWalletList.get(mWalletSpinner.getSelectedItemPosition());
                String data;
                if(mExportType == ExportTypes.PrivateSeed.ordinal())
                    data = mPrivateSeed;
                else
                    data = mFilepath;
                exportWithEmail(w, data);
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
                ((NavigationActivity)getActivity()).pushFragment(new HelpDialog(HelpDialog.EXPORT_WALLET_OPTIONS), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(4);
                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                Calendar firstDay = Calendar.getInstance();
                firstDay.set(Calendar.DAY_OF_WEEK, 1);

                mFromButton.setText((firstDay.get(Calendar.MONTH)+1)+"/"+ firstDay.get(Calendar.DAY_OF_MONTH)+"/"+ firstDay.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" "+ today.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
            }
        });
        mThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(5);
                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                mFromButton.setText((today.get(Calendar.MONTH)+1)+"/1/"+ today.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" "+ today.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
            }
        });
        mToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(3);

                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                mFromButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" "+ today.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
            }
        });
        mYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(0);
                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DATE, -1);

                mFromButton.setText((yesterday.get(Calendar.MONTH)+1)+"/"+ yesterday.get(Calendar.DAY_OF_MONTH)+"/"+ yesterday.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((yesterday.get(Calendar.MONTH)+1)+"/"+ yesterday.get(Calendar.DAY_OF_MONTH)+"/"+ yesterday.get(Calendar.YEAR)+" 11:59 pm");
            }
        });
        mLastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(2);
                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                String tempYear = "";
                String tempMonth = "";
                if(today.get(Calendar.MONTH)==0){
                    tempYear = ""+(today.get(Calendar.YEAR)-1);
                    tempMonth = "12";
                }else{
                    tempYear = ""+(today.get(Calendar.YEAR));
                    tempMonth = ""+(today.get(Calendar.MONTH));
                }
                int year = today.get(Calendar.YEAR);
                String tempDay = "";
                if(today.get(Calendar.MONTH)==2) {
                    if (year % 4 != 0) {
                        tempDay = "28";
                    }else if(year % 100 != 0){
                        tempDay = "29";
                    }else if(year % 400 != 0){
                        tempDay = "28";
                    }else{
                        tempDay = "29";
                    }
                }else if(today.get(Calendar.MONTH)==1 || today.get(Calendar.MONTH)==3|| today.get(Calendar.MONTH)==5|| today.get(Calendar.MONTH)==7|| today.get(Calendar.MONTH)==8|| today.get(Calendar.MONTH)==10|| today.get(Calendar.MONTH)==12){
                    tempDay = "31";
                }else{
                    tempDay = "30";
                }
                mFromButton.setText(tempMonth+"/1/"+tempYear+" 12:00 am");
                mToButton.setText(tempMonth+"/"+tempDay+"/"+tempYear+" 11:59 pm");
            }
        });
        mLastWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HighlightTimeButton(1);
                String AMPM = "";
                if(today.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(today.get(Calendar.MINUTE)<10){
                    tempMin = "0"+ today.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+ today.get(Calendar.MINUTE);
                }
                Calendar lastWeek = Calendar.getInstance();
                lastWeek.add(Calendar.WEEK_OF_YEAR, -1);
                lastWeek.set(Calendar.DAY_OF_WEEK, 1);

                mFromButton.setText((lastWeek.get(Calendar.MONTH)+1)+"/"+ lastWeek.get(Calendar.DAY_OF_MONTH)+"/"+ lastWeek.get(Calendar.YEAR)+" 12:00 am");

                lastWeek.set(Calendar.DAY_OF_WEEK, 7);
                mToButton.setText((lastWeek.get(Calendar.MONTH)+1)+"/"+ lastWeek.get(Calendar.DAY_OF_MONTH)+"/"+ lastWeek.get(Calendar.YEAR)+" 11:59 pm");
            }
        });

        mToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] dateTime = mToButton.getText().toString().split(" ");
                String[] date = dateTime[0].split("/");
                String[] time = dateTime[1].split(":");
                showSelectorDialog(mToButton, Integer.valueOf(date[0]),Integer.valueOf(date[1]),Integer.valueOf(date[2]),Integer.valueOf(time[0]),Integer.valueOf(time[1]),dateTime[2]);
            }
        });

        mFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] dateTime = mFromButton.getText().toString().split(" ");
                String[] date = dateTime[0].split("/");
                String[] time = dateTime[1].split(":");
                showSelectorDialog(mFromButton, Integer.valueOf(date[0]),Integer.valueOf(date[1]),Integer.valueOf(date[2]),Integer.valueOf(time[0]),Integer.valueOf(time[1]),dateTime[2]);
            }
        });

        setupUI(mExportType);

        return mView;
    }

    private void showExportButtons(){
        int type = bundle.getInt(EXPORT_TYPE);
        if(type == ExportTypes.CSV.ordinal()){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(type == ExportTypes.Quicken.ordinal()){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(type == ExportTypes.Quickbooks.ordinal()){
            mPrintButton.setVisibility(View.GONE);
            mPrintImage.setVisibility(View.GONE);
            mViewButton.setVisibility(View.GONE);
            mViewImage.setVisibility(View.GONE);
            mSDCardButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_top_archive));
            mDropBoxButton.setBackground(getResources().getDrawable(R.drawable.wallet_list_bottom));
            mSDCardButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
            mDropBoxButton.setPadding((int)getResources().getDimension(R.dimen.nine_mm),0,(int)getResources().getDimension(R.dimen.three_mm),0);
        }else if(type == ExportTypes.PDF.ordinal()){

        }else if(type == ExportTypes.PrivateSeed.ordinal()){
            mGoogleDriveButton.setVisibility(View.GONE);
            mGoogleDriveImage.setVisibility(View.GONE);
            mDropBoxButton.setVisibility(View.GONE);
            mDropBoxImage.setVisibility(View.GONE);
        }
    }

    private void HighlightTimeButton(int pos){
        for(int i = 0;i < mTimeButtons.size();i++){
            if(i==pos){
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.btn_cancel));
            }else{
                mTimeButtons.get(i).setBackground(getResources().getDrawable(R.drawable.emboss_down));
            }
        }
    }

    private void showSelectorDialog(final Button button, int indexMonth, int indexDay, int indexYear, int indexHour, int indexMinute, String AMPM) {

        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        final TimePicker timePicker = new TimePicker(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustomLight));
        timePicker.setIs24HourView(false);
        if(AMPM.equals("pm")&& indexHour!=12){
            indexHour += 12;
        }else if(AMPM.equals("am")&& indexHour==12){
            indexHour -= 12;
        }
        timePicker.setCurrentHour(indexHour);
        timePicker.setCurrentMinute(indexMinute);
        final DatePicker datePicker = new DatePicker(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustomLight));
        datePicker.setCalendarViewShown(false);
        datePicker.init(indexYear,indexMonth-1,indexDay,null);

        linearLayout.addView(datePicker);
        linearLayout.addView(timePicker);


        AlertDialog frag = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom))
                .setTitle("Pick a Date")
                .setView(linearLayout)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                HighlightTimeButton(7);
                                int time = 0;
                                String tempAMPM="";
                                if(timePicker.getCurrentHour()>12){
                                    time = timePicker.getCurrentHour()-12;
                                    tempAMPM="pm";
                                }else if(timePicker.getCurrentHour()==0){
                                    time = timePicker.getCurrentHour()+12;
                                    tempAMPM="am";
                                }else{
                                    time = timePicker.getCurrentHour();
                                    tempAMPM="am";
                                }
                                if(timePicker.getCurrentHour()==12){
                                    tempAMPM="pm";
                                }
                                String tempMin = "";
                                if(timePicker.getCurrentMinute()<10){
                                    tempMin = "0"+timePicker.getCurrentMinute();
                                }else{
                                    tempMin = ""+timePicker.getCurrentMinute();
                                }
                                button.setText((datePicker.getMonth()+1)+"/"+datePicker.getDayOfMonth()+"/"+datePicker.getYear()+" "+time+":"+tempMin+" "+tempAMPM);// TODO logic for switching if to<from
                            }
                        }
                )
                .setNegativeButton(R.string.string_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }
                )
                .create();
        frag.show();
    }

    public void goSetFromToText(){
        String AMPM = "";
        if(today.get(Calendar.AM_PM)==1){
            AMPM = "pm";
        }else{
            AMPM = "am";
        }
        String tempMin = "";
        if(today.get(Calendar.MINUTE)<10){
            tempMin = "0"+ today.get(Calendar.MINUTE);
        }else{
            tempMin = ""+ today.get(Calendar.MINUTE);
        }
        mToButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" "+ today.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
        mFromButton.setText((today.get(Calendar.MONTH)+1)+"/"+ today.get(Calendar.DAY_OF_MONTH)+"/"+ today.get(Calendar.YEAR)+" "+ today.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
    }

    private void setupData(int type, Wallet wallet) {
        if(type == ExportTypes.PrivateSeed.ordinal()) {
            mPrivateSeed = mCoreApi.getPrivateSeed(wallet);
        } else {
            mFilepath = getExportFilepath(wallet, type);
        }
    }

    private void setupUI(int type) {
        if(type == ExportTypes.PrivateSeed.ordinal()) {
            mDatesLayout.setVisibility(View.GONE);
            mLastPeriodLayout.setVisibility(View.GONE);
            mThisPeriodLayout.setVisibility(View.GONE);
        }
    }

    private String getExportFilepath(Wallet wallet, int type)
    {
        String output = null;
        // TODO: create the proper export in the proper from using self.wallet

        // for now just hard code
        if(type == ExportTypes.CSV.ordinal()) {
            output = "[CSV Data Here]";
        } else if(type == ExportTypes.Quicken.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
        } else if(type == ExportTypes.Quickbooks.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportQuicken" ofType:@"QIF"];
        } else if(type == ExportTypes.PDF.ordinal()) {
//                output = [[NSBundle mainBundle] pathForResource:@"WalletExportPDF" ofType:@"pdf"];
        }
        return output;
    }

    private String mimeTypeFor(int type)
    {
        String strMimeType;
        if(type == ExportTypes.CSV.ordinal()) {
            strMimeType = "text/plain";
        } else if(type == ExportTypes.Quicken.ordinal()) {
            strMimeType = "application/qif";
        } else if(type == ExportTypes.Quickbooks.ordinal()) {
            strMimeType = "application/qbooks";
        } else if(type == ExportTypes.PDF.ordinal()) {
            strMimeType = "application/pdf";
        } else if(type == ExportTypes.PrivateSeed.ordinal()) {
            strMimeType = "text/plain";
        } else {
            strMimeType = "???";
        }
        return strMimeType;
    }

    private void exportWithEmail(Wallet wallet, String data) {
        // Compose
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "AirBitz Bitcoin Wallet Transactions");

        StringBuilder sb = new StringBuilder();
        sb.append("Attached are the transactions for the AirBitz Bitcoin Wallet: "+wallet.getName()+"\n");
        if(mExportType == ExportTypes.PrivateSeed.ordinal()) {
            sb.append("\n\nPrivate Seed: " + mPrivateSeed);
        } else {
            // TODO attach file from mFilename
            String mimeType = mimeTypeFor(mExportType);
        }
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(intent);
    }

}
