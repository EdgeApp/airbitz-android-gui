package com.airbitz.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airbitz.widgets.TouchImageView;

import java.util.List;

/**
 * Created by tom on 10/27/14.
 */
public class TouchImageViewPagerAdapter extends PagerAdapter {

    private List<TouchImageView> mImageViews;

    public TouchImageViewPagerAdapter(List<TouchImageView> images) {
        mImageViews = images;
    }

    @Override
    public int getCount() {
        return mImageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = mImageViews.get(position);
        container.removeView(imageView);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
