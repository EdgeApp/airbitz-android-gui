package com.airbitz.api;

import android.util.Log;

import com.airbitz.models.AccountTransaction;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Categories;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.Wallet;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 2/13/14.
 */

public class AirbitzAPI {

    static {
        System.loadLibrary("airbitz");
    }

    private static AirbitzAPI mInstance = null;

    private static String TAG = AirbitzAPI.class.getSimpleName();

    private static final String SERVER_ROOT = "https://api.airbitz.co/";
    private static final String API_PATH = SERVER_ROOT + "api/v1/";
    private static final String API_SEARCH = API_PATH + "search/";
    private static final String API_BUSINESS = API_PATH + "business/";
    private static final String API_LOCATION_SUGGEST = API_PATH + "location-suggest/";
    private static final String API_CATEGORIES = API_PATH + "categories/";
    private static final String API_AUTO_COMPLETE_LOCATION = API_PATH + "autocomplete-location/";
    private static final String API_AUTO_COMPLETE_BUSINESS = API_PATH + "autocomplete-business/";

    public static AirbitzAPI getApi(){
        if(mInstance == null){
            mInstance = new AirbitzAPI();
        }
        return mInstance;
    }

    private AirbitzAPI(){
    }

    public static String getServerRoot(){
        return SERVER_ROOT;
    }

    public String getRequest(String url){
        return getRequest(url,"");
    }

    public static String getRequest(String url, String params) {
        StringBuffer stringBuffer = new StringBuffer("");
        BufferedReader bufferedReader = null;
        Log.d(TAG, url + params.toString());
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet();

            URI uri = new URI(url+params);
            httpGet.setURI(uri);
            String token = "b24805c59bf8ded704c659de3aa1be966f3065bc";
            httpGet.addHeader("Authorization", "Token " + token + "");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                stringBuffer.append(readLine);
                stringBuffer.append("\n");
                readLine = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String temp = stringBuffer.toString();
        if(temp == null){
            temp = "{}";
        }
        return temp;
    }

    public static String createURLParams(List<NameValuePair> params){
        String result = "";
        if(params.size() > 0){
            result += "?";
            for(int index = 0; index < params.size();index++){
                NameValuePair keyVal = params.get(index);
                if(keyVal.getValue() != null && keyVal.getValue() != "" ){
                    if(index > 0){
                        result += "&";
                    }
                    String paramKey = keyVal.getName();
                    String paramVal = keyVal.getValue().replace(" ", "%20");
                    result +=paramKey + "=" + paramVal;
                }
            }
        }

        return result;
    }


    public String getSearchByTerm(String term, String page_size, String page, String sort){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("term", term));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getNextPage(String url){
        return getRequest(url,"");
    }

    public String getSearchByLocation(String location, String page_size, String page, String sort){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("location", location));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByLatLong(String latlong, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", latlong));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByLatLongAndBusiness(String latlong, String businessName, String category, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", latlong));

        if(businessName.length()>0){
            params.add(new BasicNameValuePair("term", businessName));
        }
        if(category.length()>0){
            params.add(new BasicNameValuePair("category", category));
        }

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByRadius(String radius, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("radius", radius));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByBounds(String bounds, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("bounds", bounds));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByBoundsAndBusiness(String bounds, String businessName, String category, String ll, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("bounds", bounds));

        if(businessName.length()>0){
            params.add(new BasicNameValuePair("term", businessName));
        }

        if(ll.length()>0){
            params.add(new BasicNameValuePair("ll", ll));
        }

        if(category.length()>0){
            params.add(new BasicNameValuePair("category", category));
        }

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByCategory(String category, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("category", category));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public BusinessDetail getHttpBusiness(int bizId){

        String url = API_BUSINESS+bizId +"/";

        String response = getRequest(url, null);

        try{
            return new BusinessDetail(new JSONObject(response));
        }catch (JSONException e){
            Log.e(TAG, ""+e.getMessage());
        }catch (Exception e){
            Log.e(TAG, ""+e.getMessage());
        }
        return null;
    }


    public String getBusinessPhoto(String bizId){

        return getRequest(API_BUSINESS+bizId +"/photos/", "");
    }


    public String getHttpLocationSuggest(String ll){

        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", ll));

        String response = getRequest(API_LOCATION_SUGGEST, createURLParams(params));
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("near");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    public Categories getHttpCategories(String sort){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("sort", sort));

        String response =  getRequest(API_CATEGORIES, createURLParams(params));
        try{
            return new Categories(new JSONObject(response));
        }catch (JSONException e){
            Log.e(TAG, ""+e.getMessage());
        }catch (Exception e){
            Log.e(TAG, ""+e.getMessage());
        }
        return null;
    }


    public List<LocationSearchResult> getHttpAutoCompleteLocation(String term, String ll){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        List<LocationSearchResult> resultList = new ArrayList<LocationSearchResult>();

        if(term.length() > 0){

            params.add(new BasicNameValuePair("term", term));
        }

        if(ll.length()>0){
            params.add(new BasicNameValuePair("ll", ll));
        }

        String response =  getRequest(API_AUTO_COMPLETE_LOCATION, createURLParams(params));
        try{
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse != null){
                JSONArray results = jsonResponse.getJSONArray("results");
                for(int index = 0; index<results.length();index ++){
                    final String locationName = results.getString(index);
                    resultList.add(new LocationSearchResult(locationName, false));
                }
            }
        }catch (JSONException e){
            Log.d(TAG, ""+e.getMessage());
        } catch(Exception e){
            e.printStackTrace();
        }

        return resultList;

    }

    public List<Business> getHttpAutoCompleteBusiness(String term, String location, String ll){

        List<NameValuePair> params = new ArrayList<NameValuePair>();


        if(term.length()>0){
            params.add(new BasicNameValuePair("term", term));
        }

        if(location.length() > 0){
            params.add(new BasicNameValuePair("location", location));
        }

        if(ll.length() > 0){
            params.add(new BasicNameValuePair("ll", ll));
        }


        String response = getRequest(API_AUTO_COMPLETE_BUSINESS, createURLParams(params));
        try{
            JSONObject jsonResponse = new JSONObject(response);
            return Business.generateBusinessObjectListFromJSON(jsonResponse.getJSONArray("results"));
        }catch (JSONException e){
            Log.e(TAG, "" + e.getMessage());
        }catch (Exception e){
            Log.e(TAG, "" + e.getMessage());
        }
        return null;
    }

    public String getBusinessById(String businessId){
        return getRequest(API_BUSINESS+businessId,"");
    }

    public String getBusinessByIdAndLatLong(String businessId, String ll){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", ll));
        return getRequest(API_BUSINESS+businessId+"/",createURLParams(params));
    }

    public String getSearchByCategoryAndLocation(String category, String location, String page_size, String page, String sort){
        //parse to SearchResult
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("category", category));
        params.add(new BasicNameValuePair("location", location));

        if(page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByCategoryOrBusinessAndLocation(String name, String location, String page_size, String page, String sort, String businessFlag, String ll){
        //parse to SearchResult
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        if(location.length()>0){
            params.add(new BasicNameValuePair("location", location));
        }
        if(ll.length()>0){
            params.add(new BasicNameValuePair("ll", ll));
        }
        if(businessFlag.equalsIgnoreCase("business")){
            params.add(new BasicNameValuePair("term", name));
        }
        else if(businessFlag.equalsIgnoreCase("category")){
            params.add(new BasicNameValuePair("category", name));
        }

        if(page_size.length() > 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }

        if(page.length() > 0){
            params.add(new BasicNameValuePair("page", page));
        }

        if(sort.length() > 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    /*
        Get wallets with their transactions
     */
    public static List<Wallet> getWallets() {
        // TODO replace with API call
        List<Wallet> list = new ArrayList<Wallet>();
        list.add(new Wallet("Baseball Team", "B15.000"));
        list.add(new Wallet("Fantasy Football", "B10.000"));
        list.add(new Wallet("Shared", "B0.000"));
        list.add(new Wallet("Mexico", "B0.000"));
        list.add(new Wallet("Alpha Centauri", "B0.000"));
        list.add(new Wallet("Other", "B0.000"));
        return list;
    }

    /*
        Get wallets with their transactions
     */
    public static List<AccountTransaction> getTransactions(String walletName) {
        // TODO replace with API call
        List<AccountTransaction> list = new ArrayList<AccountTransaction>();
        list.add(new AccountTransaction("Matt Kemp","DEC 10","B25.000", "-B5.000"));
        list.add(new AccountTransaction("John Madden","DEC 15","B30.000", "-B65.000"));
        list.add(new AccountTransaction("kelly@gmail.com", "NOV 1", "B95.000", "-B95.000"));

        return list;
    }



    /***********************************************************************************************
     * Core library handling
     */

    /**
     * AirBitz Core Error Structure
     *
     * This structure contains the detailed information associated
     * with an error.
     * Most AirBitz Core functions should offer the option of passing
     * a pointer to this structure to be filled out in the event of
     * error.
     *
     */
    class ABCError
    {
        /** The condition code code */
        int code;
        /** String containing a description of the error */
        String szDescription;
        /** String containing the function in which the error occurred */
        String szSourceFunc;
        /** String containing the source file in which the error occurred */
        String szSourceFile;
        /** Line number in the source file in which the error occurred */
        int  nSourceLine;
    };

    /**
     * AirBitz Currency Structure
     *
     * This structure contains the id's and names of all the currencies.
     *
     */
    class ABCCurrency
    {
        /** currency ISO 4217 code */
        String szCode;
        /** currency ISO 4217 num */
        int     num;
        /** currency description */
        String szDescription;
        /** currency countries */
        String szCountries;
    };

    /**
     * AirBitz Core Wallet Structure
     *
     * This structure contains wallet information.
     * All AirBitz Core functions should offer the
     *
     */
    class ABCWalletInfo
    {
        /** wallet UUID */
        String szUUID;
        /** wallet name */
        String szName;
        /** account associated with this wallet */
        String szUserName;
        /** wallet ISO 4217 currency code */
        int             currencyNum;
        /** wallet attributes */
        int    attributes;
        /** wallet balance */
        long         balanceSatoshi;
    };

    /**
     * AirBitz Question Choice Structure
     *
     * This structure contains a recovery question choice.
     *
     */
    class ABCQuestionChoice
    {
        /** question */
        String szQuestion;
        /** question category */
        String szCategory;
        /** miniumum length of an answer for this question */
        int    minAnswerLength;
    };

    /**
     * AirBitz Question Choices Structure
     *
     * This structure contains a recovery question choices.
     *
     */
    class ABCQuestionChoices
    {
        /** number of choices */
        int        numChoices;
        /** array of choices */
        ABCQuestionChoice[] aChoices;
    };

    /**
     * AirBitz Bitcoin URI Elements
     *
     * This structure contains elements in
     * a Bitcoin URI
     *
     */
    class ABCBitcoinURIInfo
    {
        /** label for that address (e.g. name of receiver) */
        String szLabel;
        /** bitcoin address (base58) */
        String szAddress;
        /** message that shown to the user after scanning the QR code */
        String szMessage;
        /** amount of bitcoins */
        long amountSatoshi;
    };

    /**
     * AirBitz Transaction Details
     *
     * This structure contains details for transactions.
     * It is used in both transactions and transaction
     * requests.
     *
     */
    class ABCTxDetails
    {
        /** amount of bitcoins in satoshi (including fees if any) */
        long amountSatoshi;
        /** airbitz fees in satoshi */
        long amountFeesAirbitzSatoshi;
        /** miners fees in satoshi */
        long amountFeesMinersSatoshi;
        /** amount in currency */
        double amountCurrency;
        /** payer or payee */
        String szName;
        /** category for the transaction */
        String szCategory;
        /** notes for the transaction */
        String szNotes;
        /** attributes for the transaction */
        int attributes;
    };

    /**
     * AirBitz Transaction Info
     *
     * This structure contains info for a transaction.
     *
     */
    class ABCTxInfo
    {
        /** transaction identifier */
        String szID;
        /** time of creation */
        long timeCreation;
        /** count of bitcoin addresses associated with this transaciton */
        int countAddresses;
        /** bitcoin addresses associated with this transaction */
        String[] aAddresses;
        /** transaction details */
        ABCTxDetails pDetails;
    };

    /**
     * AirBitz Request Info
     *
     * This structure contains info for a request.
     *
     */
    class ABCRequestInfo
    {
        /** request identifier */
        String szID;
        /** time of creation */
        long timeCreation;
        /** request details */
        ABCTxDetails pDetails;
        /** satoshi amount in address */
        long amountSatoshi;
        /** satoshi still owed */
        long owedSatoshi;
    };

    /**
     * AirBitz Password Rule
     *
     * This structure contains info for a password rule.
     * When a password is checked, an array of these will
     * be delivered to explain what has an hasn't passed
     * for password requirements.
     *
     */
    class ABCPasswordRule
    {
        /** description of the rule */
        String szDescription;
        /** has the password passed this requirement */
        boolean bPassed;
    };

    /**
     * AirBitz Exchange Rate Source
     *
     * This structure contains the exchange rate
     * source to use for a currencies.
     *
     */
    class ABCExchangeRateSource
    {
        /** ISO 4217 currency code */
        int                         currencyNum;
        /** exchange rate source */
        String szSource;
    };

    /**
     * AirBitz Exchange Rate Sources
     *
     * This structure contains the exchange rate
     * sources to use for different currencies.
     *
     */
    class ABCExchangeRateSources
    {
        /** number of sources */
        int numSources;
        /** array of exchange rate sources */
        ABCExchangeRateSource[] aSources;
    };

    /**
     * AirBitz Bitcoin Denomination
     *
     * This structure contains the method for
     * displaying bitcoin.
     *
     */
    class ABCBitcoinDenomination
    {
        /** label (e.g., mBTC) */
        String szLabel;
        /** number of satoshi per unit (e.g., 100,000) */
        long satoshi;
    };

    /**
     * AirBitz Account Settings
     *
     * This structure contains the user settings
     * for an account.
     *
     */
    class ABCAccountSettings
    {
        /** first name (optional) */
        String szFirstName;
        /** last name (optional) */
        String szLastName;
        /** nickname (optional) */
        String szNickname;
        /** should name be listed on payments */
        boolean                        bNameOnPayments;
        /** how many minutes before auto logout */
        int                         minutesAutoLogout;
        /** language (ISO 639-1) */
        String szLanguage;
        /** default ISO 4217 currency code */
        int                         currencyNum;
        /** bitcoin exchange rate sources */
        ABCExchangeRateSources    exchangeRateSources;
        /** how to display bitcoin denomination */
        ABCBitcoinDenomination    bitcoinDenomination;
        /** use advanced features (e.g., allow offline wallet creation) */
        boolean                        bAdvancedFeatures;
    };



    // Implement this interface to see the initialize callback
    public interface BitcoinEventCallback {
        void onBitcoinEvent();
    }

    // Implement this interface to see the initialize callback
    public interface RequestCallback {
        void onRequest();
    }

    public native int ABCInitialize(
            String rootDir,
            BitcoinEventCallback fAsyncBitCoinEventCallback,
            AirbitzAPI pData,
            char[] pSeedData,
            int seedLength,
            ABCError pError);

    public native void ABCTerminate();

    public native int ABCClearKeyCache(ABCError pError);

    public native int ABCSignIn(String szUserName,
                       String szPassword,
                       RequestCallback fRequestCallback,
                       AirbitzAPI pData,
                       ABCError pError);

    public native int ABCCreateAccount(String szUserName,
                              String szPassword,
                              String szPIN,
                              RequestCallback fRequestCallback,
                              AirbitzAPI pData,
                              ABCError pError);

    public native int ABCSetAccountRecoveryQuestions(String szUserName,
                                            String szPassword,
                                            String szRecoveryQuestions,
                                            String szRecoveryAnswers,
                                            RequestCallback fRequestCallback,
                                            AirbitzAPI pData,
                                            ABCError pError);

    public native int ABCCreateWallet(String szUserName,
                             String szPassword,
                             String szWalletName,
                             int currencyNum,
                             int attributes,
                             RequestCallback fRequestCallback,
                             AirbitzAPI pData,
                             ABCError pError);

    public native int ABCGetCurrencies(ABCCurrency[] paCurrencyArray,
                              int pCount,
                              ABCError pError);

    public native int ABCGetPIN(String szUserName,
                       String szPassword,
                       String[] pszPIN,
                       ABCError pError);

    public native int ABCSetPIN(String szUserName,
                       String szPassword,
                       String szPIN,
                       ABCError pError);

    public native int ABCGetCategories(String szUserName,
                              String[][] paszCategories,
                              int pCount,
                              ABCError pError);

    public native int ABCAddCategory(String szUserName,
                            String szCategory,
                            ABCError pError);

    public native int ABCRemoveCategory(String szUserName,
                               String szCategory,
                               ABCError pError);

    public native int ABCRenameWallet(String szUserName,
                             String szPassword,
                             String szUUID,
                             String szNewWalletName,
                             ABCError pError);

    public native int ABCSetWalletAttributes(String szUserName,
                                    String szPassword,
                                    String szUUID,
                                    int attributes,
                                    ABCError pError);

    public native int ABCCheckRecoveryAnswers(String szUserName,
                                     String szRecoveryAnswers,
                                     boolean pbValid,
                                     ABCError pError);

    public native int ABCGetWalletInfo(String szUserName,
                              String szPassword,
                              String szUUID,
                              ABCWalletInfo[] ppWalletInfo,
                              ABCError pError);

    public native void ABCFreeWalletInfo(ABCWalletInfo pWalletInfo);

    public native int ABCGetWallets(String szUserName,
                           String szPassword,
                           ABCWalletInfo[][] paWalletInfo,
                           int pCount,
                           ABCError pError);

    public native void ABCFreeWalletInfoArray(ABCWalletInfo[] aWalletInfo,
                                 int nCount);

    public native int ABCSetWalletOrder(String szUserName,
                               String szPassword,
                               String[] aszUUIDArray,
                               int countUUIDs,
                               ABCError pError);

    public native int ABCGetQuestionChoices(String szUserName,
                                   RequestCallback fRequestCallback,
                                   AirbitzAPI pData,
                                   ABCError pError);

    public native void ABCFreeQuestionChoices(ABCQuestionChoices pQuestionChoices);

    public native int ABCGetRecoveryQuestions(String szUserName,
                                     String[] pszQuestions,
                                     ABCError pError);

    public native int ABCChangePassword(String szUserName,
                               String szPassword,
                               String szNewPassword,
                               String szNewPIN,
                               RequestCallback fRequestCallback,
                               AirbitzAPI pData,
                               ABCError pError);

    public native int ABCChangePasswordWithRecoveryAnswers(String szUserName,
                                                  String szRecoveryAnswers,
                                                  String szNewPassword,
                                                  String szNewPIN,
                                                  RequestCallback fRequestCallback,
                                                  AirbitzAPI pData,
                                                  ABCError pError);

    public native int ABCParseBitcoinURI(String szURI,
                                ABCBitcoinURIInfo[] ppInfo,
                                ABCError pError);

    public native void ABCFreeURIInfo(ABCBitcoinURIInfo pInfo);

    public native double ABCSatoshiToBitcoin(long  satoshi);

    public native long  ABCBitcoinToSatoshi(double bitcoin);

    public native int ABCSatoshiToCurrency(long  satoshi,
                                  double pCurrency,
                                  int currencyNum,
                                  ABCError pError);

    public native int ABCCurrencyToSatoshi(double currency,
                                  int currencyNum,
                                  long  pSatoshi,
                                  ABCError pError);

    public native int ABCCreateReceiveRequest(String szUserName,
                                     String szPassword,
                                     String szWalletUUID,
                                     ABCTxDetails pDetails,
                                     String[] pszRequestID,
                                     ABCError pError);

    public native int ABCModifyReceiveRequest(String szUserName,
                                     String szPassword,
                                     String szWalletUUID,
                                     String szRequestID,
                                     ABCTxDetails pDetails,
                                     ABCError pError);

    public native int ABCFinalizeReceiveRequest(String szUserName,
                                       String szPassword,
                                       String szWalletUUID,
                                       String szRequestID,
                                       ABCError pError);

    public native int ABCCancelReceiveRequest(String szUserName,
                                     String szPassword,
                                     String szWalletUUID,
                                     String szRequestID,
                                     ABCError pError);

    public native int ABCGenerateRequestQRCode(String szUserName,
                                      String szPassword,
                                      String szWalletUUID,
                                      String szRequestID,
                                      String[] paData,
                                      int pWidth,
                                      ABCError pError);

    public native int ABCInitiateSendRequest(String szUserName,
                                    String szPassword,
                                    String szWalletUUID,
                                    String szDestAddress,
                                    ABCTxDetails pDetails,
                                    RequestCallback fRequestCallback,
                                    AirbitzAPI pData,
                                    ABCError pError);

    public native int ABCGetTransaction(String szUserName,
                               String szPassword,
                               String szWalletUUID,
                               String szID,
                               ABCTxInfo[] ppTransaction,
                               ABCError pError);

    public native int ABCGetTransactions(String szUserName,
                                String szPassword,
                                String szWalletUUID,
                                ABCTxInfo[][] paTransactions,
                                int pCount,
                                ABCError pError);

    public native void ABCFreeTransaction(ABCTxInfo pTransaction);

    public native void ABCFreeTransactions(ABCTxInfo[] aTransactions,
                              int count);

    public native int ABCSetTransactionDetails(String szUserName,
                                      String szPassword,
                                      String szWalletUUID,
                                      String szID,
                                      ABCTxDetails pDetails,
                                      ABCError pError);

    public native int ABCGetTransactionDetails(String szUserName,
                                      String szPassword,
                                      String szWalletUUID,
                                      String szID,
                                      ABCTxDetails[] ppDetails,
                                      ABCError pError);

    public native int ABCGetRequestAddress(String szUserName,
                                  String szPassword,
                                  String szWalletUUID,
                                  String szRequestID,
                                  String[] pszAddress,
                                  ABCError pError);

    public native int ABCGetPendingRequests(String szUserName,
                                   String szPassword,
                                   String szWalletUUID,
                                   ABCRequestInfo[][] paRequests,
                                   int pCount,
                                   ABCError pError);

    public native void ABCFreeRequests(ABCRequestInfo[] aRequests,
                          int count);

    public native int ABCDuplicateTxDetails(ABCTxDetails[] ppNewDetails,
                                   ABCTxDetails pOldDetails,
                                   ABCError pError);

    public native void ABCFreeTxDetails(ABCTxDetails pDetails);

    public native int ABCCheckPassword(String szPassword,
                              double pSecondsToCrack,
                              ABCPasswordRule[][] paRules,
                              int pCountRules,
                              ABCError pError);

    public native void ABCFreePasswordRuleArray(ABCPasswordRule[] aRules,
                                   int nCount);

    public native int ABCLoadAccountSettings(String szUserName,
                                    String szPassword,
                                    ABCAccountSettings[] ppSettings,
                                    ABCError pError);

    public native int ABCUpdateAccountSettings(String szUserName,
                                      String szPassword,
                                      ABCAccountSettings pSettings,
                                      ABCError pError);

    public native void ABCFreeAccountSettings(ABCAccountSettings pSettings);

}
