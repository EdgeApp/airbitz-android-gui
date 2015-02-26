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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.settings.SettingFragment;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.objects.QRCamera;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created on 3/3/14.
 */
public class ImportFragment extends BaseFragment implements
        CoreAPI.OnWalletSweep,
        QRCamera.OnScanResult
{
    public static String URI = "com.airbitz.importfragment.uri";

    private final String TAG = getClass().getSimpleName();
    private EditText mToEdittext;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private HighlightOnPressSpinner mWalletSpinner;
    private HighlightOnPressButton mSubmitButton;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mQRCodeTextView;
    private TextView mTitleTextView;
    private NfcAdapter mNfcAdapter;
    private QRCamera mQRCamera;
    private CoreAPI mCoreAPI;
    private View mView;
    private List<Wallet> mWallets;//Actual wallets
    private Wallet mFromWallet;
    private Handler mHandler = new Handler();
    private NavigationActivity mActivity;
    private LinearLayout mImportLayout;
    private RelativeLayout mBusyLayout;
    private RelativeLayout mCameraLayout;
    private TextView mBusyText;

    private String mTweet, mToken, mMessage, mZeroMessage;
    String mSweptID;
    long mSweptAmount = -1;
    private String mSweptAddress;

    Runnable sweepNotFoundRunner = new Runnable() {
        @Override
        public void run() {
            showBusyLayout(false);
            if(isVisible()) {
                clearSweepAddress();
                mSweptAmount = -1;
                ((NavigationActivity) getActivity()).ShowFadingDialog(getString(R.string.import_wallet_timeout_message));
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
        mWallets = mCoreAPI.getCoreActiveWallets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_import_wallet, container, false);
        } else {
            return mView;
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mCameraLayout = (RelativeLayout) mView.findViewById(R.id.fragment_import_layout_camera);

        mImportLayout = (LinearLayout) mView.findViewById(R.id.fragment_import_layout);
        mBusyLayout = (RelativeLayout) mView.findViewById(R.id.fragment_import_busy_layout);
        mBusyText = (TextView) mView.findViewById(R.id.fragment_import_busy_text);
        mBusyText.setTypeface(NavigationActivity.latoBlackTypeFace);

        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);
        mQRCodeTextView = (TextView) mView.findViewById(R.id.textview_scan_qrcode);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.import_title);

        mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.IMPORT_WALLET), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        mSubmitButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_import_enter_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmit();
            }
        });

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace);
        mQRCodeTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_import_from_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.SendFrom);
        mWalletSpinner.setAdapter(dataAdapter);

        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mFromWallet = mWallets.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mToEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mToEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!textView.getText().toString().isEmpty()) {
                        attemptSubmit();
                    }
                    return true;
                }
                return false;
            }
        });


        return mView;
    }

    private void showBusyLayout(boolean on) {
        if(on) {
            mImportLayout.setVisibility(View.GONE);
            mBusyLayout.setVisibility(View.VISIBLE);
            stopCamera();
        }
        else {
            mImportLayout.setVisibility(View.VISIBLE);
            mBusyLayout.setVisibility(View.GONE);
            startCamera();
        }
    }

    private void scanQRCodes() {
        mSubmitButton.setVisibility(View.INVISIBLE);
        mCameraLayout.setVisibility(View.VISIBLE);
        startCamera();

        final NfcManager nfcManager = (NfcManager) mActivity.getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();

        if (mNfcAdapter != null && mNfcAdapter.isEnabled() && SettingFragment.getNFCPref()) {
            mQRCodeTextView.setText(getString(R.string.send_scan_text_nfc));
        }
        else {
            mQRCodeTextView.setText(getString(R.string.send_scan_text));
        }
    }

    private void stopScanningQRCodes() {
        mSubmitButton.setVisibility(View.VISIBLE);
        mCameraLayout.setVisibility(View.GONE);
        stopCamera();
    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera");
        if(mQRCamera != null) {
            mQRCamera.stopCamera();
        }
    }

    public void startCamera() {
        if(mQRCamera == null) {
            mQRCamera = new QRCamera(this, mCameraLayout);
            mQRCamera.setOnScanResultListener(this);
        }
        mQRCamera.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();

        if(args != null && args.getString(URI) != null && getHiddenBitsToken(args.getString(URI)) != null) {
            mToEdittext.setText(args.getString(URI));
            mToEdittext.clearFocus();
            mActivity.hideSoftKeyboard(mToEdittext);
            stopScanningQRCodes();
        }
        else {
            scanQRCodes();
        }

        mFromWallet = mWallets.get(0);
        mCoreAPI.setOnWalletSweepListener(this);

        clearSweepAddress();
    }

    @Override
    public void onPause() {
        super.onPause();
        showBusyLayout(false);
        stopCamera();
        mHandler.removeCallbacks(sweepNotFoundRunner);
        mCoreAPI.setOnWalletSweepListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
    }

    @Override
    public void onScanResult(String result) {
        if (result != null) {
            Log.d(TAG, "HiddenBits found");
            mToEdittext.setText(result);
            attemptSubmit();
        }
        mQRCamera.setOnScanResultListener(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mQRCamera != null && requestCode == QRCamera.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            mQRCamera.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void attemptSubmit() {
        String uriString = mToEdittext.getText().toString().trim();
        String token = getHiddenBitsToken(uriString);

        String entry = token != null ? token : uriString;

        mBusyText.setText(String.format(getString(R.string.import_wallet_busy_text), entry));
        showBusyLayout(true);
        mSweptAddress = mCoreAPI.SweepKey(mFromWallet.getUUID(), entry);

        if(mSweptAddress != null && !mSweptAddress.isEmpty()) {
            mHandler.postDelayed(sweepNotFoundRunner, 30000);

            if(token != null) { // also issue hidden bits
                int hBitzIDLength = 4;
                if(mSweptAddress.length() >= hBitzIDLength) {
                    String lastFourChars = mSweptAddress.substring(mSweptAddress.length() - hBitzIDLength, mSweptAddress.length());
                    HiddenBitsApiTask task = new HiddenBitsApiTask();
                    task.execute(lastFourChars);
                }
                else {
                    Log.d(TAG, "HiddenBits token error");
                }
            }
        }
        else {
            showBusyLayout(false);
            mActivity.ShowFadingDialog(getString(R.string.import_wallet_private_key_invalid));
        }
    }

    // Returns null if not a HiddenBits token
    public static String getHiddenBitsToken(String uriIn)
    {
        final String HBITS_SCHEME = "hbits";
        if(uriIn == null)
            return null;

        Uri uri = Uri.parse(uriIn);
        String scheme = uri.getScheme();

        if(scheme != null && scheme.equalsIgnoreCase(HBITS_SCHEME)) {
            Log.d("ImportFragment", "Good HiddenBits URI");
            return uri.toString().substring(scheme.length()+3);
        }
        else {
            Log.d("ImportFragment", "HiddenBits failed for: "+uriIn);
            return null;
        }
    }

    public class HiddenBitsApiTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Getting HiddenBits API response");
        }

        @Override
        protected String doInBackground(String... params) {
            AirbitzAPI api = AirbitzAPI.getApi();
            return api.getHiddenBits(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                return;
            }
            Log.d(TAG, "Got HiddenBits API response: " + result);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                mTweet = jsonObject.getString("tweet");
                mToken = jsonObject.getString("token");
                mZeroMessage = jsonObject.getString("zero_message");
                mMessage = jsonObject.getString("message");

                // Check to see if both paths are done
                checkHiddenBitsAsyncData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            showBusyLayout(false);
        }
    }

    @Override
    public void OnWalletSweep(String txID, long amount) {
        Log.d(TAG, "OnWalletSweep called with ID:" + txID + " and satoshis:" + amount);

        showBusyLayout(false);

        mSweptID = txID;
        mSweptAmount = amount;

        String token = getHiddenBitsToken(mToEdittext.getText().toString());
        if(token != null) { // hidden bitz
            // Check to see if both paths are done
            checkHiddenBitsAsyncData();
            return;
        }

        // if a private address sweep
        mHandler.removeCallbacks(sweepNotFoundRunner);

        clearSweepAddress();
        mActivity.showPrivateKeySweepTransaction(mSweptID, mFromWallet.getUUID(), mSweptAmount);
        mSweptAmount = -1;
    }

    private void clearSweepAddress() {
        // Clear out sweep info
        mSweptAddress = "";
        mToEdittext.setText(mSweptAddress);
    }

    // This is only called for HiddenBits
    private void checkHiddenBitsAsyncData() {
        // both async paths are finished if both of these are not empty
        if (mSweptAmount != -1 && mTweet != null)
        {
            Log.d(TAG, "Both API and OnWalletSweep are finished");

            mHandler.removeCallbacks(sweepNotFoundRunner);
            showBusyLayout(false);

            mActivity.showHiddenBitsTransaction(mSweptID, mFromWallet.getUUID(), mSweptAmount,
                    mMessage, mZeroMessage, mTweet);

            mSweptAmount = -1;
        }
    }
}
