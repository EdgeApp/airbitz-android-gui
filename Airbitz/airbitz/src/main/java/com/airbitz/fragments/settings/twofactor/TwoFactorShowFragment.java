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

package com.airbitz.fragments.settings.twofactor;

import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.android.AndroidUtils;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.w3c.dom.Text;

public class TwoFactorShowFragment extends BaseFragment
{
    private final String TAG = getClass().getSimpleName();

    private Button mImportButton, mApproveButton, mCancelButton;
    private ImageView mQRView;
    private RelativeLayout mQRViewLayout;
    private EditText mPassword;
    private Switch mEnabledSwitch;
    private TextView mTitleTextView;
    private LinearLayout mRequestView;
    private AirbitzCore mCoreAPI;
    private Account mAccount;
    private TextView mShowQRTextView;
    private Button mShowQRButton;
    private boolean isOtpEnabled = false;;

    CompoundButton.OnCheckedChangeListener mStateListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkPassword();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = AirbitzCore.getApi();
        mAccount = AirbitzApplication.getAccount();

        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public String getTitle() {
        return mActivity.getString(R.string.fragment_twofactor_show_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_twofactor_show, container, false);

        mPassword = (EditText) mView.findViewById(R.id.fragment_twofactor_show_password_edittext);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setTypeface(Typeface.DEFAULT);
        mRequestView = (LinearLayout) mView.findViewById(R.id.fragment_twofactor_request_view);
        mApproveButton = (Button) mView.findViewById(R.id.fragment_twofactor_show_button_approve);
        mApproveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRequest();
            }
        });

        mCancelButton = (Button) mView.findViewById(R.id.fragment_twofactor_show_button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });

        mImportButton = (Button) mView.findViewById(R.id.fragment_twofactor_button_import);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTwoFactorMenu();
            }
        });

        mQRViewLayout = (RelativeLayout) mView.findViewById(R.id.fragment_twofactor_show_qr_layout);
        mQRView = (ImageView) mView.findViewById(R.id.fragment_twofactor_show_qr_image);

        mEnabledSwitch = (Switch) mView.findViewById(R.id.fragment_twofactor_show_toggle_enabled);
        mEnabledSwitch.setOnCheckedChangeListener(mStateListener);

        mShowQRTextView = (TextView) mView.findViewById(R.id.fragment_twofactor_show_qr_image_textview);

        mShowQRButton = (Button) mView.findViewById(R.id.fragment_twofactor_show_qr_image_button);
        mShowQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowQRTextView.getVisibility() == View.VISIBLE) {
                    mShowQRTextView.setVisibility(View.INVISIBLE);
                    mQRView.setVisibility(View.VISIBLE);
                } else {
                    mShowQRTextView.setVisibility(View.VISIBLE);
                    mQRView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mShowQRTextView.setVisibility(View.VISIBLE);
        mQRView.setVisibility(View.INVISIBLE);


        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mPasswordTask) {
            mPasswordTask.cancel(true);
            mPasswordTask = null;
        }
        if (null != mSwitchFlippedTask) {
            mSwitchFlippedTask.cancel(true);
            mSwitchFlippedTask = null;
        }
    }

    private void confirmEnable() {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.fragment_two_factor_warn_title))
            .setMessage(String.format(
                getResources().getString(R.string.fragment_two_factor_warn_message),
                getResources().getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton(getResources().getString(R.string.string_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startSwitchTask(true);
                    }
                }
            )
            .setNegativeButton(getResources().getString(R.string.string_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        quietlyFlipSwitch(false);
                        dialog.cancel();
                    }
                }
            );
        builder.create().show();
    }

    void initUI() {
        mPassword.setText("");
        mPassword.setVisibility(mAccount.passwordExists() ? View.VISIBLE : View.GONE);

        updateTwoFactorUI(false);

        // Check for any pending reset requests
        checkStatus(false);
    }

    void updateTwoFactorUI(boolean enabled) {
        mRequestView.setVisibility(View.GONE);
        mImportButton.setVisibility(AirbitzApplication.hasOtpError() ? View.VISIBLE : View.GONE);
        quietlyFlipSwitch(enabled);
        mEnabledSwitch.setText(getString(enabled ? R.string.fragment_twofactor_show_enabled : R.string.fragment_twofactor_show_disabled));
        mQRViewLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void launchTwoFactorMenu() {
        Fragment fragment = new TwoFactorMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(TwoFactorMenuFragment.STORE_SECRET, true);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);
    }

    void checkStatus(boolean bMsg) {
        mCheckStatusTask = new CheckStatusTask(bMsg);
        mCheckStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private CheckStatusTask mCheckStatusTask;
    public class CheckStatusTask extends AsyncTask<Void, Void, AirbitzException> {
        boolean mMsg;
        boolean mEnabled;

        CheckStatusTask(boolean bMsg) {
            mMsg = bMsg;
            mEnabled = false;
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected AirbitzException doInBackground(Void... params) {
            try {
                mEnabled = mAccount.isOtpEnabled();
                return null;
            } catch (AirbitzException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final AirbitzException error) {
            onCancelled();
            isOtpEnabled = mEnabled;
            updateTwoFactorUI(isOtpEnabled);
            if (error == null) {
                checkSecret(mMsg);
            } else {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_unable_status));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    void checkSecret(boolean bMsg) {
        if (isOtpEnabled) {
            if (mAccount.otpSecret() == null) {
                mQRViewLayout.setVisibility(View.GONE);
            } else {
                mQRViewLayout.setVisibility(View.VISIBLE);
            }
        }
        showQrCode(isOtpEnabled);

        if (mAccount.otpSecret() != null) {
            mActivity.showModalProgress(true);
            checkRequest();
            if (bMsg) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_now_enabled));
            }
        } else {
            if (bMsg) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_now_disabled));
            }
        }
    }

    void showQrCode(boolean show) {
        String secret = mAccount.otpSecret();
        if (show &&
                secret != null &&
                secret.length() > 0) {
            AirbitzCore api = AirbitzCore.getApi();
            Bitmap bitmap = AndroidUtils.qrEncode(
                api.qrEncode(mAccount.otpSecret()));
            if(bitmap != null) {
                bitmap = Common.AddWhiteBorder(bitmap);
                mQRView.setImageBitmap(bitmap);
                animateQrCode(true);
            }
            else {
                mQRViewLayout.setVisibility(View.INVISIBLE);
            }
        } else {
            animateQrCode(false);
        }
    }

    void animateQrCode(boolean show) {
        if (show) {
            if (mQRViewLayout.getVisibility() != View.VISIBLE) {
                mQRViewLayout.setAlpha(0f);
                mQRViewLayout.setVisibility(View.VISIBLE);
                mQRViewLayout.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null);
            }
        } else {
            if (mQRViewLayout.getVisibility() == View.VISIBLE) {
                mQRViewLayout.setAlpha(1f);
                mQRViewLayout.setVisibility(View.VISIBLE);
                mQRViewLayout.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null);
            }
        }
    }

    void checkRequest() {
        AirbitzException error = null;
        boolean pending = false;
        try {
            pending = mCoreAPI.isOtpResetPending(AirbitzApplication.getUsername());
        } catch (AirbitzException e) {
            error = e;
        }
        if (error == null && pending) {
            mRequestView.setVisibility(View.VISIBLE);
        } else {
            mRequestView.setVisibility(View.GONE);
            if (null != error) {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, error));
            }
        }
        mActivity.showModalProgress(false);
    }

    void checkPassword() {
        mPasswordTask = new PasswordTask();
        mPasswordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    void startSwitchTask(boolean isChecked) {
        mSwitchFlippedTask = new SwitchFlippedTask(isChecked);
        mSwitchFlippedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    void quietlyFlipSwitch(boolean status) {
        mEnabledSwitch.setOnCheckedChangeListener(null);
        mEnabledSwitch.setChecked(status);
        mEnabledSwitch.setOnCheckedChangeListener(mStateListener);
    }

    private PasswordTask mPasswordTask;
    public class PasswordTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mAccount.checkPassword(mPassword.getText().toString());
        }

        @Override
        protected void onCancelled() {
            mActivity.showModalProgress(false);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivity.showModalProgress(false);
            if (isCancelled()) {
                return;
            }
            if (success) {
                if (mEnabledSwitch.isChecked()) {
                    confirmEnable();
                } else {
                    startSwitchTask(false);
                }
            } else {
                mPassword.requestFocus();
                quietlyFlipSwitch(!mEnabledSwitch.isChecked());
                mActivity.ShowOkMessageDialog(getString(R.string.activity_signup_incorrect_password),
                    getString(R.string.activity_signup_incorrect_password));
            }
        }
    }

    private SwitchFlippedTask mSwitchFlippedTask;
    public class SwitchFlippedTask extends AsyncTask<Void, Void, Boolean> {
        boolean isChecked;
        SwitchFlippedTask(boolean isChecked) {
            this.isChecked = isChecked;
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (isChecked) {
                    mAccount.otpEnable();
                } else {
                    mAccount.otpDisable();
                }
                return true;
            } catch (AirbitzException e) {
                AirbitzCore.logi("SwitchFlippedTask error");
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            onCancelled();
            if (success) {
                isOtpEnabled = isChecked;
                updateTwoFactorUI(isOtpEnabled);
                checkSecret(true);
            }
        }

        @Override
        protected void onCancelled() {
            mActivity.showModalProgress(false);
        }
    }

    private void confirmRequest() {
        if (mAccount.checkPassword(mPassword.getText().toString())) {
            mConfirmRequestTask = new ConfirmRequestTask();
            mConfirmRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            mActivity.ShowFadingDialog(getString(R.string.activity_signup_incorrect_password));
            mActivity.showModalProgress(false);
        }
    }

    private ConfirmRequestTask mConfirmRequestTask;
    public class ConfirmRequestTask extends AsyncTask<Void, Void, AirbitzException> {
        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected AirbitzException doInBackground(Void... params) {
            try {
                mAccount.otpDisable();
                return null;
            } catch (AirbitzException e) {
                AirbitzCore.logi("ConfirmRequestTask error");
                return e;
            }
        }

        @Override
        protected void onPostExecute(final AirbitzException error) {
            onCancelled();
            if (error == null) {
                mActivity.ShowFadingDialog("Request confirmed, Two Factor off.");
                updateTwoFactorUI(false);
            } else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, error));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void cancelRequest() {
        if (mAccount.checkPassword(mPassword.getText().toString())) {
            mCancelRequestTask = new CancelRequestTask();
            mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            mActivity.ShowFadingDialog(getString(R.string.activity_signup_incorrect_password));
            mActivity.showModalProgress(false);
        }
    }

    private CancelRequestTask mCancelRequestTask;
    public class CancelRequestTask extends AsyncTask<Void, Void, AirbitzException> {
        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected AirbitzException doInBackground(Void... params) {
            try {
                mAccount.otpResetCancel();
                return null;
            } catch (AirbitzException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final AirbitzException error) {
            onCancelled();
            if (error == null) {
                mActivity.ShowFadingDialog("Reset Cancelled.");
                mRequestView.setVisibility(View.GONE);
            } else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, error));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }
}
