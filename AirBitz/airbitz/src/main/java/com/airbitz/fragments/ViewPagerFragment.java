package com.airbitz.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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

        return mView;
    }

    public void setImages(List<ImageView> imageViews, int position) {
        mImageViews = imageViews;
        mPosition = position;
    }
}
