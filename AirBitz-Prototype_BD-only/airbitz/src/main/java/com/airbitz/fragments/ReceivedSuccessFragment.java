package com.airbitz.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.airbitz.models.FragmentSourceEnum;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ReceivedSuccessFragment extends Fragment implements GestureDetector.OnGestureListener{

    private TextView mSendingTextView;
    private TextView mTitleTextView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private ImageView mLogoImageView;

    private RelativeLayout mSendingLayout;
    private RelativeLayout mSuccessLayout;

    private Intent mIntent;

    private Bundle mBundle;

    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBundle = this.getArguments();
        if(mBundle == null){
            System.out.println("Success is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_received_success, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mSendingTextView = (TextView) view.findViewById(R.id.textview_sending);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mLogoImageView = (ImageView) view.findViewById(R.id.imageview_logo);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mSendingLayout = (RelativeLayout) view.findViewById(R.id.layout_sending);

        mSendingTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
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
            if(mBundle.getString(WalletsFragment.FROM_SOURCE).contains("REQUEST"))
                ((NavigationActivity) getActivity()).switchToWallets(FragmentSourceEnum.REQUEST, mBundle);
            else
                ((NavigationActivity) getActivity()).switchToWallets(FragmentSourceEnum.SEND, mBundle);
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
