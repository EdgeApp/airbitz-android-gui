package com.airbitz.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/24/14.
 */
public class WalletPasswordFragment extends Fragment implements GestureDetector.OnGestureListener{

    private EditText mPasswordEdittext;
    private ImageView mValidPasswordImageView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private GestureDetector mGestureDetector;
    private Intent mIntent;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_password, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mGestureDetector = new GestureDetector(this);

        mPasswordEdittext = (EditText) view.findViewById(R.id.wallet_password_edittext_password);
        mValidPasswordImageView = (ImageView) view.findViewById(R.id.imageview_valid_password);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);


        TextView titleTextView = (TextView) view.findViewById(R.id.textview_title);
        titleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mPasswordEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if(!hasFocus){
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                   if(((EditText) view).getText().length() != 0){

                   }
                }
            }
        });

        mPasswordEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                int keyAction = keyEvent.getAction();
                String test = "";
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);


                if(keyAction == KeyEvent.ACTION_UP){
                    switch (keyCode) {
                        case KeyEvent.FLAG_EDITOR_ACTION:
                            imm.hideSoftInputFromWindow(mPasswordEdittext.getWindowToken(), 0);
                            mValidPasswordImageView.setVisibility(View.VISIBLE);

                            if(mPasswordEdittext.getText().toString().equals("Password")){
                                mValidPasswordImageView.setImageResource(R.drawable.ico_approved);
                            }
                            else{
                                mValidPasswordImageView.setImageResource(R.drawable.ico_not_approved);
                            }
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            imm.hideSoftInputFromWindow(mPasswordEdittext.getWindowToken(), 0);
                            mValidPasswordImageView.setVisibility(View.VISIBLE);

                            if(mPasswordEdittext.getText().toString().equals("Password")){
                                mValidPasswordImageView.setImageResource(R.drawable.ico_approved);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run() {
//                                        mIntent = new Intent(WalletPasswordFragment.this, TransactionActivity.class);
//                                        startActivity(mIntent);
//                                        finish();
                                    }
                                }, 2000 );
                            }
                            else{
                                mValidPasswordImageView.setImageResource(R.drawable.ico_not_approved);
                            }

                            return true;
                        default:
                            return false;
                    }
                }

                return false;
            }

        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        return view;
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        mValidPasswordImageView.setVisibility(View.GONE);
//    }

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
                    getActivity().onBackPressed();
                    return true;
                }
            }

        }

        return false;
    }
}
