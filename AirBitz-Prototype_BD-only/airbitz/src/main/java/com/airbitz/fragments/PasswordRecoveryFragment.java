package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
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
public class PasswordRecoveryFragment extends Fragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    public static final String MODE = "com.airbitz.passwordrecovery.type";
    public static final String ANSWERS = "com.airbitz.passwordrecovery.questions";
    public static final String USERNAME = "com.airbitz.passwordrecovery.username";
    public static final String PASSWORD = "com.airbitz.passwordrecovery.password";
    public static final String PIN = "com.airbitz.passwordrecovery.pin";

    public static int SIGN_UP=0;
    public static int CHANGE_QUESTIONS = 1;
    public static int FORGOT_PASSWORD = 2;
    private int mMode;

    private enum QuestionType { STRING, NUMERIC }
    public final String UNSELECTED_QUESTION = "Question";

    private ImageButton mBackButton;
    private EditText mPasswordEditText;
    private TextView mTitleTextView;
    private Button mDoneSignUpButton;
    private Button mSkipStepButton;

    private LinearLayout mPasswordRecoveryListView;
    private ArrayList<QuestionView> mQuestionViews;

    private GetRecoveryQuestions mFetchAllQuestionsTask;
    private Map<String, Integer> mStringCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> mStringQuestions;
    private Map<String, Integer> mNumericCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> mNumericQuestions;

    private SaveQuestionsTask mSaveQuestionsTask;

    private CoreAPI mCoreAPI;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
             mView = inflater.inflate(R.layout.fragment_password_recovery, container, false);
        } else {

            return mView;
        }

        mSkipStepButton = (Button) mView.findViewById(R.id.activity_recovery_skip_button);
        mSkipStepButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSkipStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowSkipQuestionsAlert();
            }
        });

        mPasswordEditText = (EditText) mView.findViewById(R.id.activity_password_recovery_password_edittext);
        mDoneSignUpButton = (Button) mView.findViewById(R.id.activity_recovery_complete_button);
        mBackButton = (ImageButton) mView.findViewById(R.id.activity_password_recovery_back_button);

        if(getArguments() != null) {
            mMode = getArguments().getInt(MODE);
            if(mMode == CHANGE_QUESTIONS) {
                mSkipStepButton.setVisibility(View.GONE);
                mPasswordEditText.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                mDoneSignUpButton.setText(getResources().getString(R.string.activity_recovery_complete_button_change_questions));
            } else if(mMode == FORGOT_PASSWORD) {
                mSkipStepButton.setVisibility(View.INVISIBLE);
                mPasswordEditText.setVisibility(View.GONE);
                mBackButton.setVisibility(View.VISIBLE);
                mDoneSignUpButton.setText(getResources().getString(R.string.string_done));
            } else {
                // defaults for signup
            }
        }

        mStringQuestions = new ArrayList<String>();
        mNumericQuestions = new ArrayList<String>();

        mTitleTextView = (TextView) mView.findViewById(R.id.activity_recovery_title_textview);

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

        if(mMode==SIGN_UP || mMode==CHANGE_QUESTIONS) {
            mFetchAllQuestionsTask = new GetRecoveryQuestions();
            mFetchAllQuestionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            mTitleTextView.setText(getString(R.string.activity_recovery_title));
            String answers = getArguments().getString(ANSWERS);
            if(answers!=null) {
                String[] choices = answers.split("\n");
                InitializeRecoveryViews(choices);
            }
        }

        return mView;
    }

    @Override
    public boolean onBackPress() {
        ((NavigationActivity)getActivity()).hideSoftKeyboard(getView());
        if(mMode==CHANGE_QUESTIONS) {
            ((NavigationActivity) getActivity()).popFragment();
        } else if(mMode == FORGOT_PASSWORD) {
            ((NavigationActivity) getActivity()).Logout();
        }
        return true;
    }

    private void AttemptSignupOrChange() {
        //verify that all six questions have been selected
        boolean allQuestionsSelected = true;
        boolean allAnswersValid = true;
        String questions = "";
        String answers = "";

        if(mMode ==CHANGE_QUESTIONS && !mPasswordEditText.getText().toString().equals(AirbitzApplication.getPassword())) {
            ((NavigationActivity)getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_error_title), getResources().getString(R.string.activity_recovery_error_incorrect_password));
            return;
        }

        int count = 0;
        for (View view : mQuestionViews) {
            QuestionView qaView = (QuestionView) view;
            if (mMode!=FORGOT_PASSWORD && qaView.getSelectedQuestion().equals(getString(R.string.activity_recovery_question_default))) {
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
        if (allQuestionsSelected || mMode==FORGOT_PASSWORD) {
            if (allAnswersValid) {
                if(mMode==SIGN_UP) {
                    signIn();
                }
                if(mMode==FORGOT_PASSWORD) {
                    AttemptAnswerVerificationTask task = new AttemptAnswerVerificationTask();
                    task.execute(answers, getArguments().getString(USERNAME));
                } else {
                    mSaveQuestionsTask = new SaveQuestionsTask(questions, answers);
                    mSaveQuestionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
                }
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

    private void InitializeRecoveryViews(String[] questions) {
        mQuestionViews = new ArrayList<QuestionView>();
        int position = 0;
        for(String question : questions) {
            List<String> qs = new ArrayList<String>();
            qs.add(question);
            qs.add(UNSELECTED_QUESTION);
            QuestionView qv = new QuestionView(getActivity(), qs, QuestionType.STRING, position++);
            mQuestionViews.add(qv);
        }

        mPasswordRecoveryListView.removeAllViews();
        for (View v : mQuestionViews) {
            mPasswordRecoveryListView.addView(v);
        }

        mQuestionViews.get(0).getEditText().requestFocus();
    }

    /**
     * Attempt to verify answers
     */
    public class AttemptAnswerVerificationTask extends AsyncTask<String, Void, Boolean> {
        private String username;
        private String answers;
        @Override
        public void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            answers = params[0];
            username = params[1];
            boolean result = mCoreAPI.recoveryAnswers(answers, username);
            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            ((NavigationActivity)getActivity()).showModalProgress(false);

            if (success) {
                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PASSWORD_VIA_QUESTIONS);
                bundle.putString(PasswordRecoveryFragment.ANSWERS,  answers);
                bundle.putString(PasswordRecoveryFragment.USERNAME,  getArguments().getString(USERNAME));
                Fragment frag = new SignUpFragment();
                frag.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragmentNoAnimation(frag, NavigationActivity.Tabs.BD.ordinal());
            } else {
                ((NavigationActivity)getActivity()).ShowOkMessageDialog("Wrong Answers", "The given answers were incorrect. Please try again.");
            }
        }

        @Override
        protected void onCancelled() { ((NavigationActivity)getActivity()).showModalProgress(false); }
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

            if(mMode==CHANGE_QUESTIONS)
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
     * Save Questions if Signing up or Changing questions
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
                if(mMode==SIGN_UP) {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog(getResources().getString(R.string.activity_recovery_done_title), getString(R.string.activity_recovery_done_details));
                    ((NavigationActivity) getActivity()).UserJustLoggedIn();
                } else if(mMode==CHANGE_QUESTIONS) {
                    ((NavigationActivity) getActivity()).popFragment();
                }
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
                    if (ignoreSelected || mMode==FORGOT_PASSWORD) return;

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
                        if(mMode==SIGN_UP || mMode==CHANGE_QUESTIONS) {
                            if (mPosition < mQuestionViews.size() - 1) {
                                mQuestionViews.get(mPosition + 1).getSpinner().requestFocus();
                                return true;
                            } else if (mPosition == mQuestionViews.size() - 1) {
                                ((NavigationActivity) getActivity()).hideSoftKeyboard(mText);
                                mDoneSignUpButton.requestFocus();
                                return true;
                            }
                        } else {
                            if (mPosition < mQuestionViews.size() - 1) {
                                mQuestionViews.get(mPosition + 1).getEditText().requestFocus();
                                return true;
                            } else if (mPosition == mQuestionViews.size() - 1) {
                                ((NavigationActivity) getActivity()).hideSoftKeyboard(mText);
                                mDoneSignUpButton.requestFocus();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

            chosenQuestion = UNSELECTED_QUESTION;
            if(mMode==FORGOT_PASSWORD) {
                findViewById(R.id.item_recovery_question_down_arrow).setVisibility(View.GONE);
                mSpinner.setSelection(0);
                mSpinner.setClickable(false);
                mSpinner.setEnabled(false);
                mSpinner.setFocusable(false);
            } else {
                mSpinner.setSelection(currentQuestionList.size() - 1);
            }
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

        public EditText getEditText() { return mText; }

        public String getText() {
            return mText.getText().toString();
        }

        public int getMinimumCharacters() {
            return mCharLimit;
        }
    }

    private void signIn() {
        Bundle bundle = getArguments();
        String username = bundle.getString(USERNAME);
        String password = bundle.getString(PASSWORD);
        String pin = bundle.getString(PIN);

        AirbitzApplication.Login(username, password);
        mCoreAPI.SetUserPIN(pin);
        CreateDefaultCategories();
    }

    private void CreateDefaultCategories() {
        String[] defaults = getResources().getStringArray(R.array.category_defaults);

        for(String cat : defaults)
            mCoreAPI.addCategory(cat);

        List<String> cats = mCoreAPI.loadCategories();
        if(cats.size()==0 || cats.get(0).equals(defaults)) {
            Common.LogD(TAG, "Category creation failed");
        }
    }


    public void ShowSkipQuestionsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signIn();
                        ((NavigationActivity)getActivity()).popFragment();
                        ((NavigationActivity)getActivity()).finishSignup();
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

