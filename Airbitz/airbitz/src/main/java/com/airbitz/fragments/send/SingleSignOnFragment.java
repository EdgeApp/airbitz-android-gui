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

package com.airbitz.fragments.send;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.api.Constants;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.utils.RoundedTransformation;

import co.airbitz.core.Account.EdgeLoginInfo;
import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.ParsedUri;
import co.airbitz.core.Wallet;

import com.afollestad.materialdialogs.MaterialDialog;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.List;

public class SingleSignOnFragment extends BaseFragment {
    static final String BITID_IDENTITY_STORE = "Identities";

    private int _selectedTableIndex = 0;

    private int mRound;
    private View mView;
    private ImageView mImageLogo;
    private TextView mHeaderLabel;
    private TextView mAppNameLabel;
    private TextView mDescriptionView;
    private ListView mList;
    private View mHorizontalRule;
    private Button mLoginButton;
    private Button mCancelButton;
    private ParsedUri mParsedUri = null;
    private EdgeLoginInfo mEdgeLogin = null;
    private final Picasso mPicasso;
    private MaterialDialog mDialog;
    private EdgeLoginTask mEdgeLoginTask;
    private BitidTask mBitidTask;

    private boolean mBitidSParam = false;
    private boolean mBitidProvidingKYCToken = false;
    private String[] mKycTokenKeys = null;
    private Account mAccount;

    private List<List<Wallet>> mReposIndexed = null;
    private List<Integer> mReposToUseIndex = null;

    public SingleSignOnFragment() {
        mPicasso = AirbitzApplication.getPicasso();
        mRound = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2,
            AirbitzApplication.getContext().getResources().getDisplayMetrics());
        mAccount = AirbitzApplication.getAccount();
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.edge_login);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_sso, container, false);
        }
        mImageLogo = (ImageView) mView.findViewById(R.id.logo);
        mHeaderLabel = (TextView) mView.findViewById(R.id.header);
        mAppNameLabel = (TextView) mView.findViewById(R.id.app_name);
        mDescriptionView = (TextView) mView.findViewById(R.id.description);
        mList = (ListView) mView.findViewById(R.id.list);
        mList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != mKycTokenKeys) {
                    _selectedTableIndex = i;
                    login();
                } else {
                    List<Wallet> repos = mReposIndexed.get(i);
                    if (null != repos && repos.size() > 0)
                    {
                        /*
                        NSMutableArray *arrayText = [[NSMutableArray alloc] init];
                        NSMutableArray *arrayBottomText = [[NSMutableArray alloc] init];

                        for (ABCWallet *w in repos)
                        {
                            [arrayText addObject:w.name];
                            [arrayBottomText addObject:[abcAccount.settings.denomination satoshiToBTCString:w.balance withSymbol:YES cropDecimals:YES]];
                        }
                        [arrayText addObject:create_new_text];
                        [arrayBottomText addObject:@""];

                        [UtilityTableViewController launchUtilityTableViewController:self
                                                                        cellHeight:55.0
                                                                        arrayTopText:[arrayText copy]
                                                                    arrayBottomText:[arrayBottomText copy]
                                                                    arrayImageUrls:nil
                                                                        arrayImage:nil
                                                                            callback:^(int selectedIndex)
                        {
                            if (selectedIndex <= arrayText.count) {
                                mReposToUseIndex.set(row, -1);
                            } else {
                                mReposToUseIndex.set(row, = [NSNumber numberWithInt:selectedIndex];
                            }
                        }];
                        */
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mHorizontalRule = mView.findViewById(R.id.horizontal_rule);
        mLoginButton = (Button) mView.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                login();
            }
        });
        mCancelButton = (Button) mView.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (null != mEdgeLogin) {
                    try {
                        mAccount.deleteEdgeLoginRequest(mEdgeLogin.token);
                    } catch (AirbitzException e) {
                        AirbitzCore.loge(e.toString());
                    }
                }
                done();
            }
        });
        return mView;
    }

    private void setupDisplay() {
        if (null != mEdgeLogin) {
            setupEdgeDisplay();
        } else if (null != mParsedUri.type() && mParsedUri.type() == ParsedUri.UriType.BITID) {
            setupBitidDisplay();
        }
    }

    private void setupEdgeDisplay() {
        if (null != mEdgeLogin.requestorImageUrl) {
            mPicasso.load(mEdgeLogin.requestorImageUrl)
                    .transform(new RoundedTransformation(mRound, 0))
                    .into(mImageLogo, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() { }
                        @Override
                        public void onError() { }
                    });
        } else {
            mImageLogo.setVisibility(View.GONE);
            mHorizontalRule.setVisibility(View.GONE);
        }
        mAppNameLabel.setText(mEdgeLogin.requestor);
        mDescriptionView.setText(R.string.edge_description);

        List<Row> rows = new ArrayList<Row>();
        mReposIndexed = new ArrayList<List<Wallet>>();
        mReposToUseIndex = new ArrayList<Integer>();
        for (String repoType : mEdgeLogin.repoTypes) {
            List<Wallet> repos = mAccount.getEdgeLoginRepos(repoType);
            if (repos.size() > 0) {
                mReposIndexed.add(repos);
                mReposToUseIndex.add(0);
            } else {
                mReposIndexed.add(new ArrayList<Wallet>());
                mReposToUseIndex.add(-1);
            }
            Row row = new Row();
            row.repoType = repoType;
            rows.add(row);
        }
        mList.setAdapter(new SignonAdaper(mActivity, rows));
    }

    private void setupBitidDisplay() {
        String descriptionText = "";
        String label = mParsedUri.bitidDomain().replaceAll("https://", "").replaceAll("http://", "");
        mAppNameLabel.setText(label);
        mImageLogo.setVisibility(View.GONE);
        mHeaderLabel.setVisibility(View.VISIBLE);
        mHeaderLabel.setText(mActivity.getString(R.string.bitid_login_title));
        if (mParsedUri.bitidKYCProvider()) {
            mDescriptionView.setText(String.format("• %s", getString(R.string.provide_an_identity_token)));
            mBitidSParam = true;
            mBitidProvidingKYCToken = true;
            mLoginButton.setText(R.string.accept_button_text);
        } else if (mParsedUri.bitidKYCRequest()) {
            mBitidProvidingKYCToken = false;
            mBitidSParam = true;

            mKycTokenKeys = new String[] {
                "bitpos.me",
                "optus.com.au",
                "airbitz.co"
            };
            if (mKycTokenKeys.length > 0) {
                descriptionText = String.format("• %s", mActivity.getString(R.string.request_your_identity_token));
                mLoginButton.setText(R.string.approve_button_text);
            } else {
                descriptionText = String.format("• %s", mActivity.getString(R.string.request_your_identity_token_but_none));
                mKycTokenKeys = null;
                mLoginButton.setVisibility(View.GONE);
                mCancelButton.setText(R.string.back_button_text);
            }
        } else {
            mKycTokenKeys = null;
            descriptionText = mActivity.getString(R.string.verify_domain_and_approve);
            mLoginButton.setText(R.string.login_button_text);
        }

        if (mParsedUri.bitidPaymentAddress()) {
            descriptionText = String.format("%s\n• %s", descriptionText, mActivity.getString(R.string.request_payment_address));
            mBitidSParam = true;
        }

        if (mBitidSParam) {
            descriptionText = String.format("%s\n\n%s", mActivity.getString(R.string.would_like_to), descriptionText);
        }
        mDescriptionView.setText(descriptionText);

        List<Row> rows = new ArrayList<Row>();
        if (null != mKycTokenKeys) {
            for (String key : mKycTokenKeys) {
                Row row = new Row();
                row.title = key;
                row.description = "";
                row.imageUrl = null;
                rows.add(row);
            }
            mList.setAdapter(new SignonAdaper(mActivity, rows));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBitidTask != null) {
            mBitidTask.cancel(true);
            mBitidTask = null;
        }
        if (mEdgeLoginTask != null) {
            mEdgeLoginTask.cancel(true);
            mEdgeLoginTask = null;
        }
    }

    public void setParsedUri(ParsedUri parsedUri) {
        mParsedUri = parsedUri;
    }

    public void setEdgeLoginInfo(EdgeLoginInfo loginInfo) {
        mEdgeLogin = loginInfo;
    }

    private void login() {
        if (null != mEdgeLogin) {
            mEdgeLoginTask = new EdgeLoginTask();
            mEdgeLoginTask.execute(mEdgeLogin);
        } else {
            mBitidTask = new BitidTask();
            mBitidTask.execute(mParsedUri);
        }
    }

    public class EdgeLoginTask extends AsyncTask<EdgeLoginInfo, Void, Boolean> {
        @Override
        public void onPreExecute() {
            showProcessing();
        }

        @Override
        protected Boolean doInBackground(EdgeLoginInfo... infos) {
            try {
                mAccount.approveEdgeLoginRequest(infos[0]);
                return true;
            } catch (AirbitzException e) {
                AirbitzCore.loge(e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            hideProcessing();
            if (result) {
                mActivity.ShowFadingDialog(getString(R.string.successfully_logged_in),
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            } else {
                mActivity.ShowFadingDialog(getString(R.string.error_logging_in),
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            }
            done();
        }

        @Override
        protected void onCancelled() {
            hideProcessing();
        }
    }

    public class BitidTask extends AsyncTask<ParsedUri, Void, Boolean> {
        @Override
        public void onPreExecute() {
            showProcessing();
        }

        @Override
        protected Boolean doInBackground(ParsedUri... infos) {
            try {
                ParsedUri parsedUri = infos[0];
                if (!mBitidSParam) {
                    mAccount.bitidLogin(parsedUri.bitid());
                } else {
                    if (null != mKycTokenKeys) {
                        String data = mAccount.data(BITID_IDENTITY_STORE).get(mKycTokenKeys[_selectedTableIndex]);
                        mAccount.bitidLoginMeta(parsedUri.bitid(), data);
                    } else {
                        mAccount.bitidLoginMeta(parsedUri.bitid(), "");
                    }
                }
            } catch (AirbitzException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            hideProcessing();
            if (result) {
                if (mBitidProvidingKYCToken) {
                    String message =
                        String.format(mActivity.getString(R.string.identity_token_created_and_saved), mParsedUri.bitidDomain());
                    mActivity.ShowFadingDialog(message,
                        getResources().getInteger(R.integer.alert_hold_time_help_popups));
                } else if (null != mKycTokenKeys) {
                    String message = String.format("%s %s", getString(R.string.successfully_verified_identity), mKycTokenKeys[_selectedTableIndex]);
                    mActivity.ShowFadingDialog(message,
                        getResources().getInteger(R.integer.alert_hold_time_help_popups));
                } else {
                    mActivity.ShowFadingDialog(getString(R.string.successfully_logged_in),
                        getResources().getInteger(R.integer.alert_hold_time_help_popups));
                }
            } else {
                mActivity.ShowFadingDialog(getString(R.string.error_logging_in),
                    getResources().getInteger(R.integer.alert_hold_time_help_popups));
            }
            done();
        }

        @Override
        protected void onCancelled() {
            hideProcessing();
        }
    }

    protected void done() {
        mActivity.popFragment();
    }

    protected void showProcessing() {
        showDialog(mActivity.getString(R.string.approving_login_text));
    }

    protected void hideProcessing() {
        if (null != mDialog) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void showDialog(String message) {
        hideProcessing();
        MaterialDialog.Builder builder =
            new MaterialDialog.Builder(mActivity)
                    .content(message)
                    .cancelable(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false);
        mDialog = builder.build();
        mDialog.show();
    }

    static class Row {
        String repoType;
        String title;
        String description;
        String imageUrl;
    }

    public class SignonAdaper extends ArrayAdapter<Row> {
        private Context mContext;
        private List<Row> mValues;

        public SignonAdaper(Context context, List<Row> values) {
            super(context, R.layout.item_listview_business, values);
            mContext = context;
            mValues = values;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_listview_singlesignon, parent, false);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView description = (TextView) convertView.findViewById(R.id.description);
            ImageView iconImage = (ImageView) convertView.findViewById(R.id.icon);

            if (mEdgeLogin != null) {
                title.setText(mEdgeLogin.repoNames.get(position));
                List<Wallet>repos  = mReposIndexed.get(position);
                if (null != repos && repos.size() > 0) {
                    if (position >= 0) {
                        Wallet wallet = repos.get(position);
                        description.setText(String.format(mActivity.getString(R.string.or_choose), wallet.name()));
                    } else {
                        description.setText(R.string.create_new_text);
                    }
                } else {
                    description.setText(R.string.create_new_text);
                }
                String repoType = mEdgeLogin.repoTypes.get(position);
                String imageUrl = repoToImage(repoType);

                if (null != imageUrl) {
                    mPicasso.load(imageUrl)
                            .transform(new RoundedTransformation(mRound, 0))
                            .into(iconImage, new Callback.EmptyCallback() {
                                @Override
                                public void onSuccess() { }
                                @Override
                                public void onError() { }
                            });
                }
            } else if (null != mKycTokenKeys) {
                title.setText(mKycTokenKeys[position]);
                description.setText("");
                iconImage.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    private String repoToImage(String repo) {
        if ("account:repo:com.augur".equals(repo)) {
            return "https://airbitz.co/go/wp-content/uploads/2016/08/augur_logo_100.png";
        } else if ("account:repo:city.arcade".equals(repo)) {
            return "https://airbitz.co/go/wp-content/uploads/2016/08/ACLOGOnt-1.png";
        } else if ("account:repo:com.mydomain.myapp".equals(repo)) {
            return "https://airbitz.co/go/wp-content/uploads/2016/10/GenericEdgeLoginIcon.png";
        } else if ("wallet:repo:ethereum".equals(repo)) {
            return "https://airbitz.co/go/wp-content/uploads/2016/08/EthereumIcon-100w.png";
        } else if ("wallet:repo:bitcoin".equals(repo)) {
            return "https://airbitz.co/go/wp-content/uploads/2016/08/bitcoin-logo-02.png";
        }
        return null;
    }
}
