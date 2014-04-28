package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.utils.Common;

/**
 * Created on 2/12/14.
 */
public class SettingActivity extends Activity implements GestureDetector.OnGestureListener{

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

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mDisplayButton = (Button) findViewById(R.id.button_display);
        mSecurityButton = (Button) findViewById(R.id.button_security);
        mLanguageButton = (Button) findViewById(R.id.button_language);
        mExchangeRateButton = (Button) findViewById(R.id.button_exchange);
        mCategoriesButton = (Button) findViewById(R.id.button_categories);

        mLanguageTextView = (TextView) findViewById(R.id.language_chosen);
        mExchangeTextView = (TextView) findViewById(R.id.exchange_rate_chosen);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);

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
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(SettingActivity.this, "Info", "Business directory info");
            }
        });

        mDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, DisplayActivity.class);
                startActivity(intent);
            }
        });

        mSecurityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, SecurityActivity.class);
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

    @Override
    protected void onResume() {
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }
}
