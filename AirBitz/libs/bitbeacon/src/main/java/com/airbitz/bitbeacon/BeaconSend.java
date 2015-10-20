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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

// import com.airbitz.models.BleDevice;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BeaconSend {
    private final String TAG = getClass().getSimpleName();
    private final int SCAN_PERIOD_MILLIS = 1000;
    private final int SCAN_REPEAT_PERIOD = 3000;
    private final int CONNECT_TIMEOUT = 10000;

    Context mContext;
    OnPeripheralSelected mOnPeripheralSelectedListener = null;
    BluetoothAdapter mBluetoothAdapter;
    String mSelectedAdvertisedName;
    String mBroadcastName;

    List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    List<BleDevice> mFoundPeripherals = new ArrayList<BleDevice>();
    Set<String> mDeviceSet = new HashSet<String>();

    BluetoothDeviceComparator mBluetoothDeviceComparator;
    Handler mHandler = new Handler();

    public interface OnPeripheralSelected {
        public void onPeripheralError(BleDevice device);
        public void onPeripheralConnected(BleDevice device);
        public void onPeripheralDisconnect(BleDevice device);
        public void onPeripheralFailedConnect(BleDevice device);
        public void onPeripheralFailedDiscovery();

        public void onBitcoinURIReceived(String bitcoinAddress);
        public void onBitcoinURIMismatch(String response, String partialAddress, String partialAdvertisedAddress);
        public void onBitcoinURIInvalid();

        public void devicesUpdated(List<BleDevice> devices);
    }


    public BeaconSend(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setBroadcastName(String name) {
        mBroadcastName = name;
    }

    public void close() {
        mHandler.removeCallbacks(mContinuousScanRunnable);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    public void setOnPeripheralSelectedListener(OnPeripheralSelected listener) {
        mOnPeripheralSelectedListener = listener;
    }

    //********* Scanning for devices
    /*
     * Continuously fires Scans for BLE devices
     */
    public void scanForBleDevices(boolean enable) {
        if (enable) {
            mHandler.post(mContinuousScanRunnable);
        } else {
            mHandler.removeCallbacks(mContinuousScanRunnable);
            scanLeDevice(false);
        }
    }

    Runnable mContinuousScanRunnable = new Runnable() {
        @Override
        public void run() {
            mOnPeripheralSelectedListener.devicesUpdated(mPeripherals);
            scanLeDevice(true);
            mHandler.postDelayed(this, SCAN_REPEAT_PERIOD);
        }
    };

    Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mOnPeripheralSelectedListener) {
                mOnPeripheralSelectedListener.onPeripheralFailedConnect(null);
            }
        }
    };

    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

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
            mPeripherals.clear();
            if (mBluetoothDeviceComparator == null) {
                mBluetoothDeviceComparator = new BluetoothDeviceComparator();
            }
            Collections.sort(mFoundPeripherals, mBluetoothDeviceComparator);
            mPeripherals.addAll(mFoundPeripherals);
            mFoundPeripherals.clear();
            mDeviceSet.clear();
            mOnPeripheralSelectedListener.devicesUpdated(mPeripherals);
        }
    };

    class BluetoothDeviceComparator implements Comparator<BleDevice> {
        public int compare(BleDevice devA, BleDevice devB) {
            return devA.getDevice().getName().compareToIgnoreCase(devB.getDevice().getName());
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            handleResult(device, rssi, scanRecord);
        }
    };

    private void handleResult(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        if (mDeviceSet.contains(device.getAddress())) {
            return;
        }
        mDeviceSet.add(device.getAddress());
        List<UUID> uuids = parseUuids(scanRecord);
        if (device.getName() != null && uuids.size() > 0
                && uuids.get(0).toString().equalsIgnoreCase(BleUtil.AIRBITZ_SERVICE_UUID)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFoundPeripherals.add(new BleDevice(device, rssi));
                }
            });
        }
    }

    private void processResult(BleDevice bleDevice) {
        boolean alreadyFound = false;
        String name = "test";
        for (BleDevice ble : mFoundPeripherals) {
            name = ble.getDevice().getName();
            if (name != null && ble.getDevice().getName().equals(bleDevice.getDevice().getName())) {
                alreadyFound = true;
                break;
            } else if (name == null) {
                name = "test";
                alreadyFound = true;
            }
        }
        if (!alreadyFound) {
            Log.i(TAG, "New LE Device: " + name + " @ " + bleDevice.getRSSI());
            mFoundPeripherals.add(bleDevice);
        }
    }


    //************ Connecting to Device to get data
    private BluetoothGatt mBluetoothGatt;

    // Attempt GATT connection
    public void connectGatt(BleDevice result) {
        BluetoothDevice device = result.getDevice();
        mSelectedAdvertisedName = device.getName();
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        refreshDeviceCache(mBluetoothGatt);
        mHandler.postDelayed(mTimeoutRunnable, CONNECT_TIMEOUT);
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                Log.d(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

                if (null != mOnPeripheralSelectedListener) {
                    mOnPeripheralSelectedListener.onPeripheralConnected(null);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                mBluetoothGatt.close();
                mBluetoothGatt = null;

                mHandler.removeCallbacks(mTimeoutRunnable);
                if (null != mOnPeripheralSelectedListener) {
                    mOnPeripheralSelectedListener.onPeripheralDisconnect(null);
                }
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered success");
                subscribe(gatt);
            } else {
                if (null != mOnPeripheralSelectedListener) {
                    mOnPeripheralSelectedListener.onPeripheralFailedDiscovery();
                }
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic,
                                            int status) {
            Log.d(TAG, "onCharacteristicRead: " + status);
            String response = characteristic.getStringValue(0);
            if (response != null && mOnPeripheralSelectedListener != null) {
                mHandler.removeCallbacks(mTimeoutRunnable);

                Log.d(TAG, "onCharacteristicRead response = " + response);
                //make sure partial bitcoin address that was advertised is contained within the actual full bitcoin address
                String[] separate = response.split(":");
                String partialAddress;
                if(separate.length > 1) {
                    partialAddress = separate[1] != null && separate[1].length() >= 10 ?
                            separate[1].substring(0, 10) : "";

                    String partialAdvertisedAddress;
                    partialAdvertisedAddress = mSelectedAdvertisedName != null && mSelectedAdvertisedName.length() >= 10 ?
                            mSelectedAdvertisedName.substring(0, 10) : "";

                    if (mSelectedAdvertisedName == null || partialAddress.isEmpty() ||
                            !partialAdvertisedAddress.equals(partialAddress)) {
                        mOnPeripheralSelectedListener.onBitcoinURIMismatch(response, partialAddress, partialAdvertisedAddress);
                    } else {
                        Log.d(TAG, "mOnPeripheralSelectedListener.onBitcoinURIReceived(response)");
                        mOnPeripheralSelectedListener.onBitcoinURIReceived(response);
                    }
                } else {
                    mOnPeripheralSelectedListener.onBitcoinURIInvalid();
                }
            }
        }

        // On a successful write to the characteristic, do a read
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v(TAG, "onCharacteristicWrite: " + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                boolean success = gatt.readCharacteristic(characteristic);
                if (!success && null != mOnPeripheralSelectedListener) {
                    mOnPeripheralSelectedListener.onPeripheralFailedConnect(null);
                }
            }
        }
    };

    private void subscribe(BluetoothGatt gatt) {
        // Find our Airbitz service and characteristic
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID));
            if (characteristic != null) {
                Log.d(TAG, "subscribing to Airbitz...");
                final int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    // Write sendName to this characteristic requesting a response
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    String sendName = mBroadcastName;
                    // sendString has a limit of 18 characters.
                    String sendString = sendName.length() > 18 ? sendName.substring(0, 18) : sendName;
                    if (!characteristic.setValue(sendString)) {
                        Log.d(TAG, "Failed to setValue");
                    }
                    boolean success = gatt.writeCharacteristic(characteristic);
                    if (!success) {
                        if (null != mOnPeripheralSelectedListener) {
                            mOnPeripheralSelectedListener.onPeripheralFailedConnect(null);
                        }
                    }
                    return;
                }
            }
        }
        if (null != mOnPeripheralSelectedListener) {
            mOnPeripheralSelectedListener.onPeripheralFailedConnect(null);
        }
    }

    /**
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
