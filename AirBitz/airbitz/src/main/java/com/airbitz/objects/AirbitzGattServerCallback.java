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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.airbitz.activities.NavigationActivity;

import java.nio.charset.Charset;
import java.util.UUID;

/*
 * Callback for BLE peripheral mode beacon
 */
@TargetApi(21)
public class AirbitzGattServerCallback extends BluetoothGattServerCallback {
    private String TAG = getClass().getSimpleName();

    private NavigationActivity mContext;

    String mData;

    private BluetoothGattServer mGattServer;

    public void setupServices(NavigationActivity context, BluetoothGattServer gattServer, String data) {
        mContext = context;
        if (gattServer == null || data == null) {
            throw new IllegalArgumentException("gattServer or data is null");
        }
        mGattServer = gattServer;
        mData = data;

        // setup services
        { // immediate alert
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
            Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service="
                    + service.getUuid().toString());
        } else {
            Log.d(TAG, "onServiceAdded status!=GATT_SUCCESS");
        }
    }

    public void onConnectionStateChange(BluetoothDevice device, int status,
                                        int newState) {
        Log.d(TAG, "onConnectionStateChange status =" + status + "->" + newState);
    }

    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
        if (characteristic.getUuid().equals(
                UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
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
                + Boolean.toString(responseNeeded) + " offset=" + offset);
        if (characteristic.getUuid().equals( UUID.fromString(BleUtil.AIRBITZ_CHARACTERISTIC_UUID))) {
            Log.d(TAG, "Airbitz characteristic received");
            if (value != null && value.length > 0) {
                mContext.ShowFadingDialog(new String(value));
            } else {
                Log.d(TAG, "invalid value written");
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    mData.getBytes(Charset.forName("UTF-8")));
        }
    }

//    public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
//                                        int offset, BluetoothGattDescriptor descriptor) {
//        Log.d(TAG, "onDescriptorReadRequest requestId=" + requestId + " offset=" + offset);
//        if(descriptor.getUuid().equals(UUID.fromString(BleUtil.CLIENT_CHARACTERISTIC_CONFIG))) {
//            Log.d(TAG, "Airbitz descriptor read request received");
//        }
//    }
//
//    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
//                                         BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
//                                         int offset, byte[] value) {
//        Log.d(TAG, "onDescriptorWriteRequest requestId=" + requestId + " preparedWrite="
//                + Boolean.toString(preparedWrite) + " responseNeeded="
//                + Boolean.toString(responseNeeded) + " offset=" + offset);
//        if(descriptor.getUuid().equals(UUID.fromString(BleUtil.CLIENT_CHARACTERISTIC_CONFIG))) {
//            Log.d(TAG, "Airbitz descriptor write request received");
//        }
//    }

}
