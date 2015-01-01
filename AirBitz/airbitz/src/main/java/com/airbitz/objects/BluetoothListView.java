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
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


@TargetApi(21)
public class BluetoothListView extends ListView {
    private final String TAG = getClass().getSimpleName();
    private final int SCAN_PERIOD_MILLIS = 2000;
    private final int SCAN_REPEAT_PERIOD = 5000;

    private final String TRANSFER_SERVICE_UUID = "230F04B4-42FF-4CE9-94CB-ED0DC8238867";
    private final String TRANSFER_CHARACTERISTIC_UUID ="D8EF903B-B758-48FC-BBD7-F177F432A9F6";
    private final String CLIENT_CHARACTERISTIC_CONFIG ="00002900-0000-1000-8000-00805f9b34fb";

    private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
    private static boolean sIsWriting = false;


    Context mContext;
    OnPeripheralSelected mOnPeripheralSelectedListener = null;
    OnBitcoinURIReceived mOnBitcoinURIReceivedListener = null;
    OnOneScanEnded mOnOneScanEndedListener = null;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;

    List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    BluetoothSearchAdapter mSearchAdapter;
    Handler mHandler = new Handler();

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
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mOnPeripheralSelectedListener != null) {
                    mOnPeripheralSelectedListener.onPeripheralSelected(mPeripherals.get(i));
                }
            }
        });

        mSearchAdapter = new BluetoothSearchAdapter(mContext, mPeripherals);
        setAdapter(mSearchAdapter);

        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public boolean isAvailable() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
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
        mHandler.removeCallbacks(mContinuousScanRunnable);
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
    public void scanForBleDevices(boolean enable) {
        if(enable) {
            mHandler.post(mContinuousScanRunnable);
        }
        else {
            mHandler.removeCallbacks(mContinuousScanRunnable);
            scanLeDevice(false);
        }
    }

    Runnable mContinuousScanRunnable = new Runnable() {
        @Override
        public void run() {
            mPeripherals.clear();
            mSearchAdapter.notifyDataSetChanged();
            scanLeDevice(true);
            mHandler.postDelayed(this, SCAN_REPEAT_PERIOD);
        }
    };

    /*
     * Scans for BLE devices with a timeout
     * @param enable if set, enables BLE, otherwise disables
     */
    public void scanLeDevice(final boolean start) {
        if (start) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(mScanStopperRunnable, SCAN_PERIOD_MILLIS);
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
            startScan();
        } else {
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            stopScan();
        }
    }

    private void startScan() {
        //Scan for devices advertising the thermometer service
        ScanFilter airbitzFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(TRANSFER_SERVICE_UUID))
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(airbitzFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.d(TAG, "onScanResult");
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }

        private void processResult(ScanResult result) {
            boolean alreadyFound = false;
            for(BleDevice ble : mPeripherals) {
                if(ble.getDevice().getName().equals(result.getDevice().getName())) {
                    alreadyFound = true;
                    break;
                }
            }
            if(!alreadyFound) {
                Log.i(TAG, "New LE Device: " + result.getDevice().getName() + " @ " + result.getRssi());
                BleDevice device = new BleDevice(result.getDevice(), result.getRssi());
                mMessageHandler.sendMessage(Message.obtain(null, 0, device));
            }
        }
    };

    /*
     * We have a Handler to process scan results on the main thread,
     * add them to our list adapter, and update the view
     */
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BleDevice device = (BleDevice) msg.obj;
            mPeripherals.add(device);
            mSearchAdapter.notifyDataSetChanged();
        }
    };

    Runnable mScanStopperRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
            if(mOnOneScanEndedListener != null) {
                mOnOneScanEndedListener.onOneScanEnded(!mPeripherals.isEmpty());
            }
        }
    };

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

    BluetoothGattCharacteristic mCharacteristic;

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
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.v(TAG, "onCharacteristicWrite: " + status);
                    sIsWriting = false;
                    mCharacteristic = characteristic;
                    nextWrite();
                    gatt.readCharacteristic(mCharacteristic);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.v(TAG, "onDescriptorWrite: " + status);
                    sIsWriting = false;
                    nextWrite();
                    mBluetoothGatt.readDescriptor(descriptor);
//                    if(status != 0) {
//                        if(mOnBitcoinURIReceivedListener != null) {
//                            mOnBitcoinURIReceivedListener.onBitcoinURIReceived("Error onDescriptorWrite");
//                        }
//                    }
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
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    // Write username to this characteristic
                    gatt.setCharacteristicNotification(characteristic, true);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean success = characteristic.setValue("This is Sparta");
                    write(characteristic);
//                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
//                        Log.d(TAG, "Searching descriptor: " + descriptor.getUuid().toString());
//                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                        write(descriptor);
//                    }
                }

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



    //************ Helper functions

    /*
     * Android BLE does not filter on 128 bit UUIDs, so this filter is needed instead
     * Don't use startLeScan(uuids, callback), use no uuids method
     * See - http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation/21986475#21986475
     */
    private List<UUID> parseUuids(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return uuids;
    }
}
