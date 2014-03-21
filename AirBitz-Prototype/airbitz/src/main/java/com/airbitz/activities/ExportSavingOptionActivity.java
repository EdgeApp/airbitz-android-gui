package com.airbitz.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.ExportAdapter;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionActivity extends Activity implements GestureDetector.OnGestureListener{

    private EditText mAccountEdittext;
    private EditText mFromEdittext;
    private EditText mToEdittext;

    private TextView mTitleTextView;
    private TextView mAccountTexView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

    private Button mPrintButton;
    private Button mEmailButton;
    private Button mGoogleDriveButton;
    private Button mDropBoxButton;

    private ScrollView mScrollView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private ExportAdapter mExportAdapter;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_saving_options);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_root);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mAccountEdittext = (EditText) findViewById(R.id.edittext_account);
        mFromEdittext = (EditText) findViewById(R.id.edittext_from);
        mToEdittext = (EditText) findViewById(R.id.edittext_to);

        mAccountTexView = (TextView) findViewById(R.id.textview_account);
        mFromTextView = (TextView) findViewById(R.id.textview_from);
        mToTextView = (TextView) findViewById(R.id.textview_to);

        mAccountEdittext.setTypeface(LandingActivity.montserratBoldTypeFace);
        mFromEdittext.setTypeface(LandingActivity.montserratBoldTypeFace);
        mToEdittext.setTypeface(LandingActivity.montserratBoldTypeFace);

        mAccountTexView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mToTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mFromEdittext.setKeyListener(null);
        mToEdittext.setKeyListener(null);

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(ExportSavingOptionActivity.this, "Info", "Business directory info");
            }
        });

    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

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
                    finish();
                    return true;
                }
            }

        }

        return false;
    }
}
