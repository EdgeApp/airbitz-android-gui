package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ReceivedSuccessActivity extends Activity implements GestureDetector.OnGestureListener{

    private TextView mSendingTextView;
    private TextView mBitcoinAmountTextView;
    private TextView mDollarAmountTextView;
    private TextView mReceivedTextView;
    private TextView mTitleTextView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private ImageView mLogoImageView;

    private RelativeLayout mSendingLayout;
    private RelativeLayout mSuccessLayout;

    private Intent mIntent;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_success);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        mGestureDetector = new GestureDetector(this);

        mSendingTextView = (TextView) findViewById(R.id.textview_sending);
        mBitcoinAmountTextView = (TextView) findViewById(R.id.textview_bitcoin_amount);
        mDollarAmountTextView = (TextView) findViewById(R.id.textview_dollar_amount);
        mReceivedTextView = (TextView) findViewById(R.id.textview_received);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mLogoImageView = (ImageView) findViewById(R.id.imageview_logo);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mSendingLayout = (RelativeLayout) findViewById(R.id.layout_sending);
        mSuccessLayout = (RelativeLayout) findViewById(R.id.layout_success);

        mSendingTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mBitcoinAmountTextView.setTypeface(LandingActivity.montserratBoldTypeFace);
        mDollarAmountTextView.setTypeface(LandingActivity.montserratRegularTypeFace);
        mReceivedTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(ReceivedSuccessActivity.this, "Info", "Info description");
            }
        });

        new RequestSendingAsynctask().execute();
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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

    class RequestSendingAsynctask extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {

            for(int i =1;i<=10;i++){
                try {
//                    onProgressUpdate(i);
                    publishProgress(i);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0]%3 == 1){
                mLogoImageView.setImageResource(R.drawable.ico_sending_1);
                ReceivedSuccessActivity.this.mSendingTextView.setText("Sending.");
            }
            else if(values[0]%3 == 2){
                ReceivedSuccessActivity.this.mSendingTextView.setText("Sending..");
                mLogoImageView.setImageResource(R.drawable.ico_sending_2);
            }
            else if(values[0]%3 == 0){
                ReceivedSuccessActivity.this.mSendingTextView.setText("Sending...");
                mLogoImageView.setImageResource(R.drawable.ico_sending_3);
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            ReceivedSuccessActivity.this.mSendingLayout.setVisibility(View.GONE);
            ReceivedSuccessActivity.this.mSuccessLayout.setVisibility(View.VISIBLE);
            if(result == true){
                mReceivedTextView.setText("Received!!");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        mIntent = new Intent(ReceivedSuccessActivity.this, TransactionDetailActivity.class);
                        startActivity(mIntent);
                        finish();
                    }
                }, 2000 );
            }
            else{
                mReceivedTextView.setText("Failed!");
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable()
//                {
//                    @Override
//                    public void run() {
//                        mIntent = new Intent(ReceivedSuccessActivity.this, TransactionDetailActivity.class);
//                        startActivity(mIntent);
//                        finish();
//                    }
//                }, 3000 );
            }
        }
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
