package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.utils.Common;

import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class SignUpActivity extends Activity implements GestureDetector.OnGestureListener{

    private Button mNextButton;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private EditText mWithdrawalPinEditText;
    private TextView mTitleTextView;
    private TextView mHintTextView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private RelativeLayout mParentLayout;
    private ScrollView mScrollView;

    private String mUsername;
    private String mPassword;
    private String mPasswordConfirmation;
    private String mWithdrawalPin;

    private static final String specialChar = "~`!@#$%^&*()-_+=,.?/<>:;'][{}|\\\"";

    private Intent mIntent;

    private GestureDetector mGestureDetector;

    public static String KEY_USERNAME = "KEY_USERNAME";
    public static String KEY_PASSWORD = "KEY_PASSWORD";
    public static String KEY_WITHDRAWAL = "KEY_WITHDRAWAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUsernameEditText = (EditText) findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText) findViewById(R.id.edittext_password);
        mPasswordConfirmationEditText = (EditText) findViewById(R.id.edittext_repassword);
        mWithdrawalPinEditText = (EditText) findViewById(R.id.edittext_withdrawalpin);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mHintTextView = (TextView) findViewById(R.id.textview_pass_hint);
        TextView withdrawalTextView = (TextView) findViewById(R.id.textview_withdrawal);

        withdrawalTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mUsernameEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mPasswordEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mPasswordConfirmationEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mHintTextView.setTypeface(LandingActivity.montserratRegularTypeFace);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

        mUsername = mUsernameEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();
        mPasswordConfirmation = mPasswordConfirmationEditText.getText().toString();
        mWithdrawalPin = mWithdrawalPinEditText.getText().toString();

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        String pattern = ".*[" + Pattern.quote(specialChar) + "].*";

                if(mUsername.length() != 0 && mWithdrawalPin.length() != 0 &&  mPassword.length() != 0){

                    if(mPassword.length() >= 10){

                        if( mPassword.matches(".*[A-Z].*") &&
                                mPassword.matches(".*[a-z].*") &&
                                mPassword.matches(".*\\d.*") &&
                                mPassword.matches(pattern)){

                            if(mPassword.equals(mPasswordConfirmation)){

                                if(mWithdrawalPin.matches("[0-9]+")){

                                    mIntent = new Intent(SignUpActivity.this, PasswordRecoveryActivity.class);
                                    mIntent.putExtra(KEY_USERNAME, mUsername);
                                    mIntent.putExtra(KEY_PASSWORD, mPassword);
                                    mIntent.putExtra(KEY_WITHDRAWAL, mWithdrawalPin);
                                    startActivity(mIntent);

                                }
                                else{
                                    Toast.makeText(SignUpActivity.this, "Withdrawal pin must consist of only numeric characters", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "Password confirmation does not match", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(SignUpActivity.this, "Password must contain at least 1 upper and lower case, number and special character ", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(SignUpActivity.this, "Password must contain at least 10 characters", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(SignUpActivity.this, "Please fill the empty field", Toast.LENGTH_SHORT).show();
                }

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
                Common.showHelpInfo(SignUpActivity.this, "Info", "Business directory info");
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
