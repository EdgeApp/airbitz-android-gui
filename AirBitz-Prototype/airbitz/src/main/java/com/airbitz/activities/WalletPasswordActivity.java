package com.airbitz.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.models.Transaction;
import com.airbitz.utils.Common;

/**
 * Created on 2/24/14.
 */
public class WalletPasswordActivity extends Activity implements GestureDetector.OnGestureListener{

    private EditText mPasswordEdittext;
    private ImageView mValidPasswordImageView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

    private GestureDetector mGestureDetector;
    private Intent mIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_password);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mPasswordEdittext = (EditText) findViewById(R.id.edittext_password);
        mValidPasswordImageView = (ImageView) findViewById(R.id.imageview_valid_password);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);


        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);

        TextView titleTextView = (TextView) findViewById(R.id.textview_title);
        titleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

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

        mPasswordEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if(!hasFocus){
                   if(((EditText) view).getText().length() != 0){

                   }
                }
            }
        });

        mPasswordEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                int keyAction = keyEvent.getAction();
                String test = "";
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);


                if(keyAction == KeyEvent.ACTION_UP){
                    switch (keyCode) {
                        case KeyEvent.FLAG_EDITOR_ACTION:
                            imm.hideSoftInputFromWindow(mPasswordEdittext.getWindowToken(), 0);
                            mValidPasswordImageView.setVisibility(View.VISIBLE);

                            if(mPasswordEdittext.getText().toString().equals("Password")){
                                mValidPasswordImageView.setImageResource(R.drawable.ico_approved);
                            }
                            else{
                                mValidPasswordImageView.setImageResource(R.drawable.ico_not_approved);
                            }
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            imm.hideSoftInputFromWindow(mPasswordEdittext.getWindowToken(), 0);
                            mValidPasswordImageView.setVisibility(View.VISIBLE);

                            if(mPasswordEdittext.getText().toString().equals("Password")){
                                mValidPasswordImageView.setImageResource(R.drawable.ico_approved);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        mIntent = new Intent(WalletPasswordActivity.this, TransactionActivity.class);
                                        startActivity(mIntent);
                                        finish();
                                    }
                                }, 2000 );
                            }
                            else{
                                mValidPasswordImageView.setImageResource(R.drawable.ico_not_approved);
                            }

                            return true;
                        default:
                            return false;
                    }
                }

                return false;
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
                Common.showHelpInfo(WalletPasswordActivity.this, "Info", "Business directory info");
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
    public void onBackPressed() {
        super.onBackPressed();
        mValidPasswordImageView.setVisibility(View.GONE);
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
