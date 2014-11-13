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
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
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
import java.util.UUID;


@TargetApi(18)
public class BluetoothListView extends ListView {
    private final String TAG = getClass().getSimpleName();
    private final int SCAN_PERIOD_MILLIS = 2000;
    private final int SCAN_REPEAT_PERIOD = 5000;

    private final String TRANSFER_SERVICE_UUID = "230F04B4-42FF-4CE9-94CB-ED0DC8238867";
    private final String TRANSFER_CHARACTERISTIC_UUID ="D8EF903B-B758-48FC-BBD7-F177F432A9F6";

    Context mContext;
    OnPeripheralSelected mOnPeripheralSelected = null;
    BluetoothAdapter mBluetoothAdapter;

    List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    BluetoothSearchAdapter mAdapter;
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mOnPeripheralSelected != null) {
                    mOnPeripheralSelected.onPeripheralFound(mPeripherals.get(i));
                }
            }
        });

        mAdapter = new BluetoothSearchAdapter(mContext, mPeripherals);
        setAdapter(mAdapter);
    }

    public boolean isAvailable() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return (mBluetoothAdapter != null) &&
                    mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }
        else {
            return false;
        }
    }

    public void close() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
    }

    //************** Callback for notification of peripheral selected
    public interface OnPeripheralSelected {
        public void onPeripheralFound(BleDevice device);
    }
    public void setOnPeripheralFound(OnPeripheralSelected listener) {
        mOnPeripheralSelected = listener;
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
            mAdapter.notifyDataSetChanged();
            scanLeDevice(true);
            mHandler.postDelayed(this, SCAN_REPEAT_PERIOD);
        }
    };

    /*
     * Scans for BLE devices with a timeout
     * @param enable if set, enables BLE, otherwise disables
     */
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(mScanStopperRunnable, SCAN_PERIOD_MILLIS);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    Runnable mScanStopperRunnable = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<UUID> uuids = parseUuids(scanRecord);
                            if(device.getName() != null && uuids.get(0).toString().equalsIgnoreCase(TRANSFER_SERVICE_UUID)) {
                                mPeripherals.add(new BleDevice(device, rssi));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
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
                        // Find our Airbitz service and characteristic
                        List<BluetoothGattService> services = gatt.getServices();
                        for(BluetoothGattService service : services) {
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(TRANSFER_CHARACTERISTIC_UUID));
                            if(characteristic != null) {
                                // Write username to this characteristic - TODO
                                boolean success = characteristic.setValue("This is Sparta!");
                                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                                mBluetoothGatt.writeCharacteristic(characteristic);
                            }
                        }
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d(TAG, "onCharacteristicRead");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Received: " + characteristic.getStringValue(0));
                    }
                }

                @Override
                // Result of a characteristic write operation
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "Written: " + characteristic.getStringValue(0));
                    }
                }

                @Override
                // Result of a characteristic change operation
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic) {
                    Log.d(TAG, "Characteristic changed: "+characteristic.getStringValue(0));
                }
            };



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
