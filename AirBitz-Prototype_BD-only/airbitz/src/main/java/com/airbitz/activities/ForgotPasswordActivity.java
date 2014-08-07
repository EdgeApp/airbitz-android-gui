package com.airbitz.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.utils.Common;

/**
 * Created on 2/10/14.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private Button mSubmitButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mTitleTextView;

    private LinearLayout mItemsLayout;

    private String mUsername;
    private FetchQuestionsTask mFetchQuestionsTask;
    CoreAPI.QuestionChoice[] mQuestionChoices;


    private CoreAPI mCoreAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent !=null) {
            mUsername = intent.getStringExtra(SignUpActivity.KEY_USERNAME);
        }

        if(mCoreAPI==null)
            mCoreAPI = CoreAPI.getApi();

        setContentView(R.layout.activity_forgot_password);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mSubmitButton = (Button)findViewById(R.id.submitButton);
        mBackButton = (ImageButton) findViewById(R.id.fragment_category_button_back);
        mHelpButton = (ImageButton) findViewById(R.id.fragment_category_button_help);

        mTitleTextView = (TextView) findViewById(R.id.fragment_category_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mSubmitButton.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(answersCorrect(mQuestionChoices)) {
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

        mFetchQuestionsTask = new FetchQuestionsTask(mUsername);
        mFetchQuestionsTask.execute((Void) null);

    }

    private boolean answersCorrect(CoreAPI.QuestionChoice[] map) {
        boolean truth = true;
        for(int i=0; i<mItemsLayout.getChildCount(); i++) {
            View v = mItemsLayout.getChildAt(i);
//            String question = ((TextView) ((ViewGroup)v).getChildAt(0)).getText().toString();
//            String userAnswer = ((TextView) ((ViewGroup)v).getChildAt(1)).getText().toString();
//            String realAnswer = map.get(question);
//            if(!userAnswer.equals(realAnswer))
//                truth = false;
        }
        return truth;
    }

    private void populateQuestions(CoreAPI.QuestionChoice[] map) {
        for(CoreAPI.QuestionChoice s: map) {
            mItemsLayout.addView(getQueryView(s.getQuestion()));
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
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

        FetchQuestionsTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mQuestionChoices = mCoreAPI.GetQuestionChoices();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchQuestionsTask = null;

            if (success) {
                populateQuestions(mQuestionChoices);
            } else {
                showOkMessageDialogAndExit(getResources().getString(R.string.activity_forgot_no_questions));
            }
        }

        @Override
        protected void onCancelled() {
            mFetchQuestionsTask = null;
        }
    }
}
