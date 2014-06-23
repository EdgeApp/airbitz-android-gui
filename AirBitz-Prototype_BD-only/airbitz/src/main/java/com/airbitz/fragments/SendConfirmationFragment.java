package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationFragment extends Fragment{

    private TextView mFromEdittext;
    private TextView mToEdittext;
    private EditText mPinEdittext;

    private TextView mTitleTextView;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mValueTextView;
    private TextView mSlideTextView;
    private TextView mConfirmTextView;
    private TextView mPinTextView;

    private Bundle bundle;

    private boolean mConfirmChecked = false;

    private EditText mDollarValueField;
    private EditText mBitcoinValueField;

    private ImageButton mHelpButton;
    private ImageButton mBackButton;
    private ImageButton mConfirmSwipeButton;

    private float mConfirmCenter;
    private float dX = 0;
    private float rX = 0;

    private boolean doSet = false;

    private RelativeLayout mSlideLayout;

    private ImageView mConfirmImageView;

    private RelativeLayout mParentLayout;

    private ScrollView mScrollView;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mRightThreshold;
    private int mLeftThreshold;

    private boolean mSuccess = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        if(bundle == null){
            System.out.println("Send confirmation bundle is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_confirmation, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mConfirmSwipeButton = (ImageButton) view.findViewById(R.id.button_confirm_swipe);

        mFromTextView = (TextView) view.findViewById(R.id.textview_from);
        mToTextView = (TextView) view.findViewById(R.id.textview_to);
        mValueTextView = (TextView) view.findViewById(R.id.textview_value);
        mSlideTextView = (TextView) view.findViewById(R.id.textview_slide);
        mConfirmTextView = (TextView) view.findViewById(R.id.textview_confirm);
        mPinTextView = (TextView) view.findViewById(R.id.textview_pin);

        mFromEdittext = (TextView) view.findViewById(R.id.textview_from_name);
        mToEdittext = (TextView) view.findViewById(R.id.textview_to_name);
        mPinEdittext = (EditText) view.findViewById(R.id.edittext_pin);

        mDollarValueField = (EditText) view.findViewById(R.id.button_bitcoin_balance);
        mBitcoinValueField = (EditText) view.findViewById(R.id.button_dollar_balance);

        mSlideLayout = (RelativeLayout) view.findViewById(R.id.layout_slide);

        mConfirmImageView = (ImageView) view.findViewById(R.id.imageview_confirm);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mToEdittext.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mToTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
        mValueTextView.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPinTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mSlideTextView.setTypeface(NavigationActivity.latoBlackTypeFace, Typeface.BOLD);
        mConfirmTextView.setTypeface(NavigationActivity.latoBlackTypeFace);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_root);
        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mConfirmCenter = mConfirmSwipeButton.getWidth()/2;


        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{Color.parseColor("#ffffff"),Color.parseColor("#addff1")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mSlideTextView.getPaint().setShader(textShader);

        mConfirmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mConfirmChecked){
                    mConfirmImageView.setImageResource(R.drawable.btn_confirm_off);
                    mConfirmChecked = false;
                } else {
                    mConfirmImageView.setImageResource(R.drawable.btn_confirm_on);
                    mConfirmChecked = true;
                }
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });


        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        mSlideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSuccess){
                    mLeftThreshold = (int) mSlideLayout.getX();
                }
            }
        });

        mConfirmSwipeButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSuccess){
                    mRightThreshold = (int) mConfirmSwipeButton.getX();
                }
            }
        });

        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivePointerId = event.getPointerId(0);
                        dX = (int) event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        rX = event.getRawX();
                        float delta = rX - dX;
                        if (delta < mLeftThreshold-mConfirmCenter) {
                            mConfirmSwipeButton.setX(mLeftThreshold - mConfirmCenter);
                        } else if (delta > mRightThreshold) {
                            mConfirmSwipeButton.setX(mRightThreshold);
                        } else {
                            mConfirmSwipeButton.setX(delta);
                        }
                        //mConfirmSwipeButton.invalidate();
                        return false;
                    case MotionEvent.ACTION_UP:
                        touchEventsEnded();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mConfirmSwipeButton.setX(mRightThreshold);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                        /*pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                        final int pointerId = event.getPointerId(pointerIndex);
                        if (pointerId == mActivePointerId) {
                            touchEventsEnded();
                        }*/
                        break;
                    default:
                        break;
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

        mDollarValueField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mBitcoinValueField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }

    public void touchEventsEnded(){
        int successThreshold = mLeftThreshold + (mSlideLayout.getWidth()/4);
        if(mConfirmSwipeButton.getX() <= successThreshold){
            Fragment frag = new ReceivedSuccessFragment();
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag);
        }else{
            mConfirmSwipeButton.setX(mRightThreshold);
        }
    }
}
