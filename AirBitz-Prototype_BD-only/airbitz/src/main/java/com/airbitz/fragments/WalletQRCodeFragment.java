package com.airbitz.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;


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
        mSMSButton = (Button) view.findViewById(R.id.button_sms_address);
        mEmailButton = (Button) view.findViewById(R.id.button_email_address);
        mCancelButton = (Button) view.findViewById(R.id.button_cancel);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
//                artificialDelay.cancel(true);
                getActivity().onBackPressed();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).showNavBar();
//                artificialDelay.cancel(true);
                getActivity().onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Wallet info");
            }
        });

        //TODO integrate a finder for associating this with someone ?
        String fakeUser = "";
        String fakePhone = "";
        String id = mCoreAPI.createReceiveRequestFor(mWallet, fakeUser, fakePhone, bundle.getString(RequestFragment.BITCOIN_VALUE));
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

        return view;
    }

}
