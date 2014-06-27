package com.airbitz.api;

import android.accounts.Account;
import android.util.Log;

import com.airbitz.AirbitzApplication;
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
            if(mOnIncomingBitcoin!=null)
                mOnIncomingBitcoin.onIncomingBitcoin(info.getSzWalletUUID(), info.getSzTxID());
            else
                Log.d("CoreAPI", "incoming bitcoin event has no listener");
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_BlockHeightChange) {
            if(mOnBlockHeightChange!=null)
                mOnBlockHeightChange.onBlockHeightChange();
            else
                Log.d("CoreAPI", "block exchange event has no listener");
        } else if (type==tABC_AsyncEventType.ABC_AsyncEventType_ExchangeRateUpdate) {
            Log.d("CoreAPI", "exchange rate update event");
            if(mOnExchangeRateUpdate!=null)
                mOnExchangeRateUpdate.onExchangeRateUpdate();
            else
                Log.d("CoreAPI", "exchange rate event has no listener");
        }
    }

    // Callback interface when an incoming bitcoin is received
    private OnIncomingBitcoin mOnIncomingBitcoin;
    public interface OnIncomingBitcoin {
        public void onIncomingBitcoin(String walletUUID, String txId);
    }
    public void setOnIncomingBitcoinListener(OnIncomingBitcoin listener) {
        mOnIncomingBitcoin = listener;
    }

    // Callback interface when a block height change is received
    private OnBlockHeightChange mOnBlockHeightChange;
    public interface OnBlockHeightChange {
        public void onBlockHeightChange();
    }
    public void setOnBlockHeightChangeListener(OnBlockHeightChange listener) {
        mOnBlockHeightChange = listener;
    }

    // Callback interface when an exchange rate update is received
    private OnExchangeRateUpdate mOnExchangeRateUpdate;
    public interface OnExchangeRateUpdate {
        public void onExchangeRateUpdate();
    }
    public void setOnExchangeRateUpdateListener(OnExchangeRateUpdate listener) {
        mOnExchangeRateUpdate = listener;
    }

    //***************** Wallet handling
    private static final int WALLET_ATTRIBUTE_ARCHIVE_BIT = 0x0; // BIT0 is the archive bit

    public List<Wallet> loadWallets() {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = getCoreWallets();

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
    public boolean createWallet(String walletName, String username, String password, int dollarNum) {
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pResults = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pResults);

        tABC_CC result = core.ABC_CreateWallet(username, password, walletName, dollarNum, 0, null, pVoid, pError);
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

    //************ Transaction handling
    public AccountTransaction getTransaction(String walletUUID, String szTxId)
    {
        tABC_Error Error = new tABC_Error();
        AccountTransaction transaction = null;

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
            transaction = new AccountTransaction();
            setTransaction(wallet, transaction, txInfo);
            core.ABC_FreeTransaction(txInfo);
        }
        else
        {
            Log.d("CoreAPI", "Error: CoreBridge.loadTransactions: "+ Error.getSzDescription());
        }
        return transaction;
    }

    public List<AccountTransaction> loadTransactions(Wallet wallet) {
        List<AccountTransaction> listTransactions = new ArrayList<AccountTransaction>();
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

                AccountTransaction in = new AccountTransaction(wallet.getUUID(), txi.getID(),
                        txi.getCreationTime(), wallet.getName(),
                        wallet.getAmount(), // need address?
                        wallet.getAmount(), // need category?
                        wallet.getAmount(), // need notes?
                        txi.getAddresses());

                listTransactions.add(in);
            }
            long bal = 0;
            for (AccountTransaction at : listTransactions)
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


    public void setTransaction(Wallet wallet, AccountTransaction transaction, TxInfo txInfo) {
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

    public List<AccountTransaction> searchTransactionsIn(Wallet wallet, String searchText, List<AccountTransaction> existing) {
        List<AccountTransaction> listTransactions = existing;
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

                AccountTransaction transaction = new AccountTransaction();
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

    public boolean storeTransaction(AccountTransaction transaction) {
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

    public String SatoshiToCurrencyString(long satoshi) {
        String currency = conversion(satoshi, false);
        String denominationLabel = "BTC"; //[User Singleton].denominationLabel;
        String currencyLabel = "USD";
        return "1.00 " + denominationLabel + " = " + currency + " " + currencyLabel; //[NSString stringWithFormat:@"1.00 %@ = $%.2f %@", denominationLabel, currency, currencyLabel];
    }

    public String conversion(long satoshi, boolean btc)
    {
        if (!btc)
        {
            tABC_Error error = new tABC_Error();
            SWIGTYPE_p_double currency = core.new_doublep();

            long out = satoshiToCurrency(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                    satoshi, SWIGTYPE_p_double.getCPtr(currency), SignUpActivity.DOLLAR_CURRENCY_NUMBER, tABC_Error.getCPtr(error));

            return formatCurrency(core.doublep_value(currency));
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
        private List<AccountTransaction> mTransactions = null;

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

        public List<AccountTransaction> getTransactions() {return mTransactions; }
    }

    private class pLong extends SWIGTYPE_p_long {
        public pLong(long ptr) {
            super(ptr, false);
        }
    }

    /*
     * Account Transaction handling
     */
    public static List<AccountTransaction> getTransactions(String walletName) {
        // TODO replace with API call
        List<AccountTransaction> list = new ArrayList<AccountTransaction>();
//        list.add(new AccountTransaction("Matt Kemp", "DEC 10", "B25.000", "-B5.000"));
//        list.add(new AccountTransaction("John Madden", "DEC 15", "B30.000", "-B65.000"));
//        list.add(new AccountTransaction("kelly@gmail.com", "NOV 1", "B95.000", "B95.000"));

        return list;
    }

}
