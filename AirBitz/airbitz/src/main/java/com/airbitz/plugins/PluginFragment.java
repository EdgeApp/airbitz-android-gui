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

package com.airbitz.plugins;

import android.app.FragmentTransaction;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.CoreAPI.SpendTarget;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.send.SendConfirmationFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.plugins.PluginFramework.Plugin;
import com.airbitz.plugins.PluginFramework.UiHandler;

import java.util.Stack;

public class PluginFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    private WebView mWebView;
    private boolean mPopped = false;
    private TextView mTitleTextView;
    private ViewGroup mView;
    private PluginFramework mFramework;
    private Plugin mPlugin;
    private Stack mNav;
    private Uri mUri;
    private String mUrl;

    private int previousHeight;
    private int mToolbarHeight;
    private String mTitle;
    private LinearLayout.LayoutParams frameLayoutParams;

    private SendConfirmationFragment mSendConfirmation;

    public PluginFragment(Plugin plugin) {
        mFramework = new PluginFramework(handler);
        mFramework.setup();
        mNav = new Stack<String>();
        setRetainInstance(true);
        mPlugin = plugin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.loading);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = (ViewGroup) inflater.inflate(R.layout.fragment_plugin, container, false);
        mWebView = (WebView) mView.findViewById(R.id.plugin_webview);

        mFramework.buildPluginView(mPlugin, mWebView);
        mWebView.setBackgroundColor(0x00000000);

        mToolbarHeight = getResources().getDimensionPixelSize(R.dimen.tabbar_height);

        // Resize webview
        mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                resizeWebView();
            }
        });
        frameLayoutParams = (LinearLayout.LayoutParams) mView.getLayoutParams();

        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPress();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CoreAPI.getApi().reloadWallets();

        if (mPopped && mWebView != null) {
            mView.addView(mWebView);
        }

        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = mPlugin.sourceFile;
            if (mUri != null) {
                mUrl = mPlugin.sourceFile + "?" + mUri.getEncodedQuery();
            }
            mWebView.loadUrl(mUrl);
        }
        Log.d(TAG, "URL: " + mUrl);
        if (mUri != null) {
            Log.d(TAG, "URI: " + mUri.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (null != mView) {
            // Remove view to prevent retry crash onBackPressed
            mView.removeView(mWebView);
            mPopped = true;
        }
    }

    private void resizeWebView() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != previousHeight) {
            int fullHeight = mView.getRootView().getHeight();
            int heightDifference = fullHeight - usableHeightNow;
            if (heightDifference > (fullHeight/4)) {
                frameLayoutParams.height = fullHeight - heightDifference;
            } else {
                frameLayoutParams.height = -1;
            }
            mView.requestLayout();
            previousHeight = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mView.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    @Override
    public boolean onBackPress() {
        if (mNav.size() == 0) {
            PluginFragment.popFragment(mActivity);
            return true;
        }
        if (mSendConfirmation != null) {
            popFragment();
            mSendConfirmation = null;
        } else {
            mFramework.back();
        }
        return true;
    }

    private void popFragment() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ((NavigationActivity) getActivity()).popFragment();
            }
        });
    }

    @Override
    protected void setTitle(String title) {
        mTitle = title;
        super.setTitle(title);
    }

    private UiHandler handler = new UiHandler() {
        public void showAlert(final String title, final String message) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((NavigationActivity) getActivity()).ShowFadingDialog(message, null, mActivity.getResources().getInteger(R.integer.alert_hold_time_default), true);
                }
            });
        }

        public void setTitle(final String title) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    PluginFragment.this.setTitle(title);
                }
            });
        }

        public void launchSend(final String cbid, final String uuid, final String address,
                               final long amountSatoshi, final double amountFiat,
                               final String label, final String category, final String notes) {
            final SendConfirmationFragment.OnExitHandler exitHandler = new SendConfirmationFragment.OnExitHandler() {
                public void success(String txId) {
                    mFramework.sendSuccess(cbid, uuid, txId);
                    mSendConfirmation = null;
                }
                public void error() {
                    mFramework.sendError(cbid);
                    mSendConfirmation = null;
                }
            };
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    CoreAPI api = CoreAPI.getApi();
                    SpendTarget target = api.getNewSpendTarget();
                    if(target.spendNewInternal(address, label, category, notes, amountSatoshi)) {
                        mSendConfirmation = new SendConfirmationFragment();
                        mSendConfirmation.setSpendTarget(target);
                        mSendConfirmation.setExitHandler(exitHandler);

                        Bundle bundle = new Bundle();
                        bundle.putDouble(SendFragment.AMOUNT_FIAT, amountFiat);
                        bundle.putString(SendFragment.FROM_WALLET_UUID, uuid);
                        bundle.putBoolean(SendFragment.LOCKED, true);
                        mSendConfirmation.setArguments(bundle);

                        ((NavigationActivity) getActivity()).pushFragment(mSendConfirmation, NavigationActivity.Tabs.MORE.ordinal());

                    }
                }
            });
        }

        public void showNavBar() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((NavigationActivity) getActivity()).showNavBar();
                }
            });
        }

        public void hideNavBar() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((NavigationActivity) getActivity()).hideNavBar();
                }
            });
        }

        public void exit() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (getActivity() != null) {
                        ((NavigationActivity) getActivity()).onBackPressed();
                    }
                }
            });
        }

        public void stackClear() {
            mNav.clear();
        }

        public void stackPush(String path) {
            mNav.push(path);
        }

        public void stackPop() {
            mNav.pop();
        }
    };

    public static void pushFragment(NavigationActivity mActivity, Plugin plugin, Uri uri) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        Bundle bundle = new Bundle();
        PluginFragment fragment = new PluginFragment(plugin);
        fragment.setUri(uri);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
        mActivity.getFragmentManager().executePendingTransactions();
    }
}
