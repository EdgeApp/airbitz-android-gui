package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.PasswordRecoveryAdapter;
import com.airbitz.api.SWIGTYPE_p_p_sABC_QuestionChoice;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_QuestionChoice;
import com.airbitz.api.tABC_QuestionChoices;
import com.airbitz.api.tABC_RequestResults;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2/10/14.
 * 6/11/14 twb: iPhone implements 6 questions, or skip this step. Two are String,
 * two are Numeric, and two are Address, in that order. See PasswordRecoveryViewcontroller.m.
 * Also, following questions in the same category don't repeat earlier questions.
 */
public class PasswordRecoveryActivity extends Activity {

    private Button mDoneSignUpButton;

//    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private ImageButton mSkipStepButton;

    private TextView mTitleTextView;

    private ArrayList<View> mItemsList;

    private String mUsername;
    private String mPassword;
    private String mWithdrawal;

    private View dummy;

    private int spinnerCount = 1;

    private Intent mIntent;

    private LinearLayout mLayoutRecovery;
    private LinearLayout mPasswordRecoveryListView;
    private List<View> mQuestionViews;
    private List<String> mQuestions;

    private FetchQuestionsTask mFetchQuestionsTask;
    private Map<String, Integer> mStringCategory = new HashMap<String, Integer>();
    private Map<String, Integer> mNumberCategory = new HashMap<String, Integer>();
    private Map<String, Integer> mAddressCategory = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mIntent = new Intent(PasswordRecoveryActivity.this, NavigationActivity.class);

        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mLayoutRecovery = (LinearLayout) findViewById(R.id.layout_pass_recovery);

        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mSkipStepButton = (ImageButton) findViewById(R.id.button_skip_step);
        mDoneSignUpButton = (Button) findViewById(R.id.button_complete_signup);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        dummy = findViewById(R.id.password_dummy);

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
                //saveQuestionsAndAnswers();
                startActivity(mIntent);
                finish();
            }
        });

        mPasswordRecoveryListView = (LinearLayout) findViewById(R.id.password_recovery_listview);
        mQuestionViews = new ArrayList<View>();
        while(spinnerCount !=7) {
            mQuestions = getQuestionList(null);
            mQuestionViews.add(getQuestionView());
            spinnerCount++;
        }
        populateQuestionViews();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dummy.requestFocus();

        mFetchQuestionsTask = new FetchQuestionsTask("junktest5");
        mFetchQuestionsTask.execute((Void) null);
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

    /**
     * Represents an asynchronous question fetch task
     */
    public class FetchQuestionsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;

        tABC_Error pError = new tABC_Error();
        QuestionResults pData = new QuestionResults();

        FetchQuestionsTask(String username) {
//            // next for lines for testing only
//            String seed = "adlkjaljblkajsf";
//            tABC_CC code = core.ABC_Initialize(getApplication().getFilesDir().toString(), null, null, seed, seed.length(), pError);

            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            tABC_CC result = core.ABC_GetQuestionChoices(mUsername, null, pData, pError);
            if(result == tABC_CC.ABC_CC_Ok) { // && pData.getBSuccess()) {
                QuestionChoices qc = new QuestionChoices(pData.getPtrPtr());
                long num = qc.getNumChoices();
                QuestionChoice[] choices = qc.getChoices();

                if(num>0) {
                    //TODO setup the map of questions and answers.
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchQuestionsTask = null;

            if (success) {
//                populateQuestions(questionMap);
            } else {
//                showNoQuestionsDialog();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchQuestionsTask = null;
        }

    }

    private List<String> getQuestionList(tABC_QuestionChoices questionChoices) {
        //TODO replace with server questions


        List<String> out = new ArrayList<String>();


        if(spinnerCount == 1 || spinnerCount == 2) {
            for (String quest : getResources().getStringArray(R.array.password_recovery_1)) {
                out.add(quest);
            }
        }else if(spinnerCount == 3 || spinnerCount == 4){
            for (String quest : getResources().getStringArray(R.array.password_recovery_2)) {
                out.add(quest);
            }
        }else if(spinnerCount == 5 || spinnerCount == 6){
            for (String quest : getResources().getStringArray(R.array.password_recovery_3)) {
                out.add(quest);
            }
        }
        out.add("Question");
        return out;
    }

    private class QuestionResults extends tABC_RequestResults {
        public long getPtrPtr() {
           QuestionChoices fake = new QuestionChoices(getCPtr(this)); // A fake to get *ptr
           return fake.getNumChoices();
        }
    }

    private class QuestionChoices extends tABC_QuestionChoices {
        long mNumChoices = 0;
        long mChoiceStart = 0;
        QuestionChoice[] choices;

        public QuestionChoices (long pv) {
           super(pv, false);
           if(pv!=0) {
                mNumChoices = super.getNumChoices();
            }
        }

        public long getNumChoices() { return mNumChoices; }

        public QuestionChoice[] getChoices() {
            choices = new QuestionChoice[(int) mNumChoices];
            SWIGTYPE_p_p_sABC_QuestionChoice start = super.getAChoices();
            for(int i=0; i<mNumChoices; i++) {
                QuestionChoices fake = new QuestionChoices(PPVoid.getPtr(start, i*4));
                mChoiceStart = fake.getNumChoices();
                choices[i] = new QuestionChoice(new PVOID(mChoiceStart));
            }
            return choices;
        }
    }

    private class QuestionChoice extends tABC_QuestionChoice {
        String mQuestion = null;
        String mCategory = null;
        long mMinLength = -1;

        public QuestionChoice(SWIGTYPE_p_void pv) {
            super(PVoidStatic.getPtr(pv), false);
            if(PVoidStatic.getPtr(pv)!=0) {
                mQuestion = super.getSzQuestion();
                mCategory = super.getSzCategory();
                mMinLength = super.getMinAnswerLength();
            }
        }

        public String getQuestion() { return mQuestion; }

        public long getMinLength() { return mMinLength; }

        public String getCategory() { return mCategory; }

    }

    private class PVOID extends SWIGTYPE_p_void {
        public PVOID(long p) {
            super(p, false);
        }
    }

    private static class PVoidStatic extends SWIGTYPE_p_void {
        public static long getPtr(SWIGTYPE_p_void p) { return getCPtr(p); }
    }

    private static class PPVoid extends SWIGTYPE_p_p_sABC_QuestionChoice {
        public static long getPtr(SWIGTYPE_p_p_sABC_QuestionChoice p) { return getCPtr(p); }
        public static long getPtr(SWIGTYPE_p_p_sABC_QuestionChoice p, long i) { return getCPtr(p)+i; }
    }



    private View getQuestionView() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.item_password_recovery, null);
        final Spinner mySpinner = (Spinner)view.findViewById(R.id.item_password_recovery_spinner);

        mItemsList = new ArrayList<View>();
        for(String question: mQuestions) {
            TextView b = (TextView) inflater.inflate(R.layout.item_password_recovery_spinner, null);
            b.setText(question);
            mItemsList.add(b);
        }

        final PasswordRecoveryAdapter adapter = new PasswordRecoveryAdapter(this, mItemsList, mQuestions);
        adapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);

        mySpinner.setAdapter(adapter);
        mySpinner.setDropDownWidth((int)getResources().getDimension(R.dimen.spinner_width_password));

        final EditText edittext = (EditText) view.findViewById(R.id.item_password_recovery_answer);
        final View redRing = view.findViewById(R.id.red_ring);
        edittext.setTypeface(NavigationActivity.montserratRegularTypeFace);
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

        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                try{
                    if(!edittext.getText().toString().isEmpty() && edittext.getText().toString().length() <10){
                        redRing.setVisibility(View.VISIBLE);
                    }else{
                        redRing.setVisibility(View.GONE);
                    }
                }catch(Exception e ){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mySpinner.getSelectedItemPosition() != adapter.getCount()) {
                    edittext.setFocusableInTouchMode(true);  //if this is not already set
                    edittext.requestFocus();  //to move the cursor
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(edittext, InputMethodManager.SHOW_FORCED);
                    edittext.setCursorVisible(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mySpinner.setSelection(adapter.getCount());
        edittext.clearFocus();
        edittext.setCursorVisible(false);
        return view;
    }

}

