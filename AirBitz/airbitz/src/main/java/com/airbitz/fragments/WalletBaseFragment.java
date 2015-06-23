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
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletChoiceAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.models.Wallet;

import java.util.List;

public class WalletBaseFragment extends BaseFragment implements CoreAPI.OnWalletLoaded {

    protected List<Wallet> mWallets;
    protected Wallet mWallet;

    private Handler mHandler = new Handler();
    private ListView mWalletList;
    private TextView mTitleView;
    protected CoreAPI mCoreApi;
    protected boolean mHomeEnabled = false;
    protected boolean mDropDownEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uuid = AirbitzApplication.getCurrentWallet();
        mCoreApi = CoreAPI.getApi();
        mWallets = mCoreApi.getCoreActiveWallets();
        mWallet = mCoreApi.getWalletFromUUID(uuid);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (null != toolbar) {
            toolbar.setTitle(""); // XXX: Should just disable the title
            mActivity.setSupportActionBar(toolbar);
            if (mHomeEnabled) {
                mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        mWalletList = (ListView) view.findViewById(R.id.wallet_choices);
        mWalletList.setVisibility(View.GONE);

        mTitleView = (TextView) view.findViewById(R.id.title);
        updateTitle();
        if (mDropDownEnabled) {
            mTitleView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (mWalletList.getVisibility() == View.VISIBLE) {
                        hideWalletList();
                    } else {
                        showWalletList();
                    }
                }
            });
        }

        WalletChoiceAdapter adapter = new WalletChoiceAdapter(mActivity, mWallets);
        adapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletList.setAdapter(adapter);
        mWalletList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                walletChanged(mWallets.get(i));
            }
        });
    }

    @Override
    public void onWalletsLoaded() {
        mWallets = mCoreApi.getCoreActiveWallets();

        WalletChoiceAdapter adapter = new WalletChoiceAdapter(mActivity, mWallets);
        adapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCoreApi.setOnWalletLoadedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCoreApi.setOnWalletLoadedListener(null);
    }

    protected void updateTitle() {
        mTitleView.setText(mWallet.getName() + " ▼");
    }

    protected void walletChanged(Wallet newWallet) {
        mWallet = newWallet;
        AirbitzApplication.setCurrentWallet(mWallet.getUUID());
        updateTitle();
        hideWalletList();
    }

    protected void setHomeEnabled(boolean enabled) {
        mHomeEnabled = enabled;
    }

    protected void setDropdownEnabled(boolean enabled) {
        mDropDownEnabled = enabled;
    }

    private void showWalletList() {
        mHandler.post(mAnimateIn);
    }

    private void hideWalletList() {
        mHandler.post(mAnimateOut);
    }

    Runnable mAnimateIn = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mWalletList, "alpha", 0, 1);
            animator.setDuration(100);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) { }

                @Override
                public void onAnimationCancel(Animator animator) { }

                @Override
                public void onAnimationStart(Animator animator) {
                    mWalletList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animator) { }
            });
            animator.start();
        }
    };

    Runnable mAnimateOut = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mWalletList, "alpha", 1, 0);
            animator.setDuration(100);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    mWalletList.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    mWalletList.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.start();
        }
    };
}
