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

package com.airbitz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.airbitz.models.Business;
import com.airbitz.models.LocationSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 4/2/14.
 */
public class CacheUtil {

    public static final String LOC1_KEY = "LOC1_KEY";
    public static final String LOC2_KEY = "LOC2_KEY";
    public static final String MOSTRECENT_LOCATIONSEARCH_SHARED_PREF = "LOCATION_KEY";

    public static final String MOSTRECENT_BUSINESSSEARCH_SHARED_PREF = "BUSINESS_KEY";
    public static final String BIZ1_NAME_KEY = "BIZ1_NAME_KEY";
    public static final String BIZ2_NAME_KEY = "BIZ2_NAME_KEY";
    public static final String BIZ1_TYPE_KEY = "BIZ1_TYPE_KEY";
    public static final String BIZ2_TYPE_KEY = "BIZ2_TYPE_KEY";
    public static final String BIZ1_ID_KEY = "BIZ1_ID_KEY";
    public static final String BIZ2_ID_KEY = "BIZ2_ID_KEY";

    public static void writeCachedBusinessSearchData(Context context, Business recentData) {
        SharedPreferences cachePref = null;

        cachePref = context.getSharedPreferences(MOSTRECENT_BUSINESSSEARCH_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        String name = recentData.getName();
        String type = recentData.getType();
        String id = recentData.getId();

        if (cachePref.getString(BIZ1_NAME_KEY, null) == null) {
            editor.putString(BIZ1_NAME_KEY, name);
            editor.putString(BIZ1_TYPE_KEY, type);
            editor.putString(BIZ1_ID_KEY, id);
        } else {

            if (!cachePref.getString(BIZ1_NAME_KEY, null).equalsIgnoreCase(name)) {
                editor.putString(BIZ2_NAME_KEY, cachePref.getString(BIZ1_NAME_KEY, ""));
                editor.putString(BIZ1_NAME_KEY, name);

                editor.putString(BIZ2_TYPE_KEY, cachePref.getString(BIZ1_TYPE_KEY, ""));
                editor.putString(BIZ1_TYPE_KEY, type);

                editor.putString(BIZ2_ID_KEY, cachePref.getString(BIZ1_ID_KEY, ""));
                editor.putString(BIZ1_ID_KEY, name);
            }

        }
        editor.apply();
    }

    public static List<Business> getCachedBusinessSearchData(Context context) {
        SharedPreferences cachePref = null;
        List<Business> listRecentBusiness = new ArrayList<Business>();

        cachePref = context.getSharedPreferences(MOSTRECENT_BUSINESSSEARCH_SHARED_PREF, Context.MODE_PRIVATE);

        if ((cachePref.getString(BIZ1_NAME_KEY, null) == null) && (cachePref.getString(BIZ2_NAME_KEY, null) == null)) {
            return null;
        } else {
            if (cachePref.getString(BIZ1_NAME_KEY, null) != null) {
                final Business biz1 = new Business(cachePref.getString(BIZ1_NAME_KEY, null),
                        cachePref.getString(BIZ1_TYPE_KEY, null),
                        cachePref.getString(BIZ1_ID_KEY, null));
                biz1.setIsCached(true);
                listRecentBusiness.add(biz1);
            }

            if (cachePref.getString(BIZ2_NAME_KEY, null) != null) {
                final Business biz2 = new Business(cachePref.getString(BIZ2_NAME_KEY, null),
                        cachePref.getString(BIZ2_TYPE_KEY, null),
                        cachePref.getString(BIZ2_ID_KEY, null));
                biz2.setIsCached(true);
                listRecentBusiness.add(biz2);
            }

            return listRecentBusiness;
        }
    }

    public static void writeCachedLocationSearchData(Context context, String recentData) {
        SharedPreferences cachePref = null;

        cachePref = context.getSharedPreferences(MOSTRECENT_LOCATIONSEARCH_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        if (cachePref.getString(LOC1_KEY, null) == null) {
            editor.putString(LOC1_KEY, recentData);
        } else {
            if (!cachePref.getString(LOC1_KEY, null).equalsIgnoreCase(recentData)) {
                editor.putString(LOC2_KEY, cachePref.getString(LOC1_KEY, ""));
                editor.putString(LOC1_KEY, recentData);
            }
        }
        editor.apply();
    }

    public static List<LocationSearchResult> getCachedLocationSearchData(Context context) {
        SharedPreferences cachePref = null;
        List<LocationSearchResult> listRecentLocation = new ArrayList<LocationSearchResult>();

        cachePref = context.getSharedPreferences(MOSTRECENT_LOCATIONSEARCH_SHARED_PREF, Context.MODE_PRIVATE);

        if ((cachePref.getString(LOC1_KEY, null) == null) &&
                (cachePref.getString(LOC2_KEY, null) == null)) {
            return null;
        } else {
            if (cachePref.getString(LOC1_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC1_KEY, null), true);
                listRecentLocation.add(location);
            }

            if (cachePref.getString(LOC2_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC2_KEY, null), true);
                listRecentLocation.add(location);
            }

            return listRecentLocation;
        }
    }

}
