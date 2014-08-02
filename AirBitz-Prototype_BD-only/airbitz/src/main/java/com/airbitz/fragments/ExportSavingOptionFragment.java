package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment{

    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressButton mFromButton;
    private HighlightOnPressButton mToButton;

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
    private Calendar c;

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

        c = Calendar.getInstance();

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_exportsaving_button_help);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_exportsaving_account_spinner);
        mFromButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_from_spinner);
        mToButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_exportsaving_to_spinner);

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
                showTimeButton(4);//TODO
            }
        });
        mThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(5);
                String AMPM = "";
                if(c.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(c.get(Calendar.MINUTE)<10){
                    tempMin = "0"+c.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+c.get(Calendar.MINUTE);
                }
                mFromButton.setText((c.get(Calendar.MONTH)+1)+"/1/"+c.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
            }
        });
        mToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(3);

                String AMPM = "";
                if(c.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(c.get(Calendar.MINUTE)<10){
                    tempMin = "0"+c.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+c.get(Calendar.MINUTE);
                }
                mFromButton.setText((c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR)+" 12:00 am");
                mToButton.setText((c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
            }
        });
        mYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(0);//TODO
            }
        });
        mLastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeButton(2);
                String AMPM = "";
                if(c.get(Calendar.AM_PM)==1){
                    AMPM = "pm";
                }else{
                    AMPM = "am";
                }
                String tempMin = "";
                if(c.get(Calendar.MINUTE)<10){
                    tempMin = "0"+c.get(Calendar.MINUTE);
                }else{
                    tempMin = ""+c.get(Calendar.MINUTE);
                }
                String tempYear = "";
                String tempMonth = "";
                if(c.get(Calendar.MONTH)==0){
                    tempYear = ""+(c.get(Calendar.YEAR)-1);
                    tempMonth = "12";
                }else{
                    tempYear = ""+(c.get(Calendar.YEAR));
                    tempMonth = ""+(c.get(Calendar.MONTH));
                }
                int year = c.get(Calendar.YEAR);
                String tempDay = "";
                if(c.get(Calendar.MONTH)==2) {
                    if (year % 4 != 0) {
                        tempDay = "28";
                    }else if(year % 100 != 0){
                        tempDay = "29";
                    }else if(year % 400 != 0){
                        tempDay = "28";
                    }else{
                        tempDay = "29";
                    }
                }else if(c.get(Calendar.MONTH)==1 ||c.get(Calendar.MONTH)==3||c.get(Calendar.MONTH)==5||c.get(Calendar.MONTH)==7||c.get(Calendar.MONTH)==8||c.get(Calendar.MONTH)==10||c.get(Calendar.MONTH)==12){
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
                showTimeButton(1);//TODO
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
                                showTimeButton(7);
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
        if(c.get(Calendar.AM_PM)==1){
            AMPM = "pm";
        }else{
            AMPM = "am";
        }
        String tempMin = "";
        if(c.get(Calendar.MINUTE)<10){
            tempMin = "0"+c.get(Calendar.MINUTE);
        }else{
            tempMin = ""+c.get(Calendar.MINUTE);
        }
        mToButton.setText((c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
        mFromButton.setText((c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR)+" "+c.get(Calendar.HOUR)+":"+tempMin+" "+AMPM);
    }
}
