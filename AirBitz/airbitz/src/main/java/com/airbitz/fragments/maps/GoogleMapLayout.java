package com.airbitz.fragments.maps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class GoogleMapLayout extends FrameLayout {

    public interface MapDragListener {
        public void onDragEnd();
    }

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L;
    private MapDragListener mListener;

    public GoogleMapLayout(Context context) {
        super(context);
    }

    public GoogleMapLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoogleMapLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            lastTouched = System.currentTimeMillis();
            break;
        case MotionEvent.ACTION_UP:
            final long now = System.currentTimeMillis();
            if (now - lastTouched > SCROLL_TIME) {
                if (mListener != null) {
                    mListener.onDragEnd();
                }
            }
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setDragListener(MapDragListener listener) {
        this.mListener = listener;
    }
}
