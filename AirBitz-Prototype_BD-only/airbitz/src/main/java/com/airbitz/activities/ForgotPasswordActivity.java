package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_QuestionChoices;
import com.airbitz.api.tABC_RequestResults;
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

    private String mUsername;
    private FetchQuestionsTask mFetchQuestionsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent !=null) {
            mUsername = intent.getStringExtra(SignUpActivity.KEY_USERNAME);
        }

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

        mFetchQuestionsTask = new FetchQuestionsTask(mUsername);
        mFetchQuestionsTask.execute((Void) null);

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
        map.put("What is your father\'s birthdate?", "fbday");
        map.put("What is your mother\'s maiden and current last name?", "mname");
        map.put("What is your oldest sibling\'s birthdate?", "sbday");
        map.put("Who is your favorite superhero?", "superhero");
        map.put("What is the first street address you remember living in?", "firstaddress");
        map.put("What was the address of your home in college?", "collegehome");

        return map;
    }

    private View getQueryView(String question) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.item_password_forgot, null);
        TextView questionTextView = (TextView)view.findViewById(R.id.item_password_forgot_question);
        questionTextView.setText(question);

        return view;
    }

    /**
     * An asynchronous question fetch task
     */
    public class FetchQuestionsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pData = new tABC_RequestResults();

        FetchQuestionsTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            tABC_Error pError = new tABC_Error();
            tABC_RequestResults pData = new tABC_RequestResults();
            tABC_CC result = core.ABC_GetQuestionChoices(mUsername, null, pData, pError);
//            tABC_QuestionChoices pQuestions = new tABC_QuestionChoices();
//            pQuestions.swigCPtr = (pData.getPRetData()).swigCPtr;
//            long num = pQuestions.getNumChoices();

            boolean success = result == tABC_CC.ABC_CC_Ok? true: false;
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchQuestionsTask = null;
//                if (pResults->requestType == ABC_RequestType_GetQuestionChoices)
//                {
//                    //NSLog(@"GetQuestionChoices completed with cc: %ld (%s)", (unsigned long) pResults->errorInfo.code, pResults->errorInfo.szDescription);
//                    if (pResults->bSuccess)
//                    {
//                        tABC_QuestionChoices *pQuestionChoices = (tABC_QuestionChoices *)pResults->pRetData;
//                        [controller categorizeQuestionChoices:pQuestionChoices];
//                        ABC_FreeQuestionChoices(pQuestionChoices);
//                    }
//                    [controller performSelectorOnMainThread:@selector(getPasswordRecoveryQuestionsComplete) withObject:nil waitUntilDone:FALSE];

            if (success) {
//                populateQuestionViews();
            } else {
//                showProgress(false);
//                showErrorDialog();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchQuestionsTask = null;
        }
    }

}
