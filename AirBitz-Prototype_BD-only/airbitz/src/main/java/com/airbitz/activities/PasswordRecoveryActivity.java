package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.PasswordRecoveryAdapter;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2/10/14.
 */
public class PasswordRecoveryActivity extends Activity {

    private Button mDoneSignUpButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private ImageButton mSkipStepButton;

    private TextView mTitleTextView;

    private String mUsername;
    private String mPassword;
    private String mWithdrawal;

    private Intent mIntent;

    private LinearLayout mPasswordRecoveryListView;
    private List<View> mQuestionViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mIntent = new Intent(PasswordRecoveryActivity.this, NavigationActivity.class);


        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mSkipStepButton = (ImageButton) findViewById(R.id.button_skip_step);
        mDoneSignUpButton = (Button) findViewById(R.id.button_complete_signup);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

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
                saveQuestionsAndAnswers();
                startActivity(mIntent);
                finish();
            }
        });

        mPasswordRecoveryListView = (LinearLayout) findViewById(R.id.password_recovery_listview);
        mQuestionViews = new ArrayList<View>();
        mQuestionViews.add(getQuestionView());
        populateQuestionViews();
    }

    private void saveQuestionsAndAnswers() {
        //blank Answers should not be saved even if question is selected?
        Map saveList = new HashMap<String, String>();
        for(View v: mQuestionViews) {
            Spinner spinner = (Spinner) ((RelativeLayout) ((ViewGroup)v).getChildAt(0)).getChildAt(0);
            EditText text = (EditText)  ((ViewGroup)v).getChildAt(1);
            String question = ((TextView) spinner.getChildAt(0)).getText().toString();
            String answer = text.getText().toString();
            if(answer!="") {
                saveList.put(question, answer);
            }
        }

        //TODO save questions and answers to server here from saveList in some async way

    }

    private void populateQuestionViews() {
        mPasswordRecoveryListView.removeAllViews();
        for(View v: mQuestionViews) {
            mPasswordRecoveryListView.addView(v);
        }
        mPasswordRecoveryListView.invalidate();
    }

    public void showAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(mIntent);
                        PasswordRecoveryActivity.this.finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.string_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private List<String> getQuestionList() {
        //TODO replace with server questions
        List<String> out = new ArrayList<String>();
        for(int i=0; i<5; i++) {
            out.add("Question "+i);
        }
        return out;
    }

    private View getQuestionView() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.item_password_recovery, null);
        Spinner mySpinner = (Spinner)view.findViewById(R.id.item_password_recovery_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getQuestionList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);

        final EditText edittext = (EditText) view.findViewById(R.id.item_password_recovery_answer);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    View newQuestion = getQuestionView();
                    mQuestionViews.add(newQuestion);
                    populateQuestionViews();
                    newQuestion.requestFocus();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

}

