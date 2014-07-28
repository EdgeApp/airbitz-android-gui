package com.airbitz.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Telephony;
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
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;

import java.io.ByteArrayOutputStream;


public class WalletQRCodeFragment extends Fragment {

    private ImageView mQRView;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Button mCancelButton;
    private Button mSMSButton;
    private Button mEmailButton;
    private Button mCopyButton;
    private TextView mBitcoinAmount;
    private TextView mBitcoinAddress;

    private Bitmap mQRBitmap;
    private String mID;
    private String mAddress;
    private String mContentURL;

    private Bundle bundle;

    private Wallet mWallet;

    private CoreAPI mCoreAPI;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCoreAPI = CoreAPI.getApi();
        mWallet = mCoreAPI.getWalletFromName(bundle.getString(Wallet.WALLET_NAME));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
        if( null != parentViewGroup ) {
            parentViewGroup.removeView( mView );
        }
        if(mContentURL!=null) { // delete temp file
            Log.d("WalletQRCodeFragment", "deleting temp file");
            getActivity().getContentResolver().delete(Uri.parse(mContentURL), null, null);
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
        mSMSButton = (Button) mView.findViewById(R.id.button_sms_address);
        mEmailButton = (Button) mView.findViewById(R.id.button_email_address);
        mCancelButton = (Button) mView.findViewById(R.id.button_cancel);

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
        mID = mCoreAPI.createReceiveRequestFor(mWallet, name, notes, bundle.getString(RequestFragment.BITCOIN_VALUE));
        if(mID!=null) {
            mAddress = mCoreAPI.getRequestAddress(mWallet.getUUID(), mID);
            mBitcoinAddress.setText(mAddress);
            try{
                mQRBitmap = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), mID);
                if (mQRBitmap != null) {
                    mQRView.setImageBitmap(mQRBitmap);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return mView;
    }

    private void sendSMS() {
        Intent smsIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity()); //Need to change the build to API 19

            smsIntent.putExtra(Intent.EXTRA_TEXT, "bitcoin:" + mAddress);

            if (defaultSmsPackageName != null)//Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
            {
                smsIntent.setPackage(defaultSmsPackageName);
            }
        }
        else //For earlier versions, the old method
        {
            smsIntent.putExtra("sms_body", "bitcoin:"+mAddress);
        }
        smsIntent.setType("text/plain");
        if(mQRBitmap!=null) {
            mContentURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mQRBitmap, mAddress, null);
            smsIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mContentURL));
        }
        startActivity(Intent.createChooser(smsIntent, "Request Bitcoin from..."));
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin");
        intent.putExtra(Intent.EXTRA_TEXT, "bitcoin:" + mAddress);
        if(mQRBitmap!=null) {
            mContentURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mQRBitmap, mAddress, null);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mContentURL));
        }

        startActivity(Intent.createChooser(intent, "Email bitcoin request..."));
    }
}
