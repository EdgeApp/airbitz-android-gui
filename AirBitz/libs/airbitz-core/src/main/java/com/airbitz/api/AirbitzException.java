package com.airbitz.api;

import android.content.Context;

public class AirbitzException extends Exception {
    private tABC_CC mCode;
    private tABC_Error mError;

    protected AirbitzException(Context context, tABC_CC code, tABC_Error error) {
        super(errorMap(context, code, error));
        mCode = code;
        mError = error;
    }

    public boolean isOkay() {
        return mCode == tABC_CC.ABC_CC_Ok;
    }

    public boolean isBadPassword() {
        return mCode == tABC_CC.ABC_CC_BadPassword;
    }

    public boolean isOtpError() {
        return mCode == tABC_CC.ABC_CC_InvalidOTP;
    }

    public String errorMap() {
        return AirbitzException.errorMap(null, mCode, mError);
    }
    static String errorMap(Context context, tABC_CC code, tABC_Error error) {
        if (context == null) {
            return error.getSzDescription();
        } else if (code == tABC_CC.ABC_CC_AccountAlreadyExists) {
            return context.getString(R.string.server_error_account_already_exists);
        }
        else if (code == tABC_CC.ABC_CC_AccountDoesNotExist) {
            return context.getString(R.string.server_error_account_does_not_exists);
        }
        else if (code == tABC_CC.ABC_CC_BadPassword) {
            return context.getString(R.string.server_error_bad_password);
        }
        else if (code == tABC_CC.ABC_CC_WalletAlreadyExists) {
            return context.getString(R.string.server_error_wallet_exists);
        }
        else if (code == tABC_CC.ABC_CC_InvalidWalletID) {
            return context.getString(R.string.server_error_invalid_wallet);
        }
        else if (code == tABC_CC.ABC_CC_URLError) {
            return context.getString(R.string.string_connection_error_server);
        }
        else if (code == tABC_CC.ABC_CC_ServerError) {
            return context.getString(R.string.server_error_no_connection);
        }
        else if (code == tABC_CC.ABC_CC_NoRecoveryQuestions) {
            return context.getString(R.string.server_error_no_recovery_questions);
        }
        else if (code == tABC_CC.ABC_CC_NotSupported) {
            return context.getString(R.string.server_error_not_supported);
        }
        else if (code == tABC_CC.ABC_CC_InsufficientFunds) {
            return context.getString(R.string.server_error_insufficient_funds);
        }
        else if (code == tABC_CC.ABC_CC_SpendDust) {
            return context.getString(R.string.insufficient_amount);
        }
        else if (code == tABC_CC.ABC_CC_Synchronizing) {
            return context.getString(R.string.server_error_synchronizing);
        }
        else if (code == tABC_CC.ABC_CC_NonNumericPin) {
            return context.getString(R.string.server_error_non_numeric_pin);
        }
        else if (code == tABC_CC.ABC_CC_InvalidPinWait) {
            if (null != error) {
                String description = error.getSzDescription();
                if (!"0".equals(description)) {
                    return context.getString(R.string.server_error_invalid_pin_wait, description);
                }
            }
            return context.getString(R.string.server_error_bad_pin);
        }
        else {
            return context.getString(R.string.server_error_other);
        }
    }
}
