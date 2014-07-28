package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;

import java.io.ByteArrayOutputStream;


public class WalletQRCodeFragment extends Fragment {

    private ImageView mQRView;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private HighlightOnPressButton mCancelButton;
    private HighlightOnPressButton mSMSButton;
    private HighlightOnPressButton mEmailButton;
    private HighlightOnPressButton mCopyButton;
    private TextView mBitcoinAmount;
    private TextView mBitcoinAddress;

    private Bundle bundle;

    private Wallet mWallet;

    private CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCoreAPI = CoreAPI.getApi();
        mWallet = mCoreAPI.getWalletFromName(bundle.getString(Wallet.WALLET_NAME));
    }

    private View mView;
    @Override public void onDestroyView() {
        super.onDestroyView();
        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
        if( null != parentViewGroup ) {
            parentViewGroup.removeView( mView );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        mView = inflater.inflate(R.layout.fragment_wallet_qrcode, container, false);
        ((NavigationActivity)getActivity()).hideNavBar();

        mQRView = (ImageView) mView.findViewById(R.id.qr_code_view);

        mBitcoinAmount = (TextView) mView.findViewById(R.id.textview_bitcoin_amount);
        mBitcoinAmount.setText(bundle.getString(RequestFragment.BITCOIN_VALUE));

        mBitcoinAddress = (TextView) mView.findViewById(R.id.textview_address);

        mBackButton = (ImageButton) mView.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.button_help);
        mCopyButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_copy_button);
        mSMSButton = (HighlightOnPressButton) mView.findViewById(R.id.button_sms_address);
        mEmailButton = (HighlightOnPressButton) mView.findViewById(R.id.button_email_address);
        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_cancel_button);

        mSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS();
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
                getActivity().onBackPressed();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Wallet info");
            }
        });

        String name = "";
        String notes = "";
        String id = mCoreAPI.createReceiveRequestFor(mWallet, name, notes, bundle.getString(RequestFragment.BITCOIN_VALUE));
        if(id!=null) {
            String addr = mCoreAPI.getRequestAddress(mWallet.getUUID(), id);
            mBitcoinAddress.setText(addr);
            try{
                Bitmap bm = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), id);
                if (bm != null) {
                    mQRView.setImageBitmap(bm);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return mView;
    }

    private void sendSMS() {
        String address="";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond

        String id = mCoreAPI.createReceiveRequestFor(mWallet, "", "", mBitcoinAmount.getText().toString());
        if(id!=null) {
            address = mCoreAPI.getRequestAddress(mWallet.getUUID(), id);
        }
        String strBody = "bitcoin:\n" + address;
        intent.putExtra("sms_body", strBody);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bm = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), id);
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bos.toByteArray());
        } else {
            Log.d("RequestFragment", "Could not attach qr code to mms");
        }
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void sendEmail() {
        String address="";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin");

        String id = mCoreAPI.createReceiveRequestFor(mWallet, "", "", mBitcoinAmount.getText().toString());
        if(id!=null) {
            address = mCoreAPI.getRequestAddress(mWallet.getUUID(), id);
        }
        String strBody = "bitcoin:\n" + address;

        intent.putExtra(Intent.EXTRA_TEXT, strBody);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bm = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), id);
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 0, bos);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bos.toByteArray());
        } else {
            Log.d("RequestFragment", "Could not attach qr code to email");
        }

        startActivity(Intent.createChooser(intent, "Email bitcoin request..."));
    }
}
