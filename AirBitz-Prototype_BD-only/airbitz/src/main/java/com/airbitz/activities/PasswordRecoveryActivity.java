package com.airbitz.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.airbitz.api.tABC_CC;
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
public class PasswordRecoveryActivity extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    public static final String CHANGE_QUESTIONS = "com.airbitz.passwordrecoveryactivity.change_questions";
    public static final String FORGOT_PASSWORD = "com.airbitz.passwordrecoveryactivity.forgot_password";

    private enum QuestionType { STRING, NUMERIC }
    public final String UNSELECTED_QUESTION = "Question";
    private boolean ignoreSelected = false;

    private View dummyFocus;

    private ImageButton mBackButton;
    private EditText mPasswordEditText;

    private RelativeLayout mLayoutRecovery;
    private LinearLayout mPasswordRecoveryListView;
    private ArrayList<QuestionView> mQuestionViews;

    private GetRecoveryQuestions mFetchAllQuestionsTask;
    private Map<String, Integer> mStringCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> mStringQuestions;
    private Map<String, Integer> mNumericCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> mNumericQuestions;

    private SaveQuestionsTask mSaveQuestionsTask;

    private CoreAPI mCoreAPI;
    private boolean mChangeQuestions;
    private boolean mForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_password_recovery);

        mCoreAPI = CoreAPI.getApi();

        Button mSkipStepButton = (Button) findViewById(R.id.activity_recovery_skip_button);
        mSkipStepButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSkipStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowSkipQuestionsAlert();
            }
        });

        mPasswordEditText = (EditText) findViewById(R.id.activity_password_recovery_password_edittext);
        Button mDoneSignUpButton = (Button) findViewById(R.id.activity_recovery_complete_button);
        mBackButton = (ImageButton) findViewById(R.id.activity_password_recovery_back_button);

        mChangeQuestions = getIntent().getBooleanExtra(CHANGE_QUESTIONS, false);
        mForgotPassword = getIntent().getBooleanExtra(FORGOT_PASSWORD, false);

        if(mChangeQuestions) {
            mSkipStepButton.setVisibility(View.INVISIBLE);
            mPasswordEditText.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.VISIBLE);
            mDoneSignUpButton.setText(getResources().getString(R.string.activity_recovery_complete_button_change_questions));
        } else if(mForgotPassword) {

        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_app));
        this.overridePendingTransition(R.anim.nothing, R.anim.slide_in_from_right);

        mLayoutRecovery = (RelativeLayout) findViewById(R.id.activity_recovery_container_layout);

        mStringQuestions = new ArrayList<String>();
        mNumericQuestions = new ArrayList<String>();

        TextView mTitleTextView = (TextView) findViewById(R.id.activity_recovery_title_textview);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mDoneSignUpButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        dummyFocus = findViewById(R.id.activity_recovery_dummy_focus);

        mDoneSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptSignupOrChange();
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

        mFetchAllQuestionsTask = new GetRecoveryQuestions();
        mFetchAllQuestionsTask.execute((Void) null);
    }

    private void AttemptSignupOrChange() {
        //verify that all six questions have been selected
        boolean allQuestionsSelected = true;
        boolean allAnswersValid = true;
        String questions = "";
        String answers = "";

        if(mChangeQuestions && !mPasswordEditText.getText().toString().equals(AirbitzApplication.getPassword())) {
            ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_error_incorrect_password));
            return;
        }

        int count = 0;
        for (View view : mQuestionViews) {
            QuestionView qaView = (QuestionView) view;
            if (qaView.getSelectedQuestion().equals(getString(R.string.activity_recovery_question_default))) {
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
                questions += qaView.getSelectedQuestion();
                answers += qaView.getText();
            }
            count++;
        }
        if (allQuestionsSelected) {
            if (allAnswersValid) {
                mSaveQuestionsTask = new SaveQuestionsTask(questions, answers);
                mSaveQuestionsTask.execute((Void) null);
            } else {
                ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_answer_questions_alert));
            }
        } else {
            ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_pick_questions_alert));
        }
    }

    private void InitializeQuestionViews() {
        ignoreSelected = true;
        mQuestionViews = new ArrayList<QuestionView>();
        mQuestionViews.add(new QuestionView(this, mStringQuestions, QuestionType.STRING));
        mQuestionViews.add(new QuestionView(this, mStringQuestions, QuestionType.STRING));
        mQuestionViews.add(new QuestionView(this, mStringQuestions,QuestionType.STRING));
        mQuestionViews.add(new QuestionView(this, mStringQuestions, QuestionType.STRING));
        mQuestionViews.add(new QuestionView(this, mNumericQuestions, QuestionType.NUMERIC));
        mQuestionViews.add(new QuestionView(this, mNumericQuestions, QuestionType.NUMERIC));

        mPasswordRecoveryListView.removeAllViews();
        for (View v : mQuestionViews) {
            mPasswordRecoveryListView.addView(v);
        }
        mPasswordRecoveryListView.invalidate();
        ignoreSelected = false;
    }


    /**
     * Represents an asynchronous question fetch task
     */
    public class GetRecoveryQuestions extends AsyncTask<Void, Void, Boolean> {
        CoreAPI.QuestionChoice[] mChoices;

        @Override
        public void onPreExecute() {
            showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mChoices = mCoreAPI.GetQuestionChoices();
                if(mChoices.length > 0) {

                    for(CoreAPI.QuestionChoice choice : mChoices) {
                        String category = choice.getCategory();
                        if(category.equals("string")) {
                            mStringCategory.put(choice.getQuestion(), (int) choice.getMinLength());
                            mStringQuestions.add(choice.getQuestion());
                        } else if(category.equals("numeric")) {
                            mNumericCategory.put(choice.getQuestion(), (int) choice.getMinLength());
                            mNumericQuestions.add(choice.getQuestion());
                        }
                    }
                    mStringQuestions.add(getString(R.string.activity_recovery_question_default));
                    mNumericQuestions.add(getString(R.string.activity_recovery_question_default));
                    return true;
                } else {
                    Common.LogD(TAG, "No Questions");
                    return false;
                }
            }

        @Override
        protected void onPostExecute(final Boolean success) {
            showModalProgress(false);
            mFetchAllQuestionsTask = null;

            if (success) {
                InitializeQuestionViews();
            }

            dummyFocus.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mFetchAllQuestionsTask = null;
        }

    }


    /**
     * Represents an asynchronous question fetch task
     */
    public class GetQuestionChoicesTask extends AsyncTask<Void, Void, Boolean> {
        CoreAPI.QuestionChoice[] mChoices;


        @Override
        public void onPreExecute() {
            showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showModalProgress(false);
            mFetchAllQuestionsTask = null;

            if (success) {
                InitializeQuestionViews();
            }

            dummyFocus.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mFetchAllQuestionsTask = null;
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

        @Override
        public void onPreExecute() {
            showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC result = mCoreAPI.SaveRecoveryAnswers(mQuestions, mAnswers);
            return result == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveQuestionsTask = null;
            showModalProgress(false);
            if (!success) {
                ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_error_save_failed));
            } else {
                ShowMessageAlertAndExit(getResources().getString(R.string.activity_recovery_error_title), getString(R.string.activity_recovery_done_details));
            }
        }

        @Override
        protected void onCancelled() {
            mSaveQuestionsTask = null;
        }
    }

    private List<String> getUnchosenQuestions(List<String> questions) {
        List<String> unusedQuestions = new ArrayList<String>(questions);

        if (!questions.isEmpty()) {
            for (QuestionView qv : mQuestionViews) {
                String selected = qv.getSelectedQuestion();
                if (!selected.equals(UNSELECTED_QUESTION)) {
                    unusedQuestions.remove(selected);
                }
            }
        }

        return unusedQuestions;
    }

    private void updateQuestionLists(QuestionView notThis) {
        ignoreSelected = true;
        for (QuestionView qv : mQuestionViews) {
            if (qv!=notThis) {
                List<String> unchosen;
                if(qv.mType==QuestionType.STRING) {
                    unchosen = getUnchosenQuestions(mStringQuestions);
                } else  {
                    unchosen = getUnchosenQuestions(mNumericQuestions);
                }
                if(!qv.getSelectedQuestion().equals(UNSELECTED_QUESTION)) {
                    unchosen.add(0, qv.getSelectedQuestion());
                }
                qv.setAvailableQuestions(unchosen);
            }
        }
        ignoreSelected = false;
    }

    private class QuestionView extends LinearLayout {
        Context mContext;
        public QuestionType mType;
        public String chosenQuestion = "";
        private int mCharLimit = 0;
        private Spinner mSpinner;
        private EditText mText;
        private PasswordRecoveryAdapter mAdapter;
        private List<String> currentQuestionList;
        int pos;

        private QuestionView me = this;

        public QuestionView(Context context, List<String> questions, QuestionType type) {
            super(context);
            mContext = context;
            mType = type;
            currentQuestionList = questions;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.item_password_recovery, this);
            mSpinner = (Spinner)findViewById(R.id.item_recovery_question_spinner);
            mSpinner.setDropDownWidth((int) getResources().getDimension(R.dimen.spinner_width_password));
            mSpinner.setFocusable(true);
            mSpinner.setFocusableInTouchMode(true);
            mAdapter = new PasswordRecoveryAdapter(context, currentQuestionList);
            mAdapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);
            mSpinner.setAdapter(mAdapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(ignoreSelected) return;

                    if(mType==QuestionType.STRING) {
                        chosenQuestion = getSelectedQuestion();
                        if(mStringCategory.containsKey(chosenQuestion))
                            mCharLimit = mStringCategory.get(chosenQuestion);
                    } else if(mType==QuestionType.NUMERIC){
                        chosenQuestion = getSelectedQuestion();
                        if(mNumericCategory.containsKey(chosenQuestion))
                            mCharLimit = mNumericCategory.get(chosenQuestion);
                    }
                    updateQuestionLists(me);
                    if(mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                        mText.setFocusableInTouchMode(true);  //if this is not already set
                        mText.requestFocus();  //to move the cursor
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mText, InputMethodManager.SHOW_FORCED);
                        mText.setCursorVisible(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            mText = (EditText) findViewById(R.id.item_recovery_answer_edittext);
            final View redRing = findViewById(R.id.item_recovery_answer_redring);
            mText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

            mText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

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
                public void afterTextChanged(Editable editable) { }
            });

            mText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        pos = mQuestionViews.indexOf(me);
                    }
                }
            });

            mText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (pos != mQuestionViews.size() - 1) {
                            (mQuestionViews.get(pos + 1)).getSpinner().performClick();
                            (mQuestionViews.get(pos + 1)).getSpinner().requestFocus();
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
                        view.performClick();
                    }
                }
            });

            chosenQuestion = UNSELECTED_QUESTION;
            mSpinner.setSelection(currentQuestionList.size() - 1);
        }

        public String getSelectedQuestion() {
            return ((TextView) mSpinner.getSelectedView()).getText().toString();
        }

        public void setAvailableQuestions(List<String> questions) {
            if(mAdapter!=null) {
                mAdapter.addQuestions(questions);
                if(questions.contains(chosenQuestion))
                    mSpinner.setSelection(questions.indexOf(chosenQuestion));
            }
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

    public void ShowSkipQuestionsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
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

}

