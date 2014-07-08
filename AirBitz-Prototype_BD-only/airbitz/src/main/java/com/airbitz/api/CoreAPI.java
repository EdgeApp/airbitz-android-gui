package com.airbitz.api;

import android.os.Handler;
import android.util.Log;

import com.airbitz.AirbitzApplication;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 6/20/14.
 * This class is a bridge to the ndk core code, acting like a viewmodel
 */
public class CoreAPI {
    private static String TAG = AirbitzAPI.class.getSimpleName();
    private static int ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS = 60;
    public static int ABC_DENOMINATION_BTC = 0;
    public static int ABC_DENOMINATION_MBTC = 1;
    public static int ABC_DENOMINATION_UBTC = 2;

    static {
        System.loadLibrary("abc");
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
    public native String getStringAtPtr(long jarg1);
    public native byte[] getBytesAtPtr(long jarg1, int length);
    public native void int64_tp_assign(long jarg1, long jarg2);
    public native int satoshiToCurrency(String jarg1, String jarg2, long satoshi, long currencyp, int currencyNum, long error);
    public native int setWalletOrder(String jarg1, String jarg2, String[] jarg3, tABC_Error jarg5);
    public native void coreInitialize(String jfile, String jseed, long jseedLength, long jerrorp);
    public native void RegisterAsyncCallback ();

    public void Initialize(String file, String seed, long seedLength){
        tABC_Error error = new tABC_Error();
        RegisterAsyncCallback();
        coreInitialize(file, seed, seedLength, error.getCPtr(error));
    }

    //***************** Callback handling
    public void callbackAsyncBitcoinInfo(long asyncBitCoinInfo_ptr) {
        tABC_AsyncBitCoinInfo info = new tABC_AsyncBitCoinInfo(asyncBitCoinInfo_ptr, false);
        tABC_AsyncEventType type = info.getEventType();
        if(type==tABC_AsyncEventType.ABC_AsyncEventType_IncomingBitCoin) {
            if(mOnIncomingBitcoin!=null) {
                mIncomingBitcoinUUID = info.getSzWalletUUID();
                mIncomingBitcoinTxID = info.getSzTxID();
                handler.post(IncomingBitcoinUpdater);
            }
            else
                Log.d("CoreAPI", "incoming bitcoin event has no listener");
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_BlockHeightChange) {
            if(mOnBlockHeightChange!=null)
                handler.post(BlockHeightUpdater);
            else
                Log.d("CoreAPI", "block exchange event has no listener");
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_ExchangeRateUpdate) {
            Log.d("CoreAPI", "exchange rate update event");
            if(mOnExchangeRateUpdate!=null)
                handler.post(ExchangeUpdater);
            else
                Log.d("CoreAPI", "exchange rate event has no listener");
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


    // Callback interface when an exchange rate update is received
    private OnExchangeRateUpdate mOnExchangeRateUpdate;
    public interface OnExchangeRateUpdate {
        public void onExchangeRateUpdate();
    }
    public void setOnExchangeRateUpdateListener(OnExchangeRateUpdate listener) {
        mOnExchangeRateUpdate = listener;
    }
    final Runnable ExchangeUpdater = new Runnable() {
        public void run() { mOnExchangeRateUpdate.onExchangeRateUpdate(); }
    };

    //***************** Wallet handling
    private static final int WALLET_ATTRIBUTE_ARCHIVE_BIT = 0x0; // BIT0 is the archive bit

    public List<Wallet> loadWallets() {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets();

        if(coreList==null)
            coreList = new ArrayList<Wallet>();
        list.add(new Wallet("xkmODCMdsokmKOSDnvOSDvnoMSDMSsdcslkmdcwlksmdcL", "Hello"));//Wallet HEADER
        // Loop through and find non-archived wallets first
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & (1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT)) != 1)
                list.add(wallet);
        }
        list.add(new Wallet("SDCMMLlsdkmsdclmLSsmcwencJSSKDWlmckeLSDlnnsAMd", "Goodbye")); //Archive HEADER
        // Loop through and find archived wallets now
        for (Wallet wallet : coreList) {
            if ((wallet.getAttributes() & (1 << CoreAPI.WALLET_ATTRIBUTE_ARCHIVE_BIT)) == 1)
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
            Log.d("CoreAPI", "Create wallet failed - "+pError.getSzDescription()+", at "+pError.getSzSourceFunc());
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
        wallet.setBalance(info.getBalance());
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
            Wallet wallet = new Wallet(info.getName(), "");
            wallet.setName(info.getName());
            wallet.setUUID(info.getUUID());
            wallet.setAttributes(info.getAttributes());
            wallet.setBalance(info.getBalance());
            wallet.setCurrencyNum(info.getCurrencyNum());
            wallet.setTransactions(getTransactions(wallet.getName()));

            return wallet;
        }
        else
        {
            Log.d("", "Error: CoreBridge.getWallet: " + Error.getSzDescription());
            return null;
        }
    }

    public void setPint64_t(SWIGTYPE_p_int64_t p, long value) {
        int64_tp_assign(SWIGTYPE_p_int64_t.getCPtr(p), value);
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
            Log.d("CoreAPI", "Error: CoreBridge.setWalletOrder" + Error.getSzDescription());
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
                Log.d("CoreAPI", "Error: CoreBridge.setWalletAttributes: "+ Error.getSzDescription());
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

    public String getUserBTCDenomination() {
        tABC_AccountSettings settings = loadAccountSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination == null) {
            Log.d("CoreAPI", "Bad bitcoin denomination from core settings");
            return "";
        }
        return mBTCDenominations[bitcoinDenomination.getDenominationType()];
    }

    public String getUserBTCSymbol() {
        tABC_AccountSettings settings = loadAccountSettings();
        tABC_BitcoinDenomination bitcoinDenomination = settings.getBitcoinDenomination();
        if(bitcoinDenomination == null) {
            Log.d("CoreAPI", "Bad bitcoin denomination from core settings");
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
            return mCoreSettings;
        } else {
            String message = Error.getSzDescription()+", "+Error.getSzSourceFunc();
            Log.d("CoreAPI", "Load settings failed - "+message);
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

    private class QuestionChoice extends tABC_QuestionChoice {
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
            Log.d("CoreAPI", "Could not find wallet for "+ walletUUID);
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
            Log.d("CoreAPI", "Error: CoreBridge.loadTransactions: "+ Error.getSzDescription());
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

            for (int i = count -1; i >0 ; --i) {
                pLong temp = new pLong(base.getPtr(base, i * 4));
                long start = core.longp_value(temp);
                TxInfo txi = new TxInfo(start);

                Transaction in = new Transaction(wallet.getUUID(), txi.getID(),
                        txi.getCreationTime(), wallet.getName(),
                        wallet.getAmount(), // need address?
                        wallet.getAmount(), // need category?
                        wallet.getAmount(), // need notes?
                        txi.getAddresses());

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
            Log.d("CoreAPI", "Error: CoreBridge.loadTransactions: "+ Error.getSzDescription());
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
        long mCountAddresses;
        long mCreationTime;
        private TxDetails mDetails;
        private String[] mAddresses;

        public TxInfo(long pv) {
            super(pv, false);
            if (pv != 0) {
                mID = super.getSzID();
                mCountAddresses = super.getCountOutputs();
                SWIGTYPE_p_int64_t temp = super.getTimeCreation();
                SWIGTYPE_p_long p = core.p64_t_to_long_ptr(temp);
                mCreationTime = core.longp_value(p);

                tABC_TxDetails txd = super.getPDetails();
                mDetails = new TxDetails(tABC_TxDetails.getCPtr(txd));
                SWIGTYPE_p_p_sABC_TxOutput a = super.getAOutputs();

                mAddresses = new String[(int) mCountAddresses];
                long base = a.getCPtr(a);
                for (int i = 0; i < mCountAddresses; ++i)
                {
                    mAddresses[i] = getStringAtPtr(base + i*4);
                }
            }
        }

        public String getID() { return mID; }
        public long getCount() { return mCountAddresses; }
        public long getCreationTime() { return mCreationTime; }
        public TxDetails getDetails() {return mDetails; }
        public String[] getAddresses() {return mAddresses; }
    }

    private class TxDetails extends tABC_TxDetails {
        long mAmountSatoshi; /** amount of bitcoins in satoshi (including fees if any) */
        long mAmountFeesAirbitzSatoshi;   /** airbitz fees in satoshi */
        long mAmountFeesMinersSatoshi;  /** miners fees in satoshi */
        double mAmountCurrency;  /** amount in currency */
        String mName;   /** payer or payee */
        int mBizId; /** payee business-directory id (0 otherwise) */
        String mCategory;   /** category for the transaction */
        String mNotes;  /** notes for the transaction */
        int mAttributes;    /** attributes for the transaction */

        public TxDetails(long pv) {
            super(pv, false);
            if (pv != 0) {
                mAmountSatoshi = core.longp_value(core.p64_t_to_long_ptr(super.getAmountSatoshi()));
                mAmountFeesAirbitzSatoshi = core.longp_value(core.p64_t_to_long_ptr(super.getAmountFeesAirbitzSatoshi()));
                mAmountFeesMinersSatoshi = core.longp_value(core.p64_t_to_long_ptr(super.getAmountFeesMinersSatoshi()));
                mAmountCurrency = super.getAmountCurrency();
                mName = super.getSzName();
                mBizId = (int) super.getBizId();
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

        public int getmBizId() { return mBizId; }
        public void setmBizId(int mBizId) { this.mBizId = mBizId; }

        public String getmCategory() { return mCategory; }
        public void setmCategory(String mCategory) { this.mCategory = mCategory; }

        public String getmNotes() { return mNotes; }
        public void setmNotes(String mNotes) { this.mNotes = mNotes; }

        public int getmAttributes() { return mAttributes; }
        public void setmAttributes(int mAttributes) { this.mAttributes = mAttributes; }
    }


    public void setTransaction(Wallet wallet, Transaction transaction, TxInfo txInfo) {
        transaction.setID(txInfo.getID());
        transaction.setName(txInfo.getDetails().getSzName());
        transaction.setNotes(txInfo.getDetails().getSzNotes());
        transaction.setCategory(txInfo.getDetails().getSzCategory());
        transaction.setDate(txInfo.getCreationTime());
        transaction.setAmountSatoshi(txInfo.getDetails().getmAmountSatoshi());
        transaction.setAmountFiat(txInfo.getDetails().getmAmountCurrency());
        transaction.setABFees(txInfo.getDetails().getmAmountFeesAirbitzSatoshi());
        transaction.setMinerFees(txInfo.getDetails().getmAmountFeesMinersSatoshi());
        transaction.setWalletName(wallet.getName());
        transaction.setWalletUUID(wallet.getUUID());
        transaction.setConfirmations(3);
        transaction.setConfirmed(false);

        if (!transaction.getName().isEmpty()) {
            transaction.setAddress(transaction.getName());
        } else {
            transaction.setAddress("1zf76dh4TG");
        }
        transaction.setBTCAddresses(txInfo.getAddresses());

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
        if (result!=tABC_CC.ABC_CC_Ok)
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
            Log.i("CoreAPI", "Error: CoreBridge.searchTransactionsIn: "+Error.getSzDescription());
        }
//        ABC_FreeTransactions(aTransactions, tCount);
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
            Log.d("CoreAPI", "Error: CoreBridge.storeTransaction:  "+Error.getSzDescription());
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
            Log.d("CoreAPI", "Error: CoreBridge.storeTransaction:  " + Error.getSzDescription());
            return false;
        }

        return true;
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
        return formatSatoshi(amount, withSymbol, 3);
    }

    public String formatSatoshi(long amount, boolean withSymbol, int decimals) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_long lp = core.new_longp();
        SWIGTYPE_p_p_char ppChar = core.longp_to_ppChar(lp);

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
            Log.d("CoreAPI", "currency index out of bounds "+index);
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

    public String BTCtoFiatStringConversion() {
        String currency = FormatString(100000000, false);
        int index = SettingsCurrencyIndex();
        String currencyLabel = mFauxCurrencyAcronyms[index];
        return "1.00 BTC = " + currency + " " + currencyLabel; //[NSString stringWithFormat:@"1.00 %@ = $%.2f %@", denominationLabel, currency, currencyLabel];
    }

    public String FormatString(long satoshi, boolean btc)
    {
        if (!btc)
        {
            tABC_AccountSettings settings = loadAccountSettings();
            int currencyNumber = settings.getCurrencyNum();

            return formatCurrency(SatoshiToCurrency(satoshi, currencyNumber));
        }
        else // currency
        {
            return formatSatoshi(satoshi);
        }
    }

    public double SatoshiToCurrency(long satoshi, int currencyNum) {
        tABC_Error error = new tABC_Error();
        SWIGTYPE_p_double currency = core.new_doublep();

        long out = satoshiToCurrency(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                satoshi, SWIGTYPE_p_double.getCPtr(currency), currencyNum, tABC_Error.getCPtr(error));

        return core.doublep_value(currency);
    }

    //*************** Exchange Rate
    Handler handler = new Handler();
    private ExchangeRateSource[] mExchangeRateSources;
    final Runnable ExchangeRateUpdater = new Runnable() {
        public void run() {
            handler.postDelayed(this, 1000*ABC_EXCHANGE_RATE_REFRESH_INTERVAL_SECONDS); // rerun every this many millis
            requestExchangeRateUpdate();
        }
    };

    private void stopExchangeRateUpdates() {
        handler.removeCallbacks(ExchangeRateUpdater);
    }

    public void startExchangeRateUpdates() {
        if(AirbitzApplication.isLoggedIn()) {
            if(mExchangeRateSources==null) {
                tABC_AccountSettings settings = loadAccountSettings();
                mExchangeRateSources = getExchangeRateSources(settings.getExchangeRateSources());
            }
            handler.post(ExchangeRateUpdater);
        }
    }

    // Exchange Rate updates comes in asynchronously
    public void requestExchangeRateUpdate()
    {
        if (AirbitzApplication.isLoggedIn())
        {
            tABC_Error error = new tABC_Error();
            // Check the default currency for updates
            for(ExchangeRateSource source : mExchangeRateSources) {
                core.ABC_RequestExchangeRateUpdate(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                        source.getCurrencyNum(), null, null, error);
            }
        }
    }


    //**************** Wallet handling
    private List<Wallet> getCoreWallets() {
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
                Wallet in = new Wallet(wi.getName(), "");
                in.setBalance(wi.getBalance());
                in.setUUID(wi.getUUID());
                in.setAttributes(wi.getAttributes());
                in.setCurrencyNum(wi.getCurrencyNum());
                in.setTransactions(wi.getTransactions());
                mWallets.add(in);
            }
            core.ABC_FreeWalletInfoArray(core.longp_to_ppWalletinfo(new pLong(ptrToInfo)), count);
            return mWallets;
        } else {
            Log.d("CoreAPI", "getCoreWallets failed.");
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
                //TODO transactions here?
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

}
