/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted provided that
 * the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the Airbitz Project.
 */
package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;

public class BaseFragment extends Fragment {
    public static Integer DURATION = 300;

    protected NavigationActivity mActivity;
    protected Toolbar mToolbar;
    protected boolean mDrawerEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setDrawerEnabled(boolean enabled) {
        mDrawerEnabled = enabled;
    }

    @Override
    public void onStart() {
        View view = getView();
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mActivity.setSupportActionBar(mToolbar);
            if (mDrawerEnabled) {
                mToolbar.setNavigationIcon(R.drawable.ic_drawer_black);
                mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                updateNavigationIcon();
            }
        }
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (NavigationActivity) activity;
    }

    public NavigationActivity getBaseActivity() {
        return mActivity;
    }

    // For debug builds, watch for memory leaks of all fragments
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Overriding the fragment transition animations to use variable display width
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float displayWidth = size.x;
        Animator animator = null;
        if(mActivity == null) {
            mActivity = (NavigationActivity) getActivity();
        }

        switch(nextAnim) {
            case R.animator.slide_in_from_left:
                animator = ObjectAnimator.ofFloat(this, "translationX", -displayWidth, 0);
                break;
            case R.animator.slide_in_from_right:
                animator = ObjectAnimator.ofFloat(this, "translationX", displayWidth, 0);
                break;
            case R.animator.slide_out_left:
                animator = ObjectAnimator.ofFloat(this, "translationX", 0, -displayWidth);
                break;
            case R.animator.slide_out_right:
                animator = ObjectAnimator.ofFloat(this, "translationX", 0, displayWidth);
                break;
            default:
                animator = null;
        }

        if(animator != null) {
            animator.setDuration(DURATION);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    Log.d("BaseFragment", "Animation starting, setting FLAG_NOT_TOUCHABLE");
                    mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    Log.d("BaseFragment", "Animation ended, clearing FLAG_NOT_TOUCHABLE");
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
        return animator;
    }

    public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
        @Override
        protected void onPreExecute() {
            if(mActivity == null) {
                mActivity = (NavigationActivity) getActivity();
            }
            mActivity.mAsyncTasks.push(this);
        }

        @Override
        protected void onPostExecute(Result result) {
            onCancelled();
        }

        @Override
        protected void onCancelled() {
            mActivity.mAsyncTasks.pop();
        }
    }

    protected void updateNavigationIcon() {
        if (mToolbar != null) {
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_drawer_black);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mActivity.isDrawerOpen()) {
                        mActivity.closeDrawer();
                    } else {
                        mActivity.openDrawer();
                    }
                }
            });
        }
    }
}
