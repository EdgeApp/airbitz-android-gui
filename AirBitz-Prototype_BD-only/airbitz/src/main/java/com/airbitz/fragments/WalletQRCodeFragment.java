package com.airbitz.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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

    private Bitmap mQRBitmap;
    private String mID;
    private String mAddress;
    private String mContentURL;
    private String mRequestURI;

    private List<String> mContactNames = new ArrayList<String>();
    private Map<String, String> mContactPhones = new LinkedHashMap<String, String>();


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

        mCreateBitmapTask = new CreateBitmapTask();
        mCreateBitmapTask.execute();
        return mView;
    }


    private CreateBitmapTask mCreateBitmapTask;
    public class CreateBitmapTask extends AsyncTask<Void, Void, Void> {

        CreateBitmapTask() { }

        @Override
        protected Void doInBackground(Void... params) {
            mID = mCoreAPI.createReceiveRequestFor(mWallet, "", "", bundle.getString(RequestFragment.BITCOIN_VALUE));
            if(mID!=null) {
                mAddress = mCoreAPI.getRequestAddress(mWallet.getUUID(), mID);
                try{
                    mQRBitmap = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), mID);
                    mRequestURI = mCoreAPI.getRequestURI();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mCreateBitmapTask = null;
            mBitcoinAddress.setText(mAddress);
            if (mQRBitmap != null) {
                mQRView.setImageBitmap(mQRBitmap);
            }
        }

        @Override
        protected void onCancelled() {
            mCreateBitmapTask = null;
        }
    }



    private void sendSMS() {
        Intent smsIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity()); //Need to change the build to API 19

            smsIntent.putExtra(Intent.EXTRA_TEXT, mRequestURI);

            if (defaultSmsPackageName != null)//Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
            {
                smsIntent.setPackage(defaultSmsPackageName);
            }
        }
        else //For earlier versions, the old method
        {
            smsIntent.putExtra("sms_body", mRequestURI);
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
        intent.putExtra(Intent.EXTRA_TEXT, mRequestURI);
        if(mQRBitmap!=null) {
            mContentURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mQRBitmap, mAddress, null);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mContentURL));
        }

        startActivity(Intent.createChooser(intent, "Email bitcoin request..."));
    }

    public void getMatchedContactsList(String searchTerm) {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        // Do something with phones
                    }
                    pCur.close();
                }
            }
        }
    }
}
