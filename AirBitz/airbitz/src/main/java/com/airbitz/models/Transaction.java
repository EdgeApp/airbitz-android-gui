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

/**
 * Created on 3/14/14.
 */
public class Transaction {
    public static final String TXID = "com.airbitz.Transaction.TXID";
    long mAmountFeesAirbitzSatoshi;
    long mAmountFeesMinersSatoshi;
    /**
     * miners fees in satoshi
     */
    long mBizId;
    /**
     * payee business-directory id (0 otherwise)
     */
    int mAttributes;
    private String mWalletUUID;
    private String mWalletName;
    private String mID;
    private String mMalleableID;
    private long mDate;
    private String mName;
    private String mAddress;
    private String mCategory;
    private String mNotes;
    private CoreAPI.TxOutput[] mOutputs;
    private boolean mConfirmed;
    private boolean mSyncing;
    private int mConfirmations;
    private long mAmountSatoshi;
    private double mAmountFiat;
    private long mMinerFees;
    private long mABFees;
    private long mBalance;

    public Transaction() {
        mID = "";
        mMalleableID = "";
        mWalletUUID = "";
        mWalletName = "";
        mName = "";
        mAmountSatoshi = 0;
        mDate = System.currentTimeMillis();
        mCategory = "";
        mNotes = "";
        mAmountFeesAirbitzSatoshi = 0;
        mAmountFeesMinersSatoshi = 0;
        mBizId = 0;
        mAttributes = 0;
    }

    public Transaction(String walletUUID, String id, long date, String name, long satoshi, String category,
                       String notes, CoreAPI.TxOutput[] addresses, long bizID, long ABFees, long minersFees) {
        mWalletUUID = walletUUID;
        mID = id;
        mDate = date;
        mName = name;
        mAmountSatoshi = satoshi;
        mCategory = category;
        mNotes = notes;
        mOutputs = addresses;
        mBizId = bizID;
        mAmountFeesAirbitzSatoshi = ABFees;
        mAmountFeesMinersSatoshi = minersFees;
        mAttributes = 0;
    }

    public String getWalletUUID() {
        return mWalletUUID;
    }

    public void setWalletUUID(String uuid) {
        this.mWalletUUID = uuid;
    }

    public String getWalletName() {
        return mWalletName;
    }

    public void setWalletName(String name) {
        this.mWalletName = name;
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public String getmMalleableID() {
        return mMalleableID;
    }

    public void setmMalleableID(String mID) {
        this.mMalleableID = mID;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long mDate) {
        this.mDate = mDate;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String mNotes) {
        this.mNotes = mNotes;
    }

    public CoreAPI.TxOutput[] getOutputs() {
        return mOutputs;
    }

    public void setOutputs(CoreAPI.TxOutput[] outputs) {
        this.mOutputs = outputs;
    }

    public boolean isConfirmed() {
        return mConfirmed;
    }

    public void setConfirmed(boolean mConfirmed) {
        this.mConfirmed = mConfirmed;
    }

    public boolean isSyncing() {
        return mSyncing;
    }

    public void setSyncing(boolean syncing) {
        this.mSyncing = syncing;
    }

    public int getConfirmations() {
        return mConfirmations;
    }

    public void setConfirmations(int mConfirmations) {
        this.mConfirmations = mConfirmations;
    }

    public int getAttributes() {
        return mAttributes;
    }

    public void setAttributes(int mAttributes) {
        this.mAttributes = mAttributes;
    }

    public long getAmountSatoshi() {
        return mAmountSatoshi;
    }

    public void setAmountSatoshi(long mAmountSatoshi) {
        this.mAmountSatoshi = mAmountSatoshi;
    }

    public double getAmountFiat() {
        return mAmountFiat;
    }

    public void setAmountFiat(double mAmountFiat) {
        this.mAmountFiat = mAmountFiat;
    }

    public long getMinerFees() {
        return mMinerFees;
    }

    public void setMinerFees(long mMinerFees) {
        this.mMinerFees = mMinerFees;
    }

    public long getABFees() {
        return mABFees;
    }

    public void setABFees(long mABFees) {
        this.mABFees = mABFees;
    }

    public long getBalance() {
        return mBalance;
    }

    public void setBalance(long mBalance) {
        this.mBalance = mBalance;
    }

    public long getmAmountFeesAirbitzSatoshi() {
        return mAmountFeesAirbitzSatoshi;
    }

    public void setmAmountFeesAirbitzSatoshi(long mAmountFeesAirbitzSatoshi) {
        this.mAmountFeesAirbitzSatoshi = mAmountFeesAirbitzSatoshi;
    }

    public long getmAmountFeesMinersSatoshi() {
        return mAmountFeesMinersSatoshi;
    }

    public void setmAmountFeesMinersSatoshi(long mAmountFeesMinersSatoshi) {
        this.mAmountFeesMinersSatoshi = mAmountFeesMinersSatoshi;
    }

    public long getmBizId() {
        return mBizId;
    }

    public void setmBizId(long mBizId) {
        this.mBizId = mBizId;
    }


}
