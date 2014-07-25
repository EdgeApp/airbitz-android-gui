package com.airbitz.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.ExportAdapter;
import com.airbitz.utils.Common;

/**
 * Created on 2/22/14.
 */
public class ExportSavingOptionFragment extends Fragment implements GestureDetector.OnGestureListener{

    private EditText mAccountEdittext;
    private EditText mFromEdittext;
    private EditText mToEdittext;

    private TextView mTitleTextView;
    private TextView mAccountTexView;
    private TextView mFromTextView;
    private TextView mToTextView;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

    private Button mPrintButton;
    private Button mEmailButton;
    private Button mGoogleDriveButton;
    private Button mDropBoxButton;

    private ScrollView mScrollView;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private ExportAdapter mExportAdapter;

    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private View mView;
    @Override public void onDestroyView() {
        super.onDestroyView();
        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
        if( null != parentViewGroup ) {
            parentViewGroup.removeView( mView );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        mView = inflater.inflate(R.layout.fragment_export, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_root);
        mNavigationLayout = (RelativeLayout) mView.findViewById(R.id.navigation_layout);

        mTitleTextView = (TextView) mView.findViewById(R.id.textview_title);

        mScrollView = (ScrollView) mView.findViewById(R.id.layout_scroll);

        mBackButton = (ImageButton) mView.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.button_help);

        mAccountEdittext = (EditText) mView.findViewById(R.id.edittext_account);
        mFromEdittext = (EditText) mView.findViewById(R.id.edittext_from);
        mToEdittext = (EditText) mView.findViewById(R.id.edittext_to);

        mAccountTexView = (TextView) mView.findViewById(R.id.textview_account);
        mFromTextView = (TextView) mView.findViewById(R.id.textview_from);
        mToTextView = (TextView) mView.findViewById(R.id.textview_to);

        mAccountEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mToEdittext.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mAccountTexView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mFromTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mToTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mFromEdittext.setKeyListener(null);
        mToEdittext.setKeyListener(null);

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

        return mView;
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
