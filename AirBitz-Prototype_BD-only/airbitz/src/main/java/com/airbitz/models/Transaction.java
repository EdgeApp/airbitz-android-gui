package com.airbitz.models;

/**
 * Created on 3/14/14.
 */
public class Transaction {
    public static final String TXID = "com.airbitz.Transaction.TXID";

    private String mWalletUUID;
    private String mWalletName;
    private String mID;
    private long mDate;
    private String mName;
    private String mAddress;
    private String mCategory;
    private String mNotes;
    private String[] mBTCAddresses;
    private boolean mConfirmed;
    private int mConfirmations;
    private long mAmountSatoshi;
    private double mAmountFiat;
    private long mMinerFees;
    private long mABFees;
    private long mBalance;

    public Transaction() {
        mID = "";
        mWalletUUID = "";
        mWalletName = "";
        mName = "";
        mAddress = "";
        mDate = System.currentTimeMillis();
        mCategory = "";
        mNotes = "";
    }

    public Transaction(String walletUUID, String id, long date, String name, String address, String category,
                       String notes, String[] addresses) {
        mWalletUUID = walletUUID;
        mID = id;
        mDate = date;
        mName = name;
        mAddress = address;
        mCategory = category;
        mNotes = notes;
        mBTCAddresses = addresses;
    }

    public String getWalletUUID() { return mWalletUUID; }
    public void setWalletUUID(String uuid) { this.mWalletUUID = uuid; }

    public String getWalletName() { return mWalletName; }
    public void setWalletName(String name) { this.mWalletName = name; }

    public String getID() { return mID; }
    public void setID(String mID) { this.mID = mID; }

    public long getDate() { return mDate; }
    public void setDate(long mDate) { this.mDate = mDate; }

    public String getName() { return mName; }
    public void setName(String mName) { this.mName = mName; }

    public String getAddress() { return mAddress; }
    public void setAddress(String mAddress) { this.mAddress = mAddress; }

    public String getCategory() { return mCategory; }
    public void setCategory(String mCategory) { this.mCategory = mCategory; }

    public String getNotes() { return mNotes; }
    public void setNotes(String mNotes) { this.mNotes = mNotes; }

    public String[] getBTCAddresses() { return mBTCAddresses; }
    public void setBTCAddresses(String[] mBTCAddresses) { this.mBTCAddresses = mBTCAddresses; }

    public boolean isConfirmed() { return mConfirmed; }
    public void setConfirmed(boolean mConfirmed) { this.mConfirmed = mConfirmed; }

    public int getConfirmations() { return mConfirmations; }
    public void setConfirmations(int mConfirmations) { this.mConfirmations = mConfirmations; }

    public long getAmountSatoshi() { return mAmountSatoshi; }
    public void setAmountSatoshi(long mAmountSatoshi) { this.mAmountSatoshi = mAmountSatoshi; }

    public double getAmountFiat() { return mAmountFiat; }
    public void setAmountFiat(double mAmountFiat) { this.mAmountFiat = mAmountFiat; }

    public long getMinerFees() { return mMinerFees; }
    public void setMinerFees(long mMinerFees) { this.mMinerFees = mMinerFees; }

    public long getABFees() { return mABFees; }
    public void setABFees(long mABFees) { this.mABFees = mABFees; }

    public long getBalance() { return mBalance; }
    public void setBalance(long mBalance) { this.mBalance = mBalance; }

}
