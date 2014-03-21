package com.airbitz.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.adapters.QuestionArrayAdapter;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;

/**
 * Created on 2/10/14.
 */
public class ForgotPasswordActivity extends Activity implements GestureDetector.OnGestureListener{

    private ListView mListView;
    private Button mSubmitButton;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mTitleTextView;

    private RelativeLayout mParentLayout;
    private ScrollView mScrollView;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);

        mListView = (ListView)findViewById(R.id.listView);
        mSubmitButton = (Button)findViewById(R.id.submitButton);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        ArrayList<String> questions = new ArrayList<String>();
        questions.add("Name of Favorite Teacher");
        questions.add("Mother's Maiden Name");
        questions.add("Favorite Food");
        questions.add("Favorite Movie");
        questions.add("Favorite Team");


        mListView.setDivider(null);
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mSubmitButton.setTypeface(LandingActivity.montserratBoldTypeFace);

        QuestionArrayAdapter listAdapter = new QuestionArrayAdapter(this, questions);

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

        mListView.setAdapter(listAdapter);
        ListViewUtility.setListViewHeightBasedOnChildren(mListView);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                Common.showHelpInfo(ForgotPasswordActivity.this, "Info", "Business directory info");
            }
        });
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

    @Override
    protected void onResume() {

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }
}
