package com.airbitz.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tom, adapted from coinbase DisplayQrOrNfcFragment
 */
public class Nfc
{
    private final static String TAG = "Nfc";
    private static Nfc mInstance;
    private static Activity mActivity;
    private static NfcManager mNfcManager;
    private static NfcAdapter mNfcAdapter;
    private boolean detecting = false;

    // return null if no NFC capability
    public static Nfc getNfc(Activity activity) {
        if (mInstance == null) {
            mInstance = new Nfc();
            mActivity = activity;
            // Check for available NFC Adapter
            PackageManager pm = activity.getPackageManager();
            if(pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
                Log.d(TAG, "NFC feature found");
                mNfcManager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
                mNfcAdapter = mNfcManager.getDefaultAdapter();
            }
        }
        if(mNfcAdapter!=null)
            return mInstance;
        else
            return null;
    }

    private Nfc() { }


    // Callback interface for adding and removing Nfc listeners
    private List<OnNfcReceived> mObservers = new CopyOnWriteArrayList<OnNfcReceived>();

    public interface OnNfcReceived {
        public void OnNfcReceived(String data);
    }

    public void addNfcListener(OnNfcReceived listener) {
        if(!mObservers.contains(listener)) {
            mObservers.add(listener);
            Log.d("CurrentLocationManager", "Listener added: "+listener);
        }
    }

    public void removeNfcListener(OnNfcReceived listener) {
        mObservers.remove(listener);
        Log.d("CurrentLocationManager", "Listener removed: " + listener);
    }

    private void notifyListeners(String data) {
        for(OnNfcReceived o : mObservers) {
            o.OnNfcReceived(data);
        }
    }

    // Start an NDEF message
    protected void startNfc(String uri) {
        if(!detecting) {
            Log.d(TAG, "Enable nfc forground mode");
            startDetectingNfcStateChanges();
            NdefMessage message = new NdefMessage(new NdefRecord[] { NdefRecord.createUri(uri) });
            mNfcAdapter.setNdefPushMessage(message, mActivity);
            detecting = true;
        }
    }

     // Stop an NDEF message
    protected void stopNfc() {
        if(detecting) {
            Log.d(TAG, "Disable nfc forground mode");
            mNfcAdapter.disableForegroundDispatch(mActivity);
            stopDetectingNfcStateChanges();
            detecting = false;
        }
    }

    private BroadcastReceiver mNfcReciever;
    protected void initializeNfc() {
//        nfcPendingIntent = PendingIntent.getActivity(mActivity, 0, new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


//        writeTagFilters = new IntentFilter[] {ndefDetected, tagDetected, techDetected, tagLost};

        mNfcReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }


    public void startDetectingNfcStateChanges() {
//        mActivity.registerReceiver(nfcStateChangeBroadcastReceiver, nfcStateChangeIntentFilter);
    }

    public void stopDetectingNfcStateChanges() {
//        mActivity.unregisterReceiver(nfcStateChangeBroadcastReceiver);
    }



    protected void startSettingsActivity() {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            mActivity.startActivity(new Intent(Activity.NFC_SERVICE));
        } else {
            mActivity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }
}