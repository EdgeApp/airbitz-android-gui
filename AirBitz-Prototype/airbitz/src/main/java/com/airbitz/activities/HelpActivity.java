package com.airbitz.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.utils.Common;

/**
 * Created on 3/4/14.
 */
public class HelpActivity extends Activity implements GestureDetector.OnGestureListener{
    private Button mQuestion1;
    private Button mQuestion2;
    private Button mQuestion3;
    private Button mQuestion4;

    private TextView mAnswerTextView1;
    private TextView mAnswerTextView2;
    private TextView mAnswerTextView3;
    private TextView mAnswerTextView4;
    private TextView mTitleTextView;

    private LinearLayout mQuestionLayout1;
    private LinearLayout mQuestionLayout2;
    private LinearLayout mQuestionLayout3;
    private LinearLayout mQuestionLayout4;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private boolean mQuestion1Clicked = false;
    private boolean mQuestion2Clicked = false;
    private boolean mQuestion3Clicked = false;
    private boolean mQuestion4Clicked = false;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mQuestion1 = (Button) findViewById(R.id.button_question_1);
        mQuestion2 = (Button) findViewById(R.id.button_question_2);
        mQuestion3 = (Button) findViewById(R.id.button_question_3);
        mQuestion4 = (Button) findViewById(R.id.button_question_4);

        mAnswerTextView1 = (TextView) findViewById(R.id.textview_1);
        mAnswerTextView2 = (TextView) findViewById(R.id.textview_2);
        mAnswerTextView3 = (TextView) findViewById(R.id.textview_3);
        mAnswerTextView4 = (TextView) findViewById(R.id.textview_4);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mQuestionLayout1 = (LinearLayout) findViewById(R.id.layout_separator_1);
        mQuestionLayout2 = (LinearLayout) findViewById(R.id.layout_separator_2);
        mQuestionLayout3 = (LinearLayout) findViewById(R.id.layout_separator_3);
        mQuestionLayout4 = (LinearLayout) findViewById(R.id.layout_separator_4);

        mQuestion1.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestion2.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestion3.setTypeface(LandingActivity.montserratRegularTypeFace);
        mQuestion4.setTypeface(LandingActivity.montserratRegularTypeFace);

        mAnswerTextView1.setTypeface(LandingActivity.montserratRegularTypeFace);
        mAnswerTextView2.setTypeface(LandingActivity.montserratRegularTypeFace);
        mAnswerTextView3.setTypeface(LandingActivity.montserratRegularTypeFace);
        mAnswerTextView4.setTypeface(LandingActivity.montserratRegularTypeFace);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(HelpActivity.this, "", "");
            }
        });

        mQuestion1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mQuestion1Clicked){
                    mQuestion1Clicked = false;
                    mQuestionLayout1.setVisibility(View.GONE);
                    mQuestion1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
                } else {
                    mQuestion1Clicked = true;
                    mQuestion2Clicked = false;
                    mQuestion3Clicked = false;
                    mQuestion4Clicked = false;

                    initializeDrawable();
                    mQuestion1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    mQuestionLayout1.setVisibility(View.VISIBLE);
                    mQuestionLayout2.setVisibility(View.GONE);
                    mQuestionLayout3.setVisibility(View.GONE);
                    mQuestionLayout4.setVisibility(View.GONE);
                }
            }
        });

        mQuestion2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mQuestion2Clicked){
                    mQuestion2Clicked = false;
                    mQuestionLayout2.setVisibility(View.GONE);
                    mQuestion2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
                } else {
                    mQuestion2Clicked = true;
                    mQuestion1Clicked = false;
                    mQuestion3Clicked = false;
                    mQuestion4Clicked = false;
                    initializeDrawable();
                    mQuestion2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    mQuestionLayout2.setVisibility(View.VISIBLE);
                    mQuestionLayout1.setVisibility(View.GONE);
                    mQuestionLayout3.setVisibility(View.GONE);
                    mQuestionLayout4.setVisibility(View.GONE);
                }
            }
        });

        mQuestion3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mQuestion3Clicked){
                    mQuestion3Clicked = false;
                    mQuestionLayout3.setVisibility(View.GONE);
                    mQuestion3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
                } else {
                    mQuestion3Clicked = true;
                    mQuestion2Clicked = false;
                    mQuestion1Clicked = false;
                    mQuestion4Clicked = false;
                    initializeDrawable();
                    mQuestion3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    mQuestionLayout3.setVisibility(View.VISIBLE);
                    mQuestionLayout2.setVisibility(View.GONE);
                    mQuestionLayout1.setVisibility(View.GONE);
                    mQuestionLayout4.setVisibility(View.GONE);
                }
            }
        });

        mQuestion4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mQuestion4Clicked){
                    mQuestion4Clicked = false;
                    mQuestionLayout4.setVisibility(View.GONE);
                    mQuestion4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
                } else {
                    mQuestion4Clicked = true;
                    mQuestion2Clicked = false;
                    mQuestion3Clicked = false;
                    mQuestion1Clicked = false;
                    initializeDrawable();
                    mQuestion4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    mQuestionLayout4.setVisibility(View.VISIBLE);
                    mQuestionLayout2.setVisibility(View.GONE);
                    mQuestionLayout3.setVisibility(View.GONE);
                    mQuestionLayout1.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void initializeDrawable(){

        mQuestion1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
        mQuestion2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
        mQuestion3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
        mQuestion4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_dropdown, 0);
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

    @Override
    protected void onResume() {

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }
}
