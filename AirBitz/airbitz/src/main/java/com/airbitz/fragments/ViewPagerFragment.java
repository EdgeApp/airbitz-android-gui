package com.airbitz.fragments;

import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airbitz.R;
import com.airbitz.adapters.ImageViewPagerAdapter;
import com.airbitz.objects.HighlightOnPressImageButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 10/27/14.
 */
public class ViewPagerFragment extends Fragment {
    final String TAG = getClass().getSimpleName();

    private HighlightOnPressImageButton mQuitButton;
    private List<ImageView> mImageViews = new ArrayList<ImageView>();
    private ViewPager mViewPager;
    private int mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_viewpager, container, false);

        mQuitButton = (HighlightOnPressImageButton) mView.findViewById(R.id.viewpager_close_button);
        mQuitButton.setVisibility(View.VISIBLE);
        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mViewPager = (ViewPager) mView.findViewById(R.id.fragment_viewpager_viewpager);
        mViewPager.setAdapter(new ImageViewPagerAdapter(mImageViews));
        mViewPager.setCurrentItem(mPosition);
        for(ImageView iv : mImageViews) {
            iv.setOnTouchListener(new ImageTouchListener());
        }

        return mView;
    }

    public void setImages(List<ImageView> imageViews, int position) {
        mImageViews = imageViews;
        mPosition = position;
    }

    private class ImageTouchListener implements View.OnTouchListener {
        // These matrices will be used to move and zoom image
        Matrix matrix = new Matrix();
        Matrix savedMatrix = new Matrix();

        // Touches can be in one of these 3 states
        final int NONE = 0;
        final int DRAG = 1;
        final int ZOOM = 2;
        int mode = NONE;

        // Remember some things for zooming
        PointF startPoint = new PointF();
        PointF midPoint = new PointF();
        float oldDist = 1f;
        boolean firstTouch = true;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            ImageView view = (ImageView) v;
            view.setScaleType(ImageView.ScaleType.MATRIX);
            float scale;

            // Handle touch events here...
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN: //first finger down only
                    if(firstTouch) { // setup initial matrix
                        RectF drawableRectF = new RectF(0, 0, view.getDrawable().getBounds().width(), view.getDrawable().getBounds().height());
                        RectF viewRectF = new RectF(0, 0, view.getWidth(), view.getHeight());
                        matrix.setRectToRect(drawableRectF, viewRectF, Matrix.ScaleToFit.CENTER);
                        Log.d(TAG, "first touch");
                        firstTouch = false;
                    }
                    savedMatrix.set(matrix);
                    startPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    Log.d(TAG, "action down");
                    break;

                case MotionEvent.ACTION_UP: //first finger lifted

                case MotionEvent.ACTION_POINTER_UP: //second finger lifted
                    mode = NONE;
                    Log.d(TAG, "action up");
                    break;

                case MotionEvent.ACTION_POINTER_DOWN: //second finger down
                    oldDist = spacing(event);
                    if (oldDist > 5f) {
                        savedMatrix.set(matrix);
                        midPoint(midPoint, event);
                        mode = ZOOM;
                    }
                    Log.d(TAG, "action pointer up");
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) { //movement of first finger
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                    }
                    else if (mode == ZOOM) { //pinch zooming
                        float newDist = spacing(event);
                        if (newDist > 5f) {
                            matrix.set(savedMatrix);
                            scale = newDist / oldDist;
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }
                    }
                    Log.d(TAG, "action move");
                    break;

            }

            // Perform the transformation
            view.setImageMatrix(matrix);

            return true; // indicate event was handled
        }
    };

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
