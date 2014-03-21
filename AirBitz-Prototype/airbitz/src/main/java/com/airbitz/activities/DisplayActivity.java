package com.airbitz.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
 * Created on 3/4/14.
 */
@TargetApi(17)
public class DisplayActivity extends Activity implements GestureDetector.OnGestureListener{
    private Button mDenominationButton;
    private Button mCurrencyButton;
    private Button mAdvancedFeaturesButton;

    private TextView mTitleTextView;
    private TextView mBitTextView;
    private TextView mDollarTextview;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private boolean mAdvancedFeaturesOn = true;

    public static final String ADV_FEATURE_STATE = "ADV_FEATURE_STATE";
    public static final int ADV_FEATURE_OFF = 0;
    public static final int ADV_FEATURE_ON = 1;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mBitTextView = (TextView) findViewById(R.id.denomination);
        mDollarTextview = (TextView) findViewById(R.id.dollar);

        mDenominationButton = (Button) findViewById(R.id.button_denomination);
        mCurrencyButton = (Button) findViewById(R.id.button_currency);
        mAdvancedFeaturesButton = (Button) findViewById(R.id.button_advance_features);


        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mDenominationButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mCurrencyButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mAdvancedFeaturesButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mBitTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.ITALIC);
        mDollarTextview.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.ITALIC);
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);


        int advFeatureState = getAdvFeatureStateFromSharedPreference();
        if(advFeatureState==ADV_FEATURE_OFF){
            mAdvancedFeaturesOn = false;
            Drawable toggle = getResources().getDrawable(R.drawable.btn_toggle_off);
            Drawable icon = getResources().getDrawable(R.drawable.ico_star);
            mAdvancedFeaturesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, toggle, null);
        } else {
            mAdvancedFeaturesOn = true;
            Drawable toggle = getResources().getDrawable(R.drawable.btn_toggle_on);
            Drawable icon = getResources().getDrawable(R.drawable.ico_star);
            mAdvancedFeaturesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, toggle, null);
        }

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(DisplayActivity.this, "Info", "Business directory info");
            }
        });

        mDenominationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(DisplayActivity.this, DisplayActivity.class);
//                startActivity(intent);
            }
        });

        mCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(DisplayActivity.this, SecurityActivity.class);
//                startActivity(intent);
            }
        });

        mAdvancedFeaturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdvancedFeaturesOn){
                    writeAdvFeatureToSharedPreference(ADV_FEATURE_OFF);
                    mAdvancedFeaturesOn = false;
                    Drawable toggle = getResources().getDrawable(R.drawable.btn_toggle_off);
                    Drawable icon = getResources().getDrawable(R.drawable.ico_star);
                    mAdvancedFeaturesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, toggle, null);
                    mAdvancedFeaturesButton.invalidate();
                } else {
                    writeAdvFeatureToSharedPreference(ADV_FEATURE_ON);
                    mAdvancedFeaturesOn = true;
                    Drawable toggle = getResources().getDrawable(R.drawable.btn_toggle_on);
                    Drawable icon = getResources().getDrawable(R.drawable.ico_star);
                    mAdvancedFeaturesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, toggle, null);
                    mAdvancedFeaturesButton.invalidate();
                }
            }
        });

    }

    @Override
    protected void onResume() {

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    private int getStateFromSharedPreferences(String key) {
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(key, ADV_FEATURE_OFF);
    }

    private int getAdvFeatureStateFromSharedPreference(){
        return getStateFromSharedPreferences(ADV_FEATURE_STATE);
    }

    private void writeAdvFeatureToSharedPreference(int value) {
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(ADV_FEATURE_STATE, value);
        editor.commit();
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

            if((finish.getRawX()>start.getRawX()) && (yDistance < 10)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 100){
                    finish();
                    return true;
                }
            }

        }

        return false;
    }
}
