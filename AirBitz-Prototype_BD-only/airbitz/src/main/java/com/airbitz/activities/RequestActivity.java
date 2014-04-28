package com.airbitz.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.utils.Common;

/**
 * Created on 2/13/14.
 */
public class RequestActivity extends Activity implements KeyboardView.OnKeyboardActionListener, GestureDetector.OnGestureListener{

    private EditText mBitcoinField;
    private EditText mDollarField;

    private Button mWalletButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private ImageButton mImportWalletButton;
    private ImageButton mEmailButton;
    private ImageButton mSmsButton;
    private ImageButton mQRCodeButton;

    private TextView mTitleTextView;
    private TextView mWalletTextView;
    private TextView mConverterTextView;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;
    private LinearLayout mFocusDistractorLayout;

    private ScrollView mScrollView;

    public static String CLASSNAME = "CLASSNAME";

    private Keyboard mKeyboard;
    private KeyboardView mKeyboardView;

    private Intent mIntent;
    private ClipboardManager clipboard;

    public final static int CodeDelete   = -5;
    public final static int CodeCancel   = -3;
    private int mButtonWidth = 0;

    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        mGestureDetector = new GestureDetector(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);
        mFocusDistractorLayout = (LinearLayout) findViewById(R.id.layout_focus_distractor);
        mFocusDistractorLayout.requestFocus();

        mScrollView = (ScrollView) findViewById(R.id.layout_amount);

        mKeyboard = new Keyboard(RequestActivity.this, R.xml.layout_keyboard);
        mKeyboardView = (KeyboardView) findViewById(R.id.layout_calculator);

        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(RequestActivity.this);
        mKeyboardView.setBackgroundResource(R.drawable.bg_calc);

        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);

        mBitcoinField = (EditText) findViewById(R.id.edittext_btc);
        mDollarField = (EditText) findViewById(R.id.edittext_dollar);
        mWalletButton = (Button) findViewById(R.id.button_wallet);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);
        mImportWalletButton = (ImageButton) findViewById(R.id.button_importwallet);
        mEmailButton = (ImageButton) findViewById(R.id.button_email);
        mSmsButton = (ImageButton) findViewById(R.id.button_sms);
        mQRCodeButton = (ImageButton) findViewById(R.id.button_qrcode);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mWalletTextView = (TextView) findViewById(R.id.textview_wallet);
        mConverterTextView = (TextView) findViewById(R.id.textview_converter);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mWalletTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mWalletButton.setTypeface(LandingActivity.montserratRegularTypeFace);
        mBitcoinField.setTypeface(LandingActivity.montserratRegularTypeFace);
        mDollarField.setTypeface(LandingActivity.montserratRegularTypeFace);
        mConverterTextView.setTypeface(LandingActivity.montserratRegularTypeFace);


        mWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(RequestActivity.this, WalletActivity.class);
                mIntent.putExtra(CLASSNAME, "RequestActivity");
                startActivity(mIntent);
            }
        });

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mImportWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(RequestActivity.this, ImportActivity.class);
                startActivity(mIntent);
            }
        });

        mSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("sms:"));
                startActivity(smsIntent);
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                emailIntent.setType("text/html");
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Content description");
                startActivity(Intent.createChooser(emailIntent, "Email:"));
            }
        });

        mBitcoinField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    showCustomKeyboard(view);
                }
                else{
                    hideCustomKeyboard();
                }
            }
        });

        mQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQRCodePopUpDialog();
            }
        });


        mQRCodeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = android.content.ClipData.newPlainText("private key","SdfnsdjsdnfdsnofmsdfwemfyweorwekrewojfewmfoewmfwdpsajdfewormewjwpqodenwnfiwefjweofjewofnewnfoeiwjfewnfoiewfnewiofnewofewinfewpfSdfnsdjsdnfdsnofmsdfwemfyweorwekrewojfewmfoewmfwdpsajdfewormewjwpqodenwnfiwefjweofjewofnewnfoeiwjfewnfoiewfnewiofnewofewinfewpf");
                clipboard.setPrimaryClip(clip);

                return true;
            }
        });


        mDollarField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    showCustomKeyboard(view);
                }
                else{
                    hideCustomKeyboard();
                }
            }
        });

        mBitcoinField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomKeyboard(view);
            }
        });


        mDollarField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomKeyboard(view);
            }
        });

        mBitcoinField.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {

                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });


        mDollarField.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(RequestActivity.this, "Info", "Business directory info");
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int importWidth = mImportWalletButton.getWidth();
        int emailWidth = mEmailButton.getWidth();
        int smsWidth = mSmsButton.getWidth();
        int qrCodeWidth = mQRCodeButton.getWidth();

        mImportWalletButton.getLayoutParams().height = importWidth;
        mEmailButton.getLayoutParams().height = emailWidth;
        mSmsButton.getLayoutParams().height = smsWidth;
        mQRCodeButton.getLayoutParams().height = qrCodeWidth;

    }

    public void showQRCodePopUpDialog(){
        final Dialog dialog = new Dialog(RequestActivity.this);
        dialog.setContentView(R.layout.dialog_qrcode);
        ImageView imageviewQRCode = (ImageView) dialog.findViewById(R.id.imageview_qrcode);
        imageviewQRCode.setImageResource(R.drawable.img_qr_code);
        Button okButton = (Button) dialog.findViewById(R.id.button_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override public void onBackPressed() {
        if( isCustomKeyboardVisible() ) hideCustomKeyboard(); else this.finish();
    }

    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showCustomKeyboard( View v ) {
        if( v!=null ) ((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);

        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
   }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onResume() {
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int keyCode, int[] keyCodes) {

        View focusCurrent = RequestActivity.this.getWindow().getCurrentFocus();
        if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) return;
        EditText edittext = (EditText) focusCurrent;
        Editable editable = edittext.getText();
        int start = edittext.getSelectionStart();

        if( keyCode==CodeCancel ) {
            hideCustomKeyboard();
        } else if( keyCode==CodeDelete ) {
            if( editable!=null && start>0 ) editable.delete(start - 1, start);
        } else {
            editable.insert(start, Character.toString((char) keyCode));
        }

    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start != null & finish != null){

            float yDistance = Math.abs(finish.getY() - start.getY());

            if((finish.getRawX()>start.getRawX()) && (yDistance < 15)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 50){
                    finish();
                    return true;
                }
            }

        }

        return false;
    }

}
