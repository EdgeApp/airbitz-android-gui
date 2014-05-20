package com.airbitz.objects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.airbitz.R;

/**
 * Created on 3/10/14.
 */
public class ObservableScrollView extends ScrollView {

    private ScrollViewListener scrollViewListener = null;
    private Context mContext;;
    private LinearLayout stickyPopUp;

    int[] locSticky = {0,0};
    int[] locFrame = {0,0};

    public void setContext(Context context){
        mContext = context;
    }

    public void setSticky(LinearLayout stickyLayout){
        stickyPopUp = stickyLayout;
    }

    public interface ScrollViewListener {
        void onScrollEnded(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);

    }

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {

        LinearLayout unstickyPopUp = (LinearLayout) findViewById(R.id.layout_near_you);
        View stickyView = findViewById(R.id.flag_for_sticky);

        this.getLocationOnScreen(locFrame);
        stickyView.getLocationOnScreen(locSticky);

        if(locSticky[1]<=locFrame[1]) {
            if (stickyPopUp.getVisibility() == VISIBLE || unstickyPopUp.getVisibility() == VISIBLE){
                stickyPopUp.setVisibility(VISIBLE);
                unstickyPopUp.setVisibility(GONE);
            }
        }else{
            if (stickyPopUp.getVisibility() == VISIBLE || unstickyPopUp.getVisibility() == VISIBLE) {
                stickyPopUp.setVisibility(GONE);
                unstickyPopUp.setVisibility(VISIBLE);
            }
        }


        View view = (View) getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));
        if (diff == 0) { // if diff is zero, then the bottom has been reached
            if (scrollViewListener != null) {
                scrollViewListener.onScrollEnded(this, x, y, oldx, oldy);
            }
        }
        super.onScrollChanged(x, y, oldx, oldy);
    }
}
