package com.airbitz.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;
import com.google.android.gms.fitness.data.Subscription;
import com.squareup.whorlwind.ReadResult;
import com.squareup.whorlwind.SharedPreferencesStorage;
import com.squareup.whorlwind.Whorlwind;

import org.json.JSONObject;

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import okio.ByteString;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.airbitz.api.directory.Hour.TAG;
import static com.squareup.whorlwind.ReadResult.ReadState.AUTHORIZATION_ERROR;
import static com.squareup.whorlwind.ReadResult.ReadState.NEEDS_AUTH;
import static com.squareup.whorlwind.ReadResult.ReadState.RECOVERABLE_ERROR;
import static com.squareup.whorlwind.ReadResult.ReadState.UNRECOVERABLE_ERROR;

/**
 * Created by paul on 11/5/16.
 */

public class ABCKeychain {
    private ImageView mFingerprintIcon;
    private TextView mFingerprintStatus;
    private MaterialDialog mFingerprintDialog;
    private NavigationActivity mActivity;
    private JSONObject mTouchIDUsers;
    private AirbitzCore mCoreApi;
    private rx.Subscription mSubscription;


    private SharedPreferencesStorage mStorage;

    private Whorlwind mWhorlwind;
    private boolean mHasSecureElement;

    static final long ERROR_TIMEOUT_MILLIS  = 1600;
    static final long SUCCESS_DELAY_MILLIS  = 750;
    static final String LOGINKEY_KEY        = "key_loginkey";
    static final String TOUCH_ID_USERS      = "abcTouchIdUsers";
    static final String ABC_PREFS           = "com.airbitz.prefs.abc";

    public interface AutoReloginOrTouchIDCallbacks {
        void doBeforeLogin();
        void completionWithLogin(Account account, boolean usedTouchId);
        void completionNoLogin();
        void error();
    }

    private interface GetKeychainString {
        void onSuccess(String value);
        void onClose();
        void onError();
    }

    public ABCKeychain(NavigationActivity activity) {
        mActivity = activity;
        mStorage = mActivity.sharedPreferencesStorage;
        mCoreApi = AirbitzCore.getApi();
        mSubscription = null;

//        int sdk = android.os.Build.VERSION.SDK_INT;

        mHasSecureElement = false;
        mTouchIDUsers = null;
//        if (sdk >= 24)
        {
            mWhorlwind = Whorlwind.create(mActivity, mStorage, "AirbitzKeyStore");
            if (mWhorlwind != null && mWhorlwind.canStoreSecurely()) {
                mHasSecureElement = true;

                mTouchIDUsers = getTouchIDUsers();
            }
        }
    }

    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mActivity == null) return;
            mFingerprintStatus.setTextColor(Common.resolveColor(mActivity, android.R.attr.textColorSecondary));
            mFingerprintStatus.setText(R.string.fingerprint_hint);
            mFingerprintIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };

    private void showFingerPrintDialog(String promptString, GetKeychainString callbacks) {

        mFingerprintDialog = new MaterialDialog.Builder(mActivity)
                .title(promptString)
                .customView(R.layout.fingerprint_dialog_container, false)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .cancelable(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.cancel();
                        if (mSubscription != null) {
                            mSubscription.unsubscribe();
                            mSubscription = null;
                        }
                        callbacks.onClose();
                    }
                }).build();

        mFingerprintDialog.show();
        final View v = mFingerprintDialog.getCustomView();
        assert v != null;

        mFingerprintIcon = (ImageView) v.findViewById(R.id.fingerprint_icon);
        mFingerprintStatus = (TextView) v.findViewById(R.id.fingerprint_status);
        mFingerprintStatus.setText(R.string.fingerprint_hint);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_text_hint));

    }

    public boolean canDoTouchId() {
        return mHasSecureElement;
    }

    public void fingerprintDialogAuthenticated(String value, GetKeychainString callbacks) {
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_text_hint));
        mFingerprintStatus.setText(mActivity.getString(R.string.fingerprint_success));
        mFingerprintIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mCallback.onFingerprintDialogAuthenticated();
                mFingerprintDialog.dismiss();
                callbacks.onSuccess(value);
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void fingerprintDialogError(CharSequence error) {
        if (mActivity == null) return;
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mFingerprintStatus.setText(error);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.warning_color));
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintStatus.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    public void setKeychainString (String key, String value) {
        Observable.just(value)
                .observeOn(Schedulers.io())
                .subscribe(val -> mWhorlwind.write(key, ByteString.encodeUtf8(val)));
//        mWhorlwind.write(key, ByteString.encodeUtf8(value));
    }

    public boolean hasKeychainString (String key) {
        boolean foundKey = false;
        Observable<ReadResult> result = mWhorlwind.read(key);

        return foundKey;

    }

    public void getKeychainString (String key, String promptString, GetKeychainString callbacks) {

        mSubscription = mWhorlwind.read(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    switch (result.readState) {
                        case NEEDS_AUTH:
                            // An encrypted value was found, prompt for fingerprint to decrypt.
                            // The fingerprint reader is active.
                            showFingerPrintDialog(promptString, callbacks);
                            break;
                        case UNRECOVERABLE_ERROR:
                        case AUTHORIZATION_ERROR:
                        case RECOVERABLE_ERROR:
                            // Show an error message. One may be provided in result.message.
                            // Unless the state is UNRECOVERABLE_ERROR, the fingerprint reader is still
                            // active and this stream will continue to emit result updates.
                            fingerprintDialogError("Error reading finger");
                            break;
                        case READY:
                            if (result.value != null) {
                                // Value was found and has been decrypted.
                                fingerprintDialogAuthenticated(result.value.utf8(), callbacks);
                            } else {
                                // No value was found. Fall back to password or fail silently, depending on
                                // your use case.
                                fingerprintDialogError("No fingerprint login key");
                                mFingerprintDialog.dismiss();
                                mFingerprintDialog = null;
                                callbacks.onError();
                            }
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mSubscription = null;
                                }
                            }, 100);

                            break;
                        default:
                            throw new IllegalArgumentException("Unknown state: " + result.readState);
                    }
                });
    }

    public String createKeyWithUsername(String username, String key) {
        return String.format("%s___%s", username, key);
    }

    public void enableTouchID (String username, String loginKey) {
        if (mHasSecureElement) {
            String usernameLoginKeyKey = createKeyWithUsername(username, LOGINKEY_KEY);
            setKeychainString(usernameLoginKeyKey, loginKey);
            try {
                mTouchIDUsers.put(username, true);
                setTouchIDUsers(mTouchIDUsers);
            } catch (Exception e) {
                setKeychainString(usernameLoginKeyKey, "");
            }
        }
    }

    public void disableTouchID (String username) {
        if (mHasSecureElement) {
            String usernameLoginKeyKey = createKeyWithUsername(username, LOGINKEY_KEY);
            setKeychainString(usernameLoginKeyKey, "");
            try {
                mTouchIDUsers.put(username, false);
                setTouchIDUsers(mTouchIDUsers);
            } catch (Exception e) {
            }
        }
    }

    public boolean touchIDEnabled (String username) {
        boolean enabled = false;

        if (mTouchIDUsers != null) {
            try {
                enabled = mTouchIDUsers.getBoolean(username);
            } catch (Exception e) {
                return false;
            }
        }
        return enabled;
    }

    public boolean touchIDDisabled (String username) {
        boolean disabled = false;

        if (mTouchIDUsers != null) {
            try {
                disabled = !mTouchIDUsers.getBoolean(username);
            } catch (Exception e) {
                return false;
            }
        }
        return disabled;
    }

    private void getTouchIDLoginKey (String username, String promptString, GetKeychainString callbacks) {

        if (mHasSecureElement) {
            String usernameLoginKeyKey = createKeyWithUsername(username, LOGINKEY_KEY);
            getKeychainString(usernameLoginKeyKey, promptString, callbacks);
        }
    }

    public void autoReloginOrTouchID (String username, AutoReloginOrTouchIDCallbacks callbacks) {
        if (mSubscription != null) {
            callbacks.completionNoLogin();
            return;
        }
        boolean touchIDEnabled = touchIDEnabled(username);

        if (!touchIDEnabled)
            return;

        String signInString = String.format(mActivity.getString(R.string.fingerprint_signin), username);

        getTouchIDLoginKey(username, signInString, new GetKeychainString() {
            @Override
            public void onSuccess(String value) {
                // Got the loginKey. Tell the GUI that we're going to process a login. This lets the GUI popup a spinner
                callbacks.doBeforeLogin();

                // Now attempt a login
                Account account = null;
                try {
                    account = mCoreApi.loginWithKey(username, value);
                } catch (Exception e) {

                }
                if (account == null) {
                    callbacks.error();
                } else {
                    callbacks.completionWithLogin(account, true);
                }
            }

            @Override
            public void onClose() {
                Log.e(TAG, "getTouchIDLoginKey check tessed FAILED");
                callbacks.completionNoLogin();
            }

            @Override
            public void onError() {
                Log.e(TAG, "getTouchIDLoginKey check tessed FAILED");
                callbacks.error();
            }
        });

    }

    private void runTests () {
        final String testLoginKey = "125934757";
        final String testEnabledLoginKey = "g93957g125934757";

        if (mHasSecureElement) {

            String username = "test1";
            final String usernameEnabled = "test_enabled";

            // Run through tests
            String signInString = String.format(mActivity.getString(R.string.fingerprint_signin), username);

            boolean touchIDEnabled = touchIDEnabled(username);
            if (touchIDEnabled) {
                Log.e(TAG, "Incorrect initial state");
                return;
            }
            Log.e(TAG, "touchIDEnabled initial tessed PASSED");

            touchIDEnabled = touchIDEnabled(usernameEnabled);
            if (!touchIDEnabled) {
                Log.e(TAG, "Incorrect enabled state");
                return;
            }
            Log.e(TAG, "touchIDEnabled enabled tessed PASSED");


            enableTouchID(usernameEnabled, testEnabledLoginKey);
            enableTouchID(username, testLoginKey);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                Log.e(TAG, "Failed to sleep");
                return;
            }

            Log.e(TAG, "enableTouchID tessed PASSED");

            touchIDEnabled = touchIDEnabled(username);

            if (!touchIDEnabled) {
                Log.e(TAG, "Incorrect touchID state");
                return;
            }
            Log.e(TAG, "touchIDEnabled check tessed PASSED");

            String finalUsername = username;
            getTouchIDLoginKey(username, signInString, new GetKeychainString() {
                @Override
                public void onSuccess(String value) {
                    if (value.equals(testLoginKey)) {
                        Log.e(TAG, "getTouchIDLoginKey check tessed PASSED");
                        disableTouchID(finalUsername);
                    } else {
                        Log.e(TAG, "getTouchIDLoginKey check tessed FAILED");
                        disableTouchID(finalUsername);
                    }
                }

                @Override
                public void onClose() {
                    Log.e(TAG, "getTouchIDLoginKey check tessed FAILED");
                    disableTouchID(finalUsername);
                }

                @Override
                public void onError() {
                    Log.e(TAG, "getTouchIDLoginKey check tessed FAILED");
                    disableTouchID(finalUsername);
                }
            });
        }
    }

    static JSONObject jsonTouchIDUsers = null;
    public JSONObject getTouchIDUsers() {
        if (jsonTouchIDUsers == null) {
            SharedPreferences prefs = mActivity.getSharedPreferences(ABC_PREFS, Context.MODE_PRIVATE);
            String jsonString = null;
            jsonString = prefs.getString(TOUCH_ID_USERS, null);
            if (jsonString != null) {
                try {
                    jsonTouchIDUsers = new JSONObject(jsonString);
                } catch (Exception e) {
                    jsonTouchIDUsers = new JSONObject();
                }
            } else {
                jsonTouchIDUsers = new JSONObject();
            }
        }
        return jsonTouchIDUsers;
    }

    public void setTouchIDUsers(JSONObject jsonObject) {
        String jsonString = jsonObject.toString();

        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(ABC_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(TOUCH_ID_USERS, jsonString).apply();
    }

}
