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

package com.airbitz.api.directory;

import android.util.Log;
import android.util.LruCache;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class DirectoryApi {

    private static String TAG = DirectoryApi.class.getSimpleName();

    private static final String SERVER_ROOT = "https://api.airbitz.co/";
    private static final String API_PATH = SERVER_ROOT + "api/v1/";
    private static final String API_SEARCH = API_PATH + "search/";
    private static final String API_BUSINESS = API_PATH + "business/";
    private static final String API_PLUGIN_DATA = API_PATH + "plugins/";
    private static final String API_MESSAGES = API_PATH + "notifications/";
    private static final String API_HIDDENBITZ = API_PATH + "hiddenbits/";
    private static final String API_LOCATION_SUGGEST = API_PATH + "location-suggest/";
    private static final String API_CATEGORIES = API_PATH + "categories/";
    private static final String API_AUTO_COMPLETE_LOCATION = API_PATH + "autocomplete-location/";
    private static final String API_AUTO_COMPLETE_BUSINESS = API_PATH + "autocomplete-business/";
    private static final String API_BUYSELL_OVERRIDE = API_PATH + "buyselloverride/";

    private LruCache mApiCache;
    private String mToken;
    private String mUserAgent;
    private String mClientId;
    private String mLang;

    public DirectoryApi(String token, String userAgent, String clientId, String lang) {
        this.mToken = token;
        this.mUserAgent = userAgent;
        this.mClientId = clientId;
        this.mLang = lang;
        int cacheSize = 2 * 1024 * 1024; // 2MiB
        mApiCache = new LruCache<String, String>(cacheSize) {
            protected int sizeOf(String key, String value) {
                return value.length();
            }
        };
    }

    public static String getServerRoot(){
        return SERVER_ROOT;
    }

    public String getRequest(String url) {
        return request(url, "", "GET");
    }

    public String getRequest(String url, String params) {
        return request(url, params, "GET");
    }

    public String postRequest(String url, String body) {
        // JSONObject escapes / for some reason...
        return request(url, body.replace("\\",""), "POST");
    }

    private String request(String url, String params, String method) {
        StringBuffer stringBuffer = new StringBuffer("");
        BufferedReader bufferedReader = null;
        HttpsURLConnection urlConnection = null;
        Log.d(TAG, method + ": " + url + " " + params);
        if ("GET".equals(method) && mApiCache.get(url + params) != null) {
            return (String) mApiCache.get(url + params);
        }
        try {
            TrustManager tm[] = {
                new PinManager()
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tm, null);

            URL sendUrl = null;
            if ("POST".equalsIgnoreCase(method)) {
                sendUrl = new URL(url);
            } else {
                sendUrl = new URL(url + params);
            }
            urlConnection = (HttpsURLConnection) sendUrl.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setRequestProperty("Authorization", "Token " + mToken + "");
            urlConnection.setRequestProperty("User-Agent", mUserAgent);
            urlConnection.setRequestProperty("X-Client-ID", mClientId);
            urlConnection.setRequestMethod(method);
            if ("POST".equalsIgnoreCase(method)) {
                urlConnection.setRequestProperty("Content-Length", "" +
                    Integer.toString(params.getBytes().length));
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
            }

            if ("POST".equalsIgnoreCase(method)) {
                DataOutputStream wr = new DataOutputStream(
                    urlConnection.getOutputStream ());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(in));
            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                stringBuffer.append(readLine);
                stringBuffer.append("\n");
                readLine = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String temp = stringBuffer.toString();
        if (temp == null) {
            temp = "{}";
        }
        synchronized (mApiCache) {
            mApiCache.put(url + params, temp);
        }
        return temp;
    }

    public static String createURLParams(List<NameValuePair> params){
        String result = "";
        if(params.size() > 0){
            result += "?";
            for(int index = 0; index < params.size();index++){
                NameValuePair keyVal = params.get(index);
                if(keyVal.getValue() != null && !"".equals(keyVal.getValue())){
                    if(index > 0){
                        result += "&";
                    }
                    String paramKey = keyVal.getName();
                    String paramVal = "";
                    try {
                        paramVal = URLEncoder.encode(keyVal.getValue(), "utf-8").replace(" ", "%20");
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                    result +=paramKey + "=" + paramVal;
                }
            }
        }

        return result;
    }

    public String getMessages(String since_id, String android_build) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("since_id", since_id));

        if(android_build.length() != 0){
            params.add(new BasicNameValuePair("android_build", android_build));
        }
        return getRequest(API_MESSAGES, createURLParams(params));
    }

    public String getNewBusinesses(String since, String latLong, String radius) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("since", since));
        params.add(new BasicNameValuePair("ll", latLong));
        params.add(new BasicNameValuePair("radius", radius));

        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getHiddenBits(String token) {
        return getRequest(API_HIDDENBITZ + token);
    }

    public String getSearchByTerm(String term, String category, String page_size, String page, String sort){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("term", term));
        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }

        if(category.length() != 0){
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

    public String getSearchByLatLong(String latlong, String term, String category, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", latlong));

        if (page_size.length() != 0){
            params.add(new BasicNameValuePair("page_size", page_size));
        }
        if (page.length() != 0){
            params.add(new BasicNameValuePair("page", page));
        }
        if (term.length()>0){
            params.add(new BasicNameValuePair("term", term));
        }
        if (category.length()>0){
            params.add(new BasicNameValuePair("category", category));
        }
        if (sort.length() != 0){
            params.add(new BasicNameValuePair("sort", sort));
        }
        return getRequest(API_SEARCH, createURLParams(params));
    }

    public String getSearchByLatLongAndBusiness(String latlong, String businessName, String category, String page_size, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", latlong));
        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }

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

    public String getSearchByRadius(String radius, String page_size, String ll, String page, String sort){
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("ll",ll));
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
        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }

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
        return getHttpBusiness(bizId, false);
    }

    public BusinessDetail getHttpBusiness(int bizId, boolean cacheOnly){
        String url = API_BUSINESS+bizId +"/";
        String response = (String) mApiCache.get(url);
        if (response == null && !cacheOnly) {
            response = getRequest(url, "");
            synchronized (mApiCache) {
                mApiCache.put(url, response);
            }
        }
        if (response == null) {
            return null;
        }
        try {
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
        if (getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }

        String response =  getRequest(API_CATEGORIES, createURLParams(params));
        try{
            return new Categories(new JSONObject(response));
        }catch (JSONException e){
            Log.e(TAG, ""+e.getMessage());
        }catch (Exception e){
            Log.e(TAG, "" + e.getMessage());
        }
        return null;
    }


    public List<LocationSearchResult> getHttpAutoCompleteLocation(String term, String ll){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        List<LocationSearchResult> resultList = new ArrayList<LocationSearchResult>();

        if(term.length() > 0){

            params.add(new BasicNameValuePair("term", term));
        }
        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
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
            Log.d(TAG, "" + e.getMessage());
        } catch(Exception e){
            e.printStackTrace();
        }

        return resultList;

    }

    public List<Business> getHttpAutoCompleteBusiness(String term, String category, String location, String ll){

        List<NameValuePair> params = new ArrayList<NameValuePair>();


        if(term.length()>0){
            params.add(new BasicNameValuePair("term", term));
        }

        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }

        if(category.length() > 0){
            params.add(new BasicNameValuePair("category", category));
        }

        if(location.length() > 0){
            params.add(new BasicNameValuePair("location", location));
        }

        if(ll.length() > 0){
            params.add(new BasicNameValuePair("ll", ll));
        }


        String response = getRequest(API_AUTO_COMPLETE_BUSINESS, createURLParams(params));
        Log.d(TAG, response);
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

    public String checkPluginDetails(){
        return getRequest(API_PLUGIN_DATA);
    }

    public String getBusinessById(String businessId){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }
        return getRequest(API_BUSINESS+businessId, createURLParams(params));
    }

    public String getBusinessByIdAndLatLong(String businessId, String ll){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ll", ll));
        if (getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }
        return getRequest(API_BUSINESS + businessId + "/", createURLParams(params));
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
        if(getLanguageCode() != null) {
            params.add(new BasicNameValuePair("lang", getLanguageCode()));
        }
        if(businessFlag.equalsIgnoreCase("category")){
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

    private String getLanguageCode() {
        return ("en".equals(mLang) || "es".equals(mLang)) ? mLang : null;
    }

    public Map<String, String> getCurrencyUrlOverrides() {
        String response =  getRequest(API_BUYSELL_OVERRIDE);
        try {
            Map<String, String> map = new HashMap<String, String>();
            JSONObject object = new JSONObject(response);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()){
                String k = keys.next();
                map.put(k, object.getString(k));
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
