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

package com.airbitz.models;

import com.airbitz.api.CoreAPI;

import java.util.List;

/**
 * Created on 3/13/14.
 */
public class Wallet {

    public static final String WALLET_HEADER_ID = "xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL";
    public static final String WALLET_ARCHIVE_HEADER_ID = "SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd";

    // Strings for argument passing in bundles
    public static final String WALLET_NAME = "com.airbitz.models.wallet.wallet_name";
    public static final String WALLET_AMOUNT_SATOSHI = "com.airbitz.models.wallet.wallet_amount_satoshi";
    public static final String WALLET_UUID = "com.airbitz.WalletsFragment.UUID";

    private String mName;
    private String mUUID;
    private int mCurrencyNum;
    private long mAttributes;
    private long mBalanceSatoshi = 0;
    private boolean mLoading = false;
    private List<Transaction> mTransactions;

    private CoreAPI mCoreAPI;

    private String mBalanceFormatted;

    public Wallet(String name) {
        this(name, 0, null);
    }

    public Wallet(String name, long balanceSatoshi) {
        this(name, balanceSatoshi, null);
    }

    public Wallet(String name, long balance, List<Transaction> list) {
        mName = name;
        mBalanceSatoshi = balance;
        mTransactions = list;
        mCoreAPI = CoreAPI.getApi();
    }

    public boolean isHeader() {
        return getName().equals(WALLET_HEADER_ID);
    }

    public boolean isArchiveHeader() {
        return getName().equals(WALLET_ARCHIVE_HEADER_ID);
    }

    public boolean isArchived() {
        return (getAttributes() & 0x1) == 1;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String uuid) {
        mUUID = uuid;
    }

    public int getCurrencyNum() {
        return mCurrencyNum;
    }

    public void setCurrencyNum(int num) {
        mCurrencyNum = num;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    public long getAttributes() {
        return mAttributes;
    }

    public void setAttributes(long attr) {
        mAttributes = attr;
    }

    public long getBalanceSatoshi() {
        return mBalanceSatoshi;
    }

    public void setBalanceSatoshi(long bal) {
        mBalanceSatoshi = bal;
    }

    public String getBalanceFormatted() {
        mBalanceFormatted = mCoreAPI.formatSatoshi(getBalanceSatoshi(), true);
        return mBalanceFormatted;
    }

    public void setBalanceFormatted(String amount) {
        mBalanceFormatted = amount;
    }

    public List<Transaction> getTransactions() {
        return mTransactions;
    }

    public void setTransactions(List<Transaction> list) {
        mTransactions = list;
    }
}
