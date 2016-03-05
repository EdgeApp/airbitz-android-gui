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

package com.airbitz.bitbeacon;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

public class BeaconRequest {

    private final String TAG = getClass().getSimpleName();
    private final int READVERTISE_REPEAT_PERIOD = 1000 * 60 * 2;

    private Context mContext;
    private Handler mHandler = new Handler();
    private BluetoothLeAdvertiser mBleAdvertiser;
    private BluetoothGattServer mGattServer;
    private AdvertiseCallback mAdvCallback;
    private String mBroadcastName = " ";

    public interface BeaconRequestListener {
        public void advertiseStartFailed();
        public void invalidService();
        public void receivedConnection(String text);
    };

    private BeaconRequestListener mRequestCallback;
    private String mRequestUri;

    Runnable mContinuousReAdvertiseRunnable = new Runnable() {
        @Override
        public void run() {
            stop();
            start(mRequestUri);
            mHandler.postDelayed(this, READVERTISE_REPEAT_PERIOD);
        }
    };

    public BeaconRequest(Context context) {
        mContext = context;
    }

    public void setBroadcastName(String name) {
        if (TextUtils.isEmpty(name)) {
            mBroadcastName = " ";
        } else {
            mBroadcastName = name;
        }
    }

    public void setRequestListener(BeaconRequestListener listener) {
        mRequestCallback = listener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startRepeated(String data) {
        start(data);
        mHandler.postDelayed(mContinuousReAdvertiseRunnable, READVERTISE_REPEAT_PERIOD);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void start(String data) {
        if (data == null) {
            return;
        }
        mRequestUri = data;

        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        // The name maximum is 26 characters tested
        String[] separate = data.split(":");
        String address, name = mBroadcastName;
        if(separate[1] != null && separate[1].length() >= 10) {
            address = separate[1].substring(0, 10);
        } else {
            address = data;
        }

        String advertiseText = address + name;
        advertiseText = advertiseText.length()>26 ?
                advertiseText.substring(0, 26) : advertiseText;
        Log.d(TAG, "AdvertiseText = "+adapter.getName());
        adapter.setName(advertiseText);

        mBleAdvertiser = adapter.getBluetoothLeAdvertiser();
        AirbitzGattServerCallback bgsc = new AirbitzGattServerCallback();
        mGattServer = BleUtil.getManager(mContext).openGattServer(mContext, bgsc);
        if (null == mGattServer) {
            Log.i(TAG, "openGattServer returned null. Unable to start ble service");
            return;
        }
        bgsc.setupServices(mContext, mGattServer, data);

        mAdvCallback = new AdvertiseCallback() {
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (settingsInEffect != null) {
                    Log.d(TAG, "onStartSuccess TxPowerLv="
                            + settingsInEffect.getTxPowerLevel()
                            + " mode=" + settingsInEffect.getMode()
                            + " timeout=" + settingsInEffect.getTimeout());
                } else {
                    Log.d(TAG, "onStartSuccess, settingInEffect is null");
                }
            }

            public void onStartFailure(int errorCode) {
                mRequestCallback.advertiseStartFailed();
            }
        };

        mBleAdvertiser.startAdvertising(
                createAirbitzAdvertiseSettings(true, 0),
                createAirbitzAdvertiseData(),
                createAirbitzScanResponseData(),
                mAdvCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stop() {
        mHandler.removeCallbacks(mContinuousReAdvertiseRunnable);
        if (mGattServer != null) {
            mGattServer.clearServices();
            mGattServer.close();
            mGattServer = null;
        }
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdvCallback);
            mBleAdvertiser = null;
            mAdvCallback = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseSettings createAirbitzAdvertiseSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setConnectable(connectable);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createAirbitzAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUtil.AIRBITZ_SERVICE_UUID)));
        AdvertiseData data = builder.build();
        return data;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createAirbitzScanResponseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);
        AdvertiseData data = builder.build();
        return data;
    }

    /*
    * Callback for BLE peripheral mode beacon
    */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class AirbitzGattServerCallback extends BluetoothGattServerCallback {
        private String TAG = getClass().getSimpleName();

        String mData;
        Context mContext;

        private BluetoothGattServer mGattServer;

        public void setupServices(Context context, BluetoothGattServer gattServer, String data) {
            mContext = context;
            if (gattServer == null || data == null) {
                throw new IllegalArgumentException("gattServer or data is null");
            }
            mGattServer = gattServer;
            mData = data;

            // setup Airbitz services
            {
                BluetoothGattService ias = new BluetoothGattService(
                        UUID.fromString(BleUtil.AIRBITZ_SERVICE_UUID),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
                // alert level char.
                BluetoothGattCharacteristic alc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID),
                        BluetoothGattCharacteristic.PROPERTY_READ |
                                BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE);
                ias.addCharacteristic(alc);
                mGattServer.addService(ias);
            }
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service=" + service.getUuid().toString());
            } else {
                mRequestCallback.invalidService();
            }
        }

        public void onConnectionStateChange(BluetoothDevice device, int status,
                                            int newState) {
            Log.d(TAG, "onConnectionStateChange status =" + status + "-> state =" + newState);
        }

        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
            if (characteristic.getUuid().equals(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "AIRBITZ_CHARACTERISTIC_READ");
                characteristic.setValue(mData.substring(offset));
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }
        }

        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                                 boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset
                    + " value=" + new String(value) );
            if (characteristic.getUuid().equals(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "Airbitz characteristic received");
                String displayName = new String(value);
                if (displayName.isEmpty()) {
                    displayName = "Anonymous";
                }
                mRequestCallback.receivedConnection(displayName);
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }
        }
    }
}
