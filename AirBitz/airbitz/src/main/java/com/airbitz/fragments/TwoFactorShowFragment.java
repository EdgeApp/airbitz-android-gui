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

package com.airbitz.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

/**
 * Two Factor Authentication Show
 * Created 2/6/15
 */
public class TwoFactorShowFragment extends BaseFragment
{
    private final String TAG = getClass().getSimpleName();

    HighlightOnPressImageButton mHelpButton;
    HighlightOnPressButton mImportButton, mApproveButton, mCancelButton;
    ImageButton mBackButton;
    ImageView mQRView;
    EditText mPassword;
    Switch mEnabledSwitch;
    private TextView mTitleTextView;
    boolean _isOn;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_twofactor_show, container, false);

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

//        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
//        mHelpButton.setVisibility(View.VISIBLE);
//        mHelpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.pushFragment(new HelpFragment(HelpFragment.SPEND_LIMITS), NavigationActivity.Tabs.SETTING.ordinal());
//            }
//        });

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_twofactor_show_title);

        mPassword = (EditText) mView.findViewById(R.id.fragment_twofactor_show_password_edittext);

        mApproveButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_twofactor_show_button_approve);
        mApproveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_twofactor_show_button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mImportButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_twofactor_button_import);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                _tfaMenuViewController = (TwoFactorMenuViewController *)[Util animateIn:@"TwoFactorMenuViewController" parentController:self];
//                _tfaMenuViewController.delegate = self;
//                _tfaMenuViewController.bStoreSecret = YES;
                mActivity.pushFragment(new TwoFactorMenuFragment());
            }
        });

        mQRView = (ImageView) mView.findViewById(R.id.fragment_twofactor_show_qr_image);

        mEnabledSwitch = (Switch) mView.findViewById(R.id.fragment_twofactor_show_toggle_enabled);
        mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchFlipped(isChecked);
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        initUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void initUI()
    {
        mPassword.setText("");

        _isOn = false;
        updateTwoFactorUI(false);

        // Check for any pending reset requests
        checkStatus(false);
    }

    void updateTwoFactorUI(boolean on)
    {
        if (on) {
//            _requestView.hidden = YES;
//            _requestView.hidden = YES;
            mImportButton.setVisibility(View.VISIBLE);
        } else {
//            _requestView.hidden = YES;
//            _requestView.hidden = YES;
            mQRView.setVisibility(View.GONE);
            mImportButton.setVisibility(View.GONE);
        }
        mActivity.showModalProgress(false);
        mEnabledSwitch.setChecked(on);
    }


    void checkStatus(boolean bMsg)
    {
        mCheckStatusTask = new CheckStatusTask(bMsg);
        mCheckStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * Reset Two Factor Authentication
     */
    private CheckStatusTask mCheckStatusTask;
    public class CheckStatusTask extends AsyncTask<Void, Void, tABC_CC> {
        boolean mMsg;

        CheckStatusTask(boolean bMsg) {
            mMsg = bMsg;
            mActivity.showModalProgress(true);
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            tABC_CC cc = mCoreAPI.OtpAuthGet();
            return cc;
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            updateTwoFactorUI(mCoreAPI.isTwoFactorOn());
            if(cc == tABC_CC.ABC_CC_Ok) {
                checkSecret(mMsg);
            }
            else {
                mActivity.ShowFadingDialog(getString(R.string.fragment_twofactor_show_unable_status));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Check Secret
     */
    private CheckSecretTask mCheckSecretTask;
    public class CheckSecretTask extends AsyncTask<Void, Void, tABC_CC> {
        boolean mMsg;

        CheckSecretTask(boolean bMsg) {
            mMsg = bMsg;
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            tABC_CC cc = mCoreAPI.GetTwoFactorSecret();
            return cc;
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();

            if (!(cc == tABC_CC.ABC_CC_Ok && mCoreAPI.TwoFactorSecret() != null)) {
                mQRView.setVisibility(View.GONE);
            }

            if (mCoreAPI.TwoFactorSecret() != null) {
                mActivity.showModalProgress(false);
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    void checkSecret(boolean bMsg)
    {
        if (mCoreAPI.isTwoFactorOn()) {
            mCheckSecretTask = new CheckSecretTask(bMsg);
            mCheckSecretTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
        showQrCode(mCoreAPI.isTwoFactorOn());

        if (mCoreAPI.TwoFactorSecret() != null) {
            mActivity.showModalProgress(true);
            checkRequest();
            if (bMsg) {
                mActivity.ShowFadingDialog("Two Factor Enabled");
            }
        } else {
            if (bMsg) {
                mActivity.ShowFadingDialog("Two Factor Disabled");
            }
        }
    }

    void showQrCode(boolean show)
    {
        if (show) {
//            unsigned char *pData = NULL;
//            unsigned int width;
//
//            tABC_Error error;
//            tABC_CC cc = ABC_GetTwoFactorQrCode([[User Singleton].name UTF8String],
//            [[User Singleton].password UTF8String], &pData, &width, &error);
//            if (cc == ABC_CC_Ok) {
//                UIImage *qrImage = [Util dataToImage:pData withWidth:width andHeight:width];
//                _qrCodeImageView.image = qrImage;
//                _qrCodeImageView.layer.magnificationFilter = kCAFilterNearest;
//                [self animateQrCode:YES];
//            } else {
//                _viewQRCodeFrame.hidden = YES;
//            }
//            if (pData) {
//                free(pData);
//            }
//        } else {
//            [self animateQrCode:NO];
        }
    }

    void animateQrCode(boolean show)
    {
        if (show) {
//            if (_viewQRCodeFrame.hidden) {
//                _viewQRCodeFrame.hidden = NO;
//                _viewQRCodeFrame.alpha = 0;
//                [UIView animateWithDuration:1 delay:0 options:UIViewAnimationOptionCurveLinear animations:^ {
//                    _viewQRCodeFrame.alpha = 1.0;
//                } completion:^(BOOL finished) {
//                }];
//            }
//        } else {
//            if (!_viewQRCodeFrame.hidden) {
//                _viewQRCodeFrame.alpha = 1;
//                [UIView animateWithDuration:1 delay:0 options:UIViewAnimationOptionCurveLinear animations:^ {
//                    _viewQRCodeFrame.alpha = 0;
//                } completion:^(BOOL finished) {
//                    _viewQRCodeFrame.hidden = YES;
//                }];
//            }
        }
    }

    void checkRequest()
    {
        tABC_Error error = new tABC_Error();
        boolean pending = mCoreAPI.isTwoFactorResetPending(error);
        if (error.getCode() == tABC_CC.ABC_CC_Ok) {
            mQRView.setVisibility(pending ? View.VISIBLE : View.GONE);
        } else {
            mQRView.setVisibility(View.GONE);
            mActivity.ShowFadingDialog(Common.errorMap(mActivity, error.getCode()));
        }
        mActivity.showModalProgress(false);
    }

    void switchFlipped(boolean isChecked) {
        if(mCoreAPI.PasswordOK(AirbitzApplication.getUsername(),
                AirbitzApplication.getPassword())) {
                switchTwoFactor(true);
        }
        else {
                mPassword.requestFocus();
                mEnabledSwitch.setChecked(false);
            mActivity.showModalProgress(false);
            mActivity.ShowFadingDialog("Incorrect password");
        }
    }

    void setText(boolean on)
    {
        if (on) {
            mEnabledSwitch.setText("Enabled");
        } else {
            mEnabledSwitch.setText("Disabled");
        }
    }

    void switchTwoFactor(boolean on)
    {
        mCoreAPI.enableTwoFactor(on);
        updateTwoFactorUI(on);
        checkSecret(true);
    }

    private void confirmRequest()
    {
        resetOtpNotifications();
        if(mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), AirbitzApplication.getPassword())) {
            mConfirmRequestTask = new ConfirmRequestTask();
            mConfirmRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
        else {
            mActivity.ShowFadingDialog("Incorrect password");
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Confirm Request
     */
    private ConfirmRequestTask mConfirmRequestTask;
    public class ConfirmRequestTask extends AsyncTask<Void, Void, tABC_CC> {

        ConfirmRequestTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            return mCoreAPI.enableTwoFactor(false);
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            mActivity.showModalProgress(false);

            if (cc == tABC_CC.ABC_CC_Ok) {
                mActivity.ShowFadingDialog("Request confirmed, Two Factor off.");
                updateTwoFactorUI(false);
            }
            else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, cc));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    private void cancelRequest()
    {
        resetOtpNotifications();
        if(mCoreAPI.PasswordOK(AirbitzApplication.getUsername(), AirbitzApplication.getPassword())) {
            mCancelRequestTask = new CancelRequestTask();
            mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
        else {
            mActivity.ShowFadingDialog("Incorrect password");
            mActivity.showModalProgress(false);
        }
    }

    /**
     * Cancel Request
     */
    private CancelRequestTask mCancelRequestTask;
    public class CancelRequestTask extends AsyncTask<Void, Void, tABC_CC> {

        CancelRequestTask() { }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected tABC_CC doInBackground(Void... params) {
            return mCoreAPI.cancelTwoFactorRequest();
        }

        @Override
        protected void onPostExecute(final tABC_CC cc) {
            onCancelled();
            mActivity.showModalProgress(false);

            if (cc == tABC_CC.ABC_CC_Ok) {
                mActivity.ShowFadingDialog("Reset Cancelled.");
                updateTwoFactorUI(false);
            }
            else {
                mActivity.ShowFadingDialog(Common.errorMap(mActivity, cc));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckStatusTask = null;
            mActivity.showModalProgress(false);
        }
    }

    void resetOtpNotifications() {

    }

//    #pragma mark - TwoFactorMenuViewControllerDelegate
//
//    - (void)twoFactorMenuViewControllerDone:(TwoFactorMenuViewController *)controller withBackButton:(BOOL)bBack
//    {
//        BOOL success = controller.bSuccess;
//        [Util animateOut:controller parentController:self complete:^(void) {
//            _tfaMenuViewController = nil;
//        }];
//        if (!bBack && !success) {
//            [self showFadingAlert:NSLocalizedString(@("Unable to import secret"), nil)];
//        }
//        [self checkStatus:success];
//    }

}
