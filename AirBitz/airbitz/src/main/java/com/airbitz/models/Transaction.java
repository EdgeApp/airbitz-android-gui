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
