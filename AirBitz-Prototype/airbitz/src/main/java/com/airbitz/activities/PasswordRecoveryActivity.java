package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.PasswordRecoveryAdapter;
import com.airbitz.utils.Common;

/**
 * Created on 2/10/14.
 */
public class PasswordRecoveryActivity extends Activity implements GestureDetector.OnGestureListener{


    private Button mDoneSignUpButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private ImageButton mSkipStepButton;

    private Spinner mQuestionSpinner1;
    private Spinner mQuestionSpinner2;
    private Spinner mQuestionSpinner3;
    private Spinner mQuestionSpinner4;
    private Spinner mQuestionSpinner5;
    private Spinner mQuestionSpinner6;
    private Spinner mQuestionSpinner7;
    private Spinner mQuestionSpinner8;
    private Spinner mQuestionSpinner9;
    private Spinner mQuestionSpinner10;

    private RelativeLayout mRelativeLayout1;
    private RelativeLayout mRelativeLayout2;
    private RelativeLayout mRelativeLayout3;
    private RelativeLayout mRelativeLayout4;
    private RelativeLayout mRelativeLayout5;
    private RelativeLayout mRelativeLayout6;
    private RelativeLayout mRelativeLayout7;
    private RelativeLayout mRelativeLayout8;
    private RelativeLayout mRelativeLayout9;
    private RelativeLayout mRelativeLayout10;

    private EditText mQuestionEdit1;
    private EditText mQuestionEdit2;
    private EditText mQuestionEdit3;
    private EditText mQuestionEdit4;
    private EditText mQuestionEdit5;

    private EditText mQuestionEdit6;
    private EditText mQuestionEdit7;
    private EditText mQuestionEdit8;
    private EditText mQuestionEdit9;
    private EditText mQuestionEdit10;

    private GestureDetector mGestureDetector;

    private RelativeLayout mParenLayout;
    private ScrollView mScrollView;

    private TextView mTitleTextView;
    private int mQuestionCounter = 0;
    private boolean mAnswer5HasNotCreate = true;
    private boolean mAnswer6HasNotCreate = true;
    private boolean mAnswer7HasNotCreate = true;
    private boolean mAnswer8HasNotCreate = true;
    private boolean mAnswer9HasNotCreate = true;

    private String mUsername;
    private String mPassword;
    private String mWithdrawal;

    private Intent mIntent;

    private PasswordRecoveryAdapter dataAdapter;

    private String[] mQuestionSample;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        mGestureDetector = new GestureDetector(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParenLayout = (RelativeLayout) findViewById(R.id.layout_pass_recovery);
        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mQuestionCounter = 5;
        mQuestionSample = new String[]{"Question 1", "Question 2","Question 3","Question 4",
                "Question 5","Question 6","Question 7",
                "Question 8", "Question 9", "Question 10", "Question 11", "Question 12",
                "Question 13", "Question 14", "Question 15"};

        mIntent = new Intent(PasswordRecoveryActivity.this, BusinessDirectoryActivity.class);

        dataAdapter = new PasswordRecoveryAdapter(PasswordRecoveryActivity.this, mQuestionSample);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mParenLayout.setOnTouchListener(new View.OnTouchListener() {
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


        mRelativeLayout6 = (RelativeLayout) findViewById(R.id.layout_6);
        mRelativeLayout7 = (RelativeLayout) findViewById(R.id.layout_7);
        mRelativeLayout8 = (RelativeLayout) findViewById(R.id.layout_8);
        mRelativeLayout9 = (RelativeLayout) findViewById(R.id.layout_9);
        mRelativeLayout10 = (RelativeLayout) findViewById(R.id.layout_10);

        mQuestionSpinner1 = (Spinner) findViewById(R.id.spinner_question1);
        mQuestionSpinner2 = (Spinner) findViewById(R.id.spinner_question2);
        mQuestionSpinner3 = (Spinner) findViewById(R.id.spinner_question3);
        mQuestionSpinner4 = (Spinner) findViewById(R.id.spinner_question4);
        mQuestionSpinner5 = (Spinner) findViewById(R.id.spinner_question5);

        mQuestionSpinner6 = (Spinner) findViewById(R.id.spinner_question6);
        mQuestionSpinner7 = (Spinner) findViewById(R.id.spinner_question7);
        mQuestionSpinner8 = (Spinner) findViewById(R.id.spinner_question8);
        mQuestionSpinner9 = (Spinner) findViewById(R.id.spinner_question9);
        mQuestionSpinner10 = (Spinner) findViewById(R.id.spinner_question10);

        mQuestionEdit1 = (EditText) findViewById(R.id.edittext_question1);
        mQuestionEdit2 = (EditText) findViewById(R.id.edittext_question2);
        mQuestionEdit3 = (EditText) findViewById(R.id.edittext_question3);
        mQuestionEdit4 = (EditText) findViewById(R.id.edittext_question4);
        mQuestionEdit5 = (EditText) findViewById(R.id.edittext_question5);

        mQuestionEdit6 = (EditText) findViewById(R.id.edittext_question6);
        mQuestionEdit7 = (EditText) findViewById(R.id.edittext_question7);
        mQuestionEdit8 = (EditText) findViewById(R.id.edittext_question8);
        mQuestionEdit9 = (EditText) findViewById(R.id.edittext_question9);
        mQuestionEdit10 = (EditText) findViewById(R.id.edittext_question10);


        mQuestionEdit1.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit2.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit3.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit4.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit5.setTypeface(LandingActivity.montserratRegularTypeFace);

        mQuestionEdit6.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit7.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit9.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestionEdit10.setTypeface(LandingActivity.montserratRegularTypeFace);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mSkipStepButton = (ImageButton) findViewById(R.id.button_skip_step);
        mDoneSignUpButton = (Button) findViewById(R.id.button_complete_signup);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mQuestionSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit1.setEnabled(true);
                mQuestionEdit1.setFocusable(true);


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit2.setEnabled(true);
                mQuestionEdit2.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit3.setEnabled(true);
                mQuestionEdit3.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit4.setEnabled(true);
                mQuestionEdit4.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner7.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner8.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner9.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mQuestionSpinner10.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mQuestionEdit5.setEnabled(true);
                mQuestionEdit5.setFocusable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mQuestionSpinner1.setAdapter(dataAdapter);
        mQuestionSpinner2.setAdapter(dataAdapter);
        mQuestionSpinner3.setAdapter(dataAdapter);
        mQuestionSpinner4.setAdapter(dataAdapter);
        mQuestionSpinner5.setAdapter(dataAdapter);
        mQuestionSpinner6.setAdapter(dataAdapter);
        mQuestionSpinner7.setAdapter(dataAdapter);
        mQuestionSpinner8.setAdapter(dataAdapter);
        mQuestionSpinner9.setAdapter(dataAdapter);
        mQuestionSpinner10.setAdapter(dataAdapter);

        mQuestionSpinner1.setSelection(0);
        mQuestionSpinner2.setSelection(1);
        mQuestionSpinner3.setSelection(2);
        mQuestionSpinner4.setSelection(3);
        mQuestionSpinner5.setSelection(4);
        mQuestionSpinner6.setSelection(5);
        mQuestionSpinner7.setSelection(6);
        mQuestionSpinner8.setSelection(7);
        mQuestionSpinner9.setSelection(8);
        mQuestionSpinner10.setSelection(9);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(PasswordRecoveryActivity.this, "Info", "Business directory info");
            }
        });

        mQuestionEdit5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.toString().length() > 0){
                    mRelativeLayout6.setVisibility(View.VISIBLE);
                    mQuestionEdit6.setVisibility(View.VISIBLE);
                    mQuestionSpinner6.setVisibility(View.VISIBLE);
                }
                else{
                    mRelativeLayout6.setVisibility(View.GONE);
                    mQuestionEdit6.setVisibility(View.GONE);
                    mQuestionSpinner6.setVisibility(View.GONE);
                }
            }
        });

        mQuestionEdit6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.toString().length() > 0){
                    mRelativeLayout7.setVisibility(View.VISIBLE);
                    mQuestionEdit7.setVisibility(View.VISIBLE);
                    mQuestionSpinner7.setVisibility(View.VISIBLE);
                }
                else{
                    mQuestionEdit7.setVisibility(View.GONE);
                    mQuestionSpinner7.setVisibility(View.GONE);
                    mRelativeLayout7.setVisibility(View.GONE);
                }
            }
        });

        mQuestionEdit7.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.toString().length() > 0){
                    mRelativeLayout8.setVisibility(View.VISIBLE);
                    mQuestionEdit8.setVisibility(View.VISIBLE);
                    mQuestionSpinner8.setVisibility(View.VISIBLE);
                }
                else{
                    mQuestionEdit8.setVisibility(View.GONE);
                    mQuestionSpinner8.setVisibility(View.GONE);
                    mRelativeLayout8.setVisibility(View.GONE);
                }
            }
        });

        mQuestionEdit8.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.toString().length() > 0){
                    mRelativeLayout9.setVisibility(View.VISIBLE);
                    mQuestionEdit9.setVisibility(View.VISIBLE);
                    mQuestionSpinner9.setVisibility(View.VISIBLE);
                }
                else{
                    mQuestionEdit9.setVisibility(View.GONE);
                    mQuestionSpinner9.setVisibility(View.GONE);
                    mRelativeLayout9.setVisibility(View.GONE);
                }
            }
        });

        mQuestionEdit9.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.toString().length() > 0){
                    mRelativeLayout10.setVisibility(View.VISIBLE);
                    mQuestionEdit10.setVisibility(View.VISIBLE);
                    mQuestionSpinner10.setVisibility(View.VISIBLE);
                }
                else{
                    mRelativeLayout10.setVisibility(View.GONE);
                    mQuestionEdit10.setVisibility(View.GONE);
                    mQuestionSpinner10.setVisibility(View.GONE);
                }
            }
        });


        mUsername = getIntent().getStringExtra(SignUpActivity.KEY_USERNAME);
        mPassword = getIntent().getStringExtra(SignUpActivity.KEY_PASSWORD);
        mWithdrawal = getIntent().getStringExtra(SignUpActivity.KEY_WITHDRAWAL);

        mSkipStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
        mDoneSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mIntent);
                finish();
            }
        });
    }

    public void showAlertDialog(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                PasswordRecoveryActivity.this);

        alertDialogBuilder.setTitle("Warning");

        alertDialogBuilder
                .setMessage("Are you sure want to skip?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        startActivity(mIntent);
                        PasswordRecoveryActivity.this.finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }
}

