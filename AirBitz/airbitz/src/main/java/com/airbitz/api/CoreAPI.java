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

package com.airbitz.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.models.Contact;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.utils.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tom on 6/20/14.
 * This class is a bridge to the ndk core code, acting like a viewmodel
 */
public class CoreAPI {
    private static String TAG = CoreAPI.class.getSimpleName();

    private final String CERT_FILENAME = "ca-certificates.crt";
    private static int ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS = 60;
    private static int ABC_SYNC_REFRESH_INTERVAL_SECONDS = 10;
    private static int CONFIRMED_CONFIRMATION_COUNT = 3;
    public static int ABC_DENOMINATION_BTC = 0;
    public static int ABC_DENOMINATION_MBTC = 1;
    public static int ABC_DENOMINATION_UBTC = 2;
    public static double SATOSHI_PER_BTC = 1E8;
    public static double SATOSHI_PER_mBTC = 1E5;
    public static double SATOSHI_PER_uBTC = 1E2;
    public static int OTP_RESET_DELAY_SECS = 60 * 60 * 24 * 7;

    static {
        System.loadLibrary("abc");
        System.loadLibrary("airbitz");
    }

    private static CoreAPI mInstance = null;
    private static boolean initialized = false;

    private CoreAPI() { }

    public static CoreAPI getApi() {
        if (mInstance == null) {
            mInstance = new CoreAPI();
            Log.d(TAG, "New CoreAPI");
        }
        return mInstance;
    }

    private Context mContext;

    public native String getStringAtPtr(long pointer);
    public native byte[] getBytesAtPtr(long pointer, int length);
    public native long get64BitLongAtPtr(long pointer);
    public native void set64BitLongAtPtr(long pointer, long value);
    public native int FormatAmount(long satoshi, long ppchar, long decimalplaces, boolean addSign, long perror);
    public native int satoshiToCurrency(String jarg1, String jarg2, long satoshi, long currencyp, int currencyNum, long error);
    public native int coreDataSyncAll(String jusername, String jpassword, long jerrorp);
    public native int coreDataSyncAccount(String jusername, String jpassword, long jerrorp);
    public native int coreDataSyncWallet(String jusername, String jpassword, String juuid, long jerrorp);
    public native int coreSweepKey(String jusername, String jpassword, String juuid, String wif, long ppchar, long jerrorp);
    public native int coreWatcherLoop(String juuid, long jerrorp);
    public native boolean RegisterAsyncCallback ();
    public native long ParseAmount(String jarg1, int decimalplaces);

    public void Initialize(Context context, String seed, long seedLength){
        if(!initialized) {
            tABC_Error error = new tABC_Error();
            if(RegisterAsyncCallback()) {
                Log.d(TAG, "Registered for core callbacks");
            }
            File filesDir = context.getFilesDir();
            List<String> files = Arrays.asList(filesDir.list());
            OutputStream outputStream = null;
            if(!files.contains(CERT_FILENAME)) {
                InputStream certStream = context.getResources().openRawResource(R.raw.ca_certificates);
                try {
                    outputStream = context.openFileOutput(CERT_FILENAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                copyStreamToFile(certStream, outputStream);
            }
            core.ABC_Initialize(filesDir.getPath(), filesDir.getPath() + "/" + CERT_FILENAME, seed, seedLength, error);
            initialized = true;
        }
        mContext = context;
    }

    public void setupAccountSettings() {
        newCoreSettings();
    }

    /**
     * copy file from source to destination
     *
     * @param src source
     * @param outputStream destination
     * @throws java.io.IOException in case of any problems
     */
    void copyStreamToFile(InputStream src, OutputStream outputStream) {
        final byte[] largeBuffer = new byte[1024 * 4];
        int bytesRead;

        try {
            while ((bytesRead = src.read(largeBuffer)) > 0) {
                if (largeBuffer.length == bytesRead) {
                    outputStream.write(largeBuffer);
                } else {
                    final byte[] shortBuffer = new byte[bytesRead];
                    System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead);
                    outputStream.write(shortBuffer);
                }
            }
            outputStream.flush();
            outputStream.close();
            src.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //***************** Callback handling
    public void callbackAsyncBitcoinInfo(long asyncBitCoinInfo_ptr) {
        tABC_AsyncBitCoinInfo info = new tABC_AsyncBitCoinInfo(asyncBitCoinInfo_ptr, false);
        tABC_AsyncEventType type = info.getEventType();

        Log.d(TAG, "asyncBitCoinInfo callback type = "+type.toString());
        if(type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingBitCoin) {
            if (mOnIncomingBitcoin != null) {
                mIncomingUUID = info.getSzWalletUUID();
                mIncomingTxID = info.getSzTxID();
                mPeriodicTaskHandler.removeCallbacks(IncomingBitcoinUpdater);
                mPeriodicTaskHandler.postDelayed(IncomingBitcoinUpdater, 300);
            }
            else {
                Log.d(TAG, "incoming bitcoin event has no listener");
            }
        }
        else if (type==tABC_AsyncEventType.ABC_AsyncEventType_BlockHeightChange) {
            if(mOnBlockHeightChange!=null)
                mPeriodicTaskHandler.post(BlockHeightUpdater);
            else
                Log.d(TAG, "block exchange event has no listener");
        }
        else if (type==tABC_AsyncEventType.ABC_AsyncEventType_DataSyncUpdate) {
            if (mOnDataSync != null) {
                mPeriodicTaskHandler.removeCallbacks(DataSyncUpdater);
                mPeriodicTaskHandler.postDelayed(DataSyncUpdater, 1000);
            } else {
                Log.d(TAG, "data sync event has no listener");
            }
        }
        else if (type==tABC_AsyncEventType.ABC_AsyncEventType_RemotePasswordChange) {
            if(mOnRemotePasswordChange!=null)
                mPeriodicTaskHandler.post(RemotePasswordChangeUpdater);
            else
                Log.d(TAG, "remote password event has no listener");
        }
        else if (type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingSweep) {
            if (mOnWalletSweep != null) {
                mIncomingUUID = info.getSzTxID();
                mSweepSatoshi = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(info.getSweepSatoshi()));
                mPeriodicTaskHandler.removeCallbacks(WalletSweepUpdater);
                mPeriodicTaskHandler.post(WalletSweepUpdater);
            }
            else {
                Log.d(TAG, "incoming bitcoin event has no listener");
            }
        }
    }

    private String mIncomingUUID, mIncomingTxID;
    private long mSweepSatoshi;
    // Callback interface when an incoming bitcoin is received
    private OnIncomingBitcoin mOnIncomingBitcoin;

    public interface OnIncomingBitcoin {
        public void onIncomingBitcoin(String walletUUID, String txId);
    }
    public void setOnIncomingBitcoinListener(OnIncomingBitcoin listener) {
        mOnIncomingBitcoin = listener;
    }
    final Runnable IncomingBitcoinUpdater = new Runnable() {
        public void run() { mOnIncomingBitcoin.onIncomingBitcoin(mIncomingUUID, mIncomingTxID); }
    };

    // Callback interface when a block height change is received
    private OnBlockHeightChange mOnBlockHeightChange;
    public interface OnBlockHeightChange {
        public void onBlockHeightChange();
    }
    public void setOnBlockHeightChangeListener(OnBlockHeightChange listener) {
        mOnBlockHeightChange = listener;
    }
    final Runnable BlockHeightUpdater = new Runnable() {
        public void run() {
            mCoreSettings = null;
            mOnBlockHeightChange.onBlockHeightChange();
        }
    };

    // Callback interface when a data sync change is received
    private OnDataSync mOnDataSync;
    public interface OnDataSync {
        public void OnDataSync();
    }
    public void setOnDataSyncListener(OnDataSync listener) {
        mOnDataSync = listener;
    }
    final Runnable DataSyncUpdater = new Runnable() {
        public void run() {
            mCoreSettings = null;
            startWatchers();
            mOnDataSync.OnDataSync();
        }
    };

    // Callback interface when a remote mPassword change is received
    private OnRemotePasswordChange mOnRemotePasswordChange;
    public interface OnRemotePasswordChange {
        public void OnRemotePasswordChange();
    }
    public void setOnOnRemotePasswordChangeListener(OnRemotePasswordChange listener) {
        mOnRemotePasswordChange = listener;
    }
    final Runnable RemotePasswordChangeUpdater = new Runnable() {
        public void run() { mOnRemotePasswordChange.OnRemotePasswordChange(); }
    };

    // Callback interface for a wallet sweep
    private OnWalletSweep mOnWalletSweep;
    public interface OnWalletSweep {
        public void OnWalletSweep(String uuid, long satoshis);
    }
    public void setOnWalletSweepListener(OnWalletSweep listener) {
        mOnWalletSweep = listener;
    }
    final Runnable WalletSweepUpdater = new Runnable() {
        public void run() { mOnWalletSweep.OnWalletSweep(mIncomingUUID, mSweepSatoshi); }
    };

    final Runnable ExchangeRateUpdater = new Runnable() {
        public void run() {
            mCoreSettings = null;
            mPeriodicTaskHandler.postDelayed(this, 1000 * ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS);
            updateExchangeRates();
        }
    };

    //***************** Wallet handling
    public List<Wallet> loadWallets() {
        return loadWallets(true);
    }

    public List<Wallet> loadWallets(boolean withTransactions) {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets(withTransactions);

        if(coreList==null)
            coreList = new ArrayList<Wallet>();
        Wallet headerWallet = new Wallet(Wallet.WALLET_HEADER_ID);
        headerWallet.setUUID(Wallet.WALLET_HEADER_ID);
        list.add(headerWallet);//Wallet HEADER
        // Loop through and find non-archived wallets first
        for (Wallet wallet : coreList) {
            if (!wallet.isArchived() && wallet.getName()!=null)
                list.add(wallet);
        }
        Wallet archiveWallet = new Wallet(Wallet.WALLET_ARCHIVE_HEADER_ID);
        archiveWallet.setUUID(Wallet.WALLET_ARCHIVE_HEADER_ID);
        list.add(archiveWallet); //Archive HEADER
        // Loop through and find archived wallets now
        for (Wallet wallet : coreList) {
            if (wallet.isArchived() && wallet.getName()!=null)
                list.add(wallet);
        }
        return list;
    }

    // This is a blocking call. You must wrap this in an AsyncTask or similar.
    public boolean createWallet(String username, String password, String walletName, int currencyNum) {
        if (!hasConnectivity()) {
            return false;
        }
        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_CreateWallet(username, password,
                walletName, currencyNum, ppChar, pError);
        if (result == tABC_CC.ABC_CC_Ok) {
            startWatchers();
            return true;
        } else {
            Log.d(TAG, "Create wallet failed - "+pError.getSzDescription()+", at "+pError.getSzSourceFunc());
            return result == tABC_CC.ABC_CC_Ok;
        }
    }

    public boolean renameWallet(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        tABC_CC result = core.ABC_RenameWallet(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), wallet.getName(), Error);
        return result == tABC_CC.ABC_CC_Ok;
    }

    public void reloadWallet(Wallet wallet) {
        Wallet info = getWalletFromUUID(wallet.getUUID());
        wallet.setName(info.getName());
        wallet.setUUID(info.getUUID());
        wallet.setAttributes(info.getAttributes());
        wallet.setBalanceSatoshi(info.getBalanceSatoshi());
        wallet.setCurrencyNum(info.getCurrencyNum());
    }

    public Wallet getWalletFromName(String walletName) {
        Wallet wallet = null;
        List<Wallet> wallets = loadWallets();
        for(Wallet w: wallets) {
            if (w != null && w.getName().contains(walletName)) {
                    wallet = w;
            }
        }
        if(wallet!=null) {
            wallet = getWalletFromUUID(wallet.getUUID());
        }
        return wallet;
    }

    public Wallet getWalletFromUUID(String uuid) {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_WalletInfo walletInfo = core.longp_to_ppWalletinfo(lp);

        tABC_CC result = core.ABC_GetWalletInfo(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, walletInfo, Error);

        int ptrToInfo = core.longp_value(lp);
        WalletInfo info = new WalletInfo(ptrToInfo);

        if (result ==tABC_CC.ABC_CC_Ok)
        {
            Wallet wallet = new Wallet(info.getName());
            wallet.setName(info.getName());
            wallet.setUUID(info.getUUID());
            wallet.setAttributes(info.getAttributes());
            wallet.setCurrencyNum(info.getCurrencyNum());
            wallet.setTransactions(getTransactions(wallet.getName()));
            wallet.setBalanceSatoshi(info.getBalance());

            return wallet;
        }
        else
        {
            Log.d("", "Error: CoreBridge.getWalletFromUUID: " + Error.getSzDescription());
            return null;
        }
    }

    public void setWalletOrder(List<Wallet> wallets) {
        boolean archived=false; // non-archive
        StringBuffer uuids = new StringBuffer("");
        for(Wallet wallet : wallets) {
            if(wallet.isArchiveHeader()) {
                archived=true;
            } else if(wallet.isHeader()) {
                archived=false;
            } else { // wallet is real
                uuids.append(wallet.getUUID()).append("\n");
                long attr = wallet.getAttributes();
                if(archived) {
                    wallet.setAttributes(1); //attr & (1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT));
                } else {
                    wallet.setAttributes(0); //attr & ~(1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT));
                }
                setWalletAttributes(wallet);
            }
        }

        tABC_Error Error = new tABC_Error();
        tABC_CC result = core.ABC_SetWalletOrder(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
            uuids.toString().trim(), Error);
        if (result != tABC_CC.ABC_CC_Ok) {
            Log.d(TAG, "Error: CoreBridge.setWalletOrder" + Error.getSzDescription());
        }
    }

    public boolean setWalletAttributes(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        if(AirbitzApplication.isLoggedIn()) {
            tABC_CC result = core.ABC_SetWalletArchived(AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(), wallet.getUUID(), wallet.getAttributes(), Error);
            if (result == tABC_CC.ABC_CC_Ok) {
                return true;
            }
            else {
                Log.d(TAG, "Error: CoreBridge.setWalletAttributes: "+ Error.getSzDescription());
                return false;
            }
        }
        return false;
    }

    //************ Account Recovery

    // Blocking call, wrap in AsyncTask
    public tABC_CC SignIn(String username, char[] password) {
        tABC_Error pError = new tABC_Error();
        tABC_CC result = core.ABC_SignIn(username, String.valueOf(password), pError);
        return result;
    }

    //************ Settings handling
    private String[] mFauxCurrencyAcronyms = {
        "AUD", "CAD", "CNY", "CUP", "EUR", "GBP", "HKD", "MXN", "NZD", "PHP", "USD"
    };
    private String[] mFauxCurrencyDenomination = {
        "$", "$", "¥", "₱", "€", "£", "$", "$", "$", "₱", "$"
    };
    private int[] mFauxCurrencyNumbers = {
        36, 124, 156, 192, 978, 826, 344, 484, 554, 608, 840
    };

    private String[] mBTCDenominations = {"BTC", "mBTC", "μBTC"};
    private String[] mBTCSymbols = {"฿ ", "m฿ ", "μ฿ "};

    public String GetUserPIN() {
        return coreSettings().getSzPIN();
    }

    public tABC_CC SetPin(String pin) {
        tABC_Error Error = new tABC_Error();

        tABC_CC cc = core.ABC_SetPIN(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), pin, Error);
        return cc;
    }


    //****** Spend Limiting
    public boolean GetDailySpendLimitSetting() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(AirbitzApplication.DAILY_LIMIT_SETTING_PREF + AirbitzApplication.getUsername())) {
            return prefs.getBoolean(AirbitzApplication.DAILY_LIMIT_SETTING_PREF + AirbitzApplication.getUsername(), true);
        }
        else {
            return coreSettings().getBDailySpendLimit();
        }
    }

    public void SetDailySpendLimitSetting(boolean set) {
        tABC_AccountSettings settings = coreSettings();
        settings.setBDailySpendLimit(set);
        saveAccountSettings(settings);

        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AirbitzApplication.DAILY_LIMIT_SETTING_PREF + AirbitzApplication.getUsername(), set);
        editor.apply();
    }

    public long GetDailySpendLimit() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(AirbitzApplication.DAILY_LIMIT_PREF + AirbitzApplication.getUsername())) {
            return prefs.getLong(AirbitzApplication.DAILY_LIMIT_PREF + AirbitzApplication.getUsername(), 0);
        }
        else {
            SWIGTYPE_p_int64_t satoshi = coreSettings().getDailySpendLimitSatoshis();
            return get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(satoshi));
        }
    }

    public void SetDailySpendSatoshis(long spendLimit) {
        SWIGTYPE_p_int64_t limit = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(limit), spendLimit);
        tABC_AccountSettings settings = coreSettings();
        settings.setDailySpendLimitSatoshis(limit);
        saveAccountSettings(settings);

        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(AirbitzApplication.DAILY_LIMIT_PREF + AirbitzApplication.getUsername(), spendLimit);
        editor.apply();
    }

    public boolean GetPINSpendLimitSetting() {
        return coreSettings().getBSpendRequirePin();
    }

    public void SetPINSpendLimitSetting(boolean set) {
        tABC_AccountSettings settings = coreSettings();
        settings.setBSpendRequirePin(set);
        saveAccountSettings(settings);
    }

    public long GetPINSpendLimit() {
        SWIGTYPE_p_int64_t satoshi = coreSettings().getSpendRequirePinSatoshis();
        return get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(satoshi));
    }

    public void SetPINSpendSatoshis(long spendLimit) {
        SWIGTYPE_p_int64_t limit = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(limit), spendLimit);
        tABC_AccountSettings settings = coreSettings();
        settings.setSpendRequirePinSatoshis(limit);
        saveAccountSettings(settings);
    }

    public long GetTotalSentToday(Wallet wallet) {
        Calendar beginning = Calendar.getInstance();
        long end = beginning.getTimeInMillis() / 1000;
        beginning.set(Calendar.HOUR_OF_DAY, 0);
        beginning.set(Calendar.MINUTE, 0);
        long start = beginning.getTimeInMillis() / 1000;

        List<Transaction> list = loadTransactionsRange(wallet, start, end);
        long sum=0;
        for(Transaction tx : list) {
            if(tx.getAmountSatoshi() < 0) {
                sum -= tx.getAmountSatoshi();
            }
        }

        return sum;
    }

    public String getDefaultBTCDenomination() {
        tABC_AccountSettings settings = coreSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination == null) {
            Log.d(TAG, "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCDenominations[bitcoinDenomination.getDenominationType()];
    }

    public String getUserBTCSymbol() {
        tABC_AccountSettings settings = coreSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination == null) {
            Log.d(TAG, "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCSymbols[bitcoinDenomination.getDenominationType()];
    }

    public String getUserCurrencyAcronym() {
        return mFauxCurrencyAcronyms[SettingsCurrencyIndex()];
    }

    public String getUserCurrencyDenomination() {
        return mFauxCurrencyDenomination[SettingsCurrencyIndex()];
    }

    public String getCurrencyDenomination(int currencyNum) {
        return mFauxCurrencyDenomination[CurrencyIndex(currencyNum)];
    }

    public int[] getCurrencyNumbers() {
        return mFauxCurrencyNumbers;
    }

    public String getCurrencyAcronym(int currencyNum) {
        return mFauxCurrencyAcronyms[CurrencyIndex(currencyNum)];
    }

    public String[] getCurrencyAcronyms() {
        //TEMP fix
        return mFauxCurrencyAcronyms;

//        String[] arrayCurrencies = null;
//        tABC_Error Error = new tABC_Error();
//
//        SWIGTYPE_p_int pCount = core.new_intp();
//
//        SWIGTYPE_p_long lp = core.new_longp();
//        SWIGTYPE_p_p_sABC_Currency pCurrency = core.longp_to_ppCurrency(lp);
//
//        tABC_CC result = core.ABC_GetCurrencies(pCurrency, pCount, Error);
//
//        if (result == tABC_CC.ABC_CC_Ok) {
//            int mCount = core.intp_value(pCount);
//            arrayCurrencies = new String[mCount];
//
//            long base = core.longp_value(lp);
//            for (int i = 0; i < 1; i++) //mCount; i++) //FIXME error when i > 0
//            {
//                long start = core.longp_value(new pLong(base + i * 4));
//                tABC_Currency txd = new Currency(start);
//                arrayCurrencies[i] = txd.getSzCode();
//            }
//        }
//        return arrayCurrencies;
    }

    private class Currency extends tABC_Currency {
        String mCode = null;
        int mNum = -1;
        String mCountries;
        String mDescription;

        public Currency(long pv) {
            super(pv, false);
            if(pv!=0) {
                mCode = super.getSzCode();
                mNum = super.getNum();
                mCountries = super.getSzCountries();
                mDescription = super.getSzDescription();
            }
        }

        public String getCode() { return mCode; }
        public int getmNum() { return mNum; }
        public String getCountries() { return mCountries; }
        public String getDescription() { return mDescription; }
    }

    private tABC_AccountSettings mCoreSettings;
    public tABC_AccountSettings coreSettings() {
        if(mCoreSettings != null) {
            return mCoreSettings;
        }

        tABC_CC result;
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_AccountSettings pAccountSettings = core.longp_to_ppAccountSettings(lp);

//        Log.d(TAG, "loading account settings for "+AirbitzApplication.getUsername()+","+AirbitzApplication.getPassword());
        result = core.ABC_LoadAccountSettings(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                pAccountSettings, Error);

        if(result==tABC_CC.ABC_CC_Ok) {
            mCoreSettings = new tABC_AccountSettings(core.longp_value(lp), false);
            if(mCoreSettings.getCurrencyNum() == 0) {
                mCoreSettings.setCurrencyNum(getLocaleDefaultCurrencyNum()); // US DOLLAR DEFAULT
                saveAccountSettings(mCoreSettings);
            }
            return mCoreSettings;
        } else {
            String message = Error.getSzDescription()+", "+Error.getSzSourceFunc();
            Log.d(TAG, "Load settings failed - "+message);
        }
        return null;
    }

    public tABC_AccountSettings newCoreSettings() {
        mCoreSettings = null;
        return coreSettings();
    }

    public void saveAccountSettings(tABC_AccountSettings settings) {
        tABC_CC result;
        tABC_Error Error = new tABC_Error();

//        Log.d(TAG, "saving account settings for "+AirbitzApplication.getUsername()+","+AirbitzApplication.getPassword());
        result = core.ABC_UpdateAccountSettings(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                settings, Error);
        if(result==tABC_CC.ABC_CC_Ok) {

        }
    }

    public List<ExchangeRateSource> getExchangeRateSources(tABC_ExchangeRateSources sources) {
        List<tABC_ExchangeRateSource> list = new ArrayList<tABC_ExchangeRateSource>();
        ExchangeRateSources temp = new ExchangeRateSources(sources.getCPtr(sources));
        return temp.getSources();
    }

    private class ExchangeRateSources extends tABC_ExchangeRateSources {
        long mNumSources = 0;
        long mChoiceStart = 0;
        List<ExchangeRateSource> sources;

        public ExchangeRateSources (long pv) {
            super(pv, false);
            if(pv!=0) {
                mNumSources = super.getNumSources();
            }
        }

        public long getNumSources() { return mNumSources; }

        public List<ExchangeRateSource> getSources() {
            sources = new ArrayList<ExchangeRateSource>();
            SWIGTYPE_p_p_sABC_ExchangeRateSource start = super.getASources();
            for(int i=0; i< mNumSources; i++) {
                ExchangeRateSources fake = new ExchangeRateSources(ppExchangeRateSource.getPtr(start, i * 4));
                mChoiceStart = fake.getNumSources();
                sources.add(new ExchangeRateSource(mChoiceStart));
            }
            return sources;
        }

        public void setSources(ExchangeRateSource[] sources) {

        }
    }

    public class ExchangeRateSource extends tABC_ExchangeRateSource {
        String mSource = null;
        long mCurrencyNum = -1;

        public ExchangeRateSource(long pv) {
            super(pv, false);
            if (pv != 0) {
                mSource = super.getSzSource();
                mCurrencyNum = super.getCurrencyNum();
            }
        }

        public String getSource() {
            return mSource;
        }

        public long getmCurrencyNum() {
            return mCurrencyNum;
        }
    }

    private static class ppExchangeRateSource extends SWIGTYPE_p_p_sABC_ExchangeRateSource {
        public static long getPtr(SWIGTYPE_p_p_sABC_ExchangeRateSource p) { return getCPtr(p); }
        public static long getPtr(SWIGTYPE_p_p_sABC_ExchangeRateSource p, long i) { return getCPtr(p)+i; }
    }

    public void addExchangeRateSource(String name, int currencyNumber) {
        ExchangeRateSource newSource = (ExchangeRateSource) new tABC_ExchangeRateSource();
        newSource.setSzSource(name);
        newSource.setCurrencyNum(currencyNumber);

        tABC_ExchangeRateSources sources = coreSettings().getExchangeRateSources();
        List<ExchangeRateSource> list = getExchangeRateSources(coreSettings().getExchangeRateSources());
        list.add(newSource);
//        sources.setASources();
    }

    //***************** Questions

    public QuestionChoice[] GetQuestionChoices() {

        QuestionChoice[] mChoices = null;
        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_long plong = core.new_longp();
        SWIGTYPE_p_p_sABC_QuestionChoices ppQuestionChoices = core.longp_to_ppQuestionChoices(plong);


        tABC_CC result = core.ABC_GetQuestionChoices(ppQuestionChoices, pError);
        if (result == tABC_CC.ABC_CC_Ok) {
            long lp = core.longp_value(plong);
            QuestionChoices qcs = new QuestionChoices(lp);
            mChoices = qcs.getChoices();
        }
        return mChoices;
    }

    public boolean hasRecoveryQuestionsSet() {
        String qstring = GetRecoveryQuestionsForUser(AirbitzApplication.getUsername());
        if (qstring != null) {
            String[] qs = qstring.split("\n");
            if (qs.length > 1) {
                // Recovery questions set
                return true;
            }
        }
        return false;
    }

    static final int RECOVERY_REMINDER_COUNT = 2;

    public void incRecoveryReminder() {
        incRecoveryReminder(1);
    }

    public void clearRecoveryReminder() {
        incRecoveryReminder(RECOVERY_REMINDER_COUNT);
    }

    private void incRecoveryReminder(int val) {
        tABC_AccountSettings settings = coreSettings();
        int reminderCount = settings.getRecoveryReminderCount();
        reminderCount += val;
        settings.setRecoveryReminderCount(reminderCount);
        saveAccountSettings(settings);
    }

    public boolean needsRecoveryReminder(Wallet wallet) {
        int reminderCount = coreSettings().getRecoveryReminderCount();
        if (reminderCount >= RECOVERY_REMINDER_COUNT) {
            // We reminded them enough
            return false;
        }

        if (wallet.getBalanceSatoshi() < 10000000) {
            // they do not have enough money to care
            return false;
        }

        if (hasRecoveryQuestionsSet()) {
            // Recovery questions already set
            clearRecoveryReminder();
            return false;
        }

        return true;
    }

    public String GetRecoveryQuestionsForUser(String username) {
        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_GetRecoveryQuestions(username, ppChar, pError);
        String questionString = getStringAtPtr(core.longp_value(lp));
        if (result == tABC_CC.ABC_CC_Ok) {
            return questionString;
        } else {
            return Common.errorMap(mContext, result);
        }
    }

    public tABC_CC SaveRecoveryAnswers(String mQuestions, String mAnswers, String password) {

        tABC_Error pError = new tABC_Error();

        tABC_CC result = core.ABC_SetAccountRecoveryQuestions(AirbitzApplication.getUsername(),
                password,
                mQuestions, mAnswers, pError);
        return result;
    }

    private class QuestionChoices extends tABC_QuestionChoices {
        long mNumChoices = 0;
        long mChoiceStart = 0;
        QuestionChoice[] choices;

        public QuestionChoices (long pv) {
            super(pv, false);
            if(pv!=0) {
                mNumChoices = super.getNumChoices();
            }
        }

        public long getNumChoices() { return mNumChoices; }

        public QuestionChoice[] getChoices() {
            choices = new QuestionChoice[(int) mNumChoices];
            SWIGTYPE_p_p_sABC_QuestionChoice start = super.getAChoices();
            for(int i=0; i<mNumChoices; i++) {
                QuestionChoices fake = new QuestionChoices(ppQuestionChoice.getPtr(start, i * 4));
                mChoiceStart = fake.getNumChoices();
                choices[i] = new QuestionChoice(new PVOID(mChoiceStart));
            }
            return choices;
        }
    }

    private class PVOID extends SWIGTYPE_p_void {
        public PVOID(long p) {
            super(p, false);
        }
    }

    private static class PVoidStatic extends SWIGTYPE_p_void {
        public static long getPtr(SWIGTYPE_p_void p) { return getCPtr(p); }
    }

    private static class ppQuestionChoice extends SWIGTYPE_p_p_sABC_QuestionChoice {
        public static long getPtr(SWIGTYPE_p_p_sABC_QuestionChoice p) { return getCPtr(p); }
        public static long getPtr(SWIGTYPE_p_p_sABC_QuestionChoice p, long i) { return getCPtr(p)+i; }
    }

    public class QuestionChoice extends tABC_QuestionChoice {
        String mQuestion = null;
        String mCategory = null;
        long mMinLength = -1;

        public QuestionChoice(SWIGTYPE_p_void pv) {
            super(PVoidStatic.getPtr(pv), false);
            if(PVoidStatic.getPtr(pv)!=0) {
                mQuestion = super.getSzQuestion();
                mCategory = super.getSzCategory();
                mMinLength = super.getMinAnswerLength();
            }
        }

        public String getQuestion() { return mQuestion; }

        public long getMinLength() { return mMinLength; }

        public String getCategory() { return mCategory; }
    }

    public String GetCSVExportData(String uuid, long start, long end) {
        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        SWIGTYPE_p_int64_t startTime = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(startTime), start); //0 means all transactions

        SWIGTYPE_p_int64_t endTime = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(endTime), end); //0 means all transactions

        tABC_CC result = core.ABC_CsvExport(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, startTime, endTime, ppChar, pError);

        if (result == tABC_CC.ABC_CC_Ok) {
            return getStringAtPtr(core.longp_value(lp)); // will be null for NoRecoveryQuestions
        }
        else if(result == tABC_CC.ABC_CC_NoTransaction) {
            return "";
        }
        else {
            Log.d(TAG, pError.getSzDescription() +";"+ pError.getSzSourceFile() +";"+ pError.getSzSourceFunc() +";"+ pError.getNSourceLine());
            return null;
        }
    }


    //************ Transaction handling
    public Transaction getTransaction(String walletUUID, String szTxId)
    {
        tABC_Error Error = new tABC_Error();
        Transaction transaction = null;

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_TxInfo pTxInfo = core.longp_to_ppTxInfo(lp);

        Wallet wallet = getWalletFromUUID(walletUUID);
        if (wallet == null)
        {
            Log.d(TAG, "Could not find wallet for "+ walletUUID);
            return null;
        }
        tABC_CC result = core.ABC_GetTransaction(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
            walletUUID, szTxId, pTxInfo, Error);
        if (result==tABC_CC.ABC_CC_Ok)
        {
            TxInfo txInfo = new TxInfo(core.longp_value(lp));
            transaction = new Transaction();
            setTransaction(wallet, transaction, txInfo);
            core.ABC_FreeTransaction(txInfo);
        }
        else
        {
            Log.d(TAG, "Error: CoreBridge.getTransaction: "+ Error.getSzDescription());
        }
        return transaction;
    }

    public List<Transaction> loadTransactionsRange(Wallet wallet, long start, long end) {
        List<Transaction> listTransactions = new ArrayList<Transaction>();
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int puCount = core.int_to_uint(pCount);

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_TxInfo paTxInfo = core.longp_to_pppTxInfo(lp);

        SWIGTYPE_p_int64_t startTime = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(startTime), start); //0 means all transactions

        SWIGTYPE_p_int64_t endTime = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(endTime), end); //0 means all transactions

        tABC_CC result = core.ABC_GetTransactions(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), startTime, endTime, paTxInfo, puCount, Error);

        if (result==tABC_CC.ABC_CC_Ok)
        {
            int ptrToInfo = core.longp_value(lp);
            int count = core.intp_value(pCount);
            ppTxInfo base = new ppTxInfo(ptrToInfo);

            for (int i = count-1; i >= 0 ; i--) {
                pLong temp = new pLong(base.getPtr(base, i * 4));
                TxInfo txi = new TxInfo(core.longp_value(temp));

                Transaction in = new Transaction();
                setTransaction(wallet, in, txi);

                listTransactions.add(in);
            }
            long bal = 0;
            for (Transaction at : listTransactions)
            {
                bal += at.getAmountSatoshi();
                at.setBalance(bal);
            }


            core.ABC_FreeTransactions(new SWIGTYPE_p_p_sABC_TxInfo(ptrToInfo, false), count);
            wallet.setTransactions(listTransactions);
        }
        else
        {
            Log.d(TAG, "Error: CoreBridge.loadAllTransactions: "+ Error.getSzDescription());
        }
        return listTransactions;
    }

    public List<Transaction> loadAllTransactions(Wallet wallet) {
        return loadTransactionsRange(wallet, 0, 0);
    }

    private class ppTxInfo extends SWIGTYPE_p_p_sABC_TxInfo {
        public ppTxInfo(long ptr) {
            super(ptr, false);
        }
        public long getPtr(SWIGTYPE_p_p_sABC_TxInfo p, long i) {
            return getCPtr(p) + i;
        }
    }

    private class TxInfo extends tABC_TxInfo {
        String mID;
        long mCountOutputs;
        long mCreationTime;
        private TxDetails mDetails;
        private TxOutput[] mOutputs;

        public TxInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                mID = super.getSzID();
                mCountOutputs = super.getCountOutputs();
                SWIGTYPE_p_int64_t temp = super.getTimeCreation();
                mCreationTime = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(temp));

                tABC_TxDetails txd = super.getPDetails();
                mDetails = new TxDetails(tABC_TxDetails.getCPtr(txd));

                if(mCountOutputs>0) {
                    mOutputs = new TxOutput[(int) mCountOutputs];
                    SWIGTYPE_p_p_sABC_TxOutput outputs = super.getAOutputs();
                    long base = SWIGTYPE_p_p_sABC_TxOutput.getCPtr(outputs);
                    for (int i = 0; i < mCountOutputs; i++) {
                        long start = core.longp_value(new pLong(base + i * 4));
                        mOutputs[i] = new TxOutput(start);
                    }
                }
            }
        }

        public String getID() { return mID; }
        public long getCount() { return mCountOutputs; }
        public long getCreationTime() { return mCreationTime; }
        public TxDetails getDetails() {return mDetails; }
        public TxOutput[] getOutputs() {return mOutputs; }
    }

    public class TxOutput extends tABC_TxOutput {
        /** Was this output used as an input to a tx? **/
        boolean     mInput;
        /** The number of satoshis used in the transaction **/
        long  mValue;
        /** The coin address **/
        String mAddress;
        /** The tx address **/
        String mTxId;
        /** The tx index **/
        long  mIndex;

        public TxOutput(long pv) {
            super(pv, false);
            if (pv != 0) {
                mInput = super.getInput();
                mAddress = super.getSzAddress();
                mTxId = super.getSzTxId();
                mValue = get64BitLongAtPtr(pv + 8);
//                mIndex = get64BitLongAtPtr(pv + 17);
//                for(int j=0; j<20; j++) {
//                    long temp = get64BitLongAtPtr(pv + j);
//                    long temp2 = temp;
//                }
            }
        }

        public boolean getmInput() {return mInput; }
        public long getmValue() {return mValue; }
        public String getAddress() {return mAddress; }
        public String getTxId() {return mTxId; }
        public long getmIndex() {return mIndex; }

    }

    public class TxDetails extends tABC_TxDetails {
        long mAmountSatoshi; /** amount of bitcoins in satoshi (including fees if any) */
        long mAmountFeesAirbitzSatoshi;   /** airbitz fees in satoshi */
        long mAmountFeesMinersSatoshi;  /** miners fees in satoshi */
        double mAmountCurrency;  /** amount in currency */
        String mName;   /** payer or payee */
        long mBizId; /** payee business-directory id (0 otherwise) */
        String mCategory;   /** category for the transaction */
        String mNotes;  /** notes for the transaction */
        int mAttributes;    /** attributes for the transaction */

       public TxDetails(long pv) {
            super(pv, false);
            if (pv != 0) {
                mAmountSatoshi = get64BitLongAtPtr(pv);
                mAmountFeesAirbitzSatoshi = get64BitLongAtPtr(pv+8);
                mAmountFeesMinersSatoshi = get64BitLongAtPtr(pv+16);

                mAmountCurrency = super.getAmountCurrency();

                mName = super.getSzName();
                mBizId = super.getBizId();
                mCategory = super.getSzCategory();
                mNotes = super.getSzNotes();
                mAttributes = (int) super.getAttributes();
            }
        }


        public long getmAmountSatoshi() { return mAmountSatoshi; }

        public long getmAmountFeesAirbitzSatoshi() { return mAmountFeesAirbitzSatoshi; }

        public long getmAmountFeesMinersSatoshi() { return mAmountFeesMinersSatoshi; }

        public double getmAmountCurrency() { return mAmountCurrency; }
    }

    public double GetPasswordSecondsToCrack(String password) {
        SWIGTYPE_p_double seconds = core.new_doublep();
        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int puCount = core.int_to_uint(pCount);
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_PasswordRule pppRules = core.longp_to_pppPasswordRule(lp);

        tABC_CC result = core.ABC_CheckPassword(password, seconds, pppRules, puCount, Error);

        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error in GetPasswordSecondsToCrack:  " + Error.getSzDescription());
            return 0;
        }
        return core.doublep_value(seconds);
    }

    public List<tABC_PasswordRule> GetPasswordRules(String password)
    {
        List<tABC_PasswordRule> list = new ArrayList<tABC_PasswordRule>();
        boolean bNewPasswordFieldsAreValid = true;

        SWIGTYPE_p_double seconds = core.new_doublep();
        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int puCount = core.int_to_uint(pCount);
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_PasswordRule pppRules = core.longp_to_pppPasswordRule(lp);

        tABC_CC result = core.ABC_CheckPassword(password, seconds, pppRules, puCount, Error);

        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error in PasswordRule:  " + Error.getSzDescription());
            return null;
        }

        int count = core.intp_value(pCount);

        long base = core.longp_value(lp);
        for (int i = 0; i < count; i++)
        {
            pLong temp = new pLong(base + i * 4);
            long start = core.longp_value(temp);
            tABC_PasswordRule pRule = new tABC_PasswordRule(start, false);
            list.add(pRule);
        }

        return list;
    }


    public void setTransaction(Wallet wallet, Transaction transaction, TxInfo txInfo) {
        transaction.setID(txInfo.getID());
        transaction.setName(txInfo.getDetails().getSzName());
        transaction.setNotes(txInfo.getDetails().getSzNotes());
        transaction.setCategory(txInfo.getDetails().getSzCategory());
        transaction.setmBizId(txInfo.getDetails().getBizId());
        transaction.setDate(txInfo.getCreationTime());

        transaction.setAmountSatoshi(txInfo.getDetails().getmAmountSatoshi());
        transaction.setABFees(txInfo.getDetails().getmAmountFeesAirbitzSatoshi());
        transaction.setMinerFees(txInfo.getDetails().getmAmountFeesMinersSatoshi());

        transaction.setAmountFiat(txInfo.getDetails().getmAmountCurrency());
        transaction.setWalletName(wallet.getName());
        transaction.setWalletUUID(wallet.getUUID());
        if(txInfo.getSzMalleableTxId()!=null) {
            transaction.setmMalleableID(txInfo.getSzMalleableTxId());
        }

        int confirmations = calcTxConfirmations(wallet, transaction, transaction.getmMalleableID());
        transaction.setConfirmations(confirmations);
        transaction.setConfirmed(false);
        transaction.setConfirmed(transaction.getConfirmations() >= CONFIRMED_CONFIRMATION_COUNT);
        if (!transaction.getName().isEmpty()) {
            transaction.setAddress(transaction.getName());
        } else {
            transaction.setAddress("");
        }

        if (!transaction.getName().isEmpty()) {
            transaction.setAddress(transaction.getName());
        } else {
            transaction.setAddress("");
        }
        TxOutput[] txo = txInfo.getOutputs();
        if(txo != null) {
            transaction.setOutputs(txo);
        }

    }

    public int calcTxConfirmations(Wallet wallet, Transaction t, String txId)
    {
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_int th = core.new_intp();
        SWIGTYPE_p_int bh = core.new_intp();

        t.setSyncing(false);
        if (wallet.getUUID().length() == 0 || txId.length() == 0) {
            return 0;
        }
        if (core.ABC_TxHeight(wallet.getUUID(), txId, core.int_to_uint(th), Error) != tABC_CC.ABC_CC_Ok) {
            t.setSyncing(true);
            return 0;
        }
        if (core.ABC_BlockHeight(wallet.getUUID(), core.int_to_uint(bh), Error) != tABC_CC.ABC_CC_Ok) {
            t.setSyncing(true);
            return 0;
        }

        int txHeight = core.intp_value(th);
        int blockHeight = core.intp_value(bh);
        if (txHeight == 0 || blockHeight == 0) {
            return 0;
        }
        return (blockHeight - txHeight) + 1;
    }

    public List<Transaction> searchTransactionsIn(Wallet wallet, String searchText) {
        List<Transaction> listTransactions = new ArrayList<Transaction>();
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int puCount = core.int_to_uint(pCount);

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_TxInfo paTxInfo = core.longp_to_pppTxInfo(lp);

        tABC_CC result = core.ABC_SearchTransactions(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), searchText, paTxInfo, puCount, Error);
        if (result==tABC_CC.ABC_CC_Ok)
        {
            int ptrToInfo = core.longp_value(lp);
            int count = core.intp_value(pCount);
            ppTxInfo base = new ppTxInfo(ptrToInfo);

            for (int i = count - 1; i >= 0; --i) {
                pLong temp = new pLong(base.getPtr(base, i * 4));
                long start = core.longp_value(temp);
                TxInfo txi = new TxInfo(start);

                Transaction transaction = new Transaction();
                setTransaction(wallet, transaction, txi);
                listTransactions.add(transaction);
            }
        }
        else
        {
            Log.i(TAG, "Error: CoreBridge.searchTransactionsIn: "+Error.getSzDescription());
        }
        return listTransactions;
    }

    public tABC_CC storeTransaction(Transaction transaction) {
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_TxDetails pDetails = core.longp_to_ppTxDetails(lp);

        tABC_CC result = core.ABC_GetTransactionDetails(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                transaction.getWalletUUID(), transaction.getID(), pDetails, Error);
        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error: CoreBridge.storeTransaction:  "+Error.getSzDescription());
            return result;
        }

        tABC_TxDetails details = new TxDetails(core.longp_value(lp));

        details.setSzName(transaction.getName());
        details.setSzCategory(transaction.getCategory());
        details.setSzNotes(transaction.getNotes());
        details.setAmountCurrency(transaction.getAmountFiat());
        details.setBizId(transaction.getmBizId());

        result = core.ABC_SetTransactionDetails(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                transaction.getWalletUUID(), transaction.getID(), details, Error);

        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error: CoreAPI.storeTransaction:  " + Error.getSzDescription());
        }

        return result;
    }

    //************************* Currency formatting

    public String formatDefaultCurrency(double in) {
        String pre = mBTCSymbols[coreSettings().getBitcoinDenomination().getDenominationType()];
        String out = String.format("%.3f", in);
        return pre+out;
    }


    public String formatCurrency(double in, int currencyNum, boolean withSymbol) {
        return formatCurrency(in, currencyNum, withSymbol, 2);
    }

    public String formatCurrency(double in, int currencyNum, boolean withSymbol, int decimalPlaces) {
        String pre;
        String denom = mFauxCurrencyDenomination[findCurrencyIndex(currencyNum)]+" ";
        if (in < 0)
        {
            in = Math.abs(in);
            pre = withSymbol ? "-" + denom : "-";
        } else {
            pre = withSymbol ? denom : "";
        }
        BigDecimal bd = new BigDecimal(in);
        DecimalFormat df;
        switch(decimalPlaces) {
            case 3:
                df = new DecimalFormat("#,##0.000", new DecimalFormatSymbols(Locale.getDefault()));
                break;
            default:
                df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.getDefault()));
                break;
        }

        return pre + df.format(bd.doubleValue());
    }

    private int findCurrencyIndex(int currencyNum) {
        for(int i=0; i< mFauxCurrencyNumbers.length; i++) {
            if(currencyNum == mFauxCurrencyNumbers[i])
                return i;
        }
        Log.d(TAG, "CurrencyIndex not found, using default");
        return 10; // default US
    }

    public int userDecimalPlaces() {
        int decimalPlaces = 8; // for ABC_DENOMINATION_BTC
        tABC_AccountSettings settings = coreSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination != null) {
            int label = bitcoinDenomination.getDenominationType();
            if (label == ABC_DENOMINATION_UBTC)
                decimalPlaces = 2;
            else if (label == ABC_DENOMINATION_MBTC)
                decimalPlaces = 5;
        }
        return decimalPlaces;
    }

    public String formatSatoshi(long amount) {
        return formatSatoshi(amount, true);
    }

    public String formatSatoshi(long amount, boolean withSymbol) {
        return formatSatoshi(amount, withSymbol, userDecimalPlaces());
    }

    public String formatSatoshi(long amount, boolean withSymbol, int decimals) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        int decimalPlaces = userDecimalPlaces();

        boolean negative = amount < 0;
        if(negative)
            amount = -amount;
        int result = FormatAmount(amount, SWIGTYPE_p_p_char.getCPtr(ppChar), decimalPlaces, false, tABC_Error.getCPtr(error));
        if ( result != 0)
        {
            return "";
        }
        else {
            decimalPlaces = decimals > -1 ? decimals : decimalPlaces;
            String pretext = "";
            if (negative) {
                pretext += "-";
            }
            if(withSymbol) {
                pretext += " "+ getUserBTCSymbol();
            }

            BigDecimal bd = new BigDecimal(amount);
            bd = bd.movePointLeft(decimalPlaces);

            DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.getDefault()));

            if(decimalPlaces == 5) {
                df = new DecimalFormat("#,##0.#####", new DecimalFormatSymbols(Locale.getDefault()));
            }
            else if(decimalPlaces == 8) {
                df = new DecimalFormat("#,##0.########", new DecimalFormatSymbols(Locale.getDefault()));
            }

            return pretext + df.format(bd.doubleValue());
        }
    }

    public int SettingsCurrencyIndex() {
        int index = -1;
        tABC_AccountSettings settings = coreSettings();
        int currencyNum = settings.getCurrencyNum();
        int[] currencyNumbers = getCurrencyNumbers();

        for(int i=0; i<currencyNumbers.length; i++) {
            if(currencyNumbers[i] == currencyNum)
                index = i;
        }
        if((index==-1) || (index >= currencyNumbers.length)) { // default usd
            Log.d(TAG, "currency index out of bounds "+index);
            index = currencyNumbers.length-1;
        }
        return index;
    }

    public int CurrencyIndex(int currencyNum) {
        int index = -1;
        int[] currencyNumbers = getCurrencyNumbers();

        for(int i=0; i<currencyNumbers.length; i++) {
            if(currencyNumbers[i] == currencyNum)
                index = i;
        }
        if((index==-1) || (index >= currencyNumbers.length)) { // default usd
            Log.d(TAG, "currency index out of bounds "+index);
            index = currencyNumbers.length-1;
        }
        return index;
    }

    public long denominationToSatoshi(String amount) {
        int decimalPlaces = userDecimalPlaces();

        String cleanAmount = amount.replaceAll(",", "");
        return ParseAmount(cleanAmount, decimalPlaces);
    }

    public String BTCtoFiatConversion(int currencyNum) {
        tABC_BitcoinDenomination denomination = coreSettings().getBitcoinDenomination();
        long satoshi = 100;
        int denomIndex = 0;
        if(denomination != null) {
            if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_BTC) {
                satoshi = (long) SATOSHI_PER_BTC;
                denomIndex = 0;
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_MBTC) {
                satoshi = (long) SATOSHI_PER_mBTC;
                denomIndex = 1;
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_UBTC) {
                satoshi = (long) SATOSHI_PER_uBTC;
                denomIndex = 2;
            }
        }
//        String currency = FormatCurrency(satoshi, currencyNum, false, false);
        double o = SatoshiToCurrency(satoshi, currencyNum);
        String currency = formatCurrency(o, currencyNum, true, 3);

        String currencyLabel = mFauxCurrencyAcronyms[CurrencyIndex(currencyNum)];
        return "1 "+mBTCDenominations[denomIndex]+" = " + currency + " " + currencyLabel;
    }

    public String FormatDefaultCurrency(long satoshi, boolean btc, boolean withSymbol)
    {
        int currencyNumber = coreSettings().getCurrencyNum();
        return FormatCurrency(satoshi, currencyNumber, btc, withSymbol);
    }

    public String FormatCurrency(long satoshi, int currencyNum, boolean btc, boolean withSymbol)
    {
        String out;
        if (!btc)
        {
            double o = SatoshiToCurrency(satoshi, currencyNum);
            out = formatCurrency(o, currencyNum, withSymbol);
        }
        else
        {
            out = formatSatoshi(satoshi, withSymbol, 2);
        }
        return out;
    }

    public double SatoshiToDefaultCurrency(long satoshi) {
        int num = coreSettings().getCurrencyNum();
        return SatoshiToCurrency(satoshi, num);
    }

    public double SatoshiToCurrency(long satoshi, int currencyNum) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_double currency = core.new_doublep();

        long out = satoshiToCurrency(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                satoshi, SWIGTYPE_p_double.getCPtr(currency), currencyNum, tABC_Error.getCPtr(error));

        return core.doublep_value(currency);
    }

    public long DefaultCurrencyToSatoshi(double currency) {
        return CurrencyToSatoshi(currency, coreSettings().getCurrencyNum());
    }

    public long CurrencyToSatoshi(double currency, int currencyNum) {
        tABC_Error error = new tABC_Error();
        tABC_CC result;
        SWIGTYPE_p_int64_t satoshi = core.new_int64_tp();
        SWIGTYPE_p_long l = core.p64_t_to_long_ptr(satoshi);

        result = core.ABC_CurrencyToSatoshi(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
        currency, currencyNum, satoshi, error);

        return get64BitLongAtPtr(l.getCPtr(l));
    }

    private static double MAX_SATOSHI = 9.223372036854775807E18; // = 0x7fffffffffffffff, but Java can't handle that.

    public boolean TooMuchBitcoin(String bitcoin) {
        double val=0.0;
        try {
            val = Double.parseDouble(bitcoin);
        } catch(NumberFormatException e) { // ignore any non-double
        }

        tABC_BitcoinDenomination denomination = coreSettings().getBitcoinDenomination();
        if(denomination != null) {
            if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_BTC) {
                val = val * SATOSHI_PER_BTC;
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_MBTC) {
                val = val * SATOSHI_PER_mBTC;
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_UBTC) {
                val = val * SATOSHI_PER_uBTC;
            }
        }
        return val > MAX_SATOSHI;
    }

    public boolean TooMuchFiat(String fiat, int currencyNum) {
        double maxFiat = SatoshiToCurrency((long) MAX_SATOSHI, currencyNum);
        double val=0.0;
        try {
            val = Double.parseDouble(fiat);
        } catch(NumberFormatException e) { // ignore any non-double
        }
        return val > maxFiat;
    }

    private tABC_TxDetails mReceiveRequestDetails;

    public String createReceiveRequestFor(Wallet wallet, String name, String notes, long satoshi) {
        //first need to create a transaction details struct
        double value = SatoshiToCurrency(satoshi, wallet.getCurrencyNum());

        //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        set64BitLongAtPtr(details.getCPtr(details)+0, satoshi);

        //the true fee values will be set by the core
        details.setAmountFeesAirbitzSatoshi(core.new_int64_tp());
        details.setAmountFeesMinersSatoshi(core.new_int64_tp());

        details.setAmountCurrency(value);
        details.setSzName(name);
        details.setSzNotes(notes);
        details.setSzCategory("");
        details.setAttributes(0x0); //for our own use (not used by the core)

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char pRequestID = core.longp_to_ppChar(lp);

        // create the request
        result = core.ABC_CreateReceiveRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), details, pRequestID, error);

        if (result == tABC_CC.ABC_CC_Ok)
        {
            mReceiveRequestDetails = details;
            return getStringAtPtr(core.longp_value(lp));
        }
        else
        {
            String message = result.toString() + "," + error.getSzDescription() + ", " +
                    error.getSzSourceFile()+", "+error.getSzSourceFunc()+", "+error.getNSourceLine();
            Log.d(TAG, message);
            return null;
        }
    }

    public void finalizeRequest(Contact contact, String type, String requestId, Wallet wallet)
    {
        if(mReceiveRequestDetails != null) {
            tABC_TxDetails details = mReceiveRequestDetails;
            TxDetails txDetails = new TxDetails(details.getCPtr(details));

            if (contact.getName() != null) {
                details.setSzName(contact.getName());
            } else if (contact.getEmail()!=null) {
                details.setSzName(contact.getEmail());
            } else if (contact.getPhone()!=null) {
                details.setSzName(contact.getPhone());
            }
            Calendar now = Calendar.getInstance();

            String notes = String.format("%s / %s requested via %s on %s.",
                    formatSatoshi(txDetails.getmAmountSatoshi()),
                    formatDefaultCurrency(txDetails.getmAmountCurrency()),
                    type,
                    String.format("%1$tA %1$tb %1$td %1$tY at %1$tI:%1$tM %1$Tp", now));

            details.setSzNotes(notes);

            tABC_Error Error = new tABC_Error();
            // Update the Details
            if (tABC_CC.ABC_CC_Ok != core.ABC_ModifyReceiveRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(),
                requestId,
                txDetails,
                Error))
            {
                Log.d(TAG, Error.toString());
            }
            // Finalize this request so it isn't used elsewhere
            if (tABC_CC.ABC_CC_Ok != core.ABC_FinalizeReceiveRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(),
                requestId,
                Error))
            {
                Log.d(TAG, Error.toString());
            }
            mReceiveRequestDetails = null;
        }
    }


    public class TxResult {
        private String txid=null;
        public String getTxId() { return txid; }

        public void setTxId(String txid) { this.txid = txid; }

        private String error=null;
        public String getError() { return error; }

        public void setError(String error) { this.error = error; }
    }

    //this is a blocking call
    public TxResult InitiateTransferOrSend(Wallet sourceWallet, String destinationAddress, long satoshi, String label) {
        TxResult txResult = new TxResult();

        tABC_Error error = new tABC_Error();
        Wallet destinationWallet = getWalletFromUUID(destinationAddress);
        if (satoshi > 0) {
            double value = SatoshiToCurrency(satoshi, sourceWallet.getCurrencyNum());

            //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
            tABC_TxDetails details = new tABC_TxDetails();
            tABC_CC result;

            set64BitLongAtPtr(tABC_TxDetails.getCPtr(details) + 0, satoshi);

            //the true fee values will be set by the core
            SWIGTYPE_p_int64_t feesAB = core.new_int64_tp();
            set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(feesAB), 1700);
            details.setAmountFeesAirbitzSatoshi(feesAB);
            SWIGTYPE_p_int64_t feesMiner = core.new_int64_tp();
            set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(feesMiner), 10000);
            details.setAmountFeesMinersSatoshi(feesMiner);

            details.setAmountCurrency(value);
            if (label == null) {
                details.setSzName("");
            } else {
                details.setSzName(label);
            }
            details.setSzNotes("");
            details.setSzCategory("");
            details.setAttributes(0x2); //for our own use (not used by the core)

            SWIGTYPE_p_long txid = core.new_longp();
            SWIGTYPE_p_p_char pTxId = core.longp_to_ppChar(txid);

            if (null != destinationWallet && destinationWallet.getUUID() != null) {
                tABC_TransferDetails Transfer = new tABC_TransferDetails();
                Transfer.setSzSrcWalletUUID(sourceWallet.getUUID());
                Transfer.setSzSrcName(destinationWallet.getName());
                Transfer.setSzSrcCategory("Transfer:Wallet:" + destinationWallet.getName());

                Transfer.setSzDestWalletUUID(destinationWallet.getUUID());
                Transfer.setSzDestName(sourceWallet.getName());
                Transfer.setSzDestCategory("Transfer:Wallet:" + sourceWallet.getName());

                result = core.ABC_InitiateTransfer(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), Transfer, details, pTxId, error);
            } else {
                result = core.ABC_InitiateSendRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                        sourceWallet.getUUID(), destinationAddress, details, pTxId, error);
            }

            if (result != tABC_CC.ABC_CC_Ok) {
                Log.d(TAG, "InitiateTransferOrSend:  " + error.getSzDescription() + " " + error.getSzSourceFile() + " " +
                        error.getSzSourceFunc() + " " + error.getNSourceLine());
                txResult.setError(error.getSzDescription());
            } else {
                Log.d(TAG, "Core InitiateTransferOrSend successful");
                txResult.setTxId(getStringAtPtr(core.longp_value(txid)));
            }
        } else {
            Log.d(TAG, "Initiate transfer - no funds to send");
            txResult.setError("Initiate transfer - no funds to send");
        }
        return txResult;
    }

    public long calcSendFees(String walletUUID, String sendTo, long sendAmount, boolean transferOnly)
    {
        long totalFees;
        tABC_Error error = new tABC_Error();
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_CC result;

        set64BitLongAtPtr(tABC_TxDetails.getCPtr(details)+0, sendAmount);
        set64BitLongAtPtr(tABC_TxDetails.getCPtr(details)+8, 0);
        set64BitLongAtPtr(tABC_TxDetails.getCPtr(details)+16, 0);

        details.setAmountCurrency(0);
        details.setSzName("");
        details.setSzNotes("");
        details.setSzCategory("");
        details.setAttributes(0); //for our own use (not used by the core)

        SWIGTYPE_p_int64_t fees = core.new_int64_tp();

        result = core.ABC_CalcSendFees(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                walletUUID, sendTo, transferOnly, details, fees, error);

        if (result != tABC_CC.ABC_CC_Ok)
        {
            if (error.getCode() != tABC_CC.ABC_CC_InsufficientFunds)
            {
                Log.d(TAG, "CalcSendFees error: "+error.getSzDescription());
            }
            return -1; // Insufficient funds or error
        }
        totalFees = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(fees));
        return totalFees;
    }

    //*************** Asynchronous Updating
    Handler mPeriodicTaskHandler = new Handler();
    private List<ExchangeRateSource> mExchangeRateSources;

    // Callback interface for adding and removing location change listeners
    private List<OnExchangeRatesChange> mExchangeRateObservers = Collections.synchronizedList(new ArrayList<OnExchangeRatesChange>());
    private List<OnExchangeRatesChange> mExchangeRateRemovers = new ArrayList<OnExchangeRatesChange>();

    public interface OnExchangeRatesChange {
        public void OnExchangeRatesChange();
    }

    public void startAllAsyncUpdates() {
        startWatchers();
        startExchangeRateUpdates();
        startFileSyncUpdates();
    }

    public void stopAllAsyncUpdates() {
        stopExchangeRateUpdates();
        stopFileSyncUpdates();
        stopWatchers();
    }

    public void restoreConnectivity() {
        if (!AirbitzApplication.isLoggedIn()) {
            return;
        }
        connectWatchers();
        startExchangeRateUpdates();
        startFileSyncUpdates();
    }

    public void lostConnectivity() {
        if (!AirbitzApplication.isLoggedIn()) {
            return;
        }
        stopExchangeRateUpdates();
        stopFileSyncUpdates();
        disconnectWatchers();
    }

    public void updateExchangeRates()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            mUpdateExchangeRateTask = new UpdateExchangeRateTask();

            Log.d(TAG, "Exchange Rate Update initiated.");
            mUpdateExchangeRateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private UpdateExchangeRateTask mUpdateExchangeRateTask;
    public class UpdateExchangeRateTask extends AsyncTask<Void, Void, Void> {
        UpdateExchangeRateTask() { }

        @Override
        protected Void doInBackground(Void... voids) {
            updateAllExchangeRates();
            return null;
        }

        protected void onPostExecute() {
            onExchangeRatesUpdated();
        }
    }

    public void stopExchangeRateUpdates() {
        mPeriodicTaskHandler.removeCallbacks(ExchangeRateUpdater);
    }

    public void startExchangeRateUpdates() {
        if(AirbitzApplication.isLoggedIn()) {
            mPeriodicTaskHandler.post(ExchangeRateUpdater);
        }
    }

    // Exchange Rate updates may have delay the first call
    public void updateAllExchangeRates()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            List<Wallet> walletList = getCoreWallets(false);
            Set<Integer> currencies = new HashSet();
            for(Wallet wallet : walletList) {
                if(wallet.getCurrencyNum() != -1) {
                    currencies.add(wallet.getCurrencyNum());
                }
            }
            tABC_Error error = new tABC_Error();
            for(Integer currency : currencies) {
                core.ABC_RequestExchangeRateUpdate(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                        currency, error);
            }
        }
    }

    public void addExchangeRateChangeListener(OnExchangeRatesChange listener) {
        if(mExchangeRateObservers.size() == 0) {
            startExchangeRateUpdates();
        }
        if(!mExchangeRateObservers.contains(listener)) {
            mExchangeRateObservers.add(listener);
        }
    }

    public void removeExchangeRateChangeListener(OnExchangeRatesChange listener) {
        mExchangeRateRemovers.add(listener);
        if(mExchangeRateObservers.size() <= 0) {
            stopExchangeRateUpdates();
        }
    }

    public void onExchangeRatesUpdated() {
        if(!mExchangeRateRemovers.isEmpty()) {
            for(OnExchangeRatesChange i : mExchangeRateRemovers) {
                if(mExchangeRateObservers.contains(i)) {
                    mExchangeRateObservers.remove(i);
                }
            }
            mExchangeRateRemovers.clear();
        }

        if (!mExchangeRateObservers.isEmpty()) {
            Log.d(TAG, "Exchange Rate changed");
            for(OnExchangeRatesChange listener : mExchangeRateObservers) {
                listener.OnExchangeRatesChange();
            }
        }
    }

    final Runnable FileSyncUpdater = new Runnable() {
        public void run() {
            mPeriodicTaskHandler.postDelayed(this, 1000 * ABC_SYNC_REFRESH_INTERVAL_SECONDS);
            Log.d(TAG, "Starting file sync");
            syncAllData();
        }
    };

    public void stopFileSyncUpdates() {
        mPeriodicTaskHandler.removeCallbacks(FileSyncUpdater);
    }

    public void startFileSyncUpdates() {
        if(AirbitzApplication.isLoggedIn()) {
            mPeriodicTaskHandler.post(FileSyncUpdater);
        }
    }

    public void syncAllData()
    {
        if (AirbitzApplication.isLoggedIn()) {
            if(mSyncDataTask==null) {
                mSyncDataTask = new SyncDataTask();
                mSyncDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.d(TAG, "File sync initiated.");
            }
        }
    }

    private boolean hasConnectivity() {
        ConnectivityManager cm =
            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    boolean mOTPError = false;
    public boolean hasOTPError() {
        return mOTPError;
    }

    private boolean generalInfoUpdate() {
        tABC_Error error = new tABC_Error();
        if (hasConnectivity()) {
            core.ABC_GeneralInfoUpdate(error);
            return true;
        }
        return false;
    }

    private boolean accountSync(String username, String password) {
        tABC_Error error = new tABC_Error();
        if (hasConnectivity()) {
            coreDataSyncAccount(username, password, tABC_Error.getCPtr(error));
            mOTPError = error.getCode() == tABC_CC.ABC_CC_InvalidOTP;
            return true;
        }
        return false;
    }

    private SyncDataTask mSyncDataTask;
    private boolean mDataFetched = false;
    public class SyncDataTask extends AsyncTask<Void, String, Void> {
        SyncDataTask() { }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "coreDataSyncAll called");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            generalInfoUpdate();
            // Sync Account
            accountSync(AirbitzApplication.getUsername(),
                        AirbitzApplication.getPassword());

            tABC_Error error = new tABC_Error();
            List<String> uuids = loadWalletUUIDs();
            for (String uuid : uuids) {
                if (hasConnectivity()) {
                    coreDataSyncWallet(AirbitzApplication.getUsername(),
                                    AirbitzApplication.getPassword(),
                                    uuid,
                                    tABC_Error.getCPtr(error));
                }
                publishProgress(uuid);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... uuids) {
            if (!mDataFetched) {
                for (String uuid : uuids) {
                    connectWatcher(uuid);
                }
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.d(TAG, "coreDataSyncAll returned");
            mSyncDataTask = null;
            if (!mDataFetched) {
                mDataFetched = true;
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "coreDataSyncAll cancelled");
            mSyncDataTask = null;
        }
    }

    //**************** Wallet handling

    public List<String> loadWalletUUIDs()
    {
        tABC_Error Error = new tABC_Error();
        List<String> uuids = new ArrayList<String>();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        SWIGTYPE_p_long aUUIDS = core.new_longp();
        SWIGTYPE_p_p_p_char pppUUIDs = core.longp_to_pppChar(aUUIDS);

        tABC_CC result = core.ABC_GetWalletUUIDs(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                pppUUIDs, pUCount, Error);
        if (tABC_CC.ABC_CC_Ok == result)
        {
            if (core.longp_value(aUUIDS)!=0)
            {
                int count = core.intp_value(pCount);
                long base = core.longp_value(aUUIDS);
                for (int i = 0; i < count; i++)
                {
                    pLong temp = new pLong(base + i * 4);
                    long start = core.longp_value(temp);
                    if(start!=0) {
                        uuids.add(getStringAtPtr(start));
                    }
                }
            }
        }
        return uuids;
    }

    public List<Wallet> getCoreWallets(boolean withTransactions) {
        List<Wallet> mWallets = new ArrayList<Wallet>();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_WalletInfo paWalletInfo = core.longp_to_pppWalletInfo(lp);

        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        tABC_CC result = core.ABC_GetWallets(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                paWalletInfo, pUCount, pError);

        if(result == tABC_CC.ABC_CC_Ok) {
            int ptrToInfo = core.longp_value(lp);
            int count = core.intp_value(pCount);
            ppWalletInfo base = new ppWalletInfo(ptrToInfo);

                for (int i = 0; i < count; i++) {
                    pLong temp = new pLong(base.getPtr(base, i * 4));
                    long start = core.longp_value(temp);
                    WalletInfo wi = new WalletInfo(start);
                    Wallet in = new Wallet(wi.getName());
                    in.setBalanceSatoshi(wi.getBalance());
                    in.setUUID(wi.getUUID());
                    in.setAttributes(wi.getAttributes());
                    in.setCurrencyNum(wi.getCurrencyNum());
                    if (withTransactions) {
                        in.setTransactions(loadAllTransactions(in));
                    }
                    mWallets.add(in);
            }
            core.ABC_FreeWalletInfoArray(core.longp_to_ppWalletinfo(new pLong(ptrToInfo)), count);
            return mWallets;
        } else {
            Log.d(TAG, "getCoreWallets failed.");
        }
        return null;
    }

    public boolean walletsStillLoading() {
        List<Wallet> wallets = getCoreActiveWallets();
        if(!AirbitzApplication.isLoggedIn() || wallets == null) {
            return true;
        }
        for(Wallet wallet : wallets) {
            if(wallet.isLoading()) {
                return true;
            }
        }
        return false;
    }


    public List<Wallet> getCoreActiveWallets() {
        List<Wallet> wallets = getCoreWallets(false);
        if(wallets == null) {
            return null;
        }
        List<Wallet> out = new ArrayList<Wallet>();
        for(Wallet w: wallets) {
            if(!w.isArchived())
                out.add(w);
        }
        return out;
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
        private int mCurrencyNum;
        private long mArchived;
        private List<Transaction> mTransactions = null;

        public WalletInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                mName = super.getSzName();
                mUUID = super.getSzUUID();
                mBalance = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(super.getBalanceSatoshi()));
                mCurrencyNum = super.getCurrencyNum();
                mArchived = super.getArchived();
            }
        }

        public String getName() {
            return mName;
        }

        public String getUUID() {
            return mUUID;
        }

        public long getBalance() {return mBalance; }

        public long getAttributes() {return mArchived; }

        public int getCurrencyNum() {return mCurrencyNum; }

        public List<Transaction> getTransactions() {return mTransactions; }
    }

    private class pLong extends SWIGTYPE_p_long {
        public pLong(long ptr) {
            super(ptr, false);
        }
    }

    /*
     * Account Transaction handling
     */
    public static List<Transaction> getTransactions(String walletName) {
        List<Transaction> list = new ArrayList<Transaction>();
        return list;
    }

    /*
     * Other utility functions
     */

    public byte[] getTwoFactorQRCode() {
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_unsigned_char ppData = core.longp_to_unsigned_ppChar(lp);

        SWIGTYPE_p_int pWidth = core.new_intp();
        SWIGTYPE_p_unsigned_int pWCount = core.int_to_uint(pWidth);

        tABC_Error error = new tABC_Error();
        tABC_CC cc = core.ABC_QrEncode(mTwoFactorSecret, ppData, pWCount, error);
        if (cc == tABC_CC.ABC_CC_Ok) {
            int width = core.intp_value(pWidth);
            return getBytesAtPtr(core.longp_value(lp), width*width);
        } else {
            return null;
        }
    }

    public Bitmap getTwoFactorQRCodeBitmap() {
        byte[] array = getTwoFactorQRCode();
        if(array != null)
            return FromBinary(array, (int) Math.sqrt(array.length), 4);
        else
            return null;
    }


    private String mStrRequestURI =null;
    public byte[] getQRCode(String uuid, String id) {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_unsigned_char ppChar = core.longp_to_unsigned_ppChar(lp);

        SWIGTYPE_p_long lp2 = core.new_longp();
        SWIGTYPE_p_p_char ppURI = core.longp_to_ppChar(lp2);

        SWIGTYPE_p_int pWidth = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pWidth);

        result = core.ABC_GenerateRequestQRCode(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, id, ppURI, ppChar, pUCount, error);

        int width = core.intp_value(pWidth);

        mStrRequestURI = getStringAtPtr(core.longp_value(lp2));
        return getBytesAtPtr(core.longp_value(lp), width*width);
    }

    // Must call after getQRCode()
    public String getRequestURI() {
        return mStrRequestURI;
    }

    public String getRequestAddress(String uuid, String id)  {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        result = core.ABC_GetRequestAddress(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, id, ppChar, error);

        String pAddress = null;

        if(result.equals(tABC_CC.ABC_CC_Ok)) {
            pAddress = getStringAtPtr(core.longp_value(lp));
        }

        return pAddress;
    }

    public Bitmap getQRCodeBitmap(String uuid, String id) {
        byte[] array = getQRCode(uuid, id);
        return FromBinary(array, (int) Math.sqrt(array.length), 4);
    }

    private Bitmap FromBinary(byte[] bits, int width, int scale) {
        Bitmap bmpBinary = Bitmap.createBitmap(width*scale, width*scale, Bitmap.Config.ARGB_8888);

        for(int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {
                bmpBinary.setPixel(x, y, bits[y * width + x] != 0 ? Color.BLACK : Color.WHITE);
            }
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bmpBinary, 0, 0, width, width, matrix, false);
        return resizedBitmap;
    }


    public List<String> loadCategories()
    {
        List<String> categories = new ArrayList<String>();

        // get the categories from the core
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_char aszCategories = core.longp_to_pppChar(lp);

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        tABC_CC result = core.ABC_GetCategories(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), aszCategories, pUCount, Error);

        if(result!=tABC_CC.ABC_CC_Ok) {
            Log.d(TAG, "loadCategories failed:"+Error.getSzDescription());
        }

        int count = core.intp_value(pCount);

        long base = core.longp_value(lp);
        for (int i = 0; i < count; i++)
        {
            pLong temp = new pLong(base + i * 4);
            long start = core.longp_value(temp);
            categories.add(getStringAtPtr(start));
        }

        // store the final as sorted
//        self.arrayCategories = [arrayCategories sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
        return categories;
    }

    public void addCategory(String strCategory) {
        // check and see that it doesn't already exist
        List<String> categories = loadCategories();
        if (categories != null && !categories.contains(strCategory)) {
            // add the category to the core
            Log.d(TAG, "Adding category: "+strCategory);
            tABC_Error Error = new tABC_Error();
            core.ABC_AddCategory(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), strCategory, Error);
        }
    }

    public void removeCategory(String strCategory) {
        Log.d(TAG, "Remove category: "+strCategory);
        tABC_Error Error = new tABC_Error();
        tABC_CC result = core.ABC_RemoveCategory(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), strCategory, Error);
        boolean test= result==tABC_CC.ABC_CC_Ok;
    }

    public boolean isTestNet()  {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_bool istestnet = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        result = core.ABC_IsTestNet(istestnet, error);

        if(result.equals(tABC_CC.ABC_CC_Ok)) {
            return getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0;
        } else {
            Log.d(TAG, "isTestNet error:"+error.getSzDescription());
        }
        return false;
    }

    public static String getSeedData()
    {
        String strSeed = "";

        strSeed += Build.MANUFACTURER;
        strSeed += Build.DEVICE;
        strSeed += Build.SERIAL;

        long time = System.nanoTime();
        ByteBuffer bb1 = ByteBuffer.allocate(8);
        bb1.putLong(time);
        strSeed += bb1.array();

        Random r = new Random();
        ByteBuffer bb2 = ByteBuffer.allocate(4);
        bb2.putInt(r.nextInt());
        strSeed += bb2.array();

        return strSeed;
    }

    //************************* Watcher code

    private Map<String, Thread> mWatcherTasks = new ConcurrentHashMap<String, Thread>();
    public void startWatchers() {
        List<String> wallets = loadWalletUUIDs();
        for (String uuid : wallets) {
            if (uuid!=null && !mWatcherTasks.containsKey(uuid)) {
                startWatcher(uuid);
            }
        }
        if (mDataFetched) {
            connectWatchers();
        }
    }

    private void startWatcher(String uuid) {
        tABC_Error error = new tABC_Error();
        core.ABC_WatcherStart(AirbitzApplication.getUsername(),
                              AirbitzApplication.getPassword(),
                              uuid, error);
        printABCError(error);

        Thread thread = new Thread(new WatcherRunnable(uuid));
        mWatcherTasks.put(uuid, thread);
        thread.start();

        watchAddresses(uuid);
        Log.d(TAG, "Started watcher for "+uuid);
    }

    public void connectWatchers() {
        List<String> wallets = loadWalletUUIDs();
        for (String uuid : wallets) {
            connectWatcher(uuid);
        }
    }

    public void connectWatcher(String uuid) {
        tABC_Error error = new tABC_Error();
        core.ABC_WatcherConnect(uuid, error);
        printABCError(error);
        watchAddresses(uuid);
    }

    public void disconnectWatchers() {
        for (String uuid : mWatcherTasks.keySet()) {
            tABC_Error error = new tABC_Error();
            core.ABC_WatcherDisconnect(uuid, error);
        }
    }

    private void watchAddresses(String uuid) {
        Thread thread = new Thread(new WatchAddressesRunnable(uuid));
        thread.start();
    }

    private class WatchAddressesRunnable implements Runnable {
        private final String uuid;

        WatchAddressesRunnable(final String uuid) {
            this.uuid = uuid;
        }

        public void run() {
            tABC_Error error = new tABC_Error();
            core.ABC_WatchAddresses(AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(),
                    uuid, error);
            printABCError(error);
        }
    }

    /*
     * This thread will block as long as the watchers are running
     */
    private class WatcherRunnable implements Runnable {
        private final String uuid;

        WatcherRunnable(final String uuid) {
            this.uuid = uuid;
        }

        public void run() {
            tABC_Error error = new tABC_Error();

            int result = coreWatcherLoop(uuid, tABC_Error.getCPtr(error));
        }
    }

    public void stopWatchers() {
        tABC_Error error = new tABC_Error();
        for (String uuid : mWatcherTasks.keySet()) {
            core.ABC_WatcherStop(uuid, error);
        }
        // Wait for all of the threads to finish.
        for (Thread thread : mWatcherTasks.values()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (String uuid : mWatcherTasks.keySet()) {
            core.ABC_WatcherDelete(uuid, error);
        }
        mWatcherTasks.clear();
    }

    /*
     * Prioritize wallet loop attention to this address for uuid
     */
    public void prioritizeAddress(String address, String walletUUID)
    {
        tABC_Error Error = new tABC_Error();
        core.ABC_PrioritizeAddress(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), walletUUID, address, Error);
    }


    public long maxSpendable(String walletUUID, String destAddress, boolean bTransfer)
    {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_int64_t satoshi = core.new_int64_tp();
        SWIGTYPE_p_uint64_t result = new SWIGTYPE_p_uint64_t(satoshi.getCPtr(satoshi), false);
        SWIGTYPE_p_long l = core.p64_t_to_long_ptr(satoshi);
        core.ABC_MaxSpendable(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), walletUUID,
            destAddress, bTransfer, result, Error);
        long out = get64BitLongAtPtr(l.getCPtr(l));
        Log.d(TAG, "Max spendable: "+out);
        return out;
    }

    public tABC_CC ChangePassword(String password) {
        tABC_Error Error = new tABC_Error();

//        Log.d(TAG, "Changing password to "+password + " from "+AirbitzApplication.getPassword());
        tABC_CC cc = core.ABC_ChangePassword(
                        AirbitzApplication.getUsername(),
                        password,
                        password, Error);
        return cc;
    }

    public boolean recoveryAnswers(String strAnswers, String strUserName)
    {
        SWIGTYPE_p_int lp = core.new_intp();
        SWIGTYPE_p_bool pbool = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        tABC_Error error = new tABC_Error();
        tABC_CC result = core.ABC_CheckRecoveryAnswers(strUserName, strAnswers, pbool, error);
        if (tABC_CC.ABC_CC_Ok == result)
        {
            return core.intp_value(lp)==1;
        }
        else
        {
            Log.d(TAG, error.getSzDescription());
            return false;
        }
    }

    public tABC_CC ChangePasswordWithRecoveryAnswers(String username, String recoveryAnswers, String password, String pin) {
        tABC_Error Error = new tABC_Error();
        tABC_CC cc = core.ABC_ChangePasswordWithRecoveryAnswers(
                        username, recoveryAnswers, password, Error);
        return cc;
    }

    public BitcoinURIInfo CheckURIResults(String results)
    {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_BitcoinURIInfo pUri = core.longPtr_to_ppBitcoinURIInfo(lp);

        core.ABC_ParseBitcoinURI(results, pUri, Error);

        BitcoinURIInfo uri = new BitcoinURIInfo(core.longp_value(lp));

        if (uri.getPtr(uri) != 0)
        {
            String uriAddress = uri.getSzAddress();
            long amountSatoshi = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(uri.getAmountSatoshi()));

            if (uriAddress!=null) {
                Log.d(TAG, "BitcoinURI address: "+uriAddress);
                Log.d(TAG, "BitcoinURI amount: " + amountSatoshi);

                String label = uri.getSzLabel();
                String message = uri.getSzMessage();
                if (message!=null) {
                    Log.d(TAG, "BitcoinURI message: "+message);
                }
            }
            else {
                Log.d(TAG, "BitcoinURI not a bitcoin address");
            }
        }
        else {
            Log.d(TAG, "BitcoinURI not a bitcoin address!");
        }

        return uri;
    }

    public class BitcoinURIInfo extends tABC_BitcoinURIInfo {
        public String address;
        public String label;
        public String message;
        public long amountSatoshi;
        public BitcoinURIInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                address = super.getSzAddress();
                label = super.getSzLabel();
                amountSatoshi = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(super.getAmountSatoshi()));
                message = super.getSzMessage();
            }
        }
        public long getPtr(tABC_BitcoinURIInfo p) {
            return getCPtr(p);
        }
    }

    //************** PIN relogin

    public boolean PinLoginExists(String username) {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_bool exists = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        result = core.ABC_PinLoginExists(username, exists, error);

        if(result.equals(tABC_CC.ABC_CC_Ok)) {
            return getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0;
        } else {
            Log.d(TAG, "PinLoginExists error:"+error.getSzDescription());
            return false;
        }
    }

    public tABC_CC PinLogin(String username, String pin) {
        tABC_Error pError = new tABC_Error();
        tABC_CC result = core.ABC_PinLogin(username, pin, pError);
        return result;
    }

    public void PinSetup(String username, String pin) {
        if(mPinSetup == null) {
            mPinSetup = new PinSetupTask();
            mPinSetup.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username, pin);
        }
    }

    private PinSetupTask mPinSetup;
    public class PinSetupTask extends AsyncTask {
        @Override
        protected tABC_CC doInBackground(Object... params) {
            String username = (String) params[0];
            String pin = (String) params[1];
            tABC_Error pError = new tABC_Error();
            tABC_CC result = core.ABC_PinSetup(username, pin, pError);
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            mPinSetup = null;
        }

        @Override
        protected void onCancelled() {
            mPinSetup = null;
        }
    }

    public void PINLoginDelete(String username) {
        tABC_Error pError = new tABC_Error();
        tABC_CC result = core.ABC_PinLoginDelete(username, pError);
    }

    public boolean PasswordOK(String username, String password) {
        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_bool okay = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        tABC_CC result = core.ABC_PasswordOk(username, password, okay, pError);
        if(result.equals(tABC_CC.ABC_CC_Ok)) {
            return getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0;
        } else {
            Log.d(TAG, "Password OK error:"+pError.getSzDescription());
            return false;
        }
    }

    public void SetupDefaultCurrency() {
        tABC_AccountSettings settings = coreSettings();
        settings.setCurrencyNum(getLocaleDefaultCurrencyNum());
        saveAccountSettings(settings);
    }

    public int getLocaleDefaultCurrencyNum() {
        Locale locale = Locale.getDefault();

        java.util.Currency currency = java.util.Currency.getInstance(locale);

        List<String> supported = Arrays.asList(mFauxCurrencyAcronyms);
        if(supported.contains(currency.getCurrencyCode())) {
            int number = getNumericCode(currency.getCurrencyCode());
            Log.d(TAG, "number country code: "+number);
            return number;
        } else {
            return 840;
        }
    }

    private int getNumericCode(String currencyCode) {
        int index = -1;
        for(int i=0; i<mFauxCurrencyAcronyms.length; i++) {
            if(mFauxCurrencyAcronyms[i].contains(currencyCode)) {
                index = i;
                break;
            }
        }
        if(index != -1) {
            return mFauxCurrencyNumbers[index];
        }
        return 840;
    }

    public String getPrivateSeed(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_ExportWalletSeed(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), ppChar, Error);

        if (tABC_CC.ABC_CC_Ok == result) {
            return getStringAtPtr(core.longp_value(lp));
        } else {
            return null;
        }
    }

    private void printABCError(tABC_Error pError) {
        if (pError.getCode() != tABC_CC.ABC_CC_Ok) {
            Log.d(TAG,
                String.format("Code: %s, Desc: %s, Func: %s, File: %s, Line: %d\n",
                    pError.getCode().toString(),
                    pError.getSzDescription(),
                    pError.getSzSourceFunc(),
                    pError.getSzSourceFile(),
                    pError.getNSourceLine()));
        }
    }

    public void logout() {
        stopAllAsyncUpdates();
        mCoreSettings = null;

        ClearCacheKeys();
    }

    public void ClearCacheKeys() {
        tABC_Error error = new tABC_Error();
        tABC_CC result = core.ABC_ClearKeyCache(error);
        if(result != tABC_CC.ABC_CC_Ok) {
            Log.d(TAG, error.toString());
        }
    }

    public String SweepKey(String uuid, String wif) {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        int result = coreSweepKey(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, wif, SWIGTYPE_p_p_char.getCPtr(ppChar), tABC_Error.getCPtr(Error));
        if ( result != 0) {
            return "";
        }
        else {
            return getStringAtPtr(core.longp_value(lp));
        }
    }

    public String getCoreVersion() {
        return core.ABC_VERSION;
    }

    public void uploadLogs() {
        tABC_Error Error = new tABC_Error();
        core.ABC_UploadLogs(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                Error);
    }

    //*********************** Two Factor Authentication
    boolean mTwoFactorOn = false;
    String mTwoFactorSecret;

    public boolean isTwoFactorOn() {
        return mTwoFactorOn;
    }

    // Blocking
    public tABC_CC OtpAuthGet() {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long ptimeout = core.new_longp();
        SWIGTYPE_p_int lp = core.new_intp();
        SWIGTYPE_p_bool pbool = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        tABC_CC cc = core.ABC_OtpAuthGet(AirbitzApplication.getUsername(),
            AirbitzApplication.getPassword(), pbool, ptimeout, Error);

        mTwoFactorOn = core.intp_value(lp)==1;
        return cc;
    }

    public String TwoFactorSecret() {
        return mTwoFactorSecret;
    }

    // Blocking
    public tABC_CC GetTwoFactorSecret() {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        tABC_CC cc = core.ABC_OtpKeyGet(AirbitzApplication.getUsername(), ppChar, error);
        mTwoFactorSecret = getStringAtPtr(core.longp_value(lp));
        return cc;
    }

    public boolean isTwoFactorResetPending(tABC_Error error) {
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        tABC_CC cc = core.ABC_OtpResetGet(ppChar, error);
        if (cc == tABC_CC.ABC_CC_Ok) {
            String userNames = getStringAtPtr(core.longp_value(lp));
            return userNames.contains(AirbitzApplication.getUsername());
        }
        return false;
    }

    // Blocking
    public tABC_CC enableTwoFactor(boolean on) {
        tABC_Error error = new tABC_Error();
        tABC_CC cc;
        if(on) {
            cc = core.ABC_OtpAuthSet(AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(), OTP_RESET_DELAY_SECS, error);
            if (cc == tABC_CC.ABC_CC_Ok) {
                mTwoFactorOn = true;
            }
        }
        else {
            cc = core.ABC_OtpAuthRemove(AirbitzApplication.getUsername(),
                    AirbitzApplication.getPassword(), error);
            if (cc == tABC_CC.ABC_CC_Ok) {
                mTwoFactorOn = false;
                mTwoFactorSecret = null;
                core.ABC_OtpKeyRemove(AirbitzApplication.getUsername(), error);
            }
        }
        return cc;
    }

    // Blocking
    public tABC_CC cancelTwoFactorRequest() {
        tABC_Error error = new tABC_Error();
        return core.ABC_OtpResetRemove(AirbitzApplication.getUsername(),
                AirbitzApplication.getPassword(), error);
    }
}
