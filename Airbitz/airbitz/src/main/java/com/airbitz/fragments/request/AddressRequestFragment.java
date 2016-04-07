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

package com.airbitz.fragments.request;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import co.airbitz.core.AirbitzCore;
import co.airbitz.core.ReceiveAddress;
import com.airbitz.R;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.utils.Common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class AddressRequestFragment extends WalletBaseFragment {

    public static final String URI = "com.airbitz.addressrequest.uri";

    private final String TAG = getClass().getSimpleName();

    private Button mOKButton;
    private Button mCancelButton;
    private TextView mInstruction;
    private View mView;

    private Uri mUri;
    private String strName;
    private String strCategory;
    private String maxNumAddress;
    private String strNotes;
    private String _successUrl;
    private String _errorUrl;
    private String _cancelUrl;
    private ReceiveAddress mReceiver;

    // Callback when finished
    private OnAddressRequestListener mOnAddressRequest;
    public void setOnAddressRequestListener(OnAddressRequestListener listener) {
        mOnAddressRequest = listener;
    }

    @Override
    public String getSubtitle() {
        return mActivity.getString(R.string.address_request_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_address_request, container, false);

        mInstruction = (TextView) mView.findViewById(R.id.textview_instruction);
        mCancelButton = (Button) mView.findViewById(R.id.fragment_address_request_button_cancel);
        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });

        mOKButton = (Button) mView.findViewById(R.id.fragment_address_request_button_ok);
        mOKButton.setVisibility(View.VISIBLE);
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOkay();
            }
        });
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUri = Uri.parse(getArguments().getString(URI));
        parseUri(mUri);
        mInstruction.setText(String.format(getString(R.string.address_request_message), strName));
    }

    private void parseUri(Uri uri) {
        if (uri != null) {
            Map<String, String> map;
            try {
                map = Common.splitQuery(uri);
                strName = map.containsKey("x-source") ? map.get("x-source") : "";
                strNotes = map.containsKey("notes") ? map.get("notes") : "";
                strCategory = map.containsKey("category") ? map.get("category") : "";
                maxNumAddress = map.containsKey("max-number") ? map.get("max-number") : "";
                _successUrl = map.get("x-success");
                _errorUrl = map.get("x-error");
                _cancelUrl = map.get("x-cancel");
            } catch (UnsupportedEncodingException e) {
                AirbitzCore.logi("Unsupported uri exception");
            }
        } else {
            strName = "An app ";
            strCategory = "";
            strNotes = "";
        }
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    private void goOkay() {
        createRequest();

        if (_successUrl != null) {
            String query;
            if (_successUrl.contains("?")) {
                query = "&address=";
            } else {
                query = "?address=";
            }
            try {
                query += URLEncoder.encode(mReceiver.uri().replace("?", "&"), "utf-8") + "&x-source=Airbitz";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            query = _successUrl + query;
            Uri uri = Uri.parse(query);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
                mReceiver.finalizeRequest();
            } else {
                Uri errorUri = Uri.parse(_errorUrl);
                Intent errorIntent = new Intent(Intent.ACTION_VIEW, errorUri);
                if (errorIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(errorIntent);
                }
            }
        }
        // finish
        if (mOnAddressRequest != null) {
            mOnAddressRequest.onAddressRequest();
        }
    }

    private void goCancel() {
        // finish
        if(mOnAddressRequest != null) {
            mOnAddressRequest.onAddressRequest();
        }
    }

    private void createRequest() {
        mReceiver = mWallet.newReceiveRequest().amount(0);
        mReceiver.meta().name(strName);
        mReceiver.meta().category(strCategory);
        mReceiver.meta().notes(strNotes);
    }
}
