package com.airbitz.objects;

import android.content.Context;
import android.content.SharedPreferences;

import com.airbitz.AirbitzApplication;
import com.airbitz.models.Location;

public class HomeLocation {
    public final static String HOME_LOCATION = "home_location";

    private Context mContext;
    private static Location mLocation;

    HomeLocation(Context context) {
        mContext = context;
    }

    private static HomeLocation mHomeLocation;
    public HomeLocation getInstance(Context context) {
        if(mHomeLocation == null) {
            mHomeLocation = new HomeLocation(context);
        }
        return mHomeLocation;
    }

    /*
     * Compute best home location given latest location entry
     */
    public static Location getHomeLocation(Location currentLocation) {
        //TODO implement logic to determine correct Home Location geofence

        return mLocation;
    }

    private void saveLocation(Location homeLocation) {
        SharedPreferences.Editor editor =
                mContext.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();

        String latLong = String.valueOf(homeLocation.getLatitude());
        latLong += "," + String.valueOf(homeLocation.getLongitude());

        editor.putString(HOME_LOCATION, latLong);
        editor.apply();
    }

    private Location getHomeLocation() {
        SharedPreferences prefs =
                mContext.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);

        String[] latLong = prefs.getString(HOME_LOCATION, "0,0").split(",");
        if(latLong.length !=2) {
            return null;
        }
        Location loc = new Location(Double.valueOf(latLong[0]), Double.valueOf(latLong[1]));
        return loc;
    }
}
