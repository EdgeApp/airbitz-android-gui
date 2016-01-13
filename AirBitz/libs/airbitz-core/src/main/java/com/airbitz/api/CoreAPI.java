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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.airbitz.models.Contact;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tom on 6/20/14.
 * This class is a bridge to the ndk core code, acting like a viewmodel
 */
public class CoreAPI {
    private static String TAG = CoreAPI.class.getSimpleName();

    public static String PREFS = "com.airbitz.prefs";
    public static final String DAILY_LIMIT_PREF = "com.airbitz.spendinglimits.dailylimit";
    public static final String DAILY_LIMIT_SETTING_PREF = "com.airbitz.spendinglimits.dailylimitsetting";

    private final String CERT_FILENAME = "ca-certificates.crt";
    private static int ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS = 60;
    private static int ABC_SYNC_REFRESH_INTERVAL_SECONDS = 30;
    private static int CONFIRMED_CONFIRMATION_COUNT = 3;
    public static int ABC_DENOMINATION_BTC = 0;
    public static int ABC_DENOMINATION_MBTC = 1;
    public static int ABC_DENOMINATION_UBTC = 2;
    public static double SATOSHI_PER_BTC = 1E8;
    public static double SATOSHI_PER_mBTC = 1E5;
    public static double SATOSHI_PER_uBTC = 1E2;
    public static int OTP_RESET_DELAY_SECS = 60 * 60 * 24 * 7;

    public static final String WALLET_LOADING_START_ACTION = "com.airbitz.notifications.wallets_data_loading_start";
    public static final String WALLET_LOADING_STATUS_ACTION = "com.airbitz.notifications.wallets_data_loaded";
    public static final String WALLETS_ALL_LOADED_ACTION = "com.airbitz.notifications.all_wallets_data_loaded";
    public static final String WALLETS_LOADING_BITCOIN_ACTION = "com.airbitz.notifications.bitcoin_data_loading_start";
    public static final String WALLETS_LOADED_BITCOIN_ACTION = "com.airbitz.notifications.bitcoin_data_loaded";
    public static final String WALLETS_RELOADED_ACTION = "com.airbitz.notifications.wallet_data_reloaded";

    public static final String WALLET_UUID = "com.airbitz.wallet_uuid";
    public static final String WALLET_TXID = "com.airbitz.txid";
    public static final String WALLETS_LOADED_TOTAL = "com.airbitz.wallets_loaded_total";
    public static final String WALLETS_TOTAL = "com.airbitz.wallets_total";
    public static final String AMOUNT_SWEPT = "com.airbitz.amount_swept";

    public static final String EXCHANGE_RATE_UPDATED_ACTION = "com.airbitz.notifications.exchange_rate_update";
    public static final String DATASYNC_UPDATE_ACTION = "com.airbitz.notifications.data_sync_update";
    public static final String BLOCKHEIGHT_CHANGE_ACTION = "com.airbitz.notifications.block_height_change";
    public static final String INCOMING_BITCOIN_ACTION = "com.airbitz.notifications.incoming_bitcoin";
    public static final String WALLET_SWEEP_ACTION = "com.airbitz.notifications.wallet_sweep_action";
    public static final String REMOTE_PASSWORD_CHANGE_ACTION = "com.airbitz.notifications.remote_password_change";

    public static final String OTP_ERROR_ACTION = "com.airbitz.notifications.otp_error_action";
    public static final String OTP_RESET_ACTION = "com.airbitz.notifications.otp_reset_action";
    public static final String OTP_SECRET = "com.airbitz.otp_secret";

    private static final int TX_LOADED_DELAY = 1000 * 20;

    static {
        System.loadLibrary("abc");
        System.loadLibrary("airbitz");
    }

    private static CoreAPI mInstance = null;
    private static boolean initialized = false;
    private String mUsername;
    private String mPassword;

    private CoreAPI() { }

    public static CoreAPI getApi(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new CoreAPI();
            mInstance.debugLevel(1, "New CoreAPI");
        }
        return mInstance;
    }

    public static CoreAPI getApi() {
        if (mInstance == null) {
            mInstance = new CoreAPI();
            mInstance.debugLevel(1, "New CoreAPI");
        }
        return mInstance;
    }

    private static Context mContext;

    public native String getStringAtPtr(long pointer);
    public native byte[] getBytesAtPtr(long pointer, int length);
    public native int[] getCoreCurrencyNumbers();
    public native String getCurrencyCode(int currencyNumber);
    public native String getCurrencyDescription(int currencyNumber);
    public native long get64BitLongAtPtr(long pointer);
    public native void set64BitLongAtPtr(long pointer, long value);
    public native int FormatAmount(long satoshi, long ppchar, long decimalplaces, boolean addSign, long perror);
    public native int satoshiToCurrency(String jarg1, String jarg2, long satoshi, long currencyp, int currencyNum, long error);
    public native int coreDataSyncAccount(String jusername, String jpassword, long jerrorp);
    public native int coreDataSyncWallet(String jusername, String jpassword, String juuid, long jerrorp);
    public native int coreSweepKey(String jusername, String jpassword, String juuid, String wif, long ppchar, long jerrorp);
    public native int coreWatcherLoop(String juuid, long jerrorp);
    public native boolean RegisterAsyncCallback ();
    public native long ParseAmount(String jarg1, int decimalplaces);

    public void Initialize(Context context, String airbitzApiKey, String hiddenbitzKey,
                           String seed, long seedLength){
        if(!initialized) {
            tABC_Error error = new tABC_Error();
            if(RegisterAsyncCallback()) {
                mInstance.debugLevel(1, "Registered for core callbacks");
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
            core.ABC_Initialize(filesDir.getPath(), filesDir.getPath() + "/" + CERT_FILENAME,
                    airbitzApiKey, hiddenbitzKey, seed, seedLength, error);
            initialized = true;

            // Fetch General Info
            new Thread(new Runnable() {
                public void run() {
                    generalInfoUpdate();
                }
            }).start();

            initCurrencies();
        }
    }

    static public void debugLevel(int level, String debugString) {
        int DEBUG_LEVEL = 1;

        if (level <= DEBUG_LEVEL) {
            core.ABC_Log(debugString);
        }
    }

    public void setCredentials(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    protected String getUsername() {
        return mUsername;
    }

    protected String getPassword() {
        return mPassword;
    }

    protected Context getContext() {
        return mContext;
    }

    public boolean isLoggedIn() {
        return mUsername != null;
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
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        tABC_AsyncBitCoinInfo info = new tABC_AsyncBitCoinInfo(asyncBitCoinInfo_ptr, false);
        tABC_AsyncEventType type = info.getEventType();

        mInstance.debugLevel(1, "asyncBitCoinInfo callback type = "+type.toString());
        if (type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingBitCoin) {
            mIncomingIntent = new Intent(INCOMING_BITCOIN_ACTION);
            mIncomingIntent.putExtra(WALLET_UUID, info.getSzWalletUUID());
            mIncomingIntent.putExtra(WALLET_TXID, info.getSzTxID());

            // Notify app of new tx
            mPeriodicTaskHandler.removeCallbacks(IncomingBitcoinUpdater);
            mPeriodicTaskHandler.postDelayed(IncomingBitcoinUpdater, 300);

            // Notify progress bar more txs might be coming
            mPeriodicTaskHandler.removeCallbacks(mNotifyBitcoinLoaded);
            mPeriodicTaskHandler.postDelayed(mNotifyBitcoinLoaded, TX_LOADED_DELAY);
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_BlockHeightChange) {
            mPeriodicTaskHandler.post(BlockHeightUpdater);
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_DataSyncUpdate) {
            mPeriodicTaskHandler.removeCallbacks(DataSyncUpdater);
            mPeriodicTaskHandler.postDelayed(DataSyncUpdater, 1000);

            // Notify progress bar more txs might be coming
            mPeriodicTaskHandler.removeCallbacks(mNotifyBitcoinLoaded);
            mPeriodicTaskHandler.postDelayed(mNotifyBitcoinLoaded, TX_LOADED_DELAY);
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_RemotePasswordChange) {
            manager.sendBroadcast(new Intent(REMOTE_PASSWORD_CHANGE_ACTION));
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingSweep) {
            String txid = info.getSzTxID();
            long amount = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(info.getSweepSatoshi()));
            Intent intent = new Intent(WALLET_SWEEP_ACTION);
            intent.putExtra(WALLET_TXID, txid);
            intent.putExtra(AMOUNT_SWEPT, amount);
            manager.sendBroadcast(intent);
        }
    }

    private Intent mIncomingIntent;

    final Runnable mNotifyBitcoinLoaded = new Runnable() {
        public void run() {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(WALLETS_LOADED_BITCOIN_ACTION));
        }
    };

    final Runnable IncomingBitcoinUpdater = new Runnable() {
        public void run() {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIncomingIntent);
        }
    };

    final Runnable BlockHeightUpdater = new Runnable() {
        public void run() {
            mCoreSettings = null;
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(BLOCKHEIGHT_CHANGE_ACTION));
        }
    };

    final Runnable DataSyncUpdater = new Runnable() {
        public void run() {
            mCoreSettings = null;
            startWatchers();
            reloadWallets();
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                new Intent(DATASYNC_UPDATE_ACTION));
        }
    };

    // This is a blocking call. You must wrap this in an AsyncTask or similar.
    public boolean createWallet(String username, String password, String walletName, int currencyNum) {
        if (!hasConnectivity()) {
            return false;
        }
        mInstance.debugLevel(1, "createWallet(" + walletName + "," + currencyNum + ")");
        tABC_Error pError = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_CreateWallet(username, password,
                walletName, currencyNum, ppChar, pError);
        if (result == tABC_CC.ABC_CC_Ok) {
            startWatchers();
            return true;
        } else {
            mInstance.debugLevel(1, "Create wallet failed - "+pError.getSzDescription()+", at "+pError.getSzSourceFunc());
            return result == tABC_CC.ABC_CC_Ok;
        }
    }

    public boolean removeWallet(String uuid) {
        tABC_Error error = new tABC_Error();
        tABC_CC result = core.ABC_WalletRemove(getUsername(), uuid, error);
        if (result == tABC_CC.ABC_CC_Ok) {
            stopWatcher(uuid);
            reloadWallets();
            return true;
        } else {
            return false;
        }
    }

    public boolean renameWallet(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        tABC_CC result = core.ABC_RenameWallet(mUsername, mPassword,
                wallet.getUUID(), wallet.getName(), Error);
        return result == tABC_CC.ABC_CC_Ok;
    }

    public void createAccount(String username, String password) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_CreateAccount(username, password, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    public Wallet getWalletFromUUID(String uuid) {
        if (uuid == null) {
            return null;
        }
        List<Wallet> wallets = getCoreWallets(false);
        if (wallets == null) {
            return null;
        }
        for (Wallet w : wallets) {
            if (uuid.equals(w.getUUID())) {
                return w;
            }
        }
        return null;
    }

    public Wallet getWalletFromCore(String uuid) {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        Wallet wallet = new Wallet("Loading...");
        wallet.setUUID(uuid);
        wallet.setCurrencyNum(-1); // Defaults to loading
        wallet.setTransactions(new ArrayList<Transaction>());

        if (null != mWatcherTasks.get(uuid)) {
            // Load Wallet name
            SWIGTYPE_p_long pName = core.new_longp();
            SWIGTYPE_p_p_char ppName = core.longp_to_ppChar(pName);
            result = core.ABC_WalletName(mUsername, uuid, ppName, error);
            if (result == tABC_CC.ABC_CC_Ok) {
                wallet.setName(getStringAtPtr(core.longp_value(pName)));
            }

            // Load currency
            SWIGTYPE_p_int pCurrency = core.new_intp();
            SWIGTYPE_p_unsigned_int upCurrency = core.int_to_uint(pCurrency);

            result = core.ABC_WalletCurrency(mUsername, uuid, pCurrency, error);
            if (result == tABC_CC.ABC_CC_Ok) {
                wallet.setCurrencyNum(core.intp_value(pCurrency));
            } else {
                wallet.setCurrencyNum(-1);
                wallet.setName("Loading...");
            }

            // Load balance
            SWIGTYPE_p_int64_t l = core.new_int64_tp();
            result = core.ABC_WalletBalance(mUsername, uuid, l, error);
            if (result == tABC_CC.ABC_CC_Ok) {
                wallet.setBalanceSatoshi(
                    get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(l)));
            } else {
                wallet.setBalanceSatoshi(0);
            }
        }

        // If there is a UUID there are wallet attributes
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_bool archived = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);
        result = core.ABC_WalletArchived(mUsername, uuid, archived, error);
        if (result == tABC_CC.ABC_CC_Ok) {
            wallet.setAttributes(
                getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0 ? 0x1 : 0);
        }

        return wallet;
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
        tABC_CC result = core.ABC_SetWalletOrder(mUsername, mPassword,
            uuids.toString().trim(), Error);
        if (result != tABC_CC.ABC_CC_Ok) {
            mInstance.debugLevel(1, "Error: CoreBridge.setWalletOrder" + Error.getSzDescription());
        }
    }

    public boolean setWalletAttributes(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        if (isLoggedIn()) {
            tABC_CC result = core.ABC_SetWalletArchived(mUsername,
                    mPassword, wallet.getUUID(), wallet.getAttributes(), Error);
            if (result == tABC_CC.ABC_CC_Ok) {
                return true;
            }
            else {
                mInstance.debugLevel(1, "Error: CoreBridge.setWalletAttributes: "+ Error.getSzDescription());
                return false;
            }
        }
        return false;
    }

    ReloadWalletTask mReloadWalletTask = null;
    public void reloadWallets() {
        if (mReloadWalletTask == null && isLoggedIn()) {
            mReloadWalletTask = new ReloadWalletTask();
            mReloadWalletTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Reload the wallet list on async thread and alert any listener
     */
    public class ReloadWalletTask extends AsyncTask<Void, Void, List<Wallet>> {

        @Override
        protected List<Wallet> doInBackground(Void... params) {
            return getWallets();
        }

        @Override
        protected void onPostExecute(List<Wallet> walletList) {
            mCoreWallets = walletList;
            LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(new Intent(WALLETS_RELOADED_ACTION));
            mReloadWalletTask = null;
        }
    }

    //************ Account Recovery

    // Blocking call, wrap in AsyncTask
    public void SignIn(String username, String password) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_SignIn(username, password, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    //************ Currency handling
    private int[] mCurrencyNumbers;
    private Map<Integer, String> mCurrencySymbolCache = new HashMap<>();
    private Map<Integer, String> mCurrencyCodeCache = new HashMap<>();
    private Map<Integer, String> mCurrencyDescriptionCache = new HashMap<>();

    public String currencyCodeLookup(int currencyNum)
    {
        String cached = mCurrencyCodeCache.get(currencyNum);
        if (cached != null) {
            return cached;
        }

        String code = getCurrencyCode(currencyNum);
        if(code != null) {
            mCurrencyCodeCache.put(currencyNum, code);
            return code;
        }

        return "";
    }

    public String currencyDescriptionLookup(int currencyNum)
    {
        String cached = mCurrencyDescriptionCache.get(currencyNum);
        if (cached != null) {
            return cached;
        }

        String description = getCurrencyDescription(currencyNum);
        if(description != null) {
            mCurrencyDescriptionCache.put(currencyNum, description);
            return description;
        }

        return "";
    }

    public String currencySymbolLookup(int currencyNum)
    {
        String cached = mCurrencySymbolCache.get(currencyNum);
        if (cached != null) {
            return cached;
        }

        try {
            String code = currencyCodeLookup(currencyNum);
            String symbol  = Currency.getInstance(code).getSymbol();
            if(symbol != null) {
                mCurrencySymbolCache.put(currencyNum, symbol);
                return symbol;
            }
            else {
                mInstance.debugLevel(1, "Bad currency code: " + code);
                return "";
            }
        }
        catch (Exception e) {
            return "";
        }
    }

    public String getUserCurrencyAcronym() {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return currencyCodeLookup(840);
        } else {
            return currencyCodeLookup(settings.settings().getCurrencyNum());
        }
    }

    public String getUserCurrencySymbol() {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return currencySymbolLookup(840);
        } else {
            return currencySymbolLookup(settings.settings().getCurrencyNum());
        }
    }

    public String getCurrencyDenomination(int currencyNum) {
        return currencySymbolLookup(currencyNum);
    }

    public int[] getCurrencyNumberArray() {
        ArrayList<Integer> intKeys = new ArrayList<Integer>(mCurrencyCodeCache.keySet());
        int[] ints = new int[intKeys.size()];
        int i = 0;
        for (Integer n : intKeys) {
            ints[i++] = n;
        }
        return ints;
    }

    public String getCurrencyAcronym(int currencyNum) {
        return currencyCodeLookup(currencyNum);
    }

    public List<String> getCurrencyCodeAndDescriptionArray() {
        initCurrencies();
        List<String> strings = new ArrayList<>();
        // Populate all codes and lists and the return list
        for(Integer number : mCurrencyNumbers) {
            String code = currencyCodeLookup(number);
            String description = currencyDescriptionLookup(number);
            String symbol = currencySymbolLookup(number);
            strings.add(code + " - " + description);
        }
        return strings;
    }

    public List<String> getCurrencyCodeArray() {
        initCurrencies();
        List<String> strings = new ArrayList<>();
        // Populate all codes and lists and the return list
        for(Integer number : mCurrencyNumbers) {
            String code = currencyCodeLookup(number);
            strings.add(code);
        }
        return strings;
    }

    public void initCurrencies() {
        if(mCurrencyNumbers == null) {
            mCurrencyNumbers = getCoreCurrencyNumbers();
            mCurrencySymbolCache = new HashMap<>();
            mCurrencyCodeCache = new HashMap<>();
            mCurrencyDescriptionCache = new HashMap<>();
            for(Integer number : mCurrencyNumbers) {
                currencyCodeLookup(number);
                currencyDescriptionLookup(number);
                currencySymbolLookup(number);
            }
        }
    }

    //************ Settings handling
    private String[] mBTCDenominations = {"BTC", "mBTC", "bits"};
    private String[] mBTCSymbols = {"Ƀ ", "mɃ ", "ƀ "};

    public String GetUserPIN() {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            return settings.settings().getSzPIN();
        }
        return "";
    }

    public void SetPin(String pin) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        tABC_CC cc = core.ABC_SetPIN(mUsername, mPassword, pin, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }


    //****** Spend Limiting
    public boolean GetDailySpendLimitSetting() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.contains(DAILY_LIMIT_SETTING_PREF + mUsername)) {
            return prefs.getBoolean(DAILY_LIMIT_SETTING_PREF + mUsername, true);
        } else {
            AccountSettings settings = coreSettings();
            if (settings != null) {
                return coreSettings().settings().getBDailySpendLimit();
            }
            return false;
        }
    }

    public void SetDailySpendLimitSetting(boolean set) {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        settings.settings().setBDailySpendLimit(set);
        try {
            settings.save();

            SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(DAILY_LIMIT_SETTING_PREF + mUsername, set);
            editor.apply();
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "SetDailySpendLimitSetting error:" + e.errorMap());
        }
    }

    public long GetDailySpendLimit() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(DAILY_LIMIT_PREF + mUsername)) {
            return prefs.getLong(DAILY_LIMIT_PREF + mUsername, 0);
        }
        else {
            AccountSettings settings = coreSettings();
            if (settings != null) {
                SWIGTYPE_p_int64_t satoshi = settings.settings().getDailySpendLimitSatoshis();
                return get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(satoshi));
            }
            return 0;
        }
    }

    public void SetDailySpendSatoshis(long spendLimit) {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        SWIGTYPE_p_int64_t limit = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(limit), spendLimit);
        settings.settings().setDailySpendLimitSatoshis(limit);
        try {
            settings.save();

            SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(DAILY_LIMIT_PREF + mUsername, spendLimit);
            editor.apply();
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "SetDailySpendSatoshis error:" + e.errorMap());
        }
    }

    public boolean GetPINSpendLimitSetting() {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            return settings.settings().getBSpendRequirePin();
        }
        return true;
    }

    public void SetPINSpendLimitSetting(boolean set) {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        settings.settings().setBSpendRequirePin(set);
        try {
            settings.save();
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "SetPINSpendLimitSetting error:" + e.errorMap());
        }
    }

    public long GetPINSpendLimit() {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            SWIGTYPE_p_int64_t satoshi = settings.settings().getSpendRequirePinSatoshis();
            return get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(satoshi));
        }
        return 0;
    }

    public void SetPINSpendSatoshis(long spendLimit) {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        SWIGTYPE_p_int64_t limit = core.new_int64_tp();
        set64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(limit), spendLimit);
        settings.settings().setSpendRequirePinSatoshis(limit);
        try {
            settings.save();
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "SetPINSpendSatoshis error:" + e.errorMap());
        }
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
        AccountSettings settings = coreSettings();
        if(settings == null) {
            return "";
        }
        tABC_BitcoinDenomination bitcoinDenomination =
            settings.settings().getBitcoinDenomination();
        if (bitcoinDenomination == null) {
            mInstance.debugLevel(1, "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCDenominations[bitcoinDenomination.getDenominationType()];
    }

    public String getUserBTCSymbol() {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return "";
        }
        tABC_BitcoinDenomination bitcoinDenomination =
            settings.settings().getBitcoinDenomination();
        if (bitcoinDenomination == null) {
            mInstance.debugLevel(1, "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCSymbols[bitcoinDenomination.getDenominationType()];
    }


    private AccountSettings mCoreSettings;
    public AccountSettings coreSettings() {
        if (mCoreSettings != null) {
            return mCoreSettings;
        }
        try {
            mCoreSettings = new AccountSettings(this).load();
            return mCoreSettings;
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "coreSettings error:" + e.errorMap());
            return null;
        }
    }

    public AccountSettings newCoreSettings() {
        mCoreSettings = null;
        return coreSettings();
    }

    public List<String> getExchangeRateSources() {
        List<String> sources = new ArrayList<>();
        sources.add("Bitstamp");
        sources.add("BraveNewCoin");
        sources.add("Coinbase");
        sources.add("CleverCoin");
        return sources;
    }


    public boolean incrementPinCount() {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return false;
        }
        int pinLoginCount =
            settings.settings().getPinLoginCount();
        pinLoginCount++;
        settings.settings().setPinLoginCount(pinLoginCount);
        try {
            settings.save();
            if (pinLoginCount == 3
                    || pinLoginCount == 10
                    || pinLoginCount == 40
                    || pinLoginCount == 100) {
                return true;
            }
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "incrementPinCount error:" + e.errorMap());
            return false;
        }
        return false;
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
        try {
            String qstring = GetRecoveryQuestionsForUser(mUsername);
            if (qstring != null) {
                String[] qs = qstring.split("\n");
                if (qs.length > 1) {
                    // Recovery questions set
                    return true;
                }
            }
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "hasRecoveryQuestionsSet error:" + e.errorMap());
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
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        int reminderCount = settings.settings().getRecoveryReminderCount();
        reminderCount += val;
        settings.settings().setRecoveryReminderCount(reminderCount);
        try {
            settings.save();
        } catch (AirbitzException e) {
            mInstance.debugLevel(1, "incRecoveryReminder error:" + e.errorMap());
        }
    }

    public boolean needsRecoveryReminder(Wallet wallet) {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            int reminderCount = settings.settings().getRecoveryReminderCount();
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
        }
        return true;
    }

    public String GetRecoveryQuestionsForUser(String username) throws AirbitzException {
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_GetRecoveryQuestions(username, ppChar, error);
        String questionString = getStringAtPtr(core.longp_value(lp));
        if (result == tABC_CC.ABC_CC_Ok) {
            return questionString;
        } else {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    public void SaveRecoveryAnswers(String mQuestions, String mAnswers, String password) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_SetAccountRecoveryQuestions(mUsername,
                password, mQuestions, mAnswers, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
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

        tABC_CC result = core.ABC_CsvExport(mUsername, mPassword,
                uuid, startTime, endTime, ppChar, pError);

        if (result == tABC_CC.ABC_CC_Ok) {
            return getStringAtPtr(core.longp_value(lp)); // will be null for NoRecoveryQuestions
        }
        else if(result == tABC_CC.ABC_CC_NoTransaction) {
            return "";
        }
        else {
            mInstance.debugLevel(1, pError.getSzDescription() +";"+ pError.getSzSourceFile() +";"+ pError.getSzSourceFunc() +";"+ pError.getNSourceLine());
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
            mInstance.debugLevel(1, "Could not find wallet for "+ walletUUID);
            return null;
        }
        tABC_CC result = core.ABC_GetTransaction(mUsername, mPassword,
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
            mInstance.debugLevel(1, "Error: CoreBridge.getTransaction: "+ Error.getSzDescription());
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

        tABC_CC result = core.ABC_GetTransactions(mUsername, mPassword,
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
            mInstance.debugLevel(1, "Error: CoreBridge.loadAllTransactions: "+ Error.getSzDescription());
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
                mValue = get64BitLongAtPtr(SWIGTYPE_p_int64_t.getCPtr(getValue()));
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
                mAmountSatoshi = get64BitLongAtPtr(
                    SWIGTYPE_p_int64_t.getCPtr(getAmountSatoshi()));
                mAmountFeesAirbitzSatoshi = get64BitLongAtPtr(
                    SWIGTYPE_p_int64_t.getCPtr(getAmountFeesAirbitzSatoshi()));
                mAmountFeesMinersSatoshi = get64BitLongAtPtr(
                    SWIGTYPE_p_int64_t.getCPtr(getAmountFeesMinersSatoshi()));
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
            mInstance.debugLevel(1, "Error in GetPasswordSecondsToCrack:  " + Error.getSzDescription());
            return 0;
        }
        return core.doublep_value(seconds);
    }

    public List<PasswordRule> GetPasswordRules(String password)
    {
        List<PasswordRule> list = new ArrayList<PasswordRule>();
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
            mInstance.debugLevel(1, "Error in PasswordRule:  " + Error.getSzDescription());
            return null;
        }

        int count = core.intp_value(pCount);

        long base = core.longp_value(lp);
        for (int i = 0; i < count; i++)
        {
            pLong temp = new pLong(base + i * 4);
            long start = core.longp_value(temp);
            PasswordRule pRule = new PasswordRule(start, false);
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

        int confirmations = calcTxConfirmations(wallet, transaction, transaction.getID());
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
        if (core.ABC_TxHeight(wallet.getUUID(), txId, th, Error) != tABC_CC.ABC_CC_Ok) {
            t.setSyncing(true);
            return 0;
        }
        if (core.ABC_BlockHeight(wallet.getUUID(), bh, Error) != tABC_CC.ABC_CC_Ok) {
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

        tABC_CC result = core.ABC_SearchTransactions(mUsername, mPassword,
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
            Log.i(TAG, "Error: CoreBridge.searchTransactionsIn: " + Error.getSzDescription());
        }
        return listTransactions;
    }

    public void storeTransaction(Transaction transaction) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_TxDetails pDetails = core.longp_to_ppTxDetails(lp);

        core.ABC_GetTransactionDetails(mUsername, mPassword,
                transaction.getWalletUUID(), transaction.getID(), pDetails, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }

        tABC_TxDetails details = new TxDetails(core.longp_value(lp));
        details.setSzName(transaction.getName());
        details.setSzCategory(transaction.getCategory());
        details.setSzNotes(transaction.getNotes());
        details.setAmountCurrency(transaction.getAmountFiat());
        details.setBizId(transaction.getmBizId());

        error = new tABC_Error();
        core.ABC_SetTransactionDetails(mUsername, mPassword,
                transaction.getWalletUUID(), transaction.getID(), details, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
        mMainHandler.sendEmptyMessage(RELOAD);
    }

    //************************* Currency formatting

    public String formatDefaultCurrency(double in) {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            String pre = mBTCSymbols[settings.settings().getBitcoinDenomination().getDenominationType()];
            String out = String.format("%.3f", in);
            return pre+out;
        }
        return "";
    }


    public String formatCurrency(double in, int currencyNum, boolean withSymbol) {
        return formatCurrency(in, currencyNum, withSymbol, 2);
    }

    public String formatCurrency(double in, int currencyNum, boolean withSymbol, int decimalPlaces) {
        String pre;
        String denom = currencySymbolLookup(currencyNum) + " ";
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
        for(int i=0; i< mCurrencyNumbers.length; i++) {
            if(currencyNum == mCurrencyNumbers[i])
                return i;
        }
        mInstance.debugLevel(1, "CurrencyIndex not found, using default");
        return 10; // default US
    }

    public int userDecimalPlaces() {
        int decimalPlaces = 8; // for ABC_DENOMINATION_BTC
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return 2;
        }
        tABC_BitcoinDenomination bitcoinDenomination =
            settings.settings().getBitcoinDenomination();
        if (bitcoinDenomination != null) {
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
                pretext += getUserBTCSymbol();
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

    private int mCurrencyIndex = 0;
    public int SettingsCurrencyIndex() {
        int index = -1;
        int currencyNum;
        AccountSettings settings = coreSettings();
        if(settings == null && mCurrencyIndex != 0) {
            currencyNum = mCurrencyIndex;
        }
        else {
            currencyNum = settings.settings().getCurrencyNum();
            mCurrencyIndex = currencyNum;
        }
        int[] currencyNumbers = getCurrencyNumberArray();

        for(int i=0; i<currencyNumbers.length; i++) {
            if(currencyNumbers[i] == currencyNum)
                index = i;
        }
        if((index==-1) || (index >= currencyNumbers.length)) { // default usd
            mInstance.debugLevel(1, "currency index out of bounds "+index);
            index = currencyNumbers.length-1;
        }
        return index;
    }

    public int CurrencyIndex(int currencyNum) {
        int index = -1;
        int[] currencyNumbers = getCurrencyNumberArray();

        for(int i=0; i<currencyNumbers.length; i++) {
            if(currencyNumbers[i] == currencyNum)
                index = i;
        }
        if((index==-1) || (index >= currencyNumbers.length)) { // default usd
            mInstance.debugLevel(1, "currency index out of bounds "+index);
            index = currencyNumbers.length-1;
        }
        return index;
    }

    public long denominationToSatoshi(String amount) {
        int decimalPlaces = userDecimalPlaces();

        try {
            // Parse using the current locale
            Number cleanAmount =
                new DecimalFormat().parse(amount, new ParsePosition(0));
            if (null == cleanAmount) {
                return 0L;
            }
            // Convert to BD so we don't lose precision
            BigDecimal bd = BigDecimal.valueOf(cleanAmount.doubleValue());
            DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.getDefault()));
            String bdstr = df.format(bd.doubleValue());
            long parseamt = ParseAmount(bdstr, decimalPlaces);
            long max = Math.max(parseamt, 0);
            return max;
        } catch (Exception e) {
            // Shhhhh
        }
        return 0L;
    }

    public String BTCtoFiatConversion(int currencyNum) {
        AccountSettings settings = coreSettings();
        if(settings != null) {
            tABC_BitcoinDenomination denomination =
                settings.settings().getBitcoinDenomination();
            long satoshi = 100;
            int denomIndex = 0;
            int fiatDecimals = 2;
            String amtBTCDenom = "1 ";
            if(denomination != null) {
                if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_BTC) {
                    satoshi = (long) SATOSHI_PER_BTC;
                    denomIndex = 0;
                    fiatDecimals = 2;
                    amtBTCDenom = "1 ";
                } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_MBTC) {
                    satoshi = (long) SATOSHI_PER_mBTC;
                    denomIndex = 1;
                    fiatDecimals = 3;
                    amtBTCDenom = "1 ";
                } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_UBTC) {
                    satoshi = (long) SATOSHI_PER_uBTC;
                    denomIndex = 2;
                    fiatDecimals = 3;
                    amtBTCDenom = "1000 ";
                }
            }
//        String currency = FormatCurrency(satoshi, currencyNum, false, false);
            double o = SatoshiToCurrency(satoshi, currencyNum);
            if (denomIndex == 2)
            {
                // unit of 'bits' is so small it's useless to show it's conversion rate
                // Instead show "1000 bits = $0.253 USD"
                o = o * 1000;
            }
            String currency = formatCurrency(o, currencyNum, true, fiatDecimals);

            String currencyLabel = currencyCodeLookup(currencyNum);
            return amtBTCDenom + mBTCDenominations[denomIndex] + " = " + currency + " " + currencyLabel;
        }
        return "";

    }

    public String FormatDefaultCurrency(long satoshi, boolean btc, boolean withSymbol) {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            int currencyNumber = settings.settings().getCurrencyNum();
            return FormatCurrency(satoshi, currencyNumber, btc, withSymbol);
        }
        return "";
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
        AccountSettings settings = coreSettings();
        if (settings != null) {
            int num = settings.settings().getCurrencyNum();
            return SatoshiToCurrency(satoshi, num);
        }
        return 0;
    }

    public double SatoshiToCurrency(long satoshi, int currencyNum) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_double currency = core.new_doublep();

        long out = satoshiToCurrency(mUsername, mPassword,
                satoshi, SWIGTYPE_p_double.getCPtr(currency), currencyNum, tABC_Error.getCPtr(error));

        return core.doublep_value(currency);
    }

    public long DefaultCurrencyToSatoshi(double currency) {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            return CurrencyToSatoshi(currency, settings.settings().getCurrencyNum());
        }
        return 0;
    }

    public long parseFiatToSatoshi(String amount, int currencyNum) {
        try {
             Number cleanAmount =
                new DecimalFormat().parse(amount, new ParsePosition(0));
             if (null == cleanAmount) {
                 return 0;
             }
            double currency = cleanAmount.doubleValue();
            long satoshi = CurrencyToSatoshi(currency, currencyNum);

            // Round up to nearest 1 bits, .001 mBTC, .00001 BTC
            satoshi = 100 * (satoshi / 100);
            return satoshi;

        } catch (NumberFormatException e) {
            /* Sshhhhh */
        }
        return 0;
    }

    public long CurrencyToSatoshi(double currency, int currencyNum) {
        tABC_Error error = new tABC_Error();
        tABC_CC result;
        SWIGTYPE_p_int64_t satoshi = core.new_int64_tp();
        SWIGTYPE_p_long l = core.p64_t_to_long_ptr(satoshi);

        result = core.ABC_CurrencyToSatoshi(mUsername, mPassword,
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

        AccountSettings settings = coreSettings();
        if (settings != null) {
            tABC_BitcoinDenomination denomination =
                settings.settings().getBitcoinDenomination();
            if (denomination != null) {
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
        return false;
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
        double value = SatoshiToCurrency(satoshi, wallet.getCurrencyNum());
        return createReceiveRequestFor(wallet, name, notes, "", value, satoshi);
    }

    public String createReceiveRequestFor(Wallet wallet, String name, String notes, String category, double value, long satoshi) {
        return createReceiveRequestFor(wallet, name, notes, category, value, satoshi, 0);
    }

    public String createReceiveRequestFor(Wallet wallet, String name, String notes, String category, double value, long satoshi, long bizId) {
        //first need to create a transaction details struct

        //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_Error error = new tABC_Error();

        set64BitLongAtPtr(details.getCPtr(details)+0, satoshi);

        //the true fee values will be set by the core
        details.setAmountFeesAirbitzSatoshi(core.new_int64_tp());
        details.setAmountFeesMinersSatoshi(core.new_int64_tp());

        details.setAmountCurrency(value);
        details.setSzName(name);
        details.setSzNotes(notes);
        details.setSzCategory(category);
        details.setAttributes(0x0); //for our own use (not used by the core)
        if (0 < bizId) {
            details.setBizId(bizId);
        }

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char pRequestID = core.longp_to_ppChar(lp);

        // create the request
        core.ABC_CreateReceiveRequest(mUsername, mPassword,
                wallet.getUUID(), details, pRequestID, error);

        if (tABC_CC.ABC_CC_Ok == error.getCode()) {
            String szRequestID = getStringAtPtr(core.longp_value(lp));
            core.ABC_ModifyReceiveRequest(mUsername, mPassword,
                    wallet.getUUID(), szRequestID, details, error);
            if (tABC_CC.ABC_CC_Ok == error.getCode()) {
                mReceiveRequestDetails = details;
                return szRequestID;
            }
        }
        String message = error.getCode().toString() + "," + error.getSzDescription() + ", " +
                error.getSzSourceFile()+", "+error.getSzSourceFunc()+", "+error.getNSourceLine();
        mInstance.debugLevel(1, message);
        return null;
    }


    public boolean finalizeRequest(String uuid, String requestId)
    {
        tABC_Error error = new tABC_Error();
        // Finalize this request so it isn't used elsewhere
        core.ABC_FinalizeReceiveRequest(mUsername, mPassword, uuid, requestId, error);
        mInstance.debugLevel(1, error.getSzDescription() + " " + error.getSzSourceFunc() + " " + error.getNSourceLine());
        return error.getCode() == tABC_CC.ABC_CC_Ok;
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
            if (null == details.getSzCategory()) {
                details.setSzCategory("");
            }

            tABC_Error Error = new tABC_Error();
            // Update the Details
            if (tABC_CC.ABC_CC_Ok != core.ABC_ModifyReceiveRequest(mUsername, mPassword,
                wallet.getUUID(),
                requestId,
                txDetails,
                Error))
            {
                mInstance.debugLevel(1, Error.toString());
            }
            // Finalize this request so it isn't used elsewhere
            if (tABC_CC.ABC_CC_Ok != core.ABC_FinalizeReceiveRequest(mUsername, mPassword,
                wallet.getUUID(),
                requestId,
                Error))
            {
                mInstance.debugLevel(1, Error.toString());
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

    //*************** Asynchronous Updating
    Handler mPeriodicTaskHandler = new Handler();

    private Handler mMainHandler;
    private Handler mCoreHandler;
    private Handler mWatcherHandler;
    private Handler mDataHandler;
    private Handler mExchangeHandler;
    private boolean mDataFetched = false;

    final static int RELOAD = 0;
    final static int REPEAT = 1;
    final static int LAST = 2;

    private class DataHandler extends Handler {
        DataHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            if (REPEAT == msg.what) {
                postDelayed(new Runnable() {
                    public void run() {
                        syncAllData();
                    }
                }, ABC_SYNC_REFRESH_INTERVAL_SECONDS * 1000);
            }
        }
    }

    private class ExchangeHandler extends Handler {
        ExchangeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            if (REPEAT == msg.what) {
                postDelayed(new Runnable() {
                    public void run() {
                        updateExchangeRates();
                    }
                }, 1000 * ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS);
            }
        }
    }

    private class MainHandler extends Handler {
        MainHandler() {
            super();
        }

        @Override
        public void handleMessage(final Message msg) {
            if (RELOAD == msg.what) {
                reloadWallets();
            }
        }
    }

    public void startAllAsyncUpdates() {
        mMainHandler = new MainHandler();

        HandlerThread ht = new HandlerThread("Data Handler");
        ht.start();
        mDataHandler = new DataHandler(ht.getLooper());

        ht = new HandlerThread("Exchange Handler");
        ht.start();
        mExchangeHandler = new ExchangeHandler(ht.getLooper());

        ht = new HandlerThread("ABC Core");
        ht.start();
        mCoreHandler = new Handler(ht.getLooper());

        ht = new HandlerThread("Watchers");
        ht.start();
        mWatcherHandler = new Handler(ht.getLooper());

        final List<String> uuids = loadWalletUUIDs();
        final int walletCount = uuids.size();
        mCoreHandler.post(new Runnable() {
            public void run() {
                // Started loading...
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                    new Intent(WALLET_LOADING_START_ACTION));
            }
        });
        for (final String uuid : uuids) {
            mCoreHandler.post(new Runnable() {
                public void run() {
                    tABC_Error error = new tABC_Error();
                    core.ABC_WalletLoad(mUsername, uuid, error);

                    startWatcher(uuid);
                    mMainHandler.sendEmptyMessage(RELOAD);

                    Intent intent = new Intent(WALLET_LOADING_STATUS_ACTION);
                    intent.putExtra(WALLET_UUID, uuid);
                    intent.putExtra(WALLETS_LOADED_TOTAL, mWatcherTasks.size());
                    intent.putExtra(WALLETS_TOTAL, walletCount);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            });
        }
        mCoreHandler.post(new Runnable() {
            public void run() {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                    new Intent(WALLETS_ALL_LOADED_ACTION));

                startBitcoinUpdates();
                startExchangeRateUpdates();
                startFileSyncUpdates();

                mMainHandler.sendEmptyMessage(RELOAD);
            }
        });
    }

    public void stopAllAsyncUpdates() {
        if (mCoreHandler == null
                || mDataHandler == null
                || mExchangeHandler == null
                || mWatcherHandler == null
                || mMainHandler == null) {
            return;
        }
        mCoreHandler.removeCallbacksAndMessages(null);
        mCoreHandler.sendEmptyMessage(LAST);
        mDataHandler.removeCallbacksAndMessages(null);
        mDataHandler.sendEmptyMessage(LAST);
        mExchangeHandler.removeCallbacksAndMessages(null);
        mExchangeHandler.sendEmptyMessage(LAST);
        mWatcherHandler.removeCallbacksAndMessages(null);
        mWatcherHandler.sendEmptyMessage(LAST);
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler.sendEmptyMessage(LAST);
        while (mDataHandler.hasMessages(LAST)
                || mCoreHandler.hasMessages(LAST)
                || mWatcherHandler.hasMessages(LAST)
                || mExchangeHandler.hasMessages(LAST)
                || mMainHandler.hasMessages(LAST)) {
            try {
                mInstance.debugLevel(1,
                    "Data: " + mDataHandler.hasMessages(LAST) + ", " +
                    "Core: " + mCoreHandler.hasMessages(LAST) + ", " +
                    "Watcher: " + mWatcherHandler.hasMessages(LAST) + ", " +
                    "Exchange: " + mExchangeHandler.hasMessages(LAST) + ", " +
                    "Main: " + mMainHandler.hasMessages(LAST));
                Thread.sleep(200);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }

        stopWatchers();
        stopExchangeRateUpdates();
        stopFileSyncUpdates();
    }

    public void restoreConnectivity() {
        if (!isLoggedIn()) {
            return;
        }
        connectWatchers();
        mCoreHandler.post(new Runnable() {
            public void run() {
                startExchangeRateUpdates();
            }
        });
        mCoreHandler.post(new Runnable() {
            public void run() {
                startFileSyncUpdates();
            }
        });
    }

    public void lostConnectivity() {
        if (!isLoggedIn()) {
            return;
        }
        stopExchangeRateUpdates();
        stopFileSyncUpdates();
        disconnectWatchers();
    }

    public void stopExchangeRateUpdates() {
        mExchangeHandler.removeCallbacksAndMessages(null);
        mExchangeHandler.sendEmptyMessage(LAST);
    }

    public void startBitcoinUpdates() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
            new Intent(WALLETS_LOADING_BITCOIN_ACTION));
        mPeriodicTaskHandler.removeCallbacks(mNotifyBitcoinLoaded);
        mPeriodicTaskHandler.postDelayed(mNotifyBitcoinLoaded, TX_LOADED_DELAY);
    }

    public void startExchangeRateUpdates() {
        updateExchangeRates();
    }


    public void updateExchangeRates() {
        if (null == mExchangeHandler
            || mExchangeHandler.hasMessages(REPEAT)
            || mExchangeHandler.hasMessages(LAST)) {
            return;
        }

        List<Wallet> wallets = getCoreWallets(false);
        if (isLoggedIn()
                && null != coreSettings()
                && null != wallets) {
            requestExchangeRateUpdate(coreSettings().settings().getCurrencyNum());
            for (Wallet wallet : wallets) {
                if (wallet.getCurrencyNum() != -1) {
                    requestExchangeRateUpdate(wallet.getCurrencyNum());
                }
            }
            mMainHandler.post(new Runnable() {
                public void run() {
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                        new Intent(EXCHANGE_RATE_UPDATED_ACTION));
                }
            });
        }
        mExchangeHandler.sendEmptyMessage(REPEAT);
    }

    private void requestExchangeRateUpdate(final Integer currencyNum) {
        mExchangeHandler.post(new Runnable() {
            public void run() {
                tABC_Error error = new tABC_Error();
                core.ABC_RequestExchangeRateUpdate(mUsername,
                    mPassword, currencyNum, error);
            }
        });
    }

    public void stopFileSyncUpdates() {
        if (null != mDataHandler) {
            mDataHandler.removeCallbacksAndMessages(null);
        }
    }

    public void startFileSyncUpdates() {
        syncAllData();
    }

    public void syncAllData() {
        if (mDataHandler.hasMessages(REPEAT)
            || mDataHandler.hasMessages(LAST)) {
            return;
        }
        mDataHandler.post(new Runnable() {
            public void run() {
                generalInfoUpdate();
            }
        });
        mDataHandler.post(new Runnable() {
            public void run() {
                if (!hasConnectivity()) {
                    return;
                }
                tABC_Error error = new tABC_Error();
                int ccInt = coreDataSyncAccount(mUsername, mPassword, tABC_Error.getCPtr(error));

                if (tABC_CC.swigToEnum(ccInt) == tABC_CC.ABC_CC_InvalidOTP) {
                    mMainHandler.post(new Runnable() {
                        public void run() {
                            if (isLoggedIn()) {
                                Intent intent = new Intent(OTP_ERROR_ACTION);
                                intent.putExtra(OTP_SECRET, GetTwoFactorSecret());
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                            }
                        }
                    });
                }
            }
        });

        List<String> uuids = loadWalletUUIDs();
        for (String uuid : uuids) {
            requestWalletDataSync(uuid);
        }

        mDataHandler.post(new Runnable() {
            public void run() {
                boolean pending = false;
                try {
                    pending = isTwoFactorResetPending(mUsername);
                } catch (AirbitzException e) {
                    mInstance.debugLevel(1, "mDataHandler.post error:" + e.errorMap());
                }
                final boolean isPending = pending;
                mMainHandler.post(new Runnable() {
                    public void run() {
                        if (!mDataFetched) {
                            mDataFetched = true;
                            connectWatchers();
                        }
                        if (isPending) {
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(OTP_RESET_ACTION));
                        }
                    }
                });
            }
        });
        // Repeat the data sync
        mDataHandler.sendEmptyMessage(REPEAT);
    }

    private void requestWalletDataSync(final String uuid) {
        mDataHandler.post(new Runnable() {
            public void run() {
                if (!hasConnectivity()) {
                    return;
                }
                tABC_Error error = new tABC_Error();
                coreDataSyncWallet(mUsername, mPassword,
                    uuid, tABC_Error.getCPtr(error));
                mMainHandler.post(new Runnable() {
                    public void run() {
                        if (!mDataFetched) {
                            connectWatcher(uuid);
                        }
                    }
                });
            }
        });
    }

    public boolean hasConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if ("WIFI".equalsIgnoreCase(ni.getTypeName())) {
                if (ni.isConnected()) {
                    mInstance.debugLevel(1, "Connection is WIFI");
                    return true;
                }
            }
            if ("MOBILE".equalsIgnoreCase(ni.getTypeName())) {
                if (ni.isConnected()) {
                    mInstance.debugLevel(1, "Connection is MOBILE");
                    return true;
                }
            }
        }
        return false;
    }

    boolean mOTPError = false;
    public boolean hasOTPError() {
        return mOTPError;
    }
    public void otpSetError(tABC_CC cc) {
        mOTPError = tABC_CC.ABC_CC_InvalidOTP == cc;
    }

    public void otpSetError(AirbitzException error) {
        mOTPError = error.isOtpError();
    }

    public void otpClearError() {
        mOTPError = false;
    }

    private boolean generalInfoUpdate() {
        tABC_Error error = new tABC_Error();
        if (hasConnectivity()) {
            core.ABC_GeneralInfoUpdate(error);
            return true;
        }
        return false;
    }

    public List<String> loadWalletUUIDs() {
        tABC_Error Error = new tABC_Error();
        List<String> uuids = new ArrayList<String>();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pCount);

        SWIGTYPE_p_long aUUIDS = core.new_longp();
        SWIGTYPE_p_p_p_char pppUUIDs = core.longp_to_pppChar(aUUIDS);

        tABC_CC result = core.ABC_GetWalletUUIDs(mUsername, mPassword,
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

    private List<Wallet> getWallets() {
        List<Wallet> wallets = new ArrayList<Wallet>();
        List<String> uuids = loadWalletUUIDs();
        for (String uuid : uuids) {
            wallets.add(getWalletFromCore(uuid));
        }
        return wallets;
    }

    private List<Wallet> mCoreWallets = null;
    public List<Wallet> getCoreWallets(boolean withTransactions) {
        return mCoreWallets;
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

    private class pLong extends SWIGTYPE_p_long {
        public pLong(long ptr) {
            super(ptr, false);
        }
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
        tABC_CC cc = core.ABC_QrEncode(GetTwoFactorSecret(), ppData, pWCount, error);
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

        result = core.ABC_GenerateRequestQRCode(mUsername, mPassword,
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

        result = core.ABC_GetRequestAddress(mUsername, mPassword,
                uuid, id, ppChar, error);

        String pAddress = null;

        if(result.equals(tABC_CC.ABC_CC_Ok)) {
            pAddress = getStringAtPtr(core.longp_value(lp));
        }

        return pAddress;
    }

    public Bitmap getQRCodeBitmap(String uuid, String id) {
        byte[] array = getQRCode(uuid, id);
        return FromBinary(array, (int) Math.sqrt(array.length), 16);
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

        tABC_CC result = core.ABC_GetCategories(mUsername, mPassword, aszCategories, pUCount, Error);

        if(result!=tABC_CC.ABC_CC_Ok) {
            mInstance.debugLevel(1, "loadCategories failed:"+Error.getSzDescription());
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
            mInstance.debugLevel(1, "Adding category: "+strCategory);
            tABC_Error Error = new tABC_Error();
            core.ABC_AddCategory(mUsername, mPassword, strCategory, Error);
        }
    }

    public void removeCategory(String strCategory) {
        mInstance.debugLevel(1, "Remove category: "+strCategory);
        tABC_Error Error = new tABC_Error();
        tABC_CC result = core.ABC_RemoveCategory(mUsername, mPassword, strCategory, Error);
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
            mInstance.debugLevel(1, "isTestNet error:"+error.getSzDescription());
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

        Random r = new SecureRandom();
        ByteBuffer bb2 = ByteBuffer.allocate(4);
        bb2.putInt(r.nextInt());
        strSeed += bb2.array();

        return strSeed;
    }

    //************************* Watcher code

    private Map<String, Thread> mWatcherTasks = new ConcurrentHashMap<String, Thread>();
    public void startWatchers() {
        List<String> wallets = loadWalletUUIDs();
        for (final String uuid : wallets) {
            startWatcher(uuid);
        }
        if (mDataFetched) {
            connectWatchers();
        }
    }

    private void startWatcher(final String uuid) {
        mWatcherHandler.post(new Runnable() {
            public void run() {
                if (uuid != null && !mWatcherTasks.containsKey(uuid)) {
                    tABC_Error error = new tABC_Error();
                    core.ABC_WatcherStart(mUsername, mPassword, uuid, error);
                    printABCError(error);
                    mInstance.debugLevel(1, "Started watcher for " + uuid);

                    Thread thread = new Thread(new WatcherRunnable(uuid));
                    thread.start();

                    watchAddresses(uuid);

                    if (mDataFetched) {
                        connectWatcher(uuid);
                    }
                    mWatcherTasks.put(uuid, thread);

                    // Request a data sync as soon as watcher is started
                    requestWalletDataSync(uuid);
                    mMainHandler.sendEmptyMessage(RELOAD);
                }
            }
        });
    }

    public void connectWatchers() {
        List<String> wallets = loadWalletUUIDs();
        for (final String uuid : wallets) {
            connectWatcher(uuid);
        }
    }

    public void connectWatcher(final String uuid) {
        mWatcherHandler.post(new Runnable() {
            public void run() {
                if (!hasConnectivity()) {
                    mInstance.debugLevel(1, "Skipping connect...no connectivity");
                    return;
                }

                tABC_Error error = new tABC_Error();
                core.ABC_WatcherConnect(uuid, error);
                printABCError(error);
                watchAddresses(uuid);
            }
        });
    }

    public void disconnectWatchers() {
        mWatcherHandler.post(new Runnable() {
            public void run() {
                for (String uuid : mWatcherTasks.keySet()) {
                    tABC_Error error = new tABC_Error();
                    core.ABC_WatcherDisconnect(uuid, error);
                }
            }
        });
    }

    private void watchAddresses(final String uuid) {
        tABC_Error error = new tABC_Error();
        core.ABC_WatchAddresses(mUsername,
            mPassword, uuid, error);
        printABCError(error);
    }

    public void waitOnWatchers() {
        mWatcherHandler.sendEmptyMessage(LAST);
        while (mWatcherHandler != null && mWatcherHandler.hasMessages(LAST)) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
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

    public void stopWatcher(String uuid) {
        tABC_Error error = new tABC_Error();
        core.ABC_WatcherStop(uuid, error);
        core.ABC_WatcherDelete(uuid, error);
        mWatcherTasks.remove(uuid);
    }

    public void deleteWatcherCache() {
        tABC_Error error = new tABC_Error();
        List<String> uuids = loadWalletUUIDs();
        for (String uuid : uuids) {
            core.ABC_WatcherDeleteCache(uuid, error);
        }
    }

    /*
     * Prioritize wallet loop attention to this address for uuid
     */
    public void prioritizeAddress(String address, String walletUUID)
    {
        tABC_Error Error = new tABC_Error();
        core.ABC_PrioritizeAddress(mUsername, mPassword, walletUUID, address, Error);
    }


    public void ChangePassword(String password) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        tABC_CC cc = core.ABC_ChangePassword(
            mUsername, password, password, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    public boolean recoveryAnswers(String strAnswers, String strUserName) throws AirbitzException {
        SWIGTYPE_p_int lp = core.new_intp();
        SWIGTYPE_p_bool pbool = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        tABC_Error error = new tABC_Error();
        core.ABC_CheckRecoveryAnswers(strUserName, strAnswers, pbool, error);
        if (tABC_CC.ABC_CC_Ok != error.getCode()) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
        return core.intp_value(lp)==1;
    }

    public void ChangePasswordWithRecoveryAnswers(String username, String recoveryAnswers,
            String password, String pin) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        tABC_CC cc = core.ABC_ChangePasswordWithRecoveryAnswers(
                        username, recoveryAnswers, password, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    private boolean isValidCategory(String category) {
        return category.startsWith("Expense") || category.startsWith("Exchange") ||
                category.startsWith("Income") || category.startsWith("Transfer");
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
            mInstance.debugLevel(1, "PinLoginExists error:"+error.getSzDescription());
            return false;
        }
    }

    public boolean PinLogin(String username, String pin) throws AirbitzException {
        if (username == null || pin == null) {
            tABC_Error error = new tABC_Error();
            error.setCode(tABC_CC.ABC_CC_Error);
            throw new AirbitzException(mContext, error.getCode(), error);
        }

        tABC_Error error = new tABC_Error();
        core.ABC_PinLogin(username, pin, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
        return true;
    }

    public void PinSetup() {
        if (mPinSetup == null && isLoggedIn()) {
            // Delay PinSetup after getting transactions
            mPeriodicTaskHandler.postDelayed(delayedPinSetup, 1000);
        }
    }

    public tABC_CC PinSetupBlocking() {
        AccountSettings settings = coreSettings();
        if (settings != null) {
            String username = mUsername;
            String pin = settings.settings().getSzPIN();
            tABC_Error pError = new tABC_Error();
            return core.ABC_PinSetup(username, pin, pError);
        }
        return tABC_CC.ABC_CC_Error;
    }

    final Runnable delayedPinSetup = new Runnable() {
        public void run() {
            mPinSetup = new PinSetupTask();
            mPinSetup.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    private PinSetupTask mPinSetup;
    public class PinSetupTask extends AsyncTask {
        @Override
        protected tABC_CC doInBackground(Object... params) {
            return PinSetupBlocking();
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

    OnPasswordCheckListener mOnPasswordCheckListener = null;
    public void SetOnPasswordCheckListener(OnPasswordCheckListener listener, String password) {
        mOnPasswordCheckListener = listener;
        mPeriodicTaskHandler.post(new PasswordOKAsync(password));
    }
    public interface OnPasswordCheckListener {
        void onPasswordCheck(boolean passwordOkay);
    }

    private class PasswordOKAsync implements Runnable {
        private final String mPassword;

        PasswordOKAsync(final String password) {
            this.mPassword = password;
        }

        public void run() {
            boolean check = false;
            if (mPassword == null || mPassword.isEmpty()) {
                check = !PasswordExists();
            } else {
                check = PasswordOK(mUsername, mPassword);
            }

            if(mOnPasswordCheckListener != null) {
                mOnPasswordCheckListener.onPasswordCheck(check);
                mOnPasswordCheckListener = null;
            }
        }
    }

    public boolean PasswordOK(String username, String password) {
        boolean check = false;
        if(password == null || password.isEmpty()) {
            check = !PasswordExists();
        }
        else {
            tABC_Error pError = new tABC_Error();
            SWIGTYPE_p_long lp = core.new_longp();
            SWIGTYPE_p_bool okay = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

            tABC_CC result = core.ABC_PasswordOk(username, password, okay, pError);
            if(result.equals(tABC_CC.ABC_CC_Ok)) {
                check = getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0;
            } else {
                mInstance.debugLevel(1, "Password OK error:"+pError.getSzDescription());
            }
        }

        return check;
    }

    public boolean PasswordExists() {
        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_bool exists = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        tABC_CC result = core.ABC_PasswordExists(mUsername, exists, pError);
        if(pError.getCode().equals(tABC_CC.ABC_CC_Ok)) {
            return getBytesAtPtr(lp.getCPtr(lp), 1)[0] != 0;
        } else {
            mInstance.debugLevel(1, "Password Exists error:"+pError.getSzDescription());
            return true;
        }
    }

    public void SetupDefaultCurrency() {
        AccountSettings settings = coreSettings();
        if (settings == null) {
            return;
        }
        settings.setupDefaultCurrency();
    }

    public int defaultCurrencyNum() {
        initCurrencies();
        Locale locale = Locale.getDefault();
        Currency currency = Currency.getInstance(locale);
        Map<Integer, String> supported = mCurrencyCodeCache;
        if (supported.containsValue(currency.getCurrencyCode())) {
            int number = getCurrencyNumberFromCode(currency.getCurrencyCode());
            return number;
        } else {
            return 840;
        }
    }


    public int getCurrencyNumberFromCode(String currencyCode) {
        initCurrencies();

        int index = -1;
        for(int i=0; i< mCurrencyNumbers.length; i++) {
            if(currencyCode.equals(currencyCodeLookup(mCurrencyNumbers[i]))) {
                index = i;
                break;
            }
        }
        if(index != -1) {
            return mCurrencyNumbers[index];
        }
        return 840;
    }

    public String getPrivateSeed(Wallet wallet) {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        tABC_CC result = core.ABC_ExportWalletSeed(mUsername, mPassword,
                wallet.getUUID(), ppChar, Error);

        if (tABC_CC.ABC_CC_Ok == result) {
            return getStringAtPtr(core.longp_value(lp));
        } else {
            return null;
        }
    }

    private void printABCError(tABC_Error pError) {
        if (pError.getCode() != tABC_CC.ABC_CC_Ok) {
            mInstance.debugLevel(1,
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
        mCoreWallets = null;
        mDataFetched = false;

        // Wait for data sync to exit gracefully
        AsyncTask[] as = new AsyncTask[] {
            mPinSetup, mReloadWalletTask
        };
        for (AsyncTask a : as) {
            if (a != null) {
                a.cancel(true);
                try {
                    a.get(1000, TimeUnit.MILLISECONDS);
                } catch (java.util.concurrent.CancellationException e) {
                    mInstance.debugLevel(1, "task cancelled");
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }
        clearCacheKeys();
    }

    private void clearCacheKeys() {
        tABC_Error error = new tABC_Error();
        tABC_CC result = core.ABC_ClearKeyCache(error);
        if (result != tABC_CC.ABC_CC_Ok) {
            mInstance.debugLevel(1, error.toString());
        }
    }

    public String SweepKey(String uuid, String wif) {
        tABC_Error Error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        int result = coreSweepKey(mUsername, mPassword,
                uuid, wif, SWIGTYPE_p_p_char.getCPtr(ppChar), tABC_Error.getCPtr(Error));
        if ( result != 0) {
            return "";
        }
        else {
            return getStringAtPtr(core.longp_value(lp));
        }
    }

    public String getCoreVersion() {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        core.ABC_Version(ppChar, error);
        if (error.getCode() == tABC_CC.ABC_CC_Ok) {
            return getStringAtPtr(core.longp_value(lp));
        }
        return "";
    }

    public boolean uploadLogs() {
        tABC_Error Error = new tABC_Error();

        // Send system information to end of logfile
        String deviceName = Build.MODEL;
        String deviceMan = Build.MANUFACTURER;
        String deviceBrand = Build.BRAND;
        String deviceOS = Build.VERSION.RELEASE;

        CoreAPI.debugLevel(0, "Platform:" + deviceBrand + " " + deviceMan + " " + deviceName);
        CoreAPI.debugLevel(0, "Android Version:" + deviceOS);

        core.ABC_UploadLogs(mUsername, mPassword, Error);
        return Error.getCode() == tABC_CC.ABC_CC_Ok;
    }

    //*********************** Two Factor Authentication
    boolean mTwoFactorOn = false;

    public boolean isTwoFactorOn() {
        return mTwoFactorOn;
    }

    public void otpReset(String username) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_OtpResetSet(username, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    public void otpAuthGet() throws AirbitzException {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long ptimeout = core.new_longp();
        SWIGTYPE_p_int lp = core.new_intp();
        SWIGTYPE_p_bool pbool = new SWIGTYPE_p_bool(lp.getCPtr(lp), false);

        core.ABC_OtpAuthGet(mUsername,
            mPassword, pbool, ptimeout, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }

        mTwoFactorOn = core.intp_value(lp)==1;
    }

    //Blocking
    public void OtpKeySet(String username, String secret) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_OtpKeySet(username, secret, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    // Blocking
    public String GetTwoFactorSecret() {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        tABC_CC cc = core.ABC_OtpKeyGet(mUsername, ppChar, error);
        String secret = cc == tABC_CC.ABC_CC_Ok ? getStringAtPtr(core.longp_value(lp)) : null;
        return secret;
    }

    public boolean isTwoFactorResetPending(String username) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        core.ABC_OtpResetGet(ppChar, error);
        if (error.getCode() == tABC_CC.ABC_CC_Ok) {
            String userNames = getStringAtPtr(core.longp_value(lp));
            if (userNames != null && username != null) {
                return userNames.contains(username);
            }
        } else {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
        return false;
    }

    // Blocking
    public void enableTwoFactor(boolean on) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        if (on) {
            core.ABC_OtpAuthSet(mUsername, mPassword, OTP_RESET_DELAY_SECS, error);
            if (error.getCode() == tABC_CC.ABC_CC_Ok) {
                mTwoFactorOn = true;
            } else {
                throw new AirbitzException(mContext, error.getCode(), error);
            }
        } else {
            core.ABC_OtpAuthRemove(mUsername, mPassword, error);
            if (error.getCode() == tABC_CC.ABC_CC_Ok) {
                mTwoFactorOn = false;
                core.ABC_OtpKeyRemove(mUsername, error);
            } else {
                throw new AirbitzException(mContext, error.getCode(), error);
            }
        }
    }

    public void cancelTwoFactorRequest() throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_OtpResetRemove(mUsername, mPassword, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
    }

    public String getTwoFactorDate() throws AirbitzException {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        tABC_CC cc = core.ABC_OtpResetDate(ppChar, error);
        if (error.getCode() != tABC_CC.ABC_CC_Ok) {
            throw new AirbitzException(mContext, error.getCode(), error);
        }
        return getStringAtPtr(core.longp_value(lp));
    }

    public List<String> listAccounts() {
        tABC_Error error = new tABC_Error();
        tABC_CC cc;
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        cc = core.ABC_ListAccounts(ppChar, error);
        if(cc == tABC_CC.ABC_CC_Ok) {
            List<String> array = Arrays.asList(getStringAtPtr(core.longp_value(lp)).split("\\n"));
            List<String> list = new ArrayList<String>();
            for(int i=0; i< array.size(); i++) {
                if(!array.get(i).isEmpty()) {
                    list.add(array.get(i));
                }
            }
            return list;
        }
        return null;
    }

    public boolean deleteAccount(String account) {
        tABC_Error error = new tABC_Error();
        tABC_CC cc = core.ABC_AccountDelete(account, error);
        return cc == tABC_CC.ABC_CC_Ok;
    }

    public String accountAvailable(String account) throws AirbitzException {
        tABC_Error error = new tABC_Error();

        tABC_CC cc = core.ABC_AccountAvailable(account, error);
        if (cc == tABC_CC.ABC_CC_Ok) {
            return null;
        } else {
            throw new AirbitzException(mContext, error.getCode(), null);
        }
    }

    public String createAccountAndPin(String account, String password, String pin) throws AirbitzException {
        tABC_Error error = new tABC_Error();
        core.ABC_CreateAccount(account, password, error);
        if(error.getCode() == tABC_CC.ABC_CC_Ok) {
            core.ABC_SetPIN(account, password, pin, error);
            if(error.getCode() == tABC_CC.ABC_CC_Ok) {
                return null;
            }
        }
        throw new AirbitzException(mContext, error.getCode(), null);
    }

    public static class BitidSignature {
        public String address;
        public String signature;
    }

    public BitidSignature bitidSignature(String uri, String message) {
        BitidSignature bitid = new BitidSignature();

        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long pAddress = core.new_longp();
        SWIGTYPE_p_p_char ppAddress = core.longp_to_ppChar(pAddress);
        SWIGTYPE_p_long pSignature = core.new_longp();
        SWIGTYPE_p_p_char ppSignature = core.longp_to_ppChar(pSignature);

        tABC_CC result = core.ABC_BitidSign(
            mUsername, mPassword, uri, message, ppAddress, ppSignature, error);
        if (result == tABC_CC.ABC_CC_Ok) {
            bitid.address = getStringAtPtr(core.longp_value(pAddress));
            bitid.signature = getStringAtPtr(core.longp_value(pSignature));
        }
        return bitid;
    }

    public String pluginDataGet(String pluginId, String key) {
        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        core.ABC_PluginDataGet(mUsername, mPassword,
            pluginId, key, ppChar, pError);
        if (pError.getCode() == tABC_CC.ABC_CC_Ok) {
            return getStringAtPtr(core.longp_value(lp));
        } else {
            return null;
        }
    }

    public boolean pluginDataSet(String pluginId, String key, String value) {
        tABC_Error pError = new tABC_Error();
        core.ABC_PluginDataSet(mUsername, mPassword,
                pluginId, key, value, pError);
        return pError.getCode() == tABC_CC.ABC_CC_Ok;
    }

    public boolean pluginDataRemove(String pluginId, String key) {
        tABC_Error pError = new tABC_Error();
        core.ABC_PluginDataRemove(mUsername, mPassword, pluginId, key, pError);
        return pError.getCode() == tABC_CC.ABC_CC_Ok;
    }

    public boolean pluginDataClear(String pluginId) {
        tABC_Error pError = new tABC_Error();
        core.ABC_PluginDataClear(mUsername, mPassword, pluginId, pError);
        return pError.getCode() == tABC_CC.ABC_CC_Ok;
    }

    public String getRawTransaction(String walletUUID, String txid) {
        tABC_Error pError = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        core.ABC_GetRawTransaction(
                mUsername, mPassword,
                walletUUID, txid, ppChar, pError);
        if (pError.getCode() == tABC_CC.ABC_CC_Ok) {
            return getStringAtPtr(core.longp_value(lp));
        } else {
            return null;
        }
    }

    //*************** new SpendTarget API

    public SpendTarget getNewSpendTarget() {
        return new SpendTarget();
    }
    public class SpendTarget {
        SWIGTYPE_p_long _lpSpend;
        SWIGTYPE_p_p_sABC_SpendTarget _pSpendSWIG;
        tABC_SpendTarget _pSpend;
        tABC_Error pError;
        long bizId;
        double mAmountFiat;

        public SpendTarget() {
            _lpSpend = core.new_longp();
            _pSpendSWIG = core.longPtr_to_ppSpendTarget(_lpSpend);
            _pSpend = null;
            pError = new tABC_Error();
        }

        public void dealloc() {
            if (_pSpend != null) {
                _pSpend = null;
                pError = null;
            }
        }

        public tABC_SpendTarget getSpend() {
            return _pSpend;
        }

        public long getSpendAmount() {
            return get64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(_pSpend.getAmount()));
        }

        public boolean isTransfer() {
            return !TextUtils.isEmpty(_pSpend.getSzDestUUID());
        }

        public void setSpendAmount(long amount) {
            SWIGTYPE_p_uint64_t ua = core.new_uint64_tp();
            set64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(ua), amount);
            _pSpend.setAmount(ua);
        }

        public boolean newSpend(String text) {
            tABC_Error pError = new tABC_Error();
            core.ABC_SpendNewDecode(text, _pSpendSWIG, pError);
            _pSpend = new Spend(core.longp_value(_lpSpend));
            return pError.getCode() == tABC_CC.ABC_CC_Ok;
        }

        public boolean newTransfer(String walletUUID) {
            SWIGTYPE_p_uint64_t amount = core.new_uint64_tp();
            set64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(amount), 0);
            core.ABC_SpendNewTransfer(mUsername,
                    walletUUID, amount, _pSpendSWIG, pError);
            _pSpend = new Spend(core.longp_value(_lpSpend));
            return pError.getCode() == tABC_CC.ABC_CC_Ok;
        }

        public boolean spendNewInternal(String address, String label, String category,
                                        String notes, long amountSatoshi) {
            SWIGTYPE_p_uint64_t amountS = core.new_uint64_tp();
            set64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(amountS), amountSatoshi);

            core.ABC_SpendNewInternal(address, label,
                    category, notes, amountS, _pSpendSWIG, pError);
            _pSpend = new Spend(core.longp_value(_lpSpend));
            return pError.getCode() == tABC_CC.ABC_CC_Ok;
        }

        public void setBizId(long bizId) {
            this.bizId = bizId;
        }

        public void setAmountFiat(double amountFiat) {
            this.mAmountFiat = amountFiat;
        }

        public String signTx(String walletUUID) {
            String rawTx = null;
            tABC_Error error = new tABC_Error();
            SWIGTYPE_p_long tx = core.new_longp();
            SWIGTYPE_p_p_char pRawTx = core.longp_to_ppChar(tx);

            core.ABC_SpendSignTx(mUsername, walletUUID, _pSpend, pRawTx, error);
            if (error.getCode() == tABC_CC.ABC_CC_Ok) {
                rawTx = getStringAtPtr(core.longp_value(tx));
            }
            return rawTx;
        }

        public boolean broadcastTx(String walletUUID, String rawTx) {
            tABC_Error error = new tABC_Error();
            core.ABC_SpendBroadcastTx(mUsername, walletUUID, _pSpend, rawTx, error);
            return error.getCode() == tABC_CC.ABC_CC_Ok;
        }

        public String saveTx(String walletUUID, String rawTx) {
            String id = null;
            tABC_Error error = new tABC_Error();
            SWIGTYPE_p_long txid = core.new_longp();
            SWIGTYPE_p_p_char pTxId = core.longp_to_ppChar(txid);
            core.ABC_SpendSaveTx(mUsername, walletUUID, _pSpend, rawTx, pTxId, error);
            if (error.getCode() == tABC_CC.ABC_CC_Ok) {
                id = getStringAtPtr(core.longp_value(txid));
                updateTransaction(walletUUID, id);
            }
            return id;
        }

        public String approve(String walletUUID) {
            String id = null;
            String rawTx = signTx(walletUUID);
            if (null != rawTx && broadcastTx(walletUUID, rawTx)) {
                id = saveTx(walletUUID, rawTx);
            }
            return id;
        }

        public void updateTransaction(String walletUUID, String txId) {
            String categoryText = "Transfer:Wallet:";
            Wallet destWallet = null;
            Wallet srcWallet = getWalletFromUUID(walletUUID);
            if (_pSpend != null) {
                destWallet = getWalletFromUUID(_pSpend.getSzDestUUID());
            }

            Transaction tx = getTransaction(walletUUID, txId);
            if (null != tx) {
                if (destWallet != null) {
                    tx.setName(destWallet.getName());
                    tx.setCategory(categoryText + destWallet.getName());
                }
                if (mAmountFiat > 0) {
                    tx.setAmountFiat(mAmountFiat);
                }
                if (0 < bizId) {
                    tx.setmBizId(bizId);
                }
                try {
                    storeTransaction(tx);
                } catch (AirbitzException e) {
                    mInstance.debugLevel(1, "updateTransaction 1 error:" + e.errorMap());
                }
            }

            // This was a transfer
            if (destWallet != null) {
                Transaction destTx = getTransaction(destWallet.getUUID(), txId);
                if (null != destTx) {
                    destTx.setName(srcWallet.getName());
                    destTx.setCategory(categoryText + srcWallet.getName());
                    try {
                        storeTransaction(destTx);
                    } catch (AirbitzException e) {
                        mInstance.debugLevel(1, "updateTransaction 2 error:" + e.errorMap());
                    }
                }
            }
        }

        public long maxSpendable(String walletUUID) {
            tABC_Error error = new tABC_Error();

            SWIGTYPE_p_uint64_t result = core.new_uint64_tp();

            core.ABC_SpendGetMax(mUsername, walletUUID, _pSpend, result, pError);
            long actual = get64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(result));
            return actual;
        }

        public long calcSendFees(String walletUUID) throws AirbitzException {
            tABC_Error error = new tABC_Error();
            SWIGTYPE_p_uint64_t total = core.new_uint64_tp();
            core.ABC_SpendGetFee(mUsername, walletUUID, _pSpend, total, error);

            long fees = get64BitLongAtPtr(SWIGTYPE_p_uint64_t.getCPtr(total));
            if (error.getCode() != tABC_CC.ABC_CC_Ok) {
                throw new AirbitzException(mContext, error.getCode(), error);
            }
            return fees;
        }

        public class Spend extends tABC_SpendTarget {
            public Spend(long pv) {
                super(pv, false);
                }
            public long getPtr(tABC_SpendTarget p) {
                return getCPtr(p);
            }
        }

    }

    public String parseBitidUri(String uri) {
        tABC_Error error = new tABC_Error();
        String urlDomain = null;

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        core.ABC_BitidParseUri(mUsername, null, uri, ppChar, error);
        if (error.getCode() == tABC_CC.ABC_CC_Ok) {
            urlDomain = getStringAtPtr(core.longp_value(lp));
        }
        return urlDomain;
    }

    public boolean bitidLogin(String uri) {
        tABC_Error error = new tABC_Error();
        core.ABC_BitidLogin(mUsername, null, uri, error);
        return error.getCode() == tABC_CC.ABC_CC_Ok;
    }
}
