package com.airbitz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.airbitz.models.LocationSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 4/2/14.
 */
public class LocationCacheUtil {
    
    public static final String LOC1_KEY = "LOC1_KEY";
    public static final String LOC2_KEY = "LOC2_KEY";
    public static final String LOC3_KEY = "LOC3_KEY";
    public static final String LOC4_KEY = "LOC4_KEY";
    public static final String LOC5_KEY = "LOC5_KEY";
    public static final String MOSTRECENT_LOCATIONSEARCH_SHARED_PREF = "LOCATION_KEY";

    public static void writeCachedLocationSearchData(Context context, String recentData) {
        SharedPreferences cachePref = null;

        cachePref = context.getSharedPreferences(MOSTRECENT_LOCATIONSEARCH_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        if (cachePref.getString(LOC1_KEY, null) == null) {
            editor.putString(LOC1_KEY, recentData);
        }
        else {
            if (!cachePref.getString(LOC1_KEY, null).equalsIgnoreCase(recentData)) {

                editor.putString(LOC2_KEY, cachePref.getString(LOC1_KEY, ""));
                editor.putString(LOC3_KEY, cachePref.getString(LOC2_KEY, null));
                editor.putString(LOC4_KEY, cachePref.getString(LOC3_KEY, null));
                editor.putString(LOC5_KEY, cachePref.getString(LOC4_KEY, null));
                editor.putString(LOC1_KEY, recentData);
            }
        }

        editor.commit();
    }

    public static List<LocationSearchResult> getCachedLocationSearchData(Context context) {
        SharedPreferences cachePref = null;
        List<LocationSearchResult> listRecentLocation = new ArrayList<LocationSearchResult>();

        cachePref = context.getSharedPreferences(MOSTRECENT_LOCATIONSEARCH_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        if ((cachePref.getString(LOC1_KEY, null) == null) &&
                (cachePref.getString(LOC2_KEY, null) == null) &&
                (cachePref.getString(LOC3_KEY, null) == null) &&
                (cachePref.getString(LOC4_KEY, null) == null) &&
                (cachePref.getString(LOC5_KEY, null) == null)) {
            return null;
        }
        else {
            if (cachePref.getString(LOC1_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC1_KEY, null), true);
                listRecentLocation.add(location);
            }

            if (cachePref.getString(LOC2_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC2_KEY, null), true);
                listRecentLocation.add(location);
            }

            if (cachePref.getString(LOC3_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC3_KEY, null), true);
                listRecentLocation.add(location);
            }

            if (cachePref.getString(LOC4_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC4_KEY, null), true);
                listRecentLocation.add(location);
            }

            if (cachePref.getString(LOC5_KEY, null) != null) {
                final LocationSearchResult location = new LocationSearchResult(cachePref.getString(LOC5_KEY, null), true);
                listRecentLocation.add(location);
            }

            return listRecentLocation;
        }
    }

}
