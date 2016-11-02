/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted provided that
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.fragments.settings;

import android.accounts.AccountManager;
import android.app.ActionBar;
;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.QuestionChoice;
import co.airbitz.core.Settings;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.PasswordRecoveryAdapter;
import com.airbitz.api.CoreWrapper;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.login.SignUpFragment;
import com.airbitz.fragments.settings.twofactor.TwoFactorMenuFragment;
import com.airbitz.models.Contact;
import com.airbitz.objects.MinEditText;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class PasswordRecoveryFragment extends BaseFragment implements
        NavigationActivity.OnBackPress,
        TwoFactorMenuFragment.OnTwoFactorMenuResult {
    public static final String MODE = "com.airbitz.passwordrecovery.mode";
    public static final String TYPE = "com.airbitz.passwordrecovery.type";
    public static final String TOKEN = "com.airbitz.passwordrecovery.token";
    public static final String QUESTIONS = "com.airbitz.passwordrecovery.questions";
    public static final String USERNAME = "com.airbitz.passwordrecovery.username";
    public static final String PASSWORD = "com.airbitz.passwordrecovery.password";
    public static final String PIN = "com.airbitz.passwordrecovery.pin";
    public static int SIGN_UP = 0;
    public static int CHANGE_QUESTIONS = 1;
    public static int FORGOT_PASSWORD = 2;
    public static int RECOVERY_TYPE_1 = 0;
    public static int RECOVERY_TYPE_2 = 1;
    private static final int EMAIL_INTENT_REQUEST_CODE = 0xe3a11;
    private static final int REQUEST_CODE_EMAIL = 0x47562fed;

    private final String TAG = getClass().getSimpleName();
    String mAnswers = "";
    String mQuestions = "";
    boolean ignoreSelected = false;
    private int mMode;
    private String mRecoveryToken = "";
    private int mType;
    private boolean mReturnFromTwoFactorScan = false;
    private boolean mTwoFactorSuccess = false;
    private String mTwoFactorSecret;
    private EditText mPasswordEditText;
    private Button mDoneSignUpButton;

    private LinearLayout mPasswordRecoveryListView;
    private ArrayList<QuestionView> mQuestionViews;

    private GetRecoveryQuestions mFetchAllQuestionsTask;
    private AttemptAnswerVerificationTask mAttemptAnswerVerificationTask;
    private SaveQuestionsTask mSaveQuestionsTask;

    private Map<String, Integer> mStringCategory = new HashMap<String, Integer>(); // Question, MinLength
    private List<String> mStringQuestions;
    private boolean mSaved = false;

    private AirbitzCore mCoreAPI;
    private Account mAccount;
    private NavigationActivity mActivity;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = AirbitzCore.getApi();
        mAccount = AirbitzApplication.getAccount();
        mActivity = (NavigationActivity) getActivity();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        mView = i.inflate(R.layout.fragment_password_recovery, container, false);

        mPasswordEditText = (EditText) mView.findViewById(R.id.activity_password_recovery_password_edittext);
        mPasswordEditText.setTypeface(Typeface.DEFAULT);
        mDoneSignUpButton = (Button) mView.findViewById(R.id.activity_recovery_complete_button);

        if (getArguments() != null) {
            mMode = getArguments().getInt(MODE);
            mType = getArguments().getInt(TYPE);
            mRecoveryToken = getArguments().getString(TOKEN);
            if (mMode == CHANGE_QUESTIONS) {
                mPasswordEditText.setVisibility(mAccount.passwordExists() ? View.VISIBLE : View.GONE);
                mDoneSignUpButton.setText(getResources().getString(R.string.activity_recovery_complete_button_change_questions));
                String recoveryToken = null;

                try {
                    recoveryToken = mCoreAPI.getRecovery2Token(mAccount.username());
                } catch (AirbitzException e) {

                }

                if (recoveryToken != null) {
                    new MaterialDialog.Builder(mActivity)
                            .title(R.string.disable_recovery_popup_title)
                            .content(R.string.disable_recovery_popup_message)
                            .positiveText(R.string.string_disable)
                            .negativeText(R.string.string_cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    try {
                                        mAccount.disableRecovery2();
                                        mActivity.ShowFadingDialog(getResources().getString(R.string.recovery_disabled));
                                    } catch (AirbitzException e) {
                                        mActivity.ShowFadingDialog(getResources().getString(R.string.error_disabling_recovery));
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })

                            .show();

                }
            } else if (mMode == FORGOT_PASSWORD) {
                mPasswordEditText.setVisibility(View.GONE);
                mDoneSignUpButton.setText(getResources().getString(R.string.string_done));
            } else {
                // defaults for signup
            }
        }

        mStringQuestions = new ArrayList<String>();

        mDoneSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignupOrChange(null);
            }
        });

        mPasswordRecoveryListView = (LinearLayout) mView.findViewById(R.id.activity_recovery_question_listview);
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPress();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public String getTitle() {
        if (mMode == SIGN_UP || mMode == CHANGE_QUESTIONS) {
            return mActivity.getString(R.string.activity_recovery_title);
        } else {
            return mActivity.getString(R.string.activity_recovery_title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String[] answers = {"", "", "", "", "", ""};
        if (mAnswers.isEmpty()) {
            if (mMode == SIGN_UP || mMode == CHANGE_QUESTIONS) {
                mFetchAllQuestionsTask = new GetRecoveryQuestions();
                mFetchAllQuestionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            } else {
                String[] questions = getArguments().getStringArray(QUESTIONS);
                if (questions != null) {
                    InitializeRecoveryViews(questions, answers);
                }
            }
        } else { // coming back from signup page
            answers = mAnswers.split("\n");
            String[] questions = getArguments().getStringArray(QUESTIONS);
            if (questions != null) {
                InitializeRecoveryViews(questions, answers);
            }
        }
        if(mReturnFromTwoFactorScan) {
            mReturnFromTwoFactorScan = false;
            mActivity.hideSoftKeyboard(mQuestionViews.get(0));
            mActivity.showModalProgress(true);
            if (mTwoFactorSuccess) {
                attemptSignupOrChange(mTwoFactorSecret);
            } else {
                mActivity.ShowOkMessageDialog(getString(R.string.fragment_two_factor_scan_unable_import_title),
                        getString(R.string.twofactor_unable_import_token));
            }
        }
    }

    @Override
    public boolean onBackPress() {
        boolean dirty = false;
        if(mQuestionViews != null) {
            for (View view : mQuestionViews) {
                QuestionView qaView = (QuestionView) view;
                if (!qaView.getSelectedQuestion().equals(getString(R.string.activity_recovery_question_default)) ||
                        !qaView.getText().isEmpty()) {
                    dirty = true;
                    break;
                }
            }
        }

        if (dirty && !mSaved) {
            String message;
            String title;
            if(mMode == CHANGE_QUESTIONS) {
                message = getString(R.string.activity_recovery_warning_dirty_change_message);
                title = getString(R.string.activity_recovery_warning_dirty_change_title);
            }
            else if (mMode == FORGOT_PASSWORD) {
                message = getString(R.string.activity_recovery_warning_dirty_forgot_message);
                title = getString(R.string.activity_recovery_warning_dirty_forgot_title);
            }
            else {
                return true;
            }

            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
            builder.setMessage(message)
                    .setTitle(title)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.string_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    mActivity.hideSoftKeyboard(getView());
                                    if (mMode == CHANGE_QUESTIONS) {
                                        mActivity.popFragment();
                                    } else if (mMode == FORGOT_PASSWORD) {
                                        mActivity.popFragment();
                                        mActivity.showNavBar();
                                        mActivity.Logout();
                                    }
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.string_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }
                    );
            builder.show();
        } else {
            mActivity.hideSoftKeyboard(getView());
            if (mMode == CHANGE_QUESTIONS) {
                mActivity.popFragment();
            } else if (mMode == FORGOT_PASSWORD) {
                mActivity.popFragment();
                mActivity.showNavBar();
                mActivity.Logout();
            }
        }

        return true;
    }

    private void attemptSignupOrChange(String token) {
        //verify that all six questions have been selected
        boolean allQuestionsSelected = true;
        boolean allAnswersValid = true;
        mQuestions = "";
        mAnswers = "";

        if (mMode == CHANGE_QUESTIONS && !mAccount.checkPassword(mPasswordEditText.getText().toString())) {
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_recovery_error_incorrect_password));
            return;
        }

        int count = 0;
        for (View view : mQuestionViews) {
            QuestionView qaView = (QuestionView) view;
            if (mMode != FORGOT_PASSWORD && qaView.getSelectedQuestion().equals(getString(R.string.activity_recovery_question_default))) {
                allQuestionsSelected = false;
                break;
            }
            //verify that all six answers have achieved their minimum character limit
            if (qaView.getText().length() < qaView.getMinimumCharacters() || qaView.getText().isEmpty()) {
                allAnswersValid = false;
            } else {
                //add question and answer to arrays
                if (count != 0) {
                    mQuestions += "\n";
                    mAnswers += "\n";
                }
                mQuestions += qaView.getSelectedQuestion();
                mAnswers += qaView.getText();
            }
            count++;
        }
        if (allQuestionsSelected) {
            if (allAnswersValid) {
                if (mMode == SIGN_UP) {
                    signIn();
                }
                if (mMode == FORGOT_PASSWORD) {
                    mAttemptAnswerVerificationTask = new AttemptAnswerVerificationTask();
                    mAttemptAnswerVerificationTask.execute(mAnswers, getArguments().getString(USERNAME), mRecoveryToken);
                } else {
                    attemptCommitQuestions();
                }
            } else {
                mActivity.ShowFadingDialog(getResources().getString(R.string.activity_recovery_answer_questions_alert));
            }
        } else {
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_recovery_pick_questions_alert));
        }
    }

    private void attemptCommitQuestions() {
        if (mMode == CHANGE_QUESTIONS) {
            String password = mPasswordEditText.getText().toString();
            if (!mAccount.passwordExists() && !mAccount.checkPassword(password)) {
                mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_recovery_mismatch_title),
                        getResources().getString(R.string.fragment_recovery_mismatch_message));
                return;
            }
        }
        mSaveQuestionsTask = new SaveQuestionsTask(mQuestions, mAnswers);
        mSaveQuestionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }


    private void InitializeQuestionViews() {
        mQuestionViews = new ArrayList<QuestionView>();
        int position = 0;
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, "", position++));
        mQuestionViews.add(new QuestionView(getActivity(), mStringQuestions, "", position++));

        setListWithQuestionViews(mQuestionViews);
    }

    private void setListWithQuestionViews(List<QuestionView> views) {
        mPasswordRecoveryListView.removeAllViews();
        for (View v : views) {
            mPasswordRecoveryListView.addView(v);
        }
        mPasswordRecoveryListView.invalidate();
        mQuestionViews.get(0).getEditText().requestFocus();
    }

    private void InitializeRecoveryViews(String[] questions, String[] answers) {
        mQuestionViews = new ArrayList<QuestionView>();
        int position = 0;
        for (String question : questions) {
            List<String> qs = new ArrayList<String>();
            qs.add(question);
            qs.add(getString(R.string.activity_recovery_question_default));
            QuestionView qv = new QuestionView(getActivity(), qs, answers[position], position++);
            mQuestionViews.add(qv);
        }

        setListWithQuestionViews(mQuestionViews);
    }

    private List<String> getUnchosenQuestions(List<String> questions) {
        List<String> unusedQuestions = new ArrayList<String>(questions);

        for (QuestionView qv : mQuestionViews) {
            String selected = qv.chosenQuestion;
            if (!selected.equals(getString(R.string.activity_recovery_question_default))) {
                unusedQuestions.remove(selected);
            }
        }

        return unusedQuestions;
    }

    private void updateQuestionLists(QuestionView notThis) {
        for (QuestionView qv : mQuestionViews) {
            if (qv != notThis) {
                List<String> unchosen;
                unchosen = getUnchosenQuestions(mStringQuestions);
                qv.setAvailableQuestions(unchosen);

                if (!qv.chosenQuestion.equals(getString(R.string.activity_recovery_question_default))) {
                    unchosen.add(0, qv.chosenQuestion);
                    qv.mSpinner.setSelection(0);
                } else {
                    qv.setAvailableQuestions(unchosen);
                    int index = unchosen.indexOf(getString(R.string.activity_recovery_question_default));
                    qv.mSpinner.setSelection(index);
                }
            }
        }
    }

    private void signIn() {
    }

    public void ShowSkipQuestionsAlert() {
        AlertDialogWrapper.Builder alertDialogBuilder = new AlertDialogWrapper.Builder(getActivity() /*new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom)*/);
        alertDialogBuilder.setTitle(getResources().getString(R.string.activity_recovery_prompt_title))
                .setMessage(getResources().getString(R.string.activity_recovery_prompt_skip))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signIn();
                        mActivity.popFragment();
                        mActivity.finishSignup();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.string_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Dialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFetchAllQuestionsTask != null) {
            mFetchAllQuestionsTask.cancel(true);
        }
        if (mSaveQuestionsTask != null) {
            mSaveQuestionsTask.cancel(true);
        }
        if (mAttemptAnswerVerificationTask != null) {
            mAttemptAnswerVerificationTask.cancel(true);
        }
    }

    /**
     * Attempt to verify answers
     */
    public class AttemptAnswerVerificationTask extends AsyncTask<String, Void, Boolean> {
        private String username;
        private String answers;
        private String recoveryToken;
        private AirbitzException mFailureException;

        @Override
        public void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            answers = params[0];
            username = params[1];
            recoveryToken = params[2];
            boolean result = false;
            try {
                Account account;
                if (mType == RECOVERY_TYPE_2) {
                    account = mCoreAPI.loginWithRecovery2(username, answers.split("\n"), recoveryToken, mTwoFactorSecret);
                } else {
                    account = mCoreAPI.recoveryLogin(username, answers.split("\n"), mTwoFactorSecret);
                }
                if (account == null)
                    return false;
                AirbitzApplication.Login(account);
                result = true;
            } catch (AirbitzException e) {
                mFailureException = e;
                return false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);

            if (success) {
                mActivity.ShowFadingDialog(getString(R.string.recovery_successful));

                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PASSWORD_VIA_QUESTIONS);
                bundle.putString(PasswordRecoveryFragment.QUESTIONS, answers);
                bundle.putString(PasswordRecoveryFragment.USERNAME, getArguments().getString(USERNAME));
                Fragment frag = new SignUpFragment();
                frag.setArguments(bundle);
                mActivity.pushFragmentNoAnimation(frag, NavigationActivity.Tabs.BD.ordinal());
            } else {
                if (mFailureException != null && mFailureException.isOtpError()) {
                    launchTwoFactorMenu();
                } else {
                    mActivity.ShowFadingDialog(getString(R.string.activity_recovery_error_wrong_answers_message));
                }
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.showModalProgress(false);
        }
    }

    private void launchTwoFactorMenu() {
        TwoFactorMenuFragment fragment = new TwoFactorMenuFragment();
        fragment.setOnTwoFactorMenuResult(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorMenuFragment.STORE_SECRET, false);
        bundle.putBoolean(TwoFactorMenuFragment.TEST_SECRET, false);
        bundle.putString(TwoFactorMenuFragment.USERNAME, getArguments().getString(USERNAME));
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
        mActivity.DisplayLoginOverlay(false);
    }

    @Override
    public void onTwoFactorMenuResult(boolean success, String secret) {
        // This occurs before view is shown, so pickup in onResume
        mReturnFromTwoFactorScan = true;
        mTwoFactorSecret = secret;
        mTwoFactorSuccess = success;
    }

    /**
     * Represents an asynchronous question fetch task
     */
    public class GetRecoveryQuestions extends AsyncTask<Void, Void, Boolean> {
        QuestionChoice[] mChoices;

        @Override
        public void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mChoices = mCoreAPI.recoveryQuestionChoices();
            if (mChoices.length > 0) {

                for (QuestionChoice choice : mChoices) {
                    String category = choice.category();
                    if (category.equals("recovery2")) {
                        mStringCategory.put(choice.question(), (int) choice.minLength());
                        mStringQuestions.add(choice.question());
                    }
                }
                mStringQuestions.add(getString(R.string.activity_recovery_question_default));
                return true;
            } else {
                AirbitzCore.logi("No Questions");
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);

            if (success) {
                InitializeQuestionViews();
            }

            if (mMode == CHANGE_QUESTIONS)
                mPasswordEditText.requestFocus();

            mFetchAllQuestionsTask = null;
        }

        @Override
        protected void onCancelled() {
            mFetchAllQuestionsTask = null;
            mActivity.showModalProgress(false);
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
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (mType == RECOVERY_TYPE_1) {
                    mAccount.recoverySetup(mQuestions.split("\n"), mAnswers.split("\n"));
                } else if (mType == RECOVERY_TYPE_2) {
                    mRecoveryToken = mAccount.setupRecovery2Questions(mQuestions.split("\n"), mAnswers.split("\n"));
                }

                if (mRecoveryToken == null) {
                    AirbitzCore.logi("PasswordRecoveryFragment SaveRecoveryAnswers error");
                    return false;
                }
                if (mRecoveryToken.length() < 20) {
                    AirbitzCore.logi("PasswordRecoveryFragment SaveRecoveryAnswers error");
                    return false;
                }
                return true;
            } catch (AirbitzException e) {
                AirbitzCore.logi("PasswordRecoveryFragment SaveRecoveryAnswers error");
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveQuestionsTask = null;
            mActivity.showModalProgress(false);
            if (!success) {
                mActivity.ShowFadingDialog(getResources().getString(R.string.activity_recovery_error_save_failed));
            } else {
                if (mType == RECOVERY_TYPE_1) {
                    mSaved = true;
                    mActivity.ShowMessageDialogBackPress(getResources().getString(R.string.activity_recovery_done_title), getString(R.string.activity_recovery_done_details));
                    CoreWrapper.clearRecoveryReminder(mAccount);
                    mActivity.popFragment();
                } else if (mType == RECOVERY_TYPE_2) {
                    // Launch popup to ask user to email themselves the token
                    launchSaveTokenAlert(getResources().getString(R.string.save_recovery_token_popup));
                }
            }
        }

        @Override
        protected void onCancelled() {
            mSaveQuestionsTask = null;
            mActivity.showModalProgress(false);
        }
    }

    public void launchSaveTokenAlert(String title) {

        String email = mAccount.data("ABPersonalInfo").get("email");

        if (email == null || email.length() == 0)
            email = getEmail();

        new MaterialDialog.Builder(mActivity)
                .title(title)
                .content(R.string.save_recovery_token_popup_message)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText(R.string.string_next)
                .negativeText(R.string.string_cancel)
                .input("", email, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        if (isEmailValid(input.toString())) {
                            // Save email in dataStore for use later
                            mAccount.data("ABPersonalInfo").set("email",input.toString());
                            sendEmail(input.toString());
                            dialog.dismiss();
                        } else {
                            launchSaveTokenAlert(getResources().getString(R.string.invalid_email));
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })

                .show();
    }

    private String getEmail() {
        AccountManager aManager = AccountManager.get(getActivity().getApplicationContext());
        android.accounts.Account[] accounts = aManager.getAccountsByType("com.google");

        String accountId = "";
        for (android.accounts.Account account : accounts) {
            accountId = account.name;
            break;
        }
        return accountId;
    }

    private void sendEmail(String emailAddress) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT,
                String.format(getString(R.string.recovery_token_email_subject),
                        getString(R.string.app_name)));

        String recoveryUrl = String.format("iOS<br>\n<a href=\"%1$s://recovery?token=%2$s\">%3$s://recovery?token=%4$s</a><br><br>\n",
                "airbitz", mRecoveryToken, "airbitz", mRecoveryToken);

        recoveryUrl = recoveryUrl + String.format("Android<br>\n<a href=\"https://recovery.airbitz.co/recovery?token=%1$s\">https://recovery.airbitz.co/recovery?token=%2$s</a>", mRecoveryToken, mRecoveryToken);

        String obfuscatedUsername = obfuscateString(mAccount.username());

        String body = String.format(getString(R.string.recovery_token_email_body),
                getString(R.string.app_name), obfuscatedUsername, recoveryUrl);

        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));

        startActivityForResult(Intent.createChooser(intent, "email"), EMAIL_INTENT_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EMAIL_INTENT_REQUEST_CODE) {
            mSaved = true;
            mActivity.ShowMessageDialogBackPress(getResources().getString(R.string.activity_recovery_done_title), getString(R.string.activity_recovery_done_details));
            CoreWrapper.clearRecoveryReminder(mAccount);
            mActivity.popFragment();
        }
    }

    private String obfuscateString(String str) {

        String obfuscatedStr = str;

        int strLen = str.length();
        if (strLen <= 3)
        {
            obfuscatedStr = str.substring(0, strLen - 1) + "*";
        }
        else if(strLen <= 6)
        {
            obfuscatedStr = str.substring(0, strLen - 2) + "**";
        }
        else if(strLen <= 9)
        {
            obfuscatedStr = str.substring(0, strLen - 3) + "***";
        }
        else if(strLen <= 12)
        {
            obfuscatedStr = str.substring(0, strLen - 4) + "****";
        }
        else
        {
            obfuscatedStr = str.substring(0, strLen - 5) + "*****";
        }
        return obfuscatedStr;
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,16}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private class QuestionView extends LinearLayout {
        public String chosenQuestion = "";
        Context mContext;
        int mPosition;
        private int mCharLimit = 0;
        private Spinner mSpinner;
        private MinEditText mText;
        private PasswordRecoveryAdapter mAdapter;
        private List<String> currentQuestionList;
        private QuestionView me = this;

        public QuestionView(Context context, List<String> questions, String answer, int position) {
            super(context);
            mContext = context;
            mPosition = position;
            currentQuestionList = questions;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.item_password_recovery, this);

            mText = (MinEditText) findViewById(R.id.item_recovery_answer_edittext);
            mText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

            mText.setText(answer);

            mSpinner = (Spinner) findViewById(R.id.item_recovery_question_spinner);
            mSpinner.setFocusable(true);
            mSpinner.setFocusableInTouchMode(true);
            mAdapter = new PasswordRecoveryAdapter(context, currentQuestionList);
            mAdapter.setDropDownViewResource(R.layout.item_password_recovery_spinner_dropdown);
            mSpinner.setAdapter(mAdapter);

            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    AirbitzCore.logi("spinner selection");
                    if (ignoreSelected || mMode == FORGOT_PASSWORD) return;

                    chosenQuestion = currentQuestionList.get(i);
                    AirbitzCore.logi("spinner selection not ignored=" + chosenQuestion);
                    if (mStringCategory.containsKey(chosenQuestion))
                        mCharLimit = mStringCategory.get(chosenQuestion);
                    mText.setMinLength(mCharLimit);

                    if (mSpinner.getSelectedItemPosition() != mAdapter.getCount()) {
                        QuestionView qv = mQuestionViews.get(mPosition);
                        qv.mText.setFocusableInTouchMode(true);
                        qv.mText.requestFocus();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
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
                        mActivity.showSoftKeyboard(mText);
                    }
                }
            });

            mText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        if (mMode == SIGN_UP || mMode == CHANGE_QUESTIONS) {
                            if (mPosition < mQuestionViews.size() - 1) {
                                mQuestionViews.get(mPosition + 1).getSpinner().requestFocus();
                                return true;
                            } else if (mPosition == mQuestionViews.size() - 1) {
                                mActivity.hideSoftKeyboard(mText);
                                mDoneSignUpButton.requestFocus();
                                return true;
                            }
                        } else {
                            if (mPosition < mQuestionViews.size() - 1) {
                                mQuestionViews.get(mPosition + 1).getEditText().requestFocus();
                                return true;
                            } else if (mPosition == mQuestionViews.size() - 1) {
                                mActivity.hideSoftKeyboard(mText);
                                mDoneSignUpButton.requestFocus();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

            chosenQuestion = getString(R.string.activity_recovery_question_default);
            if (mMode == FORGOT_PASSWORD) {
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

        public EditText getEditText() {
            return mText;
        }

        public void setAnswer(String answer) {
            mText.setText(answer);
        }

        public String getText() {
            return mText.getText().toString();
        }

        public int getMinimumCharacters() {
            return mCharLimit;
        }
    }
}

