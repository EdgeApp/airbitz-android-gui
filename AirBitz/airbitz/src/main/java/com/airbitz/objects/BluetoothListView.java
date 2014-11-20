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

package com.airbitz.objects;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.airbitz.adapters.BluetoothSearchAdapter;
import com.airbitz.models.BleDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


@TargetApi(18)
public class BluetoothListView extends ListView {
    private final String TAG = getClass().getSimpleName();
    private final int SCAN_PERIOD_MILLIS = 2000;
    private final int SCAN_REPEAT_PERIOD = 5000;

    public final static String TRANSFER_SERVICE_UUID = "230F04B4-42FF-4CE9-94CB-ED0DC8238867";
    public final static String TRANSFER_CHARACTERISTIC_UUID ="D8EF903B-B758-48FC-BBD7-F177F432A9F6";
    private final String CLIENT_CHARACTERISTIC_CONFIG ="00002900-0000-1000-8000-00805f9b34fb";

    private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private static boolean sIsWriting = false;


    private Context mContext;
    private OnPeripheralSelected mOnPeripheralSelectedListener = null;
    private OnBitcoinURIReceived mOnBitcoinURIReceivedListener = null;
    private OnOneScanEnded mOnOneScanEndedListener = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private ScanCallback mScanCallback;

    private List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    private BluetoothSearchAdapter mAdapter;
    private Handler mHandler = new Handler();

    public BluetoothListView(Context context) {
        super(context);
        init(context);
    }

    public BluetoothListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public BluetoothListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
            setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mOnPeripheralSelectedListener != null) {
                        mOnPeripheralSelectedListener.onPeripheralSelected(mPeripherals.get(i));
                    }
                }
            });
        }

        mAdapter = new BluetoothSearchAdapter(mContext, mPeripherals);
        setAdapter(mAdapter);
    }

    public boolean isAvailable() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            return true;
        }
        else {
            return false;
        }
    }

    public void close() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        continuousScanForDevices(false);
        mBluetoothGatt = null;
    }

    //************** Callback for notification of peripheral selected
    public interface OnPeripheralSelected {
        public void onPeripheralSelected(BleDevice device);
    }
    public void setOnPeripheralSelectedListener(OnPeripheralSelected listener) {
        mOnPeripheralSelectedListener = listener;
    }

    //************** Callback for notification of bitcoin received
    public interface OnBitcoinURIReceived {
        public void onBitcoinURIReceived(String bitcoinAddress);
    }
    public void setOnBitcoinURIReceivedListener(OnBitcoinURIReceived listener) {
        mOnBitcoinURIReceivedListener = listener;
    }

    //************** Callback for notification of each scan
    public interface OnOneScanEnded {
        public void onOneScanEnded(boolean hasDevices);
    }
    public void setOnOneScanEndedListener(OnOneScanEnded listener) {
        mOnOneScanEndedListener = listener;
    }


    //********* Scanning for devices
    /*
     * Continuously fires Scans for BLE devices
     */
    public void continuousScanForDevices(boolean enable) {
        if(enable) {
            mHandler.post(mContinuousScanRunnable);
        }
        else {
            mHandler.removeCallbacks(mContinuousScanRunnable);
            scanDevices(false);
        }
    }

    Runnable mContinuousScanRunnable = new Runnable() {
        @Override
        public void run() {
            mPeripherals.clear();
            mAdapter.notifyDataSetChanged();
            scanDevices(true);
            mHandler.postDelayed(this, SCAN_REPEAT_PERIOD);
        }
    };

    Runnable mScanStopperRunnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            if(mBluetoothScanner != null && mScanCallback != null) {
                mBluetoothScanner.stopScan(mScanCallback);
            }
            if(mOnOneScanEndedListener != null) {
                mOnOneScanEndedListener.onOneScanEnded(!mPeripherals.isEmpty());
            }
        }
    };


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanDevices(boolean enable) {
        mScanCallback =
                new ScanCallback() {
                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        Log.d(TAG, "Batch scan results returned");
                    }

                    @Override
                    public void onScanResult(int callbackType, final ScanResult result) {
                        super.onScanResult(callbackType, result);
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Log.d(TAG, "Scan result = " + result.toString());
                                List<ParcelUuid> list = result.getScanRecord().getServiceUuids();
                                if (list!=null && list.get(0).toString().equalsIgnoreCase(TRANSFER_SERVICE_UUID)) {
                                    BluetoothDevice device = result.getDevice();
//                                    Log.d(TAG, "Airbitz device found, name = " + device.getName());
                                    if(mPeripherals.size() == 0) {
                                        mPeripherals.add(new BleDevice(result.getDevice(), result.getRssi()));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    for(BleDevice bleDevice : mPeripherals) {
                                        if(!bleDevice.getDevice().getName().contains(device.getName())) {
                                            mPeripherals.add(new BleDevice(result.getDevice(), result.getRssi()));
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                    }
                };
        if (enable) {
            Log.d(TAG, "scanDevices(true)");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(mScanStopperRunnable, SCAN_PERIOD_MILLIS);

            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setServiceUuid(ParcelUuid.fromString(TRANSFER_SERVICE_UUID));
            List<ScanFilter> list = new ArrayList<ScanFilter>();
            list.add(builder.build());

            ScanSettings.Builder ssBuilder = new ScanSettings.Builder();
            ssBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

            mBluetoothScanner.startScan(list, ssBuilder.build(), mScanCallback);
        } else {
            Log.d(TAG, "scanDevices(false)");
            mBluetoothScanner.stopScan(mScanCallback);
        }

    }


    //************ Connecting to Device to get data
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    // Attempt GATT connection
    public void connectGatt(BleDevice result) {
        BluetoothDevice device = result.getDevice();
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mConnectionState = STATE_CONNECTED;
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        subscribe(gatt);
                     } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d(TAG, "onCharacteristicRead:" + status);

                    Log.d(TAG, characteristic.getUuid() +", "+ characteristic.getStringValue(0));

                    if(status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) { // Testing code
                        if(mOnBitcoinURIReceivedListener != null) {
                            mOnBitcoinURIReceivedListener.onBitcoinURIReceived("fake address");
                        }
                    }

                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.v(TAG, "onCharacteristicWrite: " + status);
                    sIsWriting = false;
                    nextWrite();
                    gatt.readCharacteristic(characteristic);
//                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
//                        Log.d(TAG, "asking read descriptor");
//                        byte[] duh = descriptor.getValue();
//                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.v(TAG, "onDescriptorWrite: " + status);
                    sIsWriting = false;
                    nextWrite();
                    Log.d(TAG, descriptor.getUuid() +", "+ descriptor.getValue().toString());
                }

                @Override
                // Result of a characteristic change operation
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic) {
                    Log.d(TAG, "Characteristic changed: ");
                    if(mOnBitcoinURIReceivedListener != null) {
                        mOnBitcoinURIReceivedListener.onBitcoinURIReceived(characteristic.getStringValue(0));
                    }
                }
            };

    private void subscribe(BluetoothGatt gatt) {
        // Find our Airbitz service and characteristic
        List<BluetoothGattService> services = gatt.getServices();
        for(BluetoothGattService service : services) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(TRANSFER_CHARACTERISTIC_UUID));
            if(characteristic != null) {
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                    Log.d(TAG, "clear notification on characteristic");
//                    setCharacteristicNotification(gatt, characteristic, false);
//                    gatt.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    // Write username to this characteristic - TODO
                    setCharacteristicNotification(gatt, characteristic, true);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean success = characteristic.setValue("This is Sparta");
                    write(characteristic);
                }
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param gatt BluetoothGatt to act on.
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.setCharacteristicNotification(characteristic, enabled);


        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            Log.d(TAG, "enabling notify on characteristic");
            if(enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }

            if((descriptor.getPermissions() | BluetoothGattDescriptor.PERMISSION_WRITE) > 0) {
                Log.d(TAG, "Descriptor has write permission");
                write(descriptor);
            }
        }
    }



    private synchronized void write(Object o) {
        if (sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(o);
        } else {
            sWriteQueue.add(o);
        }
    }

    private synchronized void nextWrite() {
        if (!sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(sWriteQueue.poll());
        }
    }

    private synchronized void doWrite(Object o) {
        if (o instanceof BluetoothGattCharacteristic) {
            sIsWriting = true;
            mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            sIsWriting = true;
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
    }

}
