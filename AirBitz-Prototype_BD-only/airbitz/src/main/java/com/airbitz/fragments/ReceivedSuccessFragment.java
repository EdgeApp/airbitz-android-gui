package com.airbitz.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ReceivedSuccessFragment extends Fragment implements GestureDetector.OnGestureListener{

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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_received_success, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mGestureDetector = new GestureDetector(this);

        mSendingTextView = (TextView) view.findViewById(R.id.textview_sending);
        mBitcoinAmountTextView = (TextView) view.findViewById(R.id.textview_bitcoin_amount);
        mDollarAmountTextView = (TextView) view.findViewById(R.id.textview_dollar_amount);
        mReceivedTextView = (TextView) view.findViewById(R.id.textview_received);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mLogoImageView = (ImageView) view.findViewById(R.id.imageview_logo);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mSendingLayout = (RelativeLayout) view.findViewById(R.id.layout_sending);
        mSuccessLayout = (RelativeLayout) view.findViewById(R.id.layout_success);

        mSendingTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitcoinAmountTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mDollarAmountTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mReceivedTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Info description");
            }
        });

        new RequestSendingAsynctask().execute();

        return view;
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
                ReceivedSuccessFragment.this.mSendingTextView.setText("Sending.");
            }
            else if(values[0]%3 == 2){
                ReceivedSuccessFragment.this.mSendingTextView.setText("Sending..");
                mLogoImageView.setImageResource(R.drawable.ico_sending_2);
            }
            else if(values[0]%3 == 0){
                ReceivedSuccessFragment.this.mSendingTextView.setText("Sending...");
                mLogoImageView.setImageResource(R.drawable.ico_sending_3);
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            ReceivedSuccessFragment.this.mSendingLayout.setVisibility(View.GONE);
            ReceivedSuccessFragment.this.mSuccessLayout.setVisibility(View.VISIBLE);
            if(result == true){
                mReceivedTextView.setText("Received!!");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
//                        mIntent = new Intent(ReceivedSuccessActivity.this, TransactionDetailActivity.class);
//                        startActivity(mIntent);
//                        finish();
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
