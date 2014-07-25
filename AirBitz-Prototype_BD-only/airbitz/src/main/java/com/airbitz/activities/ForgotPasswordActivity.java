package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.airbitz.api.SWIGTYPE_p_void;
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

    //TODO remove when server QA finished
    private Map getRecoveryQA() {
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
        Map<String, String> questionMap = new HashMap<String, String>();

        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pData = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pData);

        FetchQuestionsTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            tABC_CC result = core.ABC_GetQuestionChoices(mUsername, null, pVoid, pError);
            boolean success = result == tABC_CC.ABC_CC_Ok? true: false;

            if(success) {
                QuestionChoices qc = new QuestionChoices(pData.getPRetData());
                long num = qc.getNumChoices();

                if(num>0) {
                    //TODO setup the map of questions and answers.

                } else {
                    success = false;
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchQuestionsTask = null;

            if (success) {
                populateQuestions(questionMap);
            } else {
                showNoQuestionsDialog();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchQuestionsTask = null;
        }
    }


    private class QuestionChoices extends tABC_QuestionChoices {
        private boolean ok=true;
        public QuestionChoices(SWIGTYPE_p_void pv) {
            super(PVoid.getPtr(pv), false);
            if(PVoid.getPtr(pv)==0) {
                ok = false;
            }
        }
        public long getNumChoices() {
            if(ok)
                return super.getNumChoices();
            else
                return 0;
        }

        public Map getQuestionsMap() {
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
    }

    private static class PVoid extends SWIGTYPE_p_void {
        public static long getPtr(SWIGTYPE_p_void p) { return getCPtr(p); }
    }

    private void showNoQuestionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.activity_forgot_no_questions))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                ForgotPasswordActivity.this.finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
