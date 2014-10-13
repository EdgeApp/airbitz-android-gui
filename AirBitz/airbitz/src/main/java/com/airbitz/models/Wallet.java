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
