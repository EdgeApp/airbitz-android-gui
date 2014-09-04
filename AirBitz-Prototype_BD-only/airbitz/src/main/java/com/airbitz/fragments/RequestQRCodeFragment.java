package com.airbitz.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RequestQRCodeFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private final double BORDER_THICKNESS = 0.03;
    private ImageView mQRView;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;
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
    static final int PICK_CONTACT_SMS =1;
    static final int PICK_CONTACT_EMAIL=2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCoreAPI = CoreAPI.getApi();
        mWallet = mCoreAPI.getWalletFromName(bundle.getString(Wallet.WALLET_NAME));
    }

    @Override
    public void onPause() {
        if(mContentURL!=null) { // delete temp file
            Log.d("WalletQRCodeFragment", "deleting temp file");
            getActivity().getContentResolver().delete(Uri.parse(mContentURL), null, null);
        }
        mCoreAPI.prioritizeAddress(null, mWallet.getUUID());
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_request_qrcode, container, false);
        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
            return mView;
        }

        ((NavigationActivity)getActivity()).hideNavBar();

        mQRView = (ImageView) mView.findViewById(R.id.qr_code_view);

        mBitcoinAmount = (TextView) mView.findViewById(R.id.textview_bitcoin_amount);
        mBitcoinAmount.setText(mCoreAPI.getUserBTCSymbol()+" "+bundle.getString(RequestFragment.BITCOIN_VALUE));

        mBitcoinAddress = (TextView) mView.findViewById(R.id.textview_address);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_qrcode_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_qrcode_button_help);
        mCopyButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_copy_button);
        mSMSButton = (HighlightOnPressButton) mView.findViewById(R.id.button_sms_address);
        mEmailButton = (HighlightOnPressButton) mView.findViewById(R.id.button_email_address);
        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_cancel_button);

        mSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSMS();
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard();
            }
        });

        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmail();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity)getActivity()).showNavBar();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity)getActivity()).showNavBar();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.REQUEST_QR), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        return mView;
    }

    @Override public void onResume() {
        super.onResume();
        if(mQRBitmap==null) {
            mCreateBitmapTask = new CreateBitmapTask();
            mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private CreateBitmapTask mCreateBitmapTask;
    public class CreateBitmapTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Common.LogD(TAG, "Starting Receive Request at:"+System.currentTimeMillis());
            mID = mCoreAPI.createReceiveRequestFor(mWallet, "", "", bundle.getString(RequestFragment.BITCOIN_VALUE));
            if(mID!=null) {
                Common.LogD(TAG, "Starting Request Address at:"+System.currentTimeMillis());
                mAddress = mCoreAPI.getRequestAddress(mWallet.getUUID(), mID);
                try{
                    // data in barcode is like bitcoin:address?amount=0.001
                    Common.LogD(TAG, "Starting QRCodeBitmap at:"+System.currentTimeMillis());
                    mQRBitmap = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), mID);
                    mQRBitmap = addWhiteBorder(mQRBitmap);
                    Common.LogD(TAG, "Ending QRCodeBitmap at:"+System.currentTimeMillis());
                    mRequestURI = mCoreAPI.getRequestURI();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            ((NavigationActivity)getActivity()).showModalProgress(false);
            mCreateBitmapTask = null;
            mBitcoinAddress.setText(mAddress);
            if (mQRBitmap != null) {
                mQRView.setImageBitmap(mQRBitmap);
            }
            mCoreAPI.prioritizeAddress(mAddress, mWallet.getUUID());
        }

        @Override
        protected void onCancelled() {
            mCreateBitmapTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
        }
    }

    private Bitmap addWhiteBorder(Bitmap inBitmap) {
        Bitmap imageBitmap = Bitmap.createBitmap((int) (inBitmap.getWidth()*(1+BORDER_THICKNESS*2)),
                (int) (inBitmap.getHeight()*(1+BORDER_THICKNESS*2)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawPaint(p);
        canvas.drawBitmap(inBitmap, (int) (inBitmap.getWidth() * BORDER_THICKNESS), (int) (inBitmap.getHeight() * BORDER_THICKNESS), null);
        return imageBitmap;
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("bitcoin address", mAddress);
        clipboard.setPrimaryClip(clip);
    }

    private void startSMS() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_SMS);
    }

    private void finishSMS(String name, String phone) {
        Intent smsIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
//            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity()); //Need to change the build to API 19
//
//            smsIntent.putExtra(Intent.EXTRA_TEXT, mRequestURI);
//
//            if (defaultSmsPackageName != null)//Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
//            {
//                smsIntent.setPackage(defaultSmsPackageName);
//            }
            smsIntent = new Intent(Intent.ACTION_SENDTO);
            if(mQRBitmap!=null) {
                smsIntent.setData(Uri.parse("mmsto:" + Uri.encode(phone)));
            } else {
                smsIntent.setData(Uri.parse("smsto:" + Uri.encode(phone)));
            }
        }
        else //For earlier versions, the old method
        {
            smsIntent.putExtra("address", phone);
            smsIntent.setType("text/plain");
        }
        smsIntent.putExtra("sms_body", mRequestURI);

        //TODO Hangouts does not recognize this
        if(mQRBitmap!=null) {
            mContentURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mQRBitmap, mAddress, null);
            smsIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mContentURL));
        }

        startActivity(smsIntent);
    }

    private void startEmail() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_EMAIL);
    }

    private void finishEmail(String name, String email) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin");
        intent.putExtra(Intent.EXTRA_TEXT, mRequestURI);
        if(mQRBitmap!=null) {
            mContentURL = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mQRBitmap, mAddress, null);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mContentURL));
        }

        startActivity(intent);
    }

    //code
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT_SMS) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri result = data.getData();

                    // get the id from the Uri
                    String id = result.getLastPathSegment();

                    // query the phone numbers for the selected phone number id
                    final Cursor c = getActivity().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id}, null);

                    int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    final int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    if(c.getCount() == 1) { // contact has a single phone number
                        // get the only phone number
                        if(c.moveToFirst()) {
                            String phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            finishSMS(name, phone);
                        } else {
                            Log.w(TAG, "No Contact results");
                        }
                    } else if(c.getCount() > 1) { // contact has multiple phone numbers
                        final CharSequence[] numbers = new CharSequence[c.getCount()];

                        int i=0;
                        if(c.moveToFirst()) {
                            final String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            while(!c.isAfterLast()) { // for each phone number, add it to the numbers array
                                String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                                String number = type + ": " + c.getString(phoneIdx);
                                numbers[i++] = number;
                                c.moveToNext();
                            }
                            // build and show a simple dialog that allows the user to select a number
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Select Phone Number");
                            builder.setItems(numbers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    String number = (String) numbers[item];
                                    int index = number.indexOf(":");
                                    number = number.substring(index + 2);
                                    finishSMS(name, number);
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setOwnerActivity(getActivity());
                            alert.show();
                        } else Log.w(TAG, "No results");
                    }
                }
                break;
            case (PICK_CONTACT_EMAIL) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri result = data.getData();

                    // get the id from the Uri
                    String id = result.getLastPathSegment();

                    // query the phone numbers for the selected phone number id
                    final Cursor c = getActivity().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                            new String[]{id}, null);

                    if(c.getCount() == 1) { // contact has a single phone number
                        // get the only phone number
                        if(c.moveToFirst()) {
                            String email = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME));
                            finishEmail(name, email);
                        } else {
                            Log.w(TAG, "No Contact results");
                        }
                    } else if(c.getCount() > 1) { // contact has multiple phone numbers
                        final CharSequence[] numbers = new CharSequence[c.getCount()];

                        int i=0;
                        if(c.moveToFirst()) {
                            final String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME));
                            while(!c.isAfterLast()) { // for each phone number, add it to the numbers array
                                String type = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(this.getResources(), c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)), ""); // insert a type string in front of the number
                                String email = type + ":" + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                                numbers[i++] = email;
                                c.moveToNext();
                            }
                            // build and show a simple dialog that allows the user to select a number
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Select Email address");
                            builder.setItems(numbers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    String email = (String) numbers[item];
                                    int index = email.indexOf(":");
                                    email = email.substring(index + 1);
                                    finishEmail(name, email);
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setOwnerActivity(getActivity());
                            alert.show();
                        } else Log.w(TAG, "No results");
                    }
                }
                break;

        }
    }
}
