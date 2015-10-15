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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI.SpendTarget;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.send.SendConfirmationFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.models.Wallet;
import com.airbitz.plugins.PluginFramework.Plugin;
import com.airbitz.plugins.PluginFramework.UiHandler;

import java.util.Stack;

public class PluginFragment extends WalletBaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    private WebView mWebView;
    private TextView mTitleTextView;
    private ViewGroup mView;
    private PluginFramework mFramework;
    private Plugin mPlugin;
    private Stack<String> mNav;
    private Uri mUri;
    private String mUrl;

    private int previousHeight;
    private int mToolbarHeight;
    private String mTitle;
    private LinearLayout.LayoutParams frameLayoutParams;

    private SendConfirmationFragment mSendConfirmation;
    private String mSubtitle;

    public PluginFragment(Plugin plugin) {
        setBackEnabled(true);
        mPlugin = plugin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PluginFramework", "onCreate");

        setHasOptionsMenu(true);
        if (mFramework == null) {
            mNav = new Stack<String>();
            mFramework = new PluginFramework(handler);
            mFramework.setup();
        }
        mSubtitle = AirbitzApplication.getContext().getString(R.string.buysell_title);
    }

    @SuppressWarnings("deprecation")
    private void cleanupWebview() {
        if (mWebView != null) {
            mWebView.clearCache(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookie();
            } else {
                CookieManager.getInstance().removeAllCookies(null);
            }
        }
    }

    public void cleanup() {
        cleanupWebview();
        mFramework.destroy();
        mFramework = null;
        mWebView = null;
        mView = null;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    @Override
    protected String getSubtitle() {
        return mSubtitle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = (ViewGroup) inflater.inflate(R.layout.fragment_plugin, container, false);
        mWebView = (WebView) mView.findViewById(R.id.plugin_webview);
        cleanupWebview();
        // Allows use to nest iframes from 3rd parties
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

        mFramework.buildPluginView(mPlugin, mActivity, mWebView);
        mWebView.setBackgroundColor(0x00000000);

        mToolbarHeight = getResources().getDimensionPixelSize(R.dimen.tabbar_height);

        // Resize webview
        mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (null != mView) {
                    resizeWebView();
                }
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
        if (mWebView != null) {
            mWebView.onResume();
            mWebView.resumeTimers();
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
        // We do this after webview is resumed so current wallet is inserted
        // into plugin
        CoreAPI.getApi().reloadWallets();
        if (mWallet != null) {
            mFramework.setWallet(mWallet);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
            mWebView.pauseTimers();
        }
    }

    @Override
    protected void walletChanged(Wallet newWallet) {
        super.walletChanged(newWallet);
        mFramework.setWallet(newWallet);
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
            if (PluginFramework.isInsidePlugin(mNav)) {
                mFramework.back();
            } else {
                mWebView.goBack();
            }
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
        mSubtitle = title;
        updateTitle();
    }

    private UiHandler handler = new UiHandler() {
        public void showAlert(final String title, final String message, final boolean showSpinner) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    final NavigationActivity act = (NavigationActivity) getActivity();
                    int duration = 0;
                    boolean cancelable = true;
                    if (showSpinner) {
                        duration = getResources().getInteger(R.integer.alert_hold_time_forever);
                        cancelable = false;
                    } else {
                        duration = mActivity.getResources().getInteger(R.integer.alert_hold_time_default);
                    }
                    act.ShowFadingDialog(message, null, duration, cancelable);
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
                public void back() {
                    mFramework.sendBack(cbid);
                    mSendConfirmation = null;
                }
                public void error() {
                    if (mFramework != null) {
                        mFramework.sendError(cbid);
                    }
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

                        ((NavigationActivity) getActivity()).pushFragment(mSendConfirmation, NavigationActivity.Tabs.BUYSELL.ordinal());

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

        public void back() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (getActivity() != null) {
                        PluginFragment.popFragment(mActivity);
                    }
                }
            });
        }

        public void exit() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (getActivity() != null) {
                        PluginFragment.popFragment(mActivity);
                    }
                }
            });
        }

        public void stackClear() {
            Log.d("PluginFragment", "clear");
            mNav.clear();
        }

        public void stackPush(String path) {
            mNav.push(path);
            Log.d("PluginFragment", "stackPush");
            for (String p : mNav) {
                Log.d("PluginFragment", "\t" + p);
            }
        }

        public void stackPop() {
            Log.d("PluginFragment", "pop");
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
