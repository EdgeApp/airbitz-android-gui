package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.airbitz.activities.NavigationActivity;
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
 */
public class PasswordRecoveryFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public static final String MODE = "com.airbitz.passwordrecovery.type";
    public static int SIGN_UP=0;
    public static int CHANGE_QUESTIONS = 1;
    public static int FORGOT_PASSWORD = 2;

    private enum QuestionType { STRING, NUMERIC }
    public final String UNSELECTED_QUESTION = "Question";


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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_password_recovery, container, false);

        Button mSkipStepButton = (Button) mView.findViewById(R.id.activity_recovery_skip_button);
        mSkipStepButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSkipStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowSkipQuestionsAlert();
            }
        });

        mPasswordEditText = (EditText) mView.findViewById(R.id.activity_password_recovery_password_edittext);
        Button mDoneSignUpButton = (Button) mView.findViewById(R.id.activity_recovery_complete_button);
        mBackButton = (ImageButton) mView.findViewById(R.id.activity_password_recovery_back_button);

        if(getArguments() != null) {
            int type = getArguments().getInt(MODE);
            if(type == CHANGE_QUESTIONS) {
                mSkipStepButton.setVisibility(View.INVISIBLE);
                mPasswordEditText.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                mDoneSignUpButton.setText(getResources().getString(R.string.activity_recovery_complete_button_change_questions));
                mPasswordEditText.requestFocus();
            } else if(mForgotPassword) {
                //TODO set
            }
        }

        mLayoutRecovery = (RelativeLayout) mView.findViewById(R.id.activity_recovery_container_layout);

        mStringQuestions = new ArrayList<String>();
        mNumericQuestions = new ArrayList<String>();

        TextView mTitleTextView = (TextView) mView.findViewById(R.id.activity_recovery_title_textview);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mDoneSignUpButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mDoneSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttemptSignupOrChange();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mPasswordRecoveryListView = (LinearLayout) mView.findViewById(R.id.activity_recovery_question_listview);

        mFetchAllQuestionsTask = new GetRecoveryQuestions();
        mFetchAllQuestionsTask.execute((Void) null);

        return mView;
    }

    private void AttemptSignupOrChange() {
        //verify that all six questions have been selected
        boolean allQuestionsSelected = true;
        boolean allAnswersValid = true;
        String questions = "";
        String answers = "";

        if(mChangeQuestions && !mPasswordEditText.getText().toString().equals(AirbitzApplication.getPassword())) {
            ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_error_incorrect_password));
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
                ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_answer_questions_alert));
            }
        } else {
            ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_pick_questions_alert));
        }
    }

    private void InitializeQuestionViews() {
        mQuestionViews = new ArrayList<QuestionView>();
        int position = 0;
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, QuestionType.STRING, position++));
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, QuestionType.STRING, position++));
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, QuestionType.STRING, position++));
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, QuestionType.STRING, position++));
        mQuestionViews.add(new QuestionView(getActivity(), mNumericQuestions, QuestionType.NUMERIC, position++));
        mQuestionViews.add(new QuestionView(getActivity(), mNumericQuestions, QuestionType.NUMERIC, position++));

        mPasswordRecoveryListView.removeAllViews();
        for (View v : mQuestionViews) {
            mPasswordRecoveryListView.addView(v);
        }
        mPasswordRecoveryListView.invalidate();
    }


    /**
     * Represents an asynchronous question fetch task
     */
    public class GetRecoveryQuestions extends AsyncTask<Void, Void, Boolean> {
        CoreAPI.QuestionChoice[] mChoices;

        @Override
        public void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
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
            ((NavigationActivity)getActivity()).showModalProgress(false);

            if (success) {
                InitializeQuestionViews();
            }

            mPasswordEditText.requestFocus();
            mFetchAllQuestionsTask = null;
        }

        @Override
        protected void onCancelled() {
            mFetchAllQuestionsTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
        }

    }


    /**
     * Represents an asynchronous question fetch task
     */
    public class GetQuestionChoicesTask extends AsyncTask<Void, Void, Boolean> {
        CoreAPI.QuestionChoice[] mChoices;


        @Override
        public void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            ((NavigationActivity)getActivity()).showModalProgress(false);
            mFetchAllQuestionsTask = null;

            if (success) {
                InitializeQuestionViews();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchAllQuestionsTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
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
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC result = mCoreAPI.SaveRecoveryAnswers(mQuestions, mAnswers);
            return result == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveQuestionsTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
            if (!success) {
                ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_error_save_failed));
            } else {
                ((NavigationActivity)getActivity()).ShowMessageDialogBackPress(getResources().getString(R.string.activity_recovery_error_title), getString(R.string.activity_recovery_done_details));
            }
        }

        @Override
        protected void onCancelled() {
            mSaveQuestionsTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
        }
    }

    private List<String> getUnchosenQuestions(List<String> questions) {
        List<String> unusedQuestions = new ArrayList<String>(questions);

            for (QuestionView qv : mQuestionViews) {
                String selected = qv.chosenQuestion;
                if (!selected.equals(UNSELECTED_QUESTION)) {
                    unusedQuestions.remove(selected);
                }
            }

        return unusedQuestions;
    }

    private void updateQuestionLists(QuestionView notThis) {
        for (QuestionView qv : mQuestionViews) {
            if (qv!=notThis) {
                List<String> unchosen;
                if(qv.mType== QuestionType.STRING) {
                    unchosen = getUnchosenQuestions(mStringQuestions);
                } else  {
                    unchosen = getUnchosenQuestions(mNumericQuestions);
                }
                qv.setAvailableQuestions(unchosen);

                if(!qv.chosenQuestion.equals(UNSELECTED_QUESTION)) {
                    unchosen.add(0, qv.chosenQuestion);
                    qv.mSpinner.setSelection(0);
                } else {
                    qv.setAvailableQuestions(unchosen);
                    int index = unchosen.indexOf(UNSELECTED_QUESTION);
                    qv.mSpinner.setSelection(index);
                }
            }
        }
    }



    boolean ignoreSelected = false;
    private class QuestionView extends LinearLayout {
        Context mContext;
        public QuestionType mType;
        public String chosenQuestion = "";
        private int mCharLimit = 0;
        private Spinner mSpinner;
        private EditText mText;
        private PasswordRecoveryAdapter mAdapter;
        private List<String> currentQuestionList;
        int mPosition;

        private QuestionView me = this;

        public QuestionView(Context context, List<String> questions, QuestionType type, int position) {
            super(context);
            mContext = context;
            mType = type;
            mPosition = position;
            currentQuestionList = questions;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.item_password_recovery, this);

            mText = (EditText) findViewById(R.id.item_recovery_answer_edittext);
            final View redRing = findViewById(R.id.item_recovery_answer_redring);
            mText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
            mText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

            mSpinner = (Spinner) findViewById(R.id.item_recovery_question_spinner);
            mSpinner.setFocusable(true);
            mSpinner.setFocusableInTouchMode(true);
            mAdapter = new PasswordRecoveryAdapter(context, currentQuestionList);
            mAdapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);
            mSpinner.setAdapter(mAdapter);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Common.LogD(TAG, "spinner selection");
                    if (ignoreSelected) return;

                    chosenQuestion = currentQuestionList.get(i);
                    Common.LogD(TAG, "spinner selection not ignored="+chosenQuestion);
                    if (mType == QuestionType.STRING) {
                        if (mStringCategory.containsKey(chosenQuestion))
                            mCharLimit = mStringCategory.get(chosenQuestion);
                    } else if (mType == QuestionType.NUMERIC) {
                        if (mNumericCategory.containsKey(chosenQuestion))
                            mCharLimit = mNumericCategory.get(chosenQuestion);
                    }

                    if (mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                        QuestionView qv = mQuestionViews.get(mPosition);
                        qv.mText.setFocusableInTouchMode(true);
                        qv.mText.requestFocus();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            mSpinner.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        ignoreSelected = false;
                        mSpinner.performClick();
                    } else {
                        ignoreSelected = true;
                        updateQuestionLists(me);
                    }
                }
            });

            mText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        ((NavigationActivity)getActivity()).showSoftKeyboard(mText);
                    }
                }
            });


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

            mText.setOnEditorActionListener(new TextView.OnEditorActionListener()  {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        if (mPosition < mQuestionViews.size() - 1) {
                            mQuestionViews.get(mPosition + 1).getSpinner().requestFocus();
                            return true;
                        } else if(mPosition == mQuestionViews.size()-1) {
                            ((NavigationActivity)getActivity()).hideSoftKeyboard(mText);
                            return true;
                        }
                    }
                    return false;
                }
            });

            chosenQuestion = UNSELECTED_QUESTION;
            mSpinner.setSelection(currentQuestionList.size() - 1);
        }

        public String getSelectedQuestion() {
            return chosenQuestion;
        }

        public void setAvailableQuestions(List<String> questions) {
            if (mAdapter != null) {
                currentQuestionList = questions;
                mAdapter = new PasswordRecoveryAdapter(mContext, currentQuestionList);
                mAdapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);
                mSpinner.setAdapter(mAdapter);
            }
        }

        public Spinner getSpinner() {
            return mSpinner;
        }

        public String getText() {
            return mText.getText().toString();
        }

        public int getMinimumCharacters() {
            return mCharLimit;
        }
    }

    public void ShowSkipQuestionsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((NavigationActivity)getActivity()).popFragment();
                        ((NavigationActivity)getActivity()).showNavBar();
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

