package com.airbitz.objects;

import android.os.CountDownTimer;
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
import com.squareup.whorlwind.ReadResult;
import com.squareup.whorlwind.SharedPreferencesStorage;
import com.squareup.whorlwind.Whorlwind;

import org.json.JSONObject;

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

    private SharedPreferencesStorage mStorage;

    private Whorlwind mWhorlwind;
    private boolean mHasSecureElement;

    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;
    static final String TOUCHID_USERS_KEY    = "key_touchid_users";
    static final String LOGINKEY_KEY       = "key_loginkey";

    private interface GetKeychainString {
        void onSuccess(String value);
        void onClose();
        void onError();
    }

    public ABCKeychain(NavigationActivity activity) {
        mActivity = activity;
        mStorage = mActivity.sharedPreferencesStorage;

        int sdk = android.os.Build.VERSION.SDK_INT;

        mHasSecureElement = false;
        mTouchIDUsers = null;
        if (sdk >= 24) {
            mWhorlwind = Whorlwind.create(mActivity, mStorage, "AirbitzKeyStore");
            if (mWhorlwind != null && mWhorlwind.canStoreSecurely()) {
                mHasSecureElement = true;

                mTouchIDUsers = AirbitzApplication.getTouchIDUsers();
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
                .cancelable(true)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.cancel();
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

//        mWhorlwind.read(key)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    switch (result.readState) {
//                        case NEEDS_AUTH:
//                            break;
//                        case UNRECOVERABLE_ERROR:
//                        case AUTHORIZATION_ERROR:
//                        case RECOVERABLE_ERROR:
//                            // Show an error message. One may be provided in result.message.
//                            // Unless the state is UNRECOVERABLE_ERROR, the fingerprint reader is still
//                            // active and this stream will continue to emit result updates.
//                            fingerprintDialogError("error");
//                            break;
//                        case READY:
//                            if (result.value != null) {
//                                // Value was found and has been decrypted.
//                                fingerprintDialogAuthenticated();
//                                mActivity.showToast(result.value.utf8(), 10);
//                            } else {
//                                // No value was found. Fall back to password or fail silently, depending on
//                                // your use case.
////                                        fingerprintFallback();
//                                fingerprintDialogError("no login found");
//                                mFingerprintDialog.dismiss();
//                                mFingerprintDialog = null;
//                            }
//                            break;
//                        default:
//                            throw new IllegalArgumentException("Unknown state: " + result.readState);
//                    }
//                });
//
    }

    public void getKeychainString (String key, String promptString, GetKeychainString callbacks) {
        mWhorlwind.read(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    switch (result.readState) {
                        case NEEDS_AUTH:
                            // An encrypted value was found, prompt for fingerprint to decrypt.
                            // The fingerprint reader is active.
//                                    promptForFingerprint();
                            showFingerPrintDialog(promptString, callbacks);
                            break;
                        case UNRECOVERABLE_ERROR:
                        case AUTHORIZATION_ERROR:
                        case RECOVERABLE_ERROR:
                            // Show an error message. One may be provided in result.message.
                            // Unless the state is UNRECOVERABLE_ERROR, the fingerprint reader is still
                            // active and this stream will continue to emit result updates.
                            fingerprintDialogError("error");
                            break;
                        case READY:
                            if (result.value != null) {
                                // Value was found and has been decrypted.
                                fingerprintDialogAuthenticated(result.value.utf8(), callbacks);
//                                mActivity.showToast(result.value.utf8(), 10);
                            } else {
                                // No value was found. Fall back to password or fail silently, depending on
                                // your use case.
//                                        fingerprintFallback();
                                fingerprintDialogError("no login found");
                                mFingerprintDialog.dismiss();
                                mFingerprintDialog = null;
                                callbacks.onError();
                            }
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
                AirbitzApplication.setTouchIDUsers(mTouchIDUsers);
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
                mTouchIDUsers.remove(username);
                AirbitzApplication.setTouchIDUsers(mTouchIDUsers);
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

    private void getTouchIDLoginKey (String username, String promptString, GetKeychainString callbacks) {

        if (mHasSecureElement) {
            String usernameLoginKeyKey = createKeyWithUsername(username, LOGINKEY_KEY);
            getKeychainString(usernameLoginKeyKey, promptString, callbacks);
        }
    }

    public void autoReloginOrTouchIDIfPossible (String username) {

        final String testLoginKey = "125934757";
        final String testEnabledLoginKey = "g93957g125934757";

        if (mHasSecureElement) {

            username = "test1";
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


//            if (touchIDEnabled) {
//                hasKeychainString(usernameLoginKeyKey);
//
//                getKeychainString(username, signInString, new GetKeychainString() {
//                    @Override
//                    public void onSuccess(String value) {
//
//                    }
//
//                    @Override
//                    public void onError() {
//                    }
//                });
//            }
        }
    }

}
