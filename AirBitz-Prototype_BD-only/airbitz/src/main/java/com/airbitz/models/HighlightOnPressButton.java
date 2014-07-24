package com.airbitz.models;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by matt on 7/23/14.
 */
public class HighlightOnPressButton extends Button implements View.OnTouchListener{

    public HighlightOnPressButton(Context context) {
        super(context);
    }

    public HighlightOnPressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightOnPressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                view.setAlpha(.6f);
                break;
            case MotionEvent.ACTION_UP:
                view.setAlpha(1);
                break;
            case MotionEvent.ACTION_CANCEL:
                view.setAlpha(1);
                break;
        }
        return super.onTouchEvent(motionEvent);
    }
}
