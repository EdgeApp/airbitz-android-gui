package com.airbitz.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by tom on 6/20/14.
 * This class is a bridge to the ndk core code, acting like a viewmodel
 */
public class CoreAPI {
    private static String TAG = CoreAPI.class.getSimpleName();
    private static int ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS = 60;
    private static int ABC_SYNC_REFRESH_INTERVAL_SECONDS = 5;
    public static int ABC_DENOMINATION_BTC = 0;
    public static int ABC_DENOMINATION_MBTC = 1;
    public static int ABC_DENOMINATION_UBTC = 2;
    public static double SATOSHI_PER_BTC = 1E8;
    public static double SATOSHI_PER_mBTC = 1E5;
    public static double SATOSHI_PER_uBTC = 1E2;

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
        }
        return mInstance;
    }

    public native String getStringAtPtr(long pointer);
    public native byte[] getBytesAtPtr(long pointer, int length);
    public native long get64BitLongAtPtr(long pointer);
    public native void set64BitLongAtPtr(long pointer, long value);
    public native int FormatAmount(long satoshi, long ppchar, long decimalplaces, long perror);
    public native int satoshiToCurrency(String jarg1, String jarg2, long satoshi, long currencyp, int currencyNum, long error);
    public native int setWalletOrder(String jarg1, String jarg2, String[] jarg3, tABC_Error jarg5);
    public native void coreInitialize(String jfile, String jseed, long jseedLength, long jerrorp);
    public native void RegisterAsyncCallback ();
    public native long ParseAmount(String jarg1, int decimalplaces);

    public void Initialize(String file, String seed, long seedLength){
        if(!initialized) {
            tABC_Error error = new tABC_Error();
            RegisterAsyncCallback();
            coreInitialize(file, seed, seedLength, error.getCPtr(error));
            initialized = true;
        }
    }

    //***************** Callback handling
    public void callbackAsyncBitcoinInfo(long asyncBitCoinInfo_ptr) {
        tABC_AsyncBitCoinInfo info = new tABC_AsyncBitCoinInfo(asyncBitCoinInfo_ptr, false);
        tABC_AsyncEventType type = info.getEventType();
        if(type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingBitCoin) {
            if(mOnIncomingBitcoin!=null) {
                mIncomingBitcoinUUID = info.getSzWalletUUID();
                mIncomingBitcoinTxID = info.getSzTxID();
                mPeriodicTaskHandler.post(IncomingBitcoinUpdater);
            }
            else
                Log.d(TAG, "incoming bitcoin event has no listener");
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_BlockHeightChange) {
            if(mOnBlockHeightChange!=null)
                mPeriodicTaskHandler.post(BlockHeightUpdater);
            else
                Log.d(TAG, "block exchange event has no listener");
        }
    }

    // Callback interface when an incoming bitcoin is received
    private OnIncomingBitcoin mOnIncomingBitcoin;
    private String mIncomingBitcoinUUID, mIncomingBitcoinTxID;
    public interface OnIncomingBitcoin {
        public void onIncomingBitcoin(String walletUUID, String txId);
    }
    public void setOnIncomingBitcoinListener(OnIncomingBitcoin listener) {
        mOnIncomingBitcoin = listener;
    }
    final Runnable IncomingBitcoinUpdater = new Runnable() {
        public void run() { mOnIncomingBitcoin.onIncomingBitcoin(mIncomingBitcoinUUID, mIncomingBitcoinTxID); }
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
        public void run() { mOnBlockHeightChange.onBlockHeightChange(); }
    };

    //***************** Wallet handling
    private static final int WALLET_ATTRIBUTE_ARCHIVE_BIT = 0x0; // BIT0 is the archive bit

    public List<Wallet> loadWallets() {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets();

        if(coreList==null)
            coreList = new ArrayList<Wallet>();
        Wallet headerWallet = new Wallet("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL");
        headerWallet.setUUID("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL");
        list.add(headerWallet);//Wallet HEADER
        // Loop through and find non-archived wallets first
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & (1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT)) != 1 && wallet.getName()!=null)
                list.add(wallet);
        }
        Wallet archiveWallet = new Wallet("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd");
        archiveWallet.setUUID("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd");
        list.add(archiveWallet); //Archive HEADER
        // Loop through and find archived wallets now
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & (1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT)) == 1 && wallet.getName()!=null)
                list.add(wallet);
        }
        return list;
    }

    // This is a blocking call. You must wrap this in an AsyncTask or similar.
    public boolean createWallet(String username, String password, String walletName, int currencyNum) {
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pResults = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pResults);

        tABC_CC result = core.ABC_CreateWallet(username, password,
                walletName, currencyNum, 0, null, pVoid, pError);
        if(result==tABC_CC.ABC_CC_Ok) {
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
        Wallet info = getWallet(wallet.getUUID());
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
            wallet = getWallet(wallet.getUUID());
        }
        return wallet;
    }

    public Wallet getWallet(String uuid) {
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
            Log.d("", "Error: CoreBridge.getWallet: " + Error.getSzDescription());
            return null;
        }
    }

    public void setWalletOrder(List<Wallet> wallets) {
        String[] uuids = new String[wallets.size()-2]; // 2 extras for headers
        int count=0;
        boolean archived=false; // non-archive
        for(Wallet wallet : wallets) {
            if(wallet.isArchiveHeader()) {
                archived=true;
            } else if(wallet.isHeader()) {
                archived=false;
            } else { // wallet is real
                uuids[count++] = wallet.getUUID();
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

        int result = setWalletOrder(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
            uuids, Error);

        if(tABC_CC.swigToEnum(result) != tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error: CoreBridge.setWalletOrder" + Error.getSzDescription());
        }
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
                Log.d(TAG, "Error: CoreBridge.setWalletAttributes: "+ Error.getSzDescription());
                return false;
            }
        }
        return false;
    }

    //************ Account Recovery

    //************ Settings handling
    private String[] mFauxCurrencyAcronyms = {"CAD", "CNY", "CUP", "EUR", "GBP", "MXN", "USD"};
    private String[] mFauxCurrencyDenomination = {"$", "¥", "₱", "€", "£", "$", "$"};
    private int[] mFauxCurrencyNumbers = {124, 156, 192, 978, 826, 484, 840};

    private String[] mBTCDenominations = {"BTC", "mBTC", "μBTC"};
    private String[] mBTCSymbols = {"฿ ", "m฿ ", "μ฿ "};

    public String GetUserPIN() {
        tABC_Error error = new tABC_Error();
        tABC_CC result;
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);
        String szPIN = null;

        result = core.ABC_GetPIN(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                ppChar, error);

        if (result == tABC_CC.ABC_CC_Ok)
        {
            return getStringAtPtr(core.longp_value(lp));
        }
        String message = result.toString() + "," + error.getSzDescription() + ", " +
                error.getSzSourceFile()+", "+error.getSzSourceFunc()+", "+error.getNSourceLine();
        Log.d(TAG, message);
        return null;
    }

    public String getDefaultBTCDenomination() {
        tABC_AccountSettings settings = loadAccountSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination == null) {
            Log.d(TAG, "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCDenominations[bitcoinDenomination.getDenominationType()];
    }

    public String getUserBTCSymbol() {
        tABC_AccountSettings settings = loadAccountSettings();
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
        return mFauxCurrencyDenomination[SettingsCurrencyIndex()];
    }

    public int[] getCurrencyNumbers() {
        return mFauxCurrencyNumbers;
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
//            long start = core.longp_value(lp);
//
//            for (int i = 0; i < 1; i++) //mCount; i++) //TODO error when i > 0
//            {
//                tABC_Currency txd = new Currency(start + i * 4);
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

    private static class ppCurrency extends SWIGTYPE_p_p_sABC_Currency {
        public static long getPtr(SWIGTYPE_p_p_sABC_Currency p) { return getCPtr(p); }
        public static long getPtr(SWIGTYPE_p_p_sABC_Currency p, long i) { return getCPtr(p)+i; }
    }

    private tABC_AccountSettings mCoreSettings;
    public tABC_AccountSettings loadAccountSettings() {
        if(mCoreSettings!=null)
            return mCoreSettings;
        tABC_CC result;
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_AccountSettings pAccountSettings = core.longp_to_ppAccountSettings(lp);

        result = core.ABC_LoadAccountSettings(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                pAccountSettings, Error);

        if(result==tABC_CC.ABC_CC_Ok) {
            mCoreSettings = new tABC_AccountSettings(core.longp_value(lp), false);
            if(mCoreSettings.getCurrencyNum() == 0) {
                mCoreSettings.setCurrencyNum(840); // US DOLLAR DEFAULT
                saveAccountSettings(mCoreSettings);
            }
            return mCoreSettings;
        } else {
            String message = Error.getSzDescription()+", "+Error.getSzSourceFunc();
            Log.d(TAG, "Load settings failed - "+message);
        }
        return null;
    }

    public void saveAccountSettings(tABC_AccountSettings settings) {
        tABC_CC result;
        tABC_Error Error = new tABC_Error();

        result = core.ABC_UpdateAccountSettings(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                settings, Error);
        if(result==tABC_CC.ABC_CC_Ok) {

        }
    }

    public ExchangeRateSource[] getExchangeRateSources(tABC_ExchangeRateSources sources) {
        List<tABC_ExchangeRateSource> list = new ArrayList<tABC_ExchangeRateSource>();
        ExchangeRateSources temp = new ExchangeRateSources(sources.getCPtr(sources));
        return temp.getSources();
    }

    private class ExchangeRateSources extends tABC_ExchangeRateSources {
        long mNumSources = 0;
        long mChoiceStart = 0;
        ExchangeRateSource[] sources;

        public ExchangeRateSources (long pv) {
            super(pv, false);
            if(pv!=0) {
                mNumSources = super.getNumSources();
            }
        }

        public long getNumSources() { return mNumSources; }

        public ExchangeRateSource[] getSources() {
            sources = new ExchangeRateSource[(int) mNumSources];
            SWIGTYPE_p_p_sABC_ExchangeRateSource start = super.getASources();
            for(int i=0; i< mNumSources; i++) {
                ExchangeRateSources fake = new ExchangeRateSources(ppExchangeRateSource.getPtr(start, i * 4));
                mChoiceStart = fake.getNumSources();
                sources[i] = new ExchangeRateSource(new PVOID(mChoiceStart));
            }
            return sources;
        }
    }

    public class ExchangeRateSource extends tABC_ExchangeRateSource {
            String mSource = null;
            long mCurrencyNum = -1;

            public ExchangeRateSource(SWIGTYPE_p_void pv) {
                super(PVoidStatic.getPtr(pv), false);
                if(PVoidStatic.getPtr(pv)!=0) {
                    mSource = super.getSzSource();
                    mCurrencyNum = super.getCurrencyNum();
                }
            }

            public String getSource() { return mSource; }
            public long getmCurrencyNum() { return mCurrencyNum; }
    }

    private static class ppExchangeRateSource extends SWIGTYPE_p_p_sABC_ExchangeRateSource {
        public static long getPtr(SWIGTYPE_p_p_sABC_ExchangeRateSource p) { return getCPtr(p); }
        public static long getPtr(SWIGTYPE_p_p_sABC_ExchangeRateSource p, long i) { return getCPtr(p)+i; }
    }

    //***************** Questions
    /**
     * Get questions
     */
    public QuestionChoice[] GetRecoveryQuestions() {

        QuestionChoice[] mChoices = null;
        tABC_Error pError = new tABC_Error();
        QuestionResults pData = new QuestionResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pData);


        tABC_CC result = core.ABC_GetQuestionChoices(AirbitzApplication.getUsername(), null, pVoid, pError);
        if (result == tABC_CC.ABC_CC_Ok) {
            QuestionChoices qcs = new QuestionChoices(pData.getPtrPtr());
            long num = qcs.getNumChoices();
            mChoices = qcs.getChoices();
        }
        return mChoices;
    }

    public tABC_CC SaveRecoveryAnswers(String mQuestions, String mAnswers) {

        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pResults = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pResults);

        tABC_CC result = core.ABC_SetAccountRecoveryQuestions(AirbitzApplication.getUsername(),
                AirbitzApplication.getPassword(),
                mQuestions, mAnswers, null, pVoid, pError);
        return result;
    }

    private class QuestionResults extends tABC_RequestResults {
        public long getPtrPtr() {
            QuestionChoices fake = new QuestionChoices(getCPtr(this)); // A fake to get *ptr
            return fake.getNumChoices();
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


    //************ Transaction handling
    public Transaction getTransaction(String walletUUID, String szTxId)
    {
        tABC_Error Error = new tABC_Error();
        Transaction transaction = null;

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_TxInfo pTxInfo = core.longp_to_ppTxInfo(lp);

        Wallet wallet = getWallet(walletUUID);
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
            Log.d(TAG, "Error: CoreBridge.loadTransactions: "+ Error.getSzDescription());
        }
        return transaction;
    }

    public List<Transaction> loadTransactions(Wallet wallet) {
        List<Transaction> listTransactions = new ArrayList<Transaction>();
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_int pCount = core.new_intp();
        SWIGTYPE_p_unsigned_int puCount = core.int_to_uint(pCount);

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_p_sABC_TxInfo paTxInfo = core.longp_to_pppTxInfo(lp);

        tABC_CC result = core.ABC_GetTransactions(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                wallet.getUUID(), paTxInfo, puCount, Error);

        if (result==tABC_CC.ABC_CC_Ok)
        {
            int ptrToInfo = core.longp_value(lp);
            int count = core.intp_value(pCount);
            ppTxInfo base = new ppTxInfo(ptrToInfo);

            for (int i = count-1; i >= 0 ; i--) {
                pLong temp = new pLong(base.getPtr(base, i * 4));
                long start = core.longp_value(temp);
                TxInfo txi = new TxInfo(start);

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
            Log.d(TAG, "Error: CoreBridge.loadTransactions: "+ Error.getSzDescription());
        }
//        core.ABC_FreeTransactions(aTransactions, tCount);
        return listTransactions;
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
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                mCreationTime = core.longp_value(p);

                tABC_TxDetails txd = super.getPDetails();
                mDetails = new TxDetails(tABC_TxDetails.getCPtr(txd));
                SWIGTYPE_p_p_sABC_TxOutput a = super.getAOutputs();
                mOutputs = new TxOutput[(int) mCountOutputs];
                long base = a.getCPtr(a);
                for (int i = 0; i < mCountOutputs; ++i)
                {
                    mOutputs[i] = new TxOutput(base + i*4);
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
//                mInput = super.getInput();
                mAddress = super.getSzAddress();
                mTxId = super.getSzTxId();
                mValue = get64BitLongAtPtr(pv + 1);
                mIndex = get64BitLongAtPtr(pv + 17);
            }
        }

        public long getmValue() {return mValue; }
        public String getAddress() {return mAddress; }
        public String getTxId() {return mTxId; }
        public long getmIndex() {return mIndex; }

    }

    private class TxDetails extends tABC_TxDetails {
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
        public void setmAmountSatoshi(long mAmountSatoshi) { this.mAmountSatoshi = mAmountSatoshi; }

        public long getmAmountFeesAirbitzSatoshi() { return mAmountFeesAirbitzSatoshi; }
        public void setmAmountFeesAirbitzSatoshi(long mAmountFeesAirbitzSatoshi) { this.mAmountFeesAirbitzSatoshi = mAmountFeesAirbitzSatoshi; }

        public long getmAmountFeesMinersSatoshi() { return mAmountFeesMinersSatoshi; }
        public void setmAmountFeesMinersSatoshi(long mAmountFeesMinersSatoshi) { this.mAmountFeesMinersSatoshi = mAmountFeesMinersSatoshi; }

        public double getmAmountCurrency() { return mAmountCurrency; }
        public void setmAmountCurrency(double mAmountCurrency) { this.mAmountCurrency = mAmountCurrency; }

        public String getmName() { return mName; }
        public void setmName(String mName) { this.mName = mName; }

        public long getmBizId() { return mBizId; }
        public void setmBizId(int mBizId) { this.mBizId = mBizId; }

        public String getmCategory() { return mCategory; }
        public void setmCategory(String mCategory) { this.mCategory = mCategory; }

        public String getmNotes() { return mNotes; }
        public void setmNotes(String mNotes) { this.mNotes = mNotes; }

        public int getmAttributes() { return mAttributes; }
        public void setmAttributes(int mAttributes) { this.mAttributes = mAttributes; }
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
        transaction.setDate(txInfo.getCreationTime());

        transaction.setAmountSatoshi(txInfo.getDetails().getmAmountSatoshi());
        transaction.setABFees(txInfo.getDetails().getmAmountFeesAirbitzSatoshi());
        transaction.setMinerFees(txInfo.getDetails().getmAmountFeesMinersSatoshi());

        transaction.setAmountFiat(txInfo.getDetails().getmAmountCurrency());
        transaction.setWalletName(wallet.getName());
        transaction.setWalletUUID(wallet.getUUID());
        transaction.setConfirmations(3);
        transaction.setConfirmed(false);

        if (!transaction.getName().isEmpty()) {
            transaction.setAddress(transaction.getName());
        } else {
            transaction.setAddress("1zf76dh4TG");
        }
        transaction.setOutputs(txInfo.getOutputs());

    }

    public boolean SaveTransaction(Transaction transaction, tABC_TxDetails details) {
        tABC_Error Error = new tABC_Error();
        tABC_CC results = core.ABC_SetTransactionDetails(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                transaction.getWalletUUID(), transaction.getID(), details, Error);
        return results==tABC_CC.ABC_CC_Ok;
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

    public boolean storeTransaction(Transaction transaction) {
        tABC_Error Error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_sABC_TxDetails pDetails = core.longp_to_ppTxDetails(lp);

        tABC_CC result = core.ABC_GetTransactionDetails(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                transaction.getWalletUUID(), transaction.getID(), pDetails, Error);
        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error: CoreBridge.storeTransaction:  "+Error.getSzDescription());
            return false;
        }

        tABC_TxDetails details = new TxDetails(core.longp_value(lp));

        details.setSzName(transaction.getName());
        details.setSzCategory(transaction.getCategory());
        details.setSzNotes(transaction.getNotes());
        details.setAmountCurrency(transaction.getAmountFiat());

        result = core.ABC_SetTransactionDetails(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                transaction.getWalletUUID(), transaction.getID(), details, Error);

        if (result!=tABC_CC.ABC_CC_Ok)
        {
            Log.d(TAG, "Error: CoreAPI.storeTransaction:  " + Error.getSzDescription());
            return false;
        }

        return true;
    }

    //************************* Currency formatting

    public String formatDefaultCurrency(double in) {
        String pre = mBTCSymbols[mCoreSettings.getBitcoinDenomination().getDenominationType()];
        String out = String.format(" %.3f", in);
        return pre+out;
    }

    public String formatCurrency(double in, int currencyNum, boolean withSymbol) {
        String pre = withSymbol ? mFauxCurrencyDenomination[findCurrencyIndex(currencyNum)] : "";
        String out = String.format(" %.3f", in);
        return pre+out;
    }

    private int findCurrencyIndex(int currencyNum) {
        for(int i=0; i< mFauxCurrencyNumbers.length; i++) {
            if(currencyNum == mFauxCurrencyNumbers[i])
                return i;
        }
        Log.d(TAG, "CurrencyIndex not found, using default");
        return 6; // default US
    }

    public int currencyDecimalPlaces(String label) {
        int decimalPlaces = 5;
        if (label.contains("uBTC"))
            decimalPlaces = 2;
        else if (label.contains("mBTC"))
            decimalPlaces = 3;
        return decimalPlaces;
    }

    public int maxDecimalPlaces() {
        int decimalPlaces = 8; // for ABC_DENOMINATION_BTC
        tABC_AccountSettings settings = loadAccountSettings();
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

    public long cleanNumberString(String value) {
        String out = value.replaceAll("[^a-zA-Z0-9]","");
        return Long.valueOf(out);
    }

    public String formatSatoshi(long amount) {
        return formatSatoshi(amount, true);
    }

    public String formatSatoshi(long amount, boolean withSymbol) {
        return formatSatoshi(amount, withSymbol, -1);
    }

    public String formatSatoshi(long amount, boolean withSymbol, int decimals) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

        int decimalPlaces = maxDecimalPlaces();

        boolean negative = amount < 0;
        if(negative)
            amount = -amount;
        int result = FormatAmount(amount, ppChar.getCPtr(ppChar), decimalPlaces, error.getCPtr(error));
        if ( result != 0)
        {
            return "";
        }
        else {
            String pFormatted = getStringAtPtr(core.longp_value(lp));
            decimalPlaces = decimals > -1 ? decimals : maxDecimalPlaces();
            String pretext = "";
            if (negative) {
                pretext += "-";
            }
            if(withSymbol) {
                pretext += " "+ getUserBTCSymbol();
            }
            return pretext+pFormatted;
        }
    }

    public int SettingsCurrencyIndex() {
        int index = -1;
        tABC_AccountSettings settings = loadAccountSettings();
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
        tABC_AccountSettings settings = loadAccountSettings();
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

    public void SaveCurrencyNumber(int currencyNum) {
        tABC_AccountSettings settings = loadAccountSettings();
        settings.setCurrencyNum(currencyNum);
    }

    public int BitcoinDenominationLabel() {
        tABC_AccountSettings settings = loadAccountSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        return bitcoinDenomination.getDenominationType();
    }

    public String FiatCurrencySign() {
        int index = SettingsCurrencyIndex();
        return mFauxCurrencyDenomination[index];
    }

    public String FiatCurrencyAcronym() {
        int index = SettingsCurrencyIndex();
        return mFauxCurrencyAcronyms[index];
    }

    public long denominationToSatoshi(String amount) {
        int decimalPlaces = maxDecimalPlaces();

        String cleanAmount = amount.replaceAll(",", "");
        return ParseAmount(cleanAmount, decimalPlaces);
    }

    public String BTCtoFiatConversion(int currencyNum) {
        tABC_BitcoinDenomination denomination = mCoreSettings.getBitcoinDenomination();
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
        String currency = FormatDefaultCurrency(satoshi, false, false);
        String currencyLabel = mFauxCurrencyAcronyms[SettingsCurrencyIndex()];
        return "1 "+mBTCDenominations[denomIndex]+" = " + currency + " " + currencyLabel;
    }

    public String FormatDefaultCurrency(long satoshi, boolean btc, boolean withSymbol)
    {
        int currencyNumber = mCoreSettings.getCurrencyNum();
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
            out = formatSatoshi(satoshi, withSymbol);
        }
        return out;
    }

    public double SatoshiToDefaultCurrency(long satoshi) {
        int num = mCoreSettings.getCurrencyNum();
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
        return CurrencyToSatoshi(currency, mCoreSettings.getCurrencyNum());
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

        tABC_BitcoinDenomination denomination = mCoreSettings.getBitcoinDenomination();
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



    public String createReceiveRequestFor(Wallet wallet, String name, String notes, String btc) {
        //first need to create a transaction details struct
        long satoshi = denominationToSatoshi(btc);
        double value = SatoshiToCurrency(satoshi, wallet.getCurrencyNum());

        //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        byte[] txdetailbytes = getBytesAtPtr(details.getCPtr(details), 52);

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
            return getStringAtPtr(core.longp_value(lp));
        }
        else
        {
            String message = result.toString() + "," + error.getSzDescription() + ", " +
                    error.getSzSourceFile()+", "+error.getSzSourceFunc()+", "+error.getNSourceLine();
            Log.d("WalletQRCodeFragment", message);
            return null;
        }
    }

    //this is a blocking call
    public String InitiateTransferOrSend(Wallet sourceWallet, String destinationAddress, long satoshi) {
        String txid = null;
        tABC_Error error = new tABC_Error();
        Wallet destinationWallet = getWallet(destinationAddress);
        if (satoshi > 0) {
            double value = SatoshiToCurrency(satoshi, sourceWallet.getCurrencyNum());

            //creates a receive request.  Returns a requestID.  Caller must free this ID when done with it
            tABC_TxDetails details = new tABC_TxDetails();
            tABC_CC result;

            set64BitLongAtPtr(details.getCPtr(details) + 0, satoshi);

            //the true fee values will be set by the core
            SWIGTYPE_p_int64_t feesAB = core.new_int64_tp();
            set64BitLongAtPtr(feesAB.getCPtr(feesAB), 1700);
            details.setAmountFeesAirbitzSatoshi(feesAB);
            SWIGTYPE_p_int64_t feesMiner = core.new_int64_tp();
            set64BitLongAtPtr(feesAB.getCPtr(feesMiner), 10000);
            details.setAmountFeesMinersSatoshi(feesMiner);

            details.setAmountCurrency(value);
            details.setSzName("Anonymous");
            details.setSzNotes("");
            details.setSzCategory("");
            details.setAttributes(0x2); //for our own use (not used by the core)

            SWIGTYPE_p_long lp = core.new_longp();
            SWIGTYPE_p_p_char pRequestID = core.longp_to_ppChar(lp);
            SWIGTYPE_p_void pVoid = new SWIGTYPE_p_void(pRequestID.getCPtr(pRequestID), false);

            if (destinationWallet.getUUID() != null) {
                tABC_TransferDetails Transfer = new tABC_TransferDetails();
                Transfer.setSzSrcWalletUUID(sourceWallet.getUUID());
                Transfer.setSzSrcName(destinationWallet.getName());
                Transfer.setSzSrcCategory("Transfer:Wallet:" + destinationWallet.getName());

                Transfer.setSzDestWalletUUID(destinationWallet.getUUID());
                Transfer.setSzDestName(sourceWallet.getName());
                Transfer.setSzDestCategory("Transfer:Wallet:" + sourceWallet.getName());

                result = core.ABC_InitiateTransfer(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), Transfer, details, null, pVoid, error);
            } else {
                result = core.ABC_InitiateSendRequest(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                        sourceWallet.getUUID(), destinationAddress, details, null, pVoid, error);
            }
            if (result != tABC_CC.ABC_CC_Ok) {
                Log.d(TAG, "InitiateTransferOrSend:  " + error.getSzDescription() + " " + error.getSzSourceFile() + " " +
                        error.getSzSourceFunc() + " " + error.getNSourceLine());
            } else {
                txid = getStringAtPtr(core.longp_value(lp));
                Log.d(TAG, "TxID:  " + txid);
            }
        } else {
            Log.d(TAG, "Initiate transfer - nothing to send");
        }
        return txid;
    }

    public long calcSendFees(String walletUUID, String sendTo, long sendAmount, boolean transferOnly)
    {
        long totalFees;
        tABC_Error error = new tABC_Error();
        tABC_TxDetails details = new tABC_TxDetails();
        tABC_CC result;

        set64BitLongAtPtr(details.getCPtr(details)+0, sendAmount);
        set64BitLongAtPtr(details.getCPtr(details)+8, 0);
        set64BitLongAtPtr(details.getCPtr(details)+16, 0);

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
            return -1; //TODO is this ok for insufficient funds?
        }
        totalFees = get64BitLongAtPtr(fees.getCPtr(fees));
        return totalFees;
    }

    //*************** Asynchronous Updating
    Handler mPeriodicTaskHandler = new Handler();
    private ExchangeRateSource[] mExchangeRateSources;

    // Callback interface for adding and removing location change listeners
    private List<OnExchangeRatesChange> mExchangeRateObservers = Collections.synchronizedList(new ArrayList<OnExchangeRatesChange>());
    private List<OnExchangeRatesChange> mExchangeRateRemovers = new ArrayList<OnExchangeRatesChange>();

    public interface OnExchangeRatesChange {
        public void OnExchangeRatesChange();
    }

    public void startAllAsyncUpdates() {
        startExchangeRateUpdates();
        startFileSyncUpdates();
    }

    public void stopAllAsyncUpdates() {
        stopExchangeRateUpdates();
        stopFileSyncUpdates();
    }

    final Runnable ExchangeRateUpdater = new Runnable() {
        public void run() {
            mPeriodicTaskHandler.postDelayed(this, 1000 * ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS);
            updateExchangeRates();
        }
    };

    public void updateExchangeRates()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            mUpdateExchangeRateTask = new UpdateExchangeRateTask();

//            Log.d(TAG, "Exchange Rate Update initiated.");
            mUpdateExchangeRateTask.execute();
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

    private void stopExchangeRateUpdates() {
        mPeriodicTaskHandler.removeCallbacks(ExchangeRateUpdater);
    }

    public void startExchangeRateUpdates() {
        if(AirbitzApplication.isLoggedIn()) {
            if(mExchangeRateSources==null) {
                tABC_AccountSettings settings = loadAccountSettings();
                mExchangeRateSources = getExchangeRateSources(settings.getExchangeRateSources());
            }
            mPeriodicTaskHandler.post(ExchangeRateUpdater);
        }
    }

    // Exchange Rate updates may have delay the first call
    public void updateAllExchangeRates()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            tABC_Error error = new tABC_Error();
            for(int i=0; i< mExchangeRateSources.length; i++) {
                ExchangeRateSource source = mExchangeRateSources[i];
                core.ABC_RequestExchangeRateUpdate(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                        source.getCurrencyNum(), null, null, error);
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
            syncAllData();
        }
    };

    private void stopFileSyncUpdates() {
        mPeriodicTaskHandler.removeCallbacks(FileSyncUpdater);
    }

    public void startFileSyncUpdates() {
        if(AirbitzApplication.isLoggedIn()) {
            mPeriodicTaskHandler.post(FileSyncUpdater);
        }
    }

    public void syncAllData()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            mSyncDataTask = new SyncDataTask();

//            Log.d(TAG, "File sync initiated.");
            mSyncDataTask.execute();
        }
    }

    private SyncDataTask mSyncDataTask;
    public class SyncDataTask extends AsyncTask<Void, Void, Void> {
        SyncDataTask() { }

        @Override
        protected Void doInBackground(Void... voids) {
            tABC_Error error = new tABC_Error();
            tABC_CC val = core.ABC_DataSyncAll(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(), error);
            return null;
        }
    }




    //**************** Wallet handling
    public List<Wallet> getCoreWallets() {
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
                in.setTransactions(wi.getTransactions());
                in.setBalanceSatoshi(wi.getBalance());
                in.setUUID(wi.getUUID());
                in.setAttributes(wi.getAttributes());
                in.setCurrencyNum(wi.getCurrencyNum());
                mWallets.add(in);
            }
            core.ABC_FreeWalletInfoArray(core.longp_to_ppWalletinfo(new pLong(ptrToInfo)), count);
            return mWallets;
        } else {
            Log.d(TAG, "getCoreWallets failed.");
        }
        return null;
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
        private long mAttributes;
        private List<Transaction> mTransactions = null;

        public WalletInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                mName = super.getSzName();
                mUUID = super.getSzUUID();
                SWIGTYPE_p_int64_t temp = super.getBalanceSatoshi();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                mBalance = core.longp_value(p);
                mCurrencyNum = super.getCurrencyNum();
                mAttributes = super.getAttributes();
            }
        }

        public String getName() {
            return mName;
        }

        public String getUUID() {
            return mUUID;
        }

        public long getBalance() {return mBalance; }

        public long getAttributes() {return mAttributes; }

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

    public byte[] getQRCode(String uuid, String id) {
        tABC_CC result;
        tABC_Error error = new tABC_Error();

        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_unsigned_char ppChar = core.longp_to_unsigned_ppChar(lp);

        SWIGTYPE_p_int pWidth = core.new_intp();
        SWIGTYPE_p_unsigned_int pUCount = core.int_to_uint(pWidth);

        result = core.ABC_GenerateRequestQRCode(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                uuid, id, ppChar, pUCount, error);

        int width = core.intp_value(pWidth);
        return getBytesAtPtr(core.longp_value(lp), width*width);
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

        tABC_CC result = core.ABC_GetCategories(AirbitzApplication.getUsername(), aszCategories, pUCount, Error);

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

    public void addCategory(String strCategory, List<String> categories) {
        // check and see that it doesn't already exist
        if (categories == null || !categories.contains(strCategory)) {
            // add the category to the core
            Log.d(TAG, "Adding category: "+strCategory);
            tABC_Error Error = new tABC_Error();
            core.ABC_AddCategory(AirbitzApplication.getUsername(), strCategory, Error);
        }
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
        String strSeed = new String();

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
}
