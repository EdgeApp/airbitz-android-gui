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
import com.airbitz.activities.LandingActivity;
import com.airbitz.adapters.ExportAdapter;
import com.airbitz.utils.Common;

import java.util.Calendar;

/**
 * Created on 2/22/14.
 */
public class ExportFragment extends Fragment implements GestureDetector.OnGestureListener{
    private EditText mFromDateEdittext;
    private EditText mToDateEdittext;

    private Button mAccountButton;
    private Button mCSVButton;
    private Button mQuickenButton;
    private Button mQuickBooksButton;
    private Button mPdfbutton;
    private Button mWalletbutton;


    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private TextView mAccountTextView;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mTitleTextView;

    private Button mThisWeekButton;
    private Button mThisMonthButton;
    private Button mThisYearButton;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

    private ScrollView mScrollView;

    private GestureDetector mGestureDetector;
    private ExportAdapter mExportAdapter;

    private Intent mIntent;

    private Calendar calendar;
    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID_FROM = 10;
    static final int DATE_DIALOG_ID_TO = 11;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mGestureDetector = new GestureDetector(this);
        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_root);


        calendar = Calendar.getInstance();
//        mYear = calendar.get(Calendar.YEAR);
//        mMonth = calendar.get(Calendar.MONTH);
//        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mNavigationLayout = (RelativeLayout) view.findViewById(R.id.navigation_layout);

        mCSVButton = (Button) view.findViewById(R.id.button_csv);
        mQuickenButton = (Button) view.findViewById(R.id.button_quicken);
        mQuickBooksButton = (Button) view.findViewById(R.id.button_quickbooks);
        mPdfbutton = (Button) view.findViewById(R.id.button_pdf);
        mWalletbutton = (Button) view.findViewById(R.id.button_wallet);

        mAccountButton = (Button) view.findViewById(R.id.button_acount);
        mFromDateEdittext = (EditText) view.findViewById(R.id.edittext_from);
        mToDateEdittext = (EditText) view.findViewById(R.id.edittext_to);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mAccountTextView = (TextView) view.findViewById(R.id.textview_account);
        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mThisWeekButton = (Button) view.findViewById(R.id.button_this_week);
        mThisMonthButton = (Button) view.findViewById(R.id.button_this_month);
        mThisYearButton = (Button) view.findViewById(R.id.button_this_year);

        mAccountButton.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromDateEdittext.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mToDateEdittext.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mAccountTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mToTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mThisWeekButton.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mThisMonthButton.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mThisYearButton.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIntent = new Intent(ExportActivity.this, WalletActivity.class);
//                mIntent.putExtra(RequestActivity.CLASSNAME, "ImportActivity");
//                startActivity(mIntent);
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

        mCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIntent = new Intent(ExportFragment.this, ExportSavingOptionActivity.class);
//                startActivity(mIntent);
            }
        });
        mQuickenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIntent = new Intent(ExportFragment.this, ExportSavingOptionActivity.class);
//                startActivity(mIntent);
            }
        });
        mQuickBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIntent = new Intent(ExportFragment.this, ExportSavingOptionActivity.class);
//                startActivity(mIntent);
            }
        });
        mPdfbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mWalletbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mThisWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mThisMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mThisYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mFromDateEdittext.setKeyListener(null);
        mFromDateEdittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int[] splittedFromDate = Common.dateTextSplitter(mFromDateEdittext.getText().toString());
                showDialog(splittedFromDate[2], splittedFromDate[1]-1, splittedFromDate[0]);
                return true;
            }
        });


        mToDateEdittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int[] splittedToDate = Common.dateTextSplitter(mFromDateEdittext.getText().toString());
                showDialog(splittedToDate[2], splittedToDate[1]-1, splittedToDate[0]);
                return true;
            }
        });

        return view;
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
////        super.onWindowFocusChanged(hasFocus);
//
//        DisplayMetrics metrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//        int widthPixels = metrics.widthPixels;
//        int heightPixels = metrics.heightPixels;
//
//
//        if(widthPixels <= 480){
//            int accountWidth = mAccountTextView.getWidth();
//
//            float scale = ExportFragment.this.getResources().getDisplayMetrics().density;
//            int pixels = (int) (200 * scale + 0.5f);
//            mAccountTextView.getLayoutParams().width = pixels;
//            pixels = (int) (200 * scale + 0.5f);
//            mFromTextView.getLayoutParams().width = pixels;
//            pixels = (int) (200 * scale + 0.5f);
//            mToTextView.getLayoutParams().width = pixels;
//
//            mAccountButton.setTextSize((int) (11 * scale + 0.5f));
//            mFromDateEdittext.setTextSize((int) (9 * scale + 0.5f));
//            mToDateEdittext.setTextSize((int) (9 * scale + 0.5f));
//        }
//    }

    protected void showDialog(int a, int b, int c) {
        new DatePickerDialog(getActivity(), datePickerFromListener, a, b, c).show();
    }


    private DatePickerDialog.OnDateSetListener datePickerToListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            mMonth = selectedMonth+1;;
            mDay = selectedDay;
            mYear = selectedYear;

            ExportFragment.this.mToDateEdittext.setText(mDay+"/"+mMonth+"/"+mYear);
        }
    };

    private DatePickerDialog.OnDateSetListener datePickerFromListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            mMonth = selectedMonth+1;;
            mDay = selectedDay;
            mYear = selectedYear;

            ExportFragment.this.mFromDateEdittext.setText(mDay+"/"+mMonth+"/"+mYear);
        }
    };


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start != null & finish != null){

            float yDistance = Math.abs(finish.getY() - start.getY());

            if((finish.getRawX()>start.getRawX()) && (yDistance < 15)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 50){
                    getActivity().onBackPressed();
                    return true;
                }
            }

        }

        return false;
    }
}
