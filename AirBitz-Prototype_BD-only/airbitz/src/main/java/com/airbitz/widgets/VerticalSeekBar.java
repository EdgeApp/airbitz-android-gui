package com.airbitz.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airbitz.R;

/**
 * Created by tom on 5/6/14.
 */
public class VerticalSeekBar extends SeekBar {

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    private OnSeekBarChangeListener onChangeListener;
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onChangeListener){
        this.onChangeListener = onChangeListener;
    }

    private int lastProgress = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onChangeListener.onStartTrackingTouch(this);
                setPressed(true);
                setSelected(true);
                break;
            case MotionEvent.ACTION_MOVE:
                super.onTouchEvent(event);
                int progress = getMax() - (int) (getMax() * event.getY() / getHeight());

                // Ensure progress stays within boundaries
                if(progress < 0) {progress = 0;}
                if(progress > getMax()) {progress = getMax();}
                setProgress(progress);  // Draw progress
                if(progress != lastProgress) {
                    // Only enact listener if the progress has actually changed
                    lastProgress = progress;
                    onChangeListener.onProgressChanged(this, progress, true);
                }

                onSizeChanged(getWidth(), getHeight() , 0, 0);
                setPressed(true);
                setSelected(true);
                break;
            case MotionEvent.ACTION_UP:
                onChangeListener.onStopTrackingTouch(this);
                setPressed(false);
                setSelected(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(event);
                setPressed(false);
                setSelected(false);
                break;
        }
        return true;
    }

    public synchronized void setProgressAndThumb(int progress) {
        setProgress(progress);
        onSizeChanged(getWidth(), getHeight() , 0, 0);
        if(progress != lastProgress) {
            // Only enact listener if the progress has actually changed
            lastProgress = progress;
            onChangeListener.onProgressChanged(this, progress, true);
        }
    }

    public synchronized void setMaximum(int maximum) {
        setMax(maximum);
    }

    public synchronized int getMaximum() {
        return getMax();
    }

//    @Override
//    public void onProgressChanged(SeekBar timerBar, int arg1, boolean arg2) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb);
//        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas c = new Canvas(bmp);
//        String text = Integer.toString(timerBar.getProgress());
//        Paint p = new Paint();
//        p.setTypeface(Typeface.DEFAULT_BOLD);
//        p.setTextSize(14);
//        p.setColor(0xFFFFFFFF);
//        int width = (int) p.measureText(text);
//        int yPos = (int) ((c.getHeight() / 2) - ((p.descent() + p.ascent()) / 2));
//        c.drawText(text, (bmp.getWidth()-width)/2, yPos, p);
//        timerBar.setThumb(new BitmapDrawable(getResources(), bmp));
//    }


//    public void setThumbPosition(SeekBar seekBar){
//        int max = seekBar.getMax();
//
//        int available = seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight();
//        float scale = max > 0 ? (float) seekBar.getProgress() / (float) max : 0;
//
//        //scale = 1;
//        int pos = sb.getProgress();
//        double star = pos/(20.0);
//
//        BitmapDrawable bd = writeOnDrawable(R.drawable.star2, Double.toString(star));
//
//        int thumbWidth = bd.getIntrinsicWidth();
//        int thumbHeight = bd.getIntrinsicHeight();
//        //available -= thumbWidth;
//
//        int thumbPos = (int) (scale * available);
//        if(thumbPos <= 0+thumbWidth){
//            thumbPos += (thumbWidth/2);
//        }else if(thumbPos >= seekBar.getWidth()-thumbWidth){
//            thumbPos -= (thumbWidth/2);
//        }
//
//        bd.setBounds(new Rect(thumbPos,0,
//                thumbPos+bd.getIntrinsicWidth(),
//                bd.getIntrinsicHeight()
//        ));
//
//        seekBar.setThumb(bd);
//
//        TextView tv = (TextView)findViewById(R.id.percent);
//        tv.setText(Double.toString(star)+"%");
//    }
}