package com.airbitz.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.ImageViewPagerAdapter;
import com.airbitz.adapters.TouchImageViewPagerAdapter;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.widgets.TouchImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 10/27/14.
 */
public class ViewPagerFragment extends Fragment {
    final String TAG = getClass().getSimpleName();

    private HighlightOnPressImageButton mQuitButton;
    private List<TouchImageView> mImageViews = new ArrayList<TouchImageView>();
    private ViewPager mViewPager;
    private View mViewpagerView;
    private SeekBar mSeekBar;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();
    private int mPosition;
    private boolean mSeekbarReposition = false;
    private final int SEEKBAR_MAX = 100;
    private float mScale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_viewpager, container, false);

        mViewpagerView = mView.findViewById(R.id.fragment_viewpager);

        mQuitButton = (HighlightOnPressImageButton) mView.findViewById(R.id.viewpager_close_button);
        mQuitButton.setVisibility(View.VISIBLE);
        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mActivity = (NavigationActivity) getActivity();
        mActivity.showModalProgress(true);
        mHandler.postDelayed(loadBackground, 100);


        mScale = SEEKBAR_MAX / (mImageViews.size() - 1);

        mViewPager = (ViewPager) mView.findViewById(R.id.fragment_viewpager_viewpager);
        mViewPager.setAdapter(new TouchImageViewPagerAdapter(mImageViews));
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) { }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffsetPixels == 0) {
                    mPosition = position;
                    mActivity.showModalProgress(true);
                    mSeekBar.setProgress((int) (position * mScale));
                    mHandler.post(loadBackground);
                }
            }

            public void onPageSelected(int position) {

            }
        });

        mSeekBar = (SeekBar) mView.findViewById(R.id.viewpager_seekBar);
        if(mImageViews.size()>1) {
            mSeekBar.setMax(SEEKBAR_MAX);
            mSeekBar.setProgress(mPosition);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (!mSeekbarReposition) {
                        float nearestValue = Math.round(i / mScale);
                        mViewPager.setCurrentItem((int) nearestValue, true);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    float nearestValue = Math.round(progress / mScale);
                    mSeekbarReposition = true;
                    mSeekBar.setProgress((int) (nearestValue * mScale));
                    mSeekbarReposition = false;
                }
            });
        } else {
            mSeekBar.setVisibility(View.INVISIBLE);
        }

        return mView;
    }

    Runnable loadBackground = new Runnable() {
        @Override
        public void run() {
            Drawable drawable = mImageViews.get(mPosition).getDrawable();
            if (drawable != null) {
                mActivity.showModalProgress(false);
                Bitmap bm = drawableToBitmap(drawable);
                mViewpagerView.setBackground(new BitmapDrawable(getResources(), blur(4, bm)));
            } else {
                mHandler.postDelayed(loadBackground, 100);
            }
        }
    };

    public void setImages(List<TouchImageView> imageViews, int position) {
        mImageViews = imageViews;
        mPosition = position;
    }

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

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public Bitmap blur(int radius, Bitmap original) {
        int w = 20; //original.getWidth()/64;
        int h = 30; //original.getHeight()/64;

        Bitmap out = Bitmap.createScaledBitmap(original, w, h, true);

        int[] pix = new int[w * h];
        out.getPixels(pix, 0, w, 0, 0, w, h);

        for(int r = radius; r >= 1; r /= 2) {
            for(int i = r; i < h - r; i++) {
                for(int j = r; j < w - r; j++) {
                    int tl = pix[(i - r) * w + j - r];
                    int tr = pix[(i - r) * w + j + r];
                    int tc = pix[(i - r) * w + j];
                    int bl = pix[(i + r) * w + j - r];
                    int br = pix[(i + r) * w + j + r];
                    int bc = pix[(i + r) * w + j];
                    int cl = pix[i * w + j - r];
                    int cr = pix[i * w + j + r];

                    pix[(i * w) + j] = 0xFF000000 |
                            (((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc & 0xFF) + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF |
                            (((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br & 0xFF00) + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00 |
                            (((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000) + (br & 0xFF0000) + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
                }
            }
        }
        out.setPixels(pix, 0, w, 0, 0, w, h);
        return out;
    }
}
