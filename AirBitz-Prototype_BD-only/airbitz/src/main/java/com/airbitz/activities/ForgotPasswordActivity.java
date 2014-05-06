package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2/10/14.
 */
public class ForgotPasswordActivity extends Activity {

    private Button mSubmitButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mTitleTextView;

    private LinearLayout mItemsLayout;

    private Map mRecoveryQA;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mSubmitButton = (Button)findViewById(R.id.submitButton);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mRecoveryQA = getRecoveryQA();

        mSubmitButton.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(answersCorrect(mRecoveryQA)) {
                    //TODO Is this right when answers are correct - email password?
                   startActivity(new Intent(ForgotPasswordActivity.this, NavigationActivity.class));
                   finish();
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
                Common.showHelpInfo(ForgotPasswordActivity.this, "Info", "Business directory info");
            }
        });

        mItemsLayout = (LinearLayout) findViewById(R.id.forgot_questions_layout);

        populateQuestions(mRecoveryQA);
    }

    private boolean answersCorrect(Map<String, String> map) {
        boolean truth = true;
        for(int i=0; i<mItemsLayout.getChildCount(); i++) {
            View v = mItemsLayout.getChildAt(i);
            String question = ((TextView) ((ViewGroup)v).getChildAt(0)).getText().toString();
            String userAnswer = ((TextView) ((ViewGroup)v).getChildAt(1)).getText().toString();
            String realAnswer = map.get(question);
            if(!userAnswer.equals(realAnswer))
                truth = false;
        }
        return truth;
    }

    private void populateQuestions(Map<String, String> map) {
        for(String s: map.keySet()) {
            mItemsLayout.addView(getQueryView(s));
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    private Map getRecoveryQA() {
        //TODO replace with server received Q & A

        Map map = new HashMap<String, String>();
        ArrayList<String> questions = new ArrayList<String>();
        map.put("Name of Favorite Teacher", "Teacher");
        map.put("Mother's Maiden Name", "Name");
        map.put("Favorite Food", "Food");

        return map;
    }

    private View getQueryView(String question) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.item_password_forgot, null);
        TextView questionTextView = (TextView)view.findViewById(R.id.item_password_forgot_question);
        questionTextView.setText(question);

        return view;
    }
}
