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
import android.os.Message;
import android.os.ParcelUuid;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BluetoothSearchAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.BleDevice;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 BitBeacon - an Airbitz BLE transfer protocol for a bitcoin request

 Requestor      Sender    Network  - Bitcoin transaction roles
 Peripheral     Central            - Bluetooth GAP definitions
   Mode           Mode
    |--BitBeacon-->|        |      - Requestor Advertises partial bitcoin: URI + Name
    |     ...      |        |
    |<-------------|        |      - Subscribe then send sender's Name, requesting a response
    |------------->|        |      - ACK
    |<-------------|        |      - request Read Characteristic from peripheral
    |------------->|        |      - Sender receives full bitcoin: URI
    |     ...      |        |
    |              |------->|      - Sender initiates bitcoin transaction to network
    |<----------------------|      - Requestor receives transaction confirmation from network

 The BitBeacon protocol over Bluetooth Low Energy (BLE) uses available BLE protocols to create
 a useful service in transferring a payment request to a sender over a local BLE unsecured
 connection. Only a public URI is transferred just like a text message or email, maintaining
 normal bitcoin security.
 Instead of the requestor needing to present a POS terminal's QR code to the sender,
 Bluetooth Low Energy broadcasts the request. The sender sees this
 broadcast if their device is capable and configured appropriately.
 The sender would select the proper request, in case of other simultaneous BitBeacon transactions,
 then receive the full bitcoin URI request, the same as from any other source.
 This is the end of the BitBeacon protocol.

 The sender then completes the bitcoin transaction by starting a transfer of bitcoin
 on the bitcoin network. The requestor waits to receive confirmation of bitcoin from the bitcoin
 network. When that occurs, the bitcoin transaction is complete.

 Protocol technical details
 The Peripheral (requester) is in active scan mode to send a 62 byte advertisement. This
 advertisement has 3 Ad Structures of which 2 are of primary interest for the BitBeacon transfer:
 Type 7: 128 bit UUID - BitBeacon UUID identity
 Type 9: Local Name - Partial Public Address and optional Identity characters

 The Local Name is limited to 29 for iOS devices, and 26 for Android devices as of
 Android 5.0.1. Local Name's first 10 characters are the first 10 characters of the public address in
 the bitcoin URI. The rest of the Local Name is whatever Identity characters are needed for the
 sender to know it is from the requestor specifically, i.e. "Joe's Grill 2".

 Sender's name is also limited. For Android it's 18 characters. TBD iOS.

 Definitions
 BLE - Bluetooth Low Energy
 Peripheral - Bluetooth SIG definition of the advertiser / beacon
 Central - Bluetooth SIG definition of the scanner device listening for advertisements
 Requester - A bitcoin user making a transfer request to another user
 Sender - A bitcoin user sending bitcoin to a requester
 Network - the distributed bitcoin transaction network
*/

/* BluetoothListView specific
 The Central device is put into active scan mode to request the additional Local Name type
 which contains the first 10 bytes of the URI public address and optionally a text identifier
 up to the limits of the advertisement data. This is for the Sender
 to view in this list to visually verify the Requestor in case of multiple, simultaneous
 BLE transactions.
 Once a list item is clicked, the Sender receives the full bitcoin: URI with full
 public address and optionally the amount and requester's payment name. This request is
 announced back to this list's observer.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothListView extends ListView {
    private final String TAG = getClass().getSimpleName();
    private final int SCAN_PERIOD_MILLIS = 2000;
    private final int SCAN_REPEAT_PERIOD = 5000;

    NavigationActivity mActivity;
    OnPeripheralSelected mOnPeripheralSelectedListener = null;
    OnBitcoinURIReceived mOnBitcoinURIReceivedListener = null;
    OnOneScanEnded mOnOneScanEndedListener = null;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    String mSelectedAdvertisedName;

    List<BleDevice> mPeripherals = new ArrayList<BleDevice>();
    BluetoothSearchAdapter mSearchAdapter;
    Handler mHandler = new Handler();
    CoreAPI mCoreAPI;

    public BluetoothListView(NavigationActivity activity) {
        super(activity);
        init(activity);
    }

    public BluetoothListView(NavigationActivity activity, AttributeSet attrs, int defStyle) {
        super(activity, attrs, defStyle);
        init(activity);
    }

    public BluetoothListView(NavigationActivity activity, AttributeSet attrs) {
        super(activity, attrs);
        init(activity);
    }

    public void init(NavigationActivity activity) {
        mActivity = activity;
        mCoreAPI = CoreAPI.getApi();
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mOnPeripheralSelectedListener != null) {
                    BleDevice selectedDevice = mPeripherals.get(i);
                    mSelectedAdvertisedName = selectedDevice.getDevice().getName();
                    mSearchAdapter.selectItem(view);
                    mOnPeripheralSelectedListener.onPeripheralSelected(selectedDevice);
                }
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mSearchAdapter = new BluetoothSearchAdapter(mActivity, mPeripherals);
        setAdapter(mSearchAdapter);
    }

    public void close() {
        mHandler.removeCallbacks(mContinuousScanRunnable);
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
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
            if(mOnOneScanEndedListener != null) {
                mOnOneScanEndedListener.onOneScanEnded(!mPeripherals.isEmpty());
            }
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<UUID> uuids = parseUuids(scanRecord);
                            if(device.getName() != null && uuids.size() > 0 && uuids.get(0).toString().equalsIgnoreCase(BleUtil.AIRBITZ_SERVICE_UUID)) {
                                processResult(new BleDevice(device, rssi));
                            }
                        }
                    });
                }
            };

    private void processResult(BleDevice bleDevice) {
        boolean alreadyFound = false;
        String name = "test";
        for(BleDevice ble : mPeripherals) {
            name = ble.getDevice().getName();
            if(name != null && ble.getDevice().getName().equals(bleDevice.getDevice().getName())) {
                alreadyFound = true;
                break;
            }
            else if (name == null) {
                name = "test";
                alreadyFound = true;
            }
        }
        if(!alreadyFound) {
            Log.i(TAG, "New LE Device: " + name + " @ " + bleDevice.getRSSI());
            mPeripherals.add(bleDevice);
            mSearchAdapter.notifyDataSetChanged();
        }
    }


    //************ Connecting to Device to get data
    private BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic mCharacteristic;

    // Attempt GATT connection
    public void connectGatt(BleDevice result) {
        BluetoothDevice device = result.getDevice();
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        refreshDeviceCache(mBluetoothGatt);
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d(TAG, "Connected to GATT server.");
                        Log.d(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(TAG, "Disconnected from GATT server.");
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "onServicesDiscovered success");
                        subscribe(gatt);
                     } else {
                        mActivity.ShowFadingDialog(getResources().getString(R.string.bluetoothlistview_discovery_failed));
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d(TAG, "onCharacteristicRead: " + status);
                    mCharacteristic = characteristic;
                    String response = characteristic.getStringValue(0);
                    if(response != null && mOnBitcoinURIReceivedListener != null) {
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
                                String message = String.format(getResources().getString(R.string.bluetoothlistview_address_mismatch_message),
                                        partialAddress, partialAdvertisedAddress);
                                mActivity.ShowOkMessageDialog(getResources().getString(R.string.bluetoothlistview_address_mismatch_title),
                                        message);
                            } else {
                                mOnBitcoinURIReceivedListener.onBitcoinURIReceived(response);
                            }
                        }
                        else {
                            mActivity.ShowFadingDialog(getResources().getString(R.string.request_qr_ble_invalid_uri));
                        }
                    }
                }

                // On a successful write to the characteristic, do a read
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.v(TAG, "onCharacteristicWrite: " + status);
                    if(status == BluetoothGatt.GATT_SUCCESS) {
                        boolean success = gatt.readCharacteristic(characteristic);
                        if(!success) {
                            mActivity.ShowFadingDialog(getResources().getString(R.string.bluetoothlistview_connection_failed));
                        }
                    }
                }
            };

    private void subscribe(BluetoothGatt gatt) {
        // Find our Airbitz service and characteristic
        List<BluetoothGattService> services = gatt.getServices();
        for(BluetoothGattService service : services) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID));
            if(characteristic != null) {
                Log.d(TAG, "subscribing to Airbitz...");
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    // Write sendName to this characteristic requesting a response
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    String sendName = getResources().getString(R.string.request_qr_unknown);
                    if (mCoreAPI.coreSettings().getBNameOnPayments()) {
                        sendName = mCoreAPI.coreSettings().getSzFullName();
                    }
                    // sendString has a limit of 18 characters.
                    String sendString = sendName.length()>18 ? sendName.substring(0,18) : sendName;
                    characteristic.setValue(sendString);
                    boolean success = gatt.writeCharacteristic(characteristic);
                    if(!success) {
                        mActivity.ShowFadingDialog(getResources().getString(R.string.bluetoothlistview_connection_failed));
                    }
                }
            }
        }
    }

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
