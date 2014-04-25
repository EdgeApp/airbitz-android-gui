package com.airbitz.shared.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by dannyroa on 3/19/14.
 */
public class ShareIntentHelper {
    
    public static void dialIntent(Activity activity, String phone) {

        String uri = String.format("tel:%s", phone);

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        activity.startActivity(intent);
        
    }

    public static void directionsIntent(Activity activity,String label,  double latitude, double longitude) {

        String uriBegin = String.format("geo:%f,%f", latitude, longitude);
        String query = String.format("%f,%f(%s)", latitude, longitude, label);
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);

    }
    
}
