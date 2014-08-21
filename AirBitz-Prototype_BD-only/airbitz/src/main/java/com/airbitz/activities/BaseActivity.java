package com.airbitz.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spanned;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.airbitz.R;
import com.airbitz.fragments.HelpDialog;

import java.io.File;

public class BaseActivity extends FragmentActivity {
    private final int DIALOG_TIMEOUT_MILLIS = 60000;
    Handler mHandler;

    public void showModalProgress(final boolean show) {
        View v = findViewById(R.id.modal_indefinite_progress);
        if(show) {
            v.setVisibility(View.VISIBLE);
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true; // intercept all touches
                }
            });
            if(mHandler==null)
                mHandler = new Handler();
            mHandler.postDelayed(mProgressDialogKiller, DIALOG_TIMEOUT_MILLIS);
        } else {
            mHandler.removeCallbacks(mProgressDialogKiller);
            v.setVisibility(View.INVISIBLE);
        }
    }

    Runnable mProgressDialogKiller = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.modal_indefinite_progress).setVisibility(View.INVISIBLE);
            ShowOkMessageDialog(getResources().getString(R.string.string_connection_problem_title), getResources().getString(R.string.string_no_connection_response));
        }
    };

    public void ShowOkMessageDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ShowMessageDialogAndExit(String title, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setMessage(reason)
                .setTitle(title)
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }
}
