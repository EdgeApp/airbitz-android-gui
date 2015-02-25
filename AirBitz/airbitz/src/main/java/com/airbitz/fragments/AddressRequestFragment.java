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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletPickerAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;
import com.airbitz.models.WalletPickerEnum;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


public class AddressRequestFragment extends BaseFragment {
    public static final String URI = "com.airbitz.addressrequest.uri";

    private final String TAG = getClass().getSimpleName();

    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressButton mOKButton;
    private HighlightOnPressButton mCancelButton;
    private List<Wallet> mWallets;
    private Wallet mWallet;
    private HighlightOnPressSpinner pickWalletSpinner;
    private TextView mTitleTextView;
    private TextView mInstruction;
    private CoreAPI mCoreAPI;
    private View mView;

    private Uri mUri;
    private String strName;
    private String strCategory;
    private String strNotes;
    private String _successUrl;
    private String _errorUrl;
    private String _cancelUrl;

    // Callback when finished
    private OnAddressRequest mOnAddressRequest;
    public interface OnAddressRequest {
        public void onAddressRequest();
    }
    public void setOnAddressRequestListener(OnAddressRequest listener) {
        mOnAddressRequest = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        loadNonArchivedWallets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_address_request, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.address_request_title);

        mInstruction = (TextView) mView.findViewById(R.id.textview_instruction);

        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_address_request_button_cancel);
        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });

        mOKButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_address_request_button_ok);
        mOKButton.setVisibility(View.VISIBLE);
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goOkay();
            }
        });

        pickWalletSpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.new_wallet_spinner);
        final WalletPickerAdapter dataAdapter = new WalletPickerAdapter(getActivity(), mWallets, WalletPickerEnum.Request);
        pickWalletSpinner.setAdapter(dataAdapter);

        pickWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mWallet = mWallets.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
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

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadNonArchivedWallets() {
        mWallets = mCoreAPI.getCoreActiveWallets();
        if (pickWalletSpinner != null && pickWalletSpinner.getAdapter() != null) {
            ((WalletPickerAdapter) pickWalletSpinner.getAdapter()).notifyDataSetChanged();
        }
    }

    private void parseUri(Uri uri)
    {
        if (uri != null) {
            Map<String, String> map;
            try {
                map = Common.splitQuery(uri);
                strName = map.get("x-source");
                strNotes = map.get("notes");
                strCategory = map.get("category");
                _successUrl = map.get("x-success");
                _errorUrl = map.get("x-error");
                _cancelUrl = map.get("x-cancel");
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "Unsupported uri exception");
            }
        } else {
            strName = "An app ";
            strCategory = "";
            strNotes = "";
        }
    }

    private void goOkay()
    {
        createRequest(0);

        if (_successUrl != null) {
            String query;
            if (!_successUrl.contains("?")) {
                query = String.format("%1?addr=%2", _successUrl, mRequestURI);
            } else {
                query = String.format("%1&addr=%2", _successUrl, mRequestURI);
            }

            query += String.format("&x-source=%1", "Airbitz");
            Uri uri = Uri.parse(query);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
//                finalizeRequest();
            }
            else {
                Uri errorUri = Uri.parse(_errorUrl);
                Intent errorIntent = new Intent(Intent.ACTION_VIEW, errorUri);
                if (errorIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(errorIntent);
                }
            }
        }
        // finish
        if(mOnAddressRequest != null) {
            mOnAddressRequest.onAddressRequest();
        }
    }

    private void goCancel()
    {
        if (_cancelUrl == null) {
            _cancelUrl = _errorUrl;
        }
        if (_cancelUrl != null) {
            String cancelMessage = Uri.encode("User cancelled the request.");
            String query;
            if (!_cancelUrl.contains("?")) {
                query = String.format("%1?addr=&cancelMessage=%2", _cancelUrl, cancelMessage);
            } else {
                query = String.format("%1&addr=&cancelMessage=%2", _cancelUrl, cancelMessage);
            }
            Uri cancelUri = Uri.parse(query);
            Intent errorIntent = new Intent(Intent.ACTION_VIEW, cancelUri);
            if (errorIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(errorIntent);
            }
        }
        // finish
        if(mOnAddressRequest != null) {
            mOnAddressRequest.onAddressRequest();
        }
    }


    String mRequestID;
    String mRequestAddress;
    String mRequestURI;
    private void createRequest(long amountSatoshi)
    {
        mRequestID = "";
        mRequestAddress = "";
        mRequestURI = "";

//        unsigned int width = 0;
//        unsigned char *pData = NULL;
//        char *pszURI = NULL;
//        tABC_Error error;
//
//        char *szRequestID = [self createReceiveRequestFor:amountSatoshi withRequestState:state];
//        if (szRequestID) {
//            ABC_GenerateRequestQRCode([[User Singleton].name UTF8String],
//            [[User Singleton].password UTF8String], [wallet.strUUID UTF8String],
//            szRequestID, &pszURI, &pData, &width, &error);
//            if (error.code == ABC_CC_Ok) {
//                if (pszURI && strRequestURI) {
//                    [strRequestURI appendFormat:@"%s", pszURI];
//                    free(pszURI);
//                }
//            } else {
//                [Util printABC_Error:&error];
//            }
//        }
//        if (szRequestID) {
//            if (strRequestID) {
//                [strRequestID appendFormat:@"%s", szRequestID];
//            }
//            char *szRequestAddress = NULL;
//            tABC_CC result = ABC_GetRequestAddress([[User Singleton].name UTF8String],
//            [[User Singleton].password UTF8String], [wallet.strUUID UTF8String],
//            szRequestID, &szRequestAddress, &error);
//            [Util printABC_Error:&error];
//            if (result == ABC_CC_Ok) {
//                if (szRequestAddress && strRequestAddress) {
//                    [strRequestAddress appendFormat:@"%s", szRequestAddress];
//                }
//            }
//        }
    }

//    String createReceiveRequestFor: (SInt64)amountSatoshi withRequestState:(RequestState)state
//    {
//        tABC_Error error;
//        tABC_TxDetails details;
//
//        memset(&details, 0, sizeof(tABC_TxDetails));
//        details.amountSatoshi = 0;
//        details.amountFeesAirbitzSatoshi = 0;
//        details.amountFeesMinersSatoshi = 0;
//        details.amountCurrency = 0;
//        details.szName = (char *) [strName UTF8String];
//        details.szNotes = (char *) [strNotes UTF8String];
//        details.szCategory = (char *) [strCategory UTF8String];
//        details.attributes = 0x0; //for our own use (not used by the core)
//        details.bizId = 0;
//
//        char *pRequestID;
//        // create the request
//        ABC_CreateReceiveRequest([[User Singleton].name UTF8String],
//        [[User Singleton].password UTF8String], [wallet.strUUID UTF8String],
//        &details, &pRequestID, &error);
//        if (error.code == ABC_CC_Ok) {
//            return pRequestID;
//        } else {
//            return 0;
//        }
//    }

}
