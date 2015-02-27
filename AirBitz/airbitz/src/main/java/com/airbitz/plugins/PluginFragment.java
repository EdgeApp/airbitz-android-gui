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

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import android.graphics.Rect;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.send.SendConfirmationFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.plugins.PluginFramework.UiHandler;
import com.airbitz.plugins.PluginFramework.Plugin;
import com.airbitz.utils.Common;

import java.util.Stack;

public class PluginFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    private WebView mWebView;
    private TextView mTitleTextView;
    private View mView;
    private PluginFramework mFramework;
    private Plugin mPlugin;
    private Stack mNav;

    private int previousHeight;
    private int mToolbarHeight;
    private LinearLayout.LayoutParams frameLayoutParams;


    private SendConfirmationFragment mSendConfirmation;

    public PluginFragment() {
        mFramework = new PluginFramework(handler);
        mFramework.setup();
        mNav = new Stack<String>();
        setRetainInstance(true);

        mPlugin = new Plugin("com.glidera", "Glidera", "file:///android_asset/glidera.html#/exchange/");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }
        mView = inflater.inflate(R.layout.fragment_plugin, container, false);
        mWebView = (WebView) mView.findViewById(R.id.plugin_webview);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setVisibility(View.INVISIBLE);

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPress();
            }
        });

        mFramework.buildPluginView(mPlugin, mWebView);
        mWebView.loadUrl(mPlugin.main);
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
            return false;
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


    private UiHandler handler = new UiHandler() {
        public void showAlert(final String title, final String message) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((NavigationActivity) getActivity()).ShowFadingDialog(message, null, 5000, true);
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
                    mSendConfirmation = new SendConfirmationFragment();
                    mSendConfirmation.setExitHandler(exitHandler);
                    Bundle bundle = new Bundle();
                    bundle.putString(SendFragment.UUID, address);
                    bundle.putLong(SendFragment.AMOUNT_SATOSHI, amountSatoshi);
                    bundle.putDouble(SendFragment.AMOUNT_FIAT, amountFiat);
                    bundle.putString(SendFragment.FROM_WALLET_UUID, uuid);
                    bundle.putString(SendFragment.LABEL, label);
                    bundle.putString(SendFragment.CATEGORY, category);
                    bundle.putString(SendFragment.NOTES, notes);
                    bundle.putBoolean(SendFragment.LOCKED, true);
                    mSendConfirmation.setArguments(bundle);

                    ((NavigationActivity) getActivity()).pushFragment(mSendConfirmation, NavigationActivity.Tabs.MORE.ordinal());
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

    private void setTitle(String title) {
        mTitleTextView.setVisibility(View.VISIBLE);
        mTitleTextView.setText(title);
    }
}
