package com.airbitz.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
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

    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = this.getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet_qrcode, container, false);
        ((NavigationActivity)getActivity()).hideNavBar();

        mQRView = (ImageView) view.findViewById(R.id.qr_code_view);

        artificialDelay = new ArtificialDelay();

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

        try{
            generateQRCode_general("");
        }catch (Exception e){
            e.printStackTrace();
        }

        artificialDelay.execute();

        return view;
    }

    private void generateQRCode_general(String data)throws WriterException {
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata = "ASFASGDFHAsdfsdfagdAsfasfdasdfNFOS3090ofmkslddgasdGAgaSDgASgASg";//= Uri.encode(data, "utf-8");

        BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE,252, 252);
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
        public ArtificialDelay(){

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
            bundle.putString("transaction_id","123131312314141567535684");
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

}
