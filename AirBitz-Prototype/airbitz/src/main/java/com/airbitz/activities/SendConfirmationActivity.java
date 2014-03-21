package com.airbitz.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
import android.support.v4.view.MotionEventCompat;

import com.airbitz.R;
import com.airbitz.utils.Common;

/**
 * Created on 2/21/14.
 */
public class SendConfirmationActivity extends Activity implements GestureDetector.OnGestureListener {

    private EditText mFromEdittext;
    private EditText mToEdittext;
    private EditText mPinEdittext;

    private TextView mTitleTextView;
    private TextView mFromTextView;
    private TextView mToTextView;
    private TextView mValueTextView;
    private TextView mSlideTextView;
    private TextView mConfirmTextView;
    private TextView mPinTextView;

    private boolean mConfirmChecked = false;

    private Button mDollarValueButton;
    private Button mBitcoinValueButton;

    private ImageButton mHelpButton;
    private ImageButton mBackButton;
    private ImageButton mConfirmSwipeButton;

    private LinearLayout mSlideLayout;

    private ImageView mConfirmImageView;

    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;

    private ScrollView mScrollView;

    private Intent mIntent;

    private GestureDetector mGestureDetector;

    private int mSliderHeight = 0;
    private int mSliderWidth = 0;

    private int mSlideLayoutHeight = 0;
    private int mSlideLayoutWidth = 0;


    private float aPosX;
    private float aLastTouchX;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mRightThreshold;
    private int mLeftThreshold;
    private int mDisplayWidth;

    private boolean mSuccess = false;

    private boolean mButtonTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_confirmation);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mGestureDetector = new GestureDetector(this, this);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);
        mConfirmSwipeButton = (ImageButton) findViewById(R.id.button_confirm_swipe);

        mFromTextView = (TextView) findViewById(R.id.textview_from);
        mToTextView = (TextView) findViewById(R.id.textview_to);
        mValueTextView = (TextView) findViewById(R.id.edittext_from);
        mSlideTextView = (TextView) findViewById(R.id.edittext_from);
        mConfirmTextView = (TextView) findViewById(R.id.edittext_from);
        mPinTextView = (TextView) findViewById(R.id.textview_pin);

        mFromEdittext = (EditText) findViewById(R.id.edittext_from);
        mToEdittext = (EditText) findViewById(R.id.edittext_to);
        mPinEdittext = (EditText) findViewById(R.id.edittext_pin);

        mDollarValueButton = (Button) findViewById(R.id.button_bitcoin_balance);
        mBitcoinValueButton = (Button) findViewById(R.id.button_dollar_balance);

        mSlideLayout = (LinearLayout) findViewById(R.id.layout_slide);

        mConfirmImageView = (ImageView) findViewById(R.id.imageview_confirm);
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace, Typeface.BOLD);
        mFromEdittext.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mToEdittext.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);

        mFromTextView.setTypeface(LandingActivity.latoBlackTypeFace);
        mToTextView.setTypeface(LandingActivity.latoBlackTypeFace);
        mValueTextView.setTypeface(LandingActivity.helveticaNeueTypeFace);
        mPinTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mSlideTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.BOLD);
        mConfirmTextView.setTypeface(LandingActivity.latoBlackTypeFace);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_root);
        mScrollView = (ScrollView) findViewById(R.id.layout_scroll);


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


//        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                } else {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(mSlideLayout.isFocused()){
                    return false;
                }
                else if(!mSlideLayout.isFocused()){
                    return mGestureDetector.onTouchEvent(motionEvent);
                }

                return false;
            }
        });


        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(mSlideLayout.isFocused()||mConfirmSwipeButton.isFocused()){
//                    return true;
//                } else
                if(!mSlideLayout.isFocused()){
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
                return false;
            }
        });

        mSlideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSuccess){
                    mLeftThreshold = mSlideLayout.getLeft();
                }
            }
        });

        mConfirmSwipeButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSuccess){
                    mRightThreshold = (int) mConfirmSwipeButton.getX();
//                    mLeftThreshold += mConfirmSwipeButton.getWidth();
                }
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;

//        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                TranslateAnimation animation = new TranslateAnimation(0, -1*mDisplayWidth, 0, 0);
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                        // Save the ID of this pointer
//                        mActivePointerId = event.getPointerId(0);
//                        final float x = event.getX(mActivePointerId);
//                        final float y = event.getY(mActivePointerId);
//                        // Remember where we started
//                        aLastTouchX = x;
////to prevent an initial jump of the magnifier, aposX and aPosY must
////have the values from the magnifier frame
//                        if (aPosX == 0){
//                            aPosX = mConfirmSwipeButton.getX();
//                        }
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        animation.setDuration(500);
//                        animation.setFillAfter(false);
//                        animation.setAnimationListener(new Animation.AnimationListener() {
//                            @Override
//                            public void onAnimationStart(Animation animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animation animation) {
//                                if(!mSuccess){
//                                    mSuccess = true;
//                                    mConfirmSwipeButton.setVisibility(View.INVISIBLE);
//                                    mIntent = new Intent(SendConfirmationActivity.this, ReceivedSuccessActivity.class);
//                                    startActivity(mIntent);
//                                    finish();
//                                }
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animation animation) {
//
//                            }
//                        });
//
//                        mConfirmSwipeButton.startAnimation(animation);
//                        break;
//
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        break;
//
//                    case MotionEvent.ACTION_POINTER_UP:
////                        // Extract the index of the pointer that left the touch sensor
////                        final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
////                        final int pointerId = event.getPointerId(pointerIndex);
////                        if (pointerId == mActivePointerId) {
////                            // This was our active pointer going up. Choose a new
////                            // active pointer and adjust accordingly.
////                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
////                            mActivePointerId = event.getPointerId(newPointerIndex);
////                        }
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//
//                        // Find the index of the active pointer and fetch its position
//                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
//                        float xMove = event.getX(pointerIndexMove);
//                        float yMove = event.getY(pointerIndexMove);
//
//                        // Calculate the distance moved
//                        final float dx = xMove - aLastTouchX;
//
//                        // Move the frame
//                        aPosX += dx;
//                        if(aPosX>mRightThreshold){
//                            aPosX = mRightThreshold;
//                        }
//                        if(aPosX<=mLeftThreshold){
//                            if(!mSuccess){
//                                mSuccess = true;
//                                mIntent = new Intent(SendConfirmationActivity.this, ReceivedSuccessActivity.class);
//                                startActivity(mIntent);
//                                finish();
//                            }
//                        }
//
//// Remember this touch position for the next move event
////no! see http://stackoverflow.com/questions/17530589/jumping-imageview-while-dragging-getx-and-gety-values-are-jumping?rq=1 and
//// last comment in http://stackoverflow.com/questions/16676097/android-getx-gety-interleaves-relative-absolute-coordinates?rq=1
////aLastTouchX = xMove;
////aLastTouchY = yMove;
//
////in this area would be code for doing something with the magnified view as the frame moves.
//                        mConfirmSwipeButton.setX(aPosX);
//                        break;
//
//                    case MotionEvent.ACTION_CANCEL: {
//                        animation.setDuration(500);
//                        animation.setFillAfter(false);
//                        animation.setAnimationListener(new Animation.AnimationListener() {
//                            @Override
//                            public void onAnimationStart(Animation animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animation animation) {
//                                if(!mSuccess){
//                                    mSuccess = true;
//                                    mConfirmSwipeButton.setVisibility(View.INVISIBLE);
//                                    mIntent = new Intent(SendConfirmationActivity.this, ReceivedSuccessActivity.class);
//                                    startActivity(mIntent);
//                                    finish();
//                                }
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animation animation) {
//
//                            }
//                        });
//
//                        mConfirmSwipeButton.startAnimation(animation);
//                        mActivePointerId = INVALID_POINTER_ID;
//                        break;
//                    }
//                }
//
//                return true;
//            }
//        });

//        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                String TAG = "TEST MOVE";
//                int action = MotionEventCompat.getActionMasked(event);
//                switch(action) {
//                    case (MotionEvent.ACTION_DOWN) :
//                        Log.d(TAG, "action down");
//                        // Save the ID of this pointer
//                        mActivePointerId = event.getPointerId(0);
//                        aLastTouchX = event.getX(mActivePointerId);
//                        if (aPosX == 0){
//                            aPosX = mConfirmSwipeButton.getRight();
//                        }
//                        return true;
//
//                    case (MotionEvent.ACTION_MOVE) :
//                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
//                        Log.d(TAG, "action move");
//                        float xMove = event.getY(pointerIndexMove);
//
//                        final float dy = xMove - aLastTouchX;
//
//                        aPosX += dy;
//
////                        if(aPosX >mDragBarThreshold){
////                            aPosX = mDragBarThreshold;
////                        }
//
//                        mConfirmSwipeButton.set ((int) (aPosX));
////                        mTopLayout.setLayoutParams(param);
//                        return true;
//                    default :
//                        return true;
//                }
//            }
//        });
//        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
//
//            float x1 = 0
//                    ,
//                    x2 = 0;
//
//            float sensitivity = 50;
//            float xDistance = 0;
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                boolean moving = false;
//                switch (motionEvent.getAction()) {
//                    // when user first touches the screen we get x and y coordinate
//                    case MotionEvent.ACTION_DOWN:
//                        final int x = (int) motionEvent.getX();
//                        final int y = (int) motionEvent.getY();
////                        final Rect bounds = mConfirmSwipeButton.getBounds();
//
////                        moving = bounds.intersects(x, y, x + 1, y + 1);
//
//                        moving = true;
//
//                        return true;
//                    case MotionEvent.ACTION_UP:
//
//                        moving = false;
//
//                        if(!moving){
//
//                        }
//
//
////                        x2 = motionEvent.getX();
////
////                        xDistance = x1 - x2;
////
////                        if (xDistance >= sensitivity) {
////                            mIntent = new Intent(SendConfirmationActivity.this, ReceivedSuccessActivity.class);
////                            startActivity(mIntent);
//////                            Toast.makeText(SendConfirmationActivity.this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
////                            return true;
////                        }
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//
////                        if (moving) {
////                            final int x_new = (int) event.getX();
////                            final int y_new = (int) event.getY();
////                            mDrawTiles.draw(new DrawLogic() {
////                                @Override
////                                public void draw(Rect _surface) {
////                                    mTiles.setBounds(
////                                            x_new - mDrawWidth / 2,
////                                            y_new - mDrawHeight / 2,
////                                            x_new + mDrawWidth / 2,
////                                            y_new + mDrawHeight / 2);
////                                }
////                            });
////                        }
//                        return true;
//                    default:
//                        return false;
//                }
//                return false;
//            }
//        });



        mConfirmSwipeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mButtonTouched = true;
                return mGestureDetector.onTouchEvent(event);

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
                Common.showHelpInfo(SendConfirmationActivity.this, "Info", "Business directory info");
            }
        });

        mDollarValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mBitcoinValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        mSliderHeight = mConfirmSwipeButton.getHeight();
        mSliderWidth = mConfirmSwipeButton.getWidth();

        mSlideLayoutWidth = mSlideLayout.getWidth();
        mSlideLayoutHeight = mSlideLayout.getHeight();


        if(widthPixels <= 480){

            float scale = SendConfirmationActivity.this.getResources().getDisplayMetrics().density;
            int pixels = (int) (250 * scale + 0.5f);
            mFromTextView.getLayoutParams().width = pixels;
            pixels = (int) (250 * scale + 0.5f);
            mToTextView.getLayoutParams().width = pixels;

        }


    }


    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            if(mButtonTouched && (finish.getRawX()<start.getRawX()) && (yDistance < 15)){

                mButtonTouched = false;
                TranslateAnimation animation = new TranslateAnimation(0, -1*mDisplayWidth, 0, 0);
                animation.setDuration(500);
                animation.setFillAfter(false);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(!mSuccess){
                            mSuccess = true;
                            mConfirmSwipeButton.setVisibility(View.INVISIBLE);
                            mIntent = new Intent(SendConfirmationActivity.this, ReceivedSuccessActivity.class);
                            startActivity(mIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                mConfirmSwipeButton.startAnimation(animation);
            }

        }

        return false;
    }

}
