package com.airbitz.api;

import com.airbitz.AirbitzApplication;
import com.airbitz.models.AccountTransaction;
import com.airbitz.models.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 6/20/14.
 * This class is a bridge to the ndk core code, acting like a viewmodel
 */
public class CoreAPI {
    private static String TAG = AirbitzAPI.class.getSimpleName();

    static {
        System.loadLibrary("airbitz");
    }

    private static CoreAPI mInstance = null;

    private CoreAPI(){ }

    public static CoreAPI getApi(){
        if(mInstance == null){
            mInstance = new CoreAPI();
        }
        return mInstance;
    }


    /*
     * Wallet handling
     */
    private static final int WALLET_ATTRIBUTE_ARCHIVE_BIT = 0x1; // BIT0 is the archive bit

    public List<Wallet> getWallets() {
        // TODO replace with API call
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets();

        list.add(new Wallet("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL","Hello"));//Wallet HEADER
        // Loop through and find non-archived wallets first
        for(Wallet w : coreList) {
            if((w.getAttributes() & WALLET_ATTRIBUTE_ARCHIVE_BIT) != 1)
                list.add(w);
        }
//        list.add(new Wallet("Baseball Team", "B15.000"));
//        list.add(new Wallet("Fantasy Football", "B10.000"));
//        list.add(new Wallet("Shared", "B0.000"));
//        list.add(new Wallet("Mexico", "B0.000"));
//        list.add(new Wallet("Alpha Centauri", "B0.000"));
//        list.add(new Wallet("Other", "B0.000"));
        list.add(new Wallet("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd","Goodbye")); //Archive HEADER
        // Loop through and find non-archived wallets first
        for(Wallet w : coreList) {
            if((w.getAttributes() & WALLET_ATTRIBUTE_ARCHIVE_BIT) == 1)
                list.add(w);
        }
        return list;
    }

    private List<Wallet> getCoreWallets() {
        List<Wallet> mWallets = new ArrayList<Wallet>();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_WalletInfo test = core.longPtr_to_walletinfoPtr(lp);

        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        tABC_CC result = core.ABC_GetWallets(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                test, pUCount, pError);

        boolean success = result == tABC_CC.ABC_CC_Ok? true: false;

        int ptrToInfo = core.longp_value(lp);
        int count = core.intp_value(pCount);
        ppWalletInfo base = new ppWalletInfo(ptrToInfo);

        for(int i=0; i<count; i++) {
            pLong temp = new pLong(base.getPtr(base, i*4));
            long start = core.longp_value(temp);
            WalletInfo wi = new WalletInfo(start);
            Wallet in = new Wallet(wi.getName(), wi.mBalance);
            mWallets.add(in);
        }
        core.ABC_FreeWalletInfoArray(core.longPtr_to_walletinfoPtrPtr(new pLong(ptrToInfo)), count);
        return mWallets;
    }

    private class ppWalletInfo extends SWIGTYPE_p_p_sABC_WalletInfo {
        public ppWalletInfo(long ptr) {
            super(ptr, false);
        }
        public long getPtr(SWIGTYPE_p_p_sABC_WalletInfo p, long i) { return getCPtr(p)+i; }

    }

    private class WalletInfo extends tABC_WalletInfo {
        String mName;
        String mUUID;
        long mBalance;

        public WalletInfo(long pv) {
            super(pv, false);
            if(pv!=0) {
                mName = super.getSzName();
                mUUID = super.getSzUUID();
                SWIGTYPE_p_int64_t temp = super.getBalanceSatoshi();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                mBalance = core.longp_value(p);
                //TODO finish others?
            }
        }

        public String getName() { return mName; }
        public String getUUID() { return mUUID; }
    }

    private class pLong extends SWIGTYPE_p_long {
        public pLong(long ptr) { super(ptr, false); }
    }

    private class p64t extends SWIGTYPE_p_int64_t {
        public p64t(long ptr) {super(ptr, false);}
        public long getValue() {
            pLong pl = new pLong(getCPtr(this));
            return core.longp_value(pl);
        }
    }

    /*
     * Account Transaction handling
     */
    public static List<AccountTransaction> getTransactions(String walletName) {
        // TODO replace with API call
        List<AccountTransaction> list = new ArrayList<AccountTransaction>();
        list.add(new AccountTransaction("Matt Kemp","DEC 10","B25.000", "-B5.000"));
        list.add(new AccountTransaction("John Madden","DEC 15","B30.000", "-B65.000"));
        list.add(new AccountTransaction("kelly@gmail.com", "NOV 1", "B95.000", "B95.000"));

        return list;
    }

}
