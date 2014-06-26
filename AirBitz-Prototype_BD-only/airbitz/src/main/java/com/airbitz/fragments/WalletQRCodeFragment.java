package com.airbitz.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_double;
import com.airbitz.api.SWIGTYPE_p_int64_t;
import com.airbitz.api.SWIGTYPE_p_long;
import com.airbitz.api.SWIGTYPE_p_p_char;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_TxDetails;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


public class WalletQRCodeFragment extends Fragment {

    private ArtificialDelay artificialDelay;

    private ImageView mQRView;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Button mCancelButton;
    private TextView mBitcoinAmount;
    private TextView mBitcoinAddress;

    private Bundle bundle;

    private Wallet mWallet;
    private String mBitcoin;
    private String mFiat;

    private CoreAPI mCore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
        mCore = CoreAPI.getApi();
        mWallet = mCore.getWalletFromName(bundle.getString(Wallet.WALLET_NAME));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet_qrcode, container, false);
        ((NavigationActivity)getActivity()).hideNavBar();

        mQRView = (ImageView) view.findViewById(R.id.qr_code_view);

        mBitcoinAmount = (TextView) view.findViewById(R.id.textview_bitcoin_amount);
        mBitcoinAmount.setText(bundle.getString(RequestFragment.BITCOIN_VALUE));

        mBitcoinAddress = (TextView) view.findViewById(R.id.textview_address);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mCancelButton = (Button) view.findViewById(R.id.button_cancel);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
                artificialDelay.cancel(true);
                getActivity().onBackPressed();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
                artificialDelay.cancel(true);
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Wallet info");
            }
        });

        artificialDelay = new ArtificialDelay(bundle.getString(Wallet.WALLET_NAME),
                bundle.getString(RequestFragment.BITCOIN_VALUE), bundle.getString(RequestFragment.FIAT_VALUE));
        artificialDelay.execute();

        return view;
    }

    private String createReceiveRequestFor(String name, String notes, String btc) {
        //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        //first need to create a transaction details struct
        long satoshi = mCore.denominationToSatoshi(btc, 0);

        double value = mCore.SatoshiToCurrency(satoshi, mWallet.getCurrencyNum());

//        details.setAmountSatoshi();

        //the true fee values will be set by the core
        details.setAmountFeesAirbitzSatoshi(core.new_int64_tp());
        details.setAmountFeesMinersSatoshi(core.new_int64_tp());

        details.setAmountCurrency(value);

        details.setSzName(name);
        details.setSzNotes(notes);
        details.setSzCategory("");
        details.setAttributes(0x0); //for our own use (not used by the core)

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char pRequestID = core.longPtr_to_charPtrPtr(lp);

        // create the request
        result = core.ABC_CreateReceiveRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
            mWallet.getUUID(), details, pRequestID, error);

        if (result == tABC_CC.ABC_CC_Ok)
        {
            return mCore.getStringAtPtr(core.longp_value(lp));
        }
        else
        {
            return null;
        }
    }

    private void generateQRCode_general(String data) throws WriterException {
        com.google.zxing.Writer writer = new QRCodeWriter();
//        String finaldata = "ASFASGDFHAsdfsdfagdAsfasfdasdfNFOS3090ofmkslddgasdGAgaSDgASgASg";//= Uri.encode(data, "utf-8");

        BitMatrix bm = writer.encode(data, BarcodeFormat.QR_CODE,252, 252);
        Bitmap ImageBitmap = Bitmap.createBitmap(252, 252, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < 252; i++) {//width
            for (int j = 0; j < 252; j++) {//height
                ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
            }
        }

        if (ImageBitmap != null) {
            mQRView.setImageBitmap(ImageBitmap);
        }
    }

    class ArtificialDelay extends AsyncTask<Void, Integer, Boolean> {
        public ArtificialDelay(String name, String btcValue, String fiatValue){
            String id = createReceiveRequestFor(name, btcValue, fiatValue);
            mBitcoinAddress.setText(id);
            try{
                generateQRCode_general(id);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Fragment frag = new SuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Wallet.WALLET_NAME, mWallet.getName());
            bundle.putString("transaction_id","123131312314141567535684");
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag);
        }
    }

}
