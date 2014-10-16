package com.airbitz.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.models.Contact;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.List;


public class RequestQRCodeFragment extends Fragment implements ContactPickerFragment.ContactSelection {
    static final int PICK_CONTACT_SMS = 1;
    static final int PICK_CONTACT_EMAIL = 2;
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
    private TextView mTitleTextView;
    private Bitmap mQRBitmap;
    private String mID;
    private String mAddress;
    private String mContentURL;
    private String mRequestURI;
    private long mAmountSatoshi;
    private boolean emailType = false;
    private Bundle bundle;
    private Wallet mWallet;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private CoreAPI.TxDetails mTxDetails;
    private CreateBitmapTask mCreateBitmapTask;
    private AlertDialog mPartialDialog;
    final Runnable dialogKiller = new Runnable() {
        @Override
        public void run() {
            if (mPartialDialog != null) {
                mPartialDialog.dismiss();
                mPartialDialog = null;
            }
        }
    };
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCoreAPI = CoreAPI.getApi();
        mWallet = mCoreAPI.getWalletFromUUID(bundle.getString(Wallet.WALLET_UUID));
        mAmountSatoshi = bundle.getLong(RequestFragment.SATOSHI_VALUE, 0L);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public void onPause() {
        mCoreAPI.prioritizeAddress(null, mWallet.getUUID());
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_request_qrcode, container, false);
        } else {
            return mView;
        }

        ((NavigationActivity) getActivity()).hideNavBar();

        mQRView = (ImageView) mView.findViewById(R.id.qr_code_view);

        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mBitcoinAmount = (TextView) mView.findViewById(R.id.textview_bitcoin_amount);
        mBitcoinAmount.setText(mCoreAPI.formatSatoshi(mAmountSatoshi, true));

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
                ((NavigationActivity) getActivity()).showNavBar();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity) getActivity()).showNavBar();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.REQUEST_QR), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQRBitmap == null) {
            mCreateBitmapTask = new CreateBitmapTask();
            mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onContactSelection(Contact contact) {
        if (emailType) {
            if (mQRBitmap != null) {
                mContentURL = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), mQRBitmap, mAddress, null);
                if (mContentURL != null) {
                    finishEmail(contact, Uri.parse(mContentURL));
                } else {
                    showNoQRAttached(contact);
                }
            } else {
                mActivity.ShowOkMessageDialog("", getString(R.string.request_qr_bitmap_error));
            }
        } else {
            finishSMS(contact);
        }
    }

    private Bitmap addWhiteBorder(Bitmap inBitmap) {
        Bitmap imageBitmap = Bitmap.createBitmap((int) (inBitmap.getWidth() * (1 + BORDER_THICKNESS * 2)),
                (int) (inBitmap.getHeight() * (1 + BORDER_THICKNESS * 2)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawPaint(p);
        canvas.drawBitmap(inBitmap, (int) (inBitmap.getWidth() * BORDER_THICKNESS), (int) (inBitmap.getHeight() * BORDER_THICKNESS), null);
        return imageBitmap;
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.request_qr_title), mAddress);
        clipboard.setPrimaryClip(clip);
    }

    private void startSMS() {
        emailType = false;
        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(ContactPickerFragment.TYPE, ContactPickerFragment.SMS);
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
    }

    private void finishSMS(Contact contact) {
        String defaultName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultName = Telephony.Sms.getDefaultSmsPackage(mActivity); // Android 4.4 and up
        }

        String name = getString(R.string.request_qr_unknown);
        if (mCoreAPI.coreSettings().getBNameOnPayments()) {
            name = mCoreAPI.coreSettings().getSzFirstName() + " " +
                    mCoreAPI.coreSettings().getSzLastName();
        }
        String textToSend = fillTemplate(R.raw.sms_template, name);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        if (defaultName != null) {
            intent.setPackage(defaultName);
        }
        intent.setData(Uri.parse("smsto:" + contact.getPhone()));  // This ensures only SMS apps respond
        intent.putExtra("sms_body", textToSend);

        startActivity(Intent.createChooser(intent, "SMS"));

        mCoreAPI.finalizeRequest(contact, "SMS", mID, mWallet);
    }

    private void startEmail() {
        emailType = true;
        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(ContactPickerFragment.TYPE, ContactPickerFragment.EMAIL);
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.REQUEST.ordinal());
    }

    private void finishEmail(Contact contact, Uri uri) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        if (uri != null) {
            uris.add(Uri.parse(mContentURL));
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.request_qr_email_title));

        String name = getString(R.string.request_qr_unknown);
        if (mCoreAPI.coreSettings().getBNameOnPayments()) {
            name = mCoreAPI.coreSettings().getSzFirstName() + " " +
                    mCoreAPI.coreSettings().getSzLastName();
        }

        String html = fillTemplate(R.raw.email_template, name);

        intent.putExtra(Intent.EXTRA_STREAM, uris);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(html));
        intent.putExtra(Intent.EXTRA_HTML_TEXT, html);
        startActivity(Intent.createChooser(intent, "email"));

        mCoreAPI.finalizeRequest(contact, "Email", mID, mWallet);
    }

    private void showNoQRAttached(final Contact contact) {
        getString(R.string.request_qr_image_store_error);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.request_qr_image_store_error))
                .setTitle("")
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finishEmail(contact, null);
                                dialog.cancel();
                            }
                        }
                );
        builder.create().show();
    }

    private String fillTemplate(int id, String fullName) {
        String amountBTC = mCoreAPI.formatSatoshi(mAmountSatoshi, false, 8);
        String amountMBTC = mCoreAPI.formatSatoshi(mAmountSatoshi, false, 5);

        String bitcoinURL = "bitcoin://";
        String redirectURL = mRequestURI;

        if (mRequestURI.contains("bitcoin:")) {
            String[] typeAddress = mRequestURI.split(":");
            String address = typeAddress[1];

            bitcoinURL += address;
            redirectURL = "https://airbitz.co/blf/?address=" + address;
        }

        String content = Common.readRawTextFile(getActivity(), id);

        List<String> searchList = new ArrayList<String>();
        searchList.add("[[abtag FROM]]");
        searchList.add("[[abtag BITCOIN_URL]]");
        searchList.add("[[abtag REDIRECT_URL]]");
        searchList.add("[[abtag BITCOIN_URI]]");
        searchList.add("[[abtag ADDRESS]]");
        searchList.add("[[abtag AMOUNT_BTC]]");
        searchList.add("[[abtag AMOUNT_MBTC]]");

        List<String> replaceList = new ArrayList<String>();
        if (fullName == null)
            replaceList.add("");
        else
            replaceList.add(fullName);
        replaceList.add(bitcoinURL);
        replaceList.add(redirectURL);
        replaceList.add(mRequestURI);
        replaceList.add(mAddress);
        replaceList.add(amountBTC);
        replaceList.add(amountMBTC);

        for (int i = 0; i < searchList.size(); i++) {
            content = content.replace(searchList.get(i), replaceList.get(i));
        }
        return content;
    }

    public boolean isShowingQRCodeFor(String walletUUID, String txId) {
        Log.d(TAG, "isShowingQRCodeFor: " + walletUUID + " " + txId);
        Transaction tx = mCoreAPI.getTransaction(walletUUID, txId);
        if (tx.getOutputs() == null || mAddress == null) {
            return false;
        }
        Log.d(TAG, "isShowingQRCodeFor: hasOutputs");
        for (CoreAPI.TxOutput output : tx.getOutputs()) {
            Log.d(TAG, output.getmInput() + " " + mAddress + " " + output.getAddress());
            if (!output.getmInput() && mAddress.equals(output.getAddress())) {
                return true;
            }
        }
        Log.d(TAG, "isShowingQRCodeFor: noMatch");
        return false;
    }

    public long requestDifference(String walletUUID, String txId) {
        Log.d(TAG, "requestDifference: " + walletUUID + " " + txId);
        if (mAmountSatoshi > 0) {
            Transaction tx = mCoreAPI.getTransaction(walletUUID, txId);
            return mAmountSatoshi - tx.getAmountSatoshi();
        } else {
            return 0;
        }
    }

    public void updateWithAmount(long newAmount) {
        mAmountSatoshi = newAmount;
        mBitcoinAmount.setText(
                String.format(getResources().getString(R.string.bitcoing_remaining),
                        mCoreAPI.formatSatoshi(mAmountSatoshi, true))
        );

        if (mCreateBitmapTask != null) {
            mCreateBitmapTask.cancel(true);
        }
        // Create a new request and qr code
        mCreateBitmapTask = new CreateBitmapTask();
        mCreateBitmapTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Alert the user
        alertPartialPayment();
    }

    private void alertPartialPayment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getResources().getString(R.string.received_partial_bitcoin_message))
                .setTitle(getResources().getString(R.string.received_partial_bitcoin_title))
                .setCancelable(true)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mPartialDialog.cancel();
                                mPartialDialog = null;
                            }
                        }
                );
        mPartialDialog = builder.create();
        mPartialDialog.show();
        mHandler.postDelayed(dialogKiller, 5000);
    }

    public class CreateBitmapTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            ((NavigationActivity) getActivity()).showModalProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Starting Receive Request at:" + System.currentTimeMillis());
            mID = mCoreAPI.createReceiveRequestFor(mWallet, "", "", mAmountSatoshi);
            if (mID != null) {
                Log.d(TAG, "Starting Request Address at:" + System.currentTimeMillis());
                mAddress = mCoreAPI.getRequestAddress(mWallet.getUUID(), mID);
                try {
                    // data in barcode is like bitcoin:address?amount=0.001
                    Log.d(TAG, "Starting QRCodeBitmap at:" + System.currentTimeMillis());
                    mQRBitmap = mCoreAPI.getQRCodeBitmap(mWallet.getUUID(), mID);
                    mQRBitmap = addWhiteBorder(mQRBitmap);
                    Log.d(TAG, "Ending QRCodeBitmap at:" + System.currentTimeMillis());
                    mRequestURI = mCoreAPI.getRequestURI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            ((NavigationActivity) getActivity()).showModalProgress(false);
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
            ((NavigationActivity) getActivity()).showModalProgress(false);
        }
    }
}
