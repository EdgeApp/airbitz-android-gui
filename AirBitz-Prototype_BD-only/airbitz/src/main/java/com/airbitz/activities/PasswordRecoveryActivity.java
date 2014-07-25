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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.adapters.PasswordRecoveryAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_p_sABC_QuestionChoice;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_QuestionChoice;
import com.airbitz.api.tABC_QuestionChoices;
import com.airbitz.api.tABC_RequestResults;

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
    public static final String CHANGE_QUESTIONS = "com.airbitz.passwordrecoveryactivity.change_questions";

    private String mUsername, mPassword;

    private View dummyFocus;
    private View dummyCover;

    private ImageButton mBackButton;

    private RelativeLayout mLayoutRecovery;
    private LinearLayout mPasswordRecoveryListView;
    private List<View> mQuestionViews;

    private FetchQuestionsTask mFetchQuestionsTask;
    private Map<String, Integer> mStringCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> currentStringCategory1;
    private List<String> currentStringCategory2;
    private Map<String, Integer> mNumericCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> currentNumericCategory1;
    private List<String> currentNumericCategory2;
    private Map<String, Integer> mAddressCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> currentAddressCategory1;
    private List<String> currentAddressCategory2;

    private SaveQuestionsTask mSaveQuestionsTask;

    private CoreAPI mCoreAPI;
    private boolean mChangeQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_password_recovery);

        mCoreAPI = CoreAPI.getApi();

        Button mSkipStepButton = (Button) findViewById(R.id.activity_recovery_skip_button);
        Button mDoneSignUpButton = (Button) findViewById(R.id.activity_recovery_complete_button);
        mBackButton = (ImageButton) findViewById(R.id.activity_password_recovery_back_button);

        mChangeQuestions = getIntent().getBooleanExtra(CHANGE_QUESTIONS, false);
        if(mChangeQuestions) {
            //TODO question changes flow, user already logged in
            mUsername = AirbitzApplication.getUsername();
            mPassword = AirbitzApplication.getPassword();

            mSkipStepButton.setVisibility(View.INVISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mDoneSignUpButton.setText(getResources().getString(R.string.activity_recovery_complete_button_change_questions));
        } else {
            mUsername = getIntent().getStringExtra(SignUpActivity.KEY_USERNAME);
            mPassword = getIntent().getStringExtra(SignUpActivity.KEY_PASSWORD);

            mSkipStepButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
            mSkipStepButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowSkipQuestionsAlert();
                }
            });
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_app));
        this.overridePendingTransition(R.anim.nothing, R.anim.slide_in_from_right);

        mLayoutRecovery = (RelativeLayout) findViewById(R.id.activity_recovery_container_layout);

        currentStringCategory1 = new ArrayList<String>();
        currentStringCategory2 = new ArrayList<String>();
        currentNumericCategory1 = new ArrayList<String>();
        currentNumericCategory2 = new ArrayList<String>();
        currentAddressCategory1 = new ArrayList<String>();
        currentAddressCategory2 = new ArrayList<String>();

        TextView mTitleTextView = (TextView) findViewById(R.id.activity_recovery_title_textview);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mDoneSignUpButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        dummyFocus = findViewById(R.id.activity_recovery_dummy_focus);
        dummyCover = findViewById(R.id.activity_recovery_dummy_cover);

        mDoneSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompleteSignup();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_out_right);
            }
        });

        mPasswordRecoveryListView = (LinearLayout) findViewById(R.id.activity_recovery_question_listview);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mFetchQuestionsTask = new FetchQuestionsTask();
        mFetchQuestionsTask.execute((Void) null);
    }

    private void CompleteSignup() {
        //verify that all six questions have been selected
        dummyCover.setVisibility(View.VISIBLE);
        boolean allQuestionsSelected = true;
        boolean allAnswersValid = true;
        String questions = "";
        String answers = "";

        int count = 0;
        for (View view : mQuestionViews) {
            QuestionView qaView = (QuestionView) view;
            if (qaView.getQuestion().equals(getString(R.string.activity_recovery_question_default))) {
                allQuestionsSelected = false;
                break;
            }
            //verify that all six answers have achieved their minimum character limit
            if (qaView.getText().length() < qaView.getMinimumCharacters()) {
                allAnswersValid = false;
            } else {
                //add question and answer to arrays
                if (count != 0) {
                    questions += "\n";
                    answers += "\n";
                }
                questions += qaView.getQuestion();
                answers += qaView.getText();
            }
            count++;
        }
        if (allQuestionsSelected) {
            if (allAnswersValid) {
                mSaveQuestionsTask = new SaveQuestionsTask(questions, answers);
                mSaveQuestionsTask.execute((Void) null);
            } else {
                ShowAnswerAllQuestionsAlert();
            }
        } else {
            ShowPickAllQuestionsAlert();
        }
    }

    private void InitializeQuestionViews() {
        mQuestionViews = new ArrayList<View>();
        View mQuestionViewString1 = new QuestionView(this, currentStringCategory1,currentStringCategory2, "string");
        View mQuestionViewString2 = new QuestionView(this, currentStringCategory2,currentStringCategory1, "string");
        View mQuestionViewString3 = new QuestionView(this, currentNumericCategory1,currentNumericCategory2, "numeric");
        View mQuestionViewString4 = new QuestionView(this, currentNumericCategory2,currentNumericCategory1, "numeric");
        View mQuestionViewString5 = new QuestionView(this, currentAddressCategory1,currentAddressCategory2, "address");
        View mQuestionViewString6 = new QuestionView(this, currentAddressCategory2,currentAddressCategory1, "address");
        mQuestionViews.add(mQuestionViewString1);
        mQuestionViews.add(mQuestionViewString2);
        mQuestionViews.add(mQuestionViewString3);
        mQuestionViews.add(mQuestionViewString4);
        mQuestionViews.add(mQuestionViewString5);
        mQuestionViews.add(mQuestionViewString6);
        populateQuestionViews();
    }

    private void populateQuestionViews() {
        mPasswordRecoveryListView.removeAllViews();
        for (View v : mQuestionViews) {
            mPasswordRecoveryListView.addView(v);
        }
        mPasswordRecoveryListView.invalidate();
    }

    public void ShowSkipQuestionsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SuccessfulSignup();
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

    private void ShowAnswerAllQuestionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.activity_recovery_answer_questions_alert))
                .setTitle(getResources().getString(R.string.activity_recovery_title))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dummyCover.setVisibility(View.GONE);
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ShowPickAllQuestionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.activity_recovery_pick_questions_alert))
                .setTitle(getResources().getString(R.string.activity_recovery_title))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dummyCover.setVisibility(View.GONE);
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ShowSaveFailAlert(String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(reason)
                .setTitle(getResources().getString(R.string.activity_recovery_title))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dummyCover.setVisibility(View.GONE);
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ShowCompleteAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.activity_recovery_done_details))
                .setTitle(getResources().getString(R.string.activity_recovery_done_title))
                .setCancelable(true)
                .setNegativeButton(getResources().getString(R.string.string_back),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SuccessfulSignup();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Represents an asynchronous question fetch task
     */
    public class FetchQuestionsTask extends AsyncTask<Void, Void, Boolean> {
        CoreAPI.QuestionChoice[] mChoices;

        FetchQuestionsTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            mChoices = mCoreAPI.GetRecoveryQuestions();
                if(mChoices.length > 0) {

                    for(CoreAPI.QuestionChoice choice : mChoices) {
                        String category = choice.getCategory();
                        if(category.equals("string")) {
                            mStringCategory.put(choice.getQuestion(), (int) choice.getMinLength());
                            currentStringCategory1.add(choice.getQuestion());
                            currentStringCategory2.add(choice.getQuestion());
                        } else if(category.equals("numeric")) {
                            mNumericCategory.put(choice.getQuestion(), (int) choice.getMinLength());
                            currentNumericCategory1.add(choice.getQuestion());
                            currentNumericCategory2.add(choice.getQuestion());
                        } else if(category.equals("address")) {
                            mAddressCategory.put(choice.getQuestion(), (int) choice.getMinLength());
                            currentAddressCategory1.add(choice.getQuestion());
                            currentAddressCategory2.add(choice.getQuestion());
                        }
                    }
                    currentStringCategory1.add(getString(R.string.activity_recovery_question_default));
                    currentStringCategory2.add(getString(R.string.activity_recovery_question_default));
                    currentNumericCategory1.add(getString(R.string.activity_recovery_question_default));
                    currentNumericCategory2.add(getString(R.string.activity_recovery_question_default));
                    currentAddressCategory1.add(getString(R.string.activity_recovery_question_default));
                    currentAddressCategory2.add(getString(R.string.activity_recovery_question_default));
                    return true;
                } else {
                    System.err.println("No Questions");
                    return false;
                }
            }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFetchQuestionsTask = null;

            if (success) {
                InitializeQuestionViews();
            }

            dummyFocus.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mFetchQuestionsTask = null;
        }

    }

    /**
     * Represents an asynchronous question save task
     */
    public class SaveQuestionsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mQuestions, mAnswers;

        SaveQuestionsTask(String questions, String answers) {
            mQuestions = questions;
            mAnswers = answers;
        }

        //TODO need pre and post execute busy spinner

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC result = mCoreAPI.SaveRecoveryAnswers(mQuestions, mAnswers);
            return result == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveQuestionsTask = null;
            dummyCover.setVisibility(View.GONE);
            if (!success) {
                Log.d("PasswordRecoveryActivity", "Save recovery answers failed.");
                ShowSaveFailAlert("Save recovery answers failed.");
            } else {
                ShowCompleteAlert();
            }
        }

        @Override
        protected void onCancelled() {
            mSaveQuestionsTask = null;
        }
    }

    private void SuccessfulSignup() {
        PasswordRecoveryActivity.this.finish();
    }

    private class QuestionView extends LinearLayout {
        Context mContext;
        private String mType;
        private String chosenQuestion = "";
        private int mCharLimit = 0;
        private Spinner mSpinner;
        private Button mCover;
        private EditText mText;
        private PasswordRecoveryAdapter mAdapter;
        private List<String> currentQuestionList;
        private List<String> otherQuestionList;
        int pos;

        private QuestionView me = this;

        public QuestionView(Context context, List<String> questions,List<String> otherQuestions, String type) {
            super(context);
            mContext = context;
            this.mType = type;
            currentQuestionList = questions;
            otherQuestionList = otherQuestions;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.item_password_recovery, this);
            mSpinner = (Spinner)findViewById(R.id.item_recovery_question_spinner);
            mAdapter = new PasswordRecoveryAdapter(context, currentQuestionList);
            mAdapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);
            mSpinner.setAdapter(mAdapter);
            mSpinner.setDropDownWidth((int) getResources().getDimension(R.dimen.spinner_width_password));
            mSpinner.setFocusable(true);
            mSpinner.setFocusableInTouchMode(true);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(mType.compareTo("string")==0){
                        for(String s: mStringCategory.keySet()){
                            if(s.compareTo((String)mSpinner.getSelectedItem())==0){
                                if(mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                                    mCharLimit = mStringCategory.get(s);
                                    if(!chosenQuestion.isEmpty()) {
                                        otherQuestionList.add(chosenQuestion);
                                    }
                                    chosenQuestion = s;
                                    otherQuestionList.remove(s);
                                    List<String> tempList = new ArrayList<String>();
                                    for(String quest: mStringCategory.keySet()){
                                        if(otherQuestionList.contains(quest)){
                                            tempList.add(quest);
                                        }
                                    }
                                    tempList.add("Question");
                                    otherQuestionList.clear();
                                    otherQuestionList.addAll(tempList);
                                    //mAdapter.notifyDataSetChanged();
                                }else{
                                    mCharLimit = 0;
                                }
                            }
                        }
                    }else if(mType.compareTo("numeric")==0){
                        for(String s: mNumericCategory.keySet()){
                            if(s.compareTo((String)mSpinner.getSelectedItem())==0){
                                if(mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                                    mCharLimit = mNumericCategory.get(s);
                                    if(!chosenQuestion.isEmpty()) {
                                        otherQuestionList.add(chosenQuestion);
                                    }
                                    chosenQuestion = s;
                                    otherQuestionList.remove(s);
                                    List<String> tempList = new ArrayList<String>();
                                    for(String quest: mNumericCategory.keySet()){
                                        if(otherQuestionList.contains(quest)){
                                            tempList.add(quest);
                                        }
                                    }
                                    tempList.add("Question");
                                    otherQuestionList.clear();
                                    otherQuestionList.addAll(tempList);
                                    //mAdapter.notifyDataSetChanged();
                                }else{
                                    mCharLimit = 0;
                                }
                            }
                        }
                    }else if(mType.compareTo("address")==0){
                        for(String s: mAddressCategory.keySet()){
                            if(s.compareTo((String)mSpinner.getSelectedItem())==0){
                                if(mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                                    mCharLimit = mAddressCategory.get(s);
                                    if(!chosenQuestion.isEmpty()) {
                                        otherQuestionList.add(chosenQuestion);
                                    }
                                    chosenQuestion = s;
                                    otherQuestionList.remove(s);
                                    List<String> tempList = new ArrayList<String>();
                                    for(String quest: mAddressCategory.keySet()){
                                        if(otherQuestionList.contains(quest)){
                                            tempList.add(quest);
                                        }
                                    }
                                    tempList.add("Question");
                                    otherQuestionList.clear();
                                    otherQuestionList.addAll(tempList);
                                    //mAdapter.notifyDataSetChanged();
                                }else{
                                    mCharLimit = 0;
                                }
                            }
                        }
                    }
                    if(mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                        mText.setFocusableInTouchMode(true);  //if this is not already set
                        mText.requestFocus();  //to move the cursor
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mText, InputMethodManager.SHOW_FORCED);
                        mText.setCursorVisible(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mText = (EditText) findViewById(R.id.item_recovery_answer_edittext);
            final View redRing = findViewById(R.id.item_recovery_answer_redring);
            mText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
//        edittext.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // If the event is a key-down event on the "enter" button
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // Perform action on key press
////                    View newQuestion = getQuestionView();
////                    mQuestionViews.add(newQuestion);
////                    populateQuestionViews();
////                    newQuestion.requestFocus();
//                    return true;
//                }
//                return false;
//            }
//        });

            mText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    try {
                        if (!mText.getText().toString().isEmpty() && mText.getText().toString().length() < mCharLimit) {
                            redRing.setVisibility(View.VISIBLE);
                        } else {
                            redRing.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            mText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        pos = mQuestionViews.indexOf(me);
                        System.out.println(pos);
                    }
                }
            });

            mText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (pos != mQuestionViews.size() - 1) {
                            ((QuestionView) mQuestionViews.get(pos + 1)).getSpinner().performClick();
                            ((QuestionView) mQuestionViews.get(pos + 1)).getSpinner().requestFocus();
                        }else {
                            dummyFocus.requestFocus();
                            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                        return true;
                    }
                    return false;
                }
            });

            mSpinner.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(hasFocus) {
                        int height = mLayoutRecovery.getRootView().getHeight() - mLayoutRecovery.getHeight();
                        if (height > 100) {
                            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    }
                }
            });

            mSpinner.setSelection(mAdapter.getCount());
        }

        public String getQuestion() {
            return ((TextView) mSpinner.getSelectedView()).getText().toString();
        }

        public Spinner getSpinner(){
            return mSpinner;
        }

        public String getText() {
            return mText.getText().toString();
        }

        public int getMinimumCharacters() {
            return mCharLimit;
        }
    }

    @Override
    public void onBackPressed(){
        if(mChangeQuestions)
            super.onBackPressed();
    }

}

