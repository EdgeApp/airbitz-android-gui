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
        return stringBuffer.toString();
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
}
