package com.airbitz.api;

import android.app.Application;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.SignUpActivity;
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

    private CoreAPI() { }

    public static CoreAPI getApi() {
        if (mInstance == null) {
            mInstance = new CoreAPI();
        }
        return mInstance;
    }
    public final static native String getStringAtPtr(long jarg1);


    //*****************8 Wallet handling
    private static final int WALLET_ATTRIBUTE_ARCHIVE_BIT = 0x1; // BIT0 is the archive bit

    public List<Wallet> loadWallets() {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets();

        list.add(new Wallet("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL", "Hello"));//Wallet HEADER
        // Loop through and find non-archived wallets first
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & WALLET_ATTRIBUTE_ARCHIVE_BIT) != 1)
                list.add(wallet);
        }
        list.add(new Wallet("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd", "Goodbye")); //Archive HEADER
        // Loop through and find archived wallets now
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & WALLET_ATTRIBUTE_ARCHIVE_BIT) == 1)
                list.add(wallet);
        }
        return list;
    }

    // This is a blocking call. You must wrap this in an AsyncTask or similar.
    public boolean createWallet(String walletName, String username, String password, int dollarNum) {
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pResults = new tABC_RequestResults();

        tABC_CC result = core.ABC_CreateWallet(username, password, walletName, dollarNum, 0, null, pResults, pError);
        return result == tABC_CC.ABC_CC_Ok;
    }

    public void reloadWallet(Wallet wallet) {
        //TODO
    }

    public void setWalletOrder(List<Wallet> wallets) {
        //TODO
    }

    public boolean setWalletAttributes(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        if(AirbitzApplication.isLoggedIn()) {
            tABC_CC result = core.ABC_SetWalletAttributes(AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(), wallet.getUUID(), wallet.getAttributes(), Error);
            if (result == tABC_CC.ABC_CC_Ok) {
                return true;
            }
            else {
                Log.d("CoreAPI", "Error: CoreBridge.setWalletAttributes: "+ Error.getSzDescription());
                return false;
            }
        }
        return false;
    }

    //************ Transaction handling
    public List<AccountTransaction> loadTransactions(Wallet wallet) {
        //TODO
        return null;
    }

    public void setTransaction(Wallet wallet, AccountTransaction transaction, tABC_TxInfo txInfo) {
        //TODO
    }

    public List<AccountTransaction> searchTransactionsIn(Wallet wallet, String searchText) {
        //TODO
        return null;
    }

    public boolean storeTransaction(AccountTransaction transaction) {
        //TODO
        return false;
    }

    //************************* Currency formatting
    public String formatCurrency(double in) {
        return formatCurrency(in, true);
    }

    public String formatCurrency(double in, boolean withSymbol) {
        String pre = "";
        if (withSymbol)
            pre = "$ ";
        return pre+Double.toString(in);
    }

    public int currencyDecimalPlaces(String label) {
        int decimalPlaces = 5;
        if (label.contains("uBTC"))
            decimalPlaces = 2;
        else if (label.contains("mBTC"))
            decimalPlaces = 3;
        return decimalPlaces;
    }

    public int maxDecimalPlaces(String label) {
        int decimalPlaces = 8;
        if (label.contains("uBTC"))
            decimalPlaces = 2;
        else if (label.contains("mBTC"))
            decimalPlaces = 5;
        return decimalPlaces;
    }

    public long cleanNumberString(String value) {
        String out = value.replaceAll("[^a-zA-Z0-9]","");
        return Long.valueOf(out);
    }

    public String formatSatoshi(long amount) {
        return formatSatoshi(amount, true);
    }

    public String formatSatoshi(long amount, boolean withSymbol) {
        return formatSatoshi(amount, withSymbol, 3);
    }

    public String formatSatoshi(long amount, boolean withSymbol, int decimals) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longPtr_to_charPtrPtr(lp);

        SWIGTYPE_p_int64_t amt = core.new_int64_tp();
        core.longp_assign(core.p64_t_to_long_ptr(amt), (int) amount);

        boolean negative = amount < 0;
        tABC_CC result = core.ABC_FormatAmount(amt, ppChar, decimals, error);
        if ( result != tABC_CC.ABC_CC_Ok)
        {
            return null;
        }
        else {
            String pFormatted = getStringAtPtr(core.longp_value(lp));

            if (negative) {
                pFormatted = "(" + pFormatted + ")";
            }
            return pFormatted;
        }
    }


    public long denominationToSatoshi(String amount, int decimalPlaces) {
        long parsedAmount;
        SWIGTYPE_p_int64_t out = core.new_int64_tp();
        SWIGTYPE_p_long l = core.p64_t_to_long_ptr(out);

        String cleanAmount = amount.replaceAll(",", "");
        tABC_CC result = core.ABC_ParseAmount(cleanAmount, out, decimalPlaces);
        if (result != tABC_CC.ABC_CC_Ok)
        {
            Log.d("CoreAPI", "denomination to Satoshi error");
        }
        return core.longp_value(l);
    }

    public String conversionString(int num) {
        //TODO
        return null;
    }

    //TODO SWIG uses int for long assigns, but should be long. May have to hand code
    //Values returned are good up until max int value of 2147483647
    public String conversion(long satoshi, boolean btc)
    {
        if (!btc)
        {
            tABC_Error error = new tABC_Error();
            SWIGTYPE_p_double currency = core.new_doublep();
            SWIGTYPE_p_int64_t sat = core.new_int64_tp();
            SWIGTYPE_p_long l = core.p64_t_to_long_ptr(sat);
            core.longp_assign(l, (int) satoshi);

            core.ABC_SatoshiToCurrency(sat, currency, SignUpActivity.DOLLAR_CURRENCY_NUMBER, error);
            return formatCurrency(core.doublep_value(currency));
        }
        else // currency
        {
            return formatSatoshi(satoshi);
        }
    }

    private List<Wallet> getCoreWallets() {
        List<Wallet> mWallets = new ArrayList<Wallet>();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_WalletInfo paWalletInfo = core.longPtr_to_walletinfoPtr(lp);

        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        tABC_CC result = core.ABC_GetWallets(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                paWalletInfo, pUCount, pError);

        boolean success = result == tABC_CC.ABC_CC_Ok ? true : false;

        int ptrToInfo = core.longp_value(lp);
        int count = core.intp_value(pCount);
        ppWalletInfo base = new ppWalletInfo(ptrToInfo);

        for (int i = 0; i < count; i++) {
            pLong temp = new pLong(base.getPtr(base, i * 4));
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

        public long getPtr(SWIGTYPE_p_p_sABC_WalletInfo p, long i) {
            return getCPtr(p) + i;
        }

    }

    private class WalletInfo extends tABC_WalletInfo {
        String mName;
        String mUUID;
        long mBalance;

        public WalletInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                mName = super.getSzName();
                mUUID = super.getSzUUID();
                SWIGTYPE_p_int64_t temp = super.getBalanceSatoshi();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                mBalance = core.longp_value(p);
                //TODO finish others?
            }
        }

        public String getName() {
            return mName;
        }

        public String getUUID() {
            return mUUID;
        }
    }

    private class pLong extends SWIGTYPE_p_long {
        public pLong(long ptr) {
            super(ptr, false);
        }
    }

    private class p64t extends SWIGTYPE_p_int64_t {
        public p64t(long ptr) {
            super(ptr, false);
        }

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
        list.add(new AccountTransaction("Matt Kemp", "DEC 10", "B25.000", "-B5.000"));
        list.add(new AccountTransaction("John Madden", "DEC 15", "B30.000", "-B65.000"));
        list.add(new AccountTransaction("kelly@gmail.com", "NOV 1", "B95.000", "B95.000"));

        return list;
    }

}
