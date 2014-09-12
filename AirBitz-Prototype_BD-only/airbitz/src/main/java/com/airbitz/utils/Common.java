package com.airbitz.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

/**
 * Created on 2/13/14.
 */
public class Common {

    public static final String TAG = Common.class.getSimpleName();
    public static final String UNAVAILABLE = "unavailable";

    public static double metersToMiles(double meters) {
        return meters * (1.0 / 1609.344);
    }

    public static double milesToFeet(double miles) { return (miles*5280);}

    public static String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Common.LogD(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Common.LogD(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }

    public static LinkedHashMap<String, Uri> GetMatchedContactsList(Context context, String searchTerm) {
        LinkedHashMap<String, Uri> contactList = new LinkedHashMap<String, Uri>();
        ContentResolver cr = context.getContentResolver();
        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        Cursor cur;
        if(searchTerm==null) {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, null, null, null);
        } else {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, ContactsContract.Contacts.DISPLAY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + searchTerm + "%"), null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        }
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String photoURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                if (photoURI != null) {
                    Uri thumbUri = Uri.parse(photoURI);
                    contactList.put(name, thumbUri);
                }
            }
        }
        cur.close();
        return contactList;
    }

    public static void showHelpInfoDialog(Activity act, String title, String message){
        final Dialog dialog = new Dialog(act);
        dialog.setContentView(R.layout.dialog_help_info);
        dialog.setTitle(title);

        TextView textView = (TextView) dialog.findViewById(R.id.fragment_category_textview_title);
        textView.setText(message);
        Button okButton = (Button) dialog.findViewById(R.id.button_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static String convertStreamToString(InputStream inputStream)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();

        String line = null;

        try{
            while((line = reader.readLine()) != null){
                result.append(line);
            }
        }catch(IOException e){
            Log.e(TAG, ""+e.getMessage());
        }

        return result.toString();
    }

    public static File getDir() {
        String rootDir = "";

        if(Environment.isExternalStorageEmulated()){
            rootDir = Environment.getExternalStorageDirectory().toString()+"/"+Environment.DIRECTORY_DCIM;
        } else {
            rootDir = Environment.getDataDirectory().toString()+"/"+Environment.DIRECTORY_DCIM;
        }
        File imageDirectory = new File(rootDir);

        if(!imageDirectory.exists()){
            imageDirectory.mkdirs();
        }

        return imageDirectory;
    }


    public static int[] dateTextSplitter(String dateText){
        String[] splittedDateString = dateText.split("/");
        int[] splittedDate =  new int[splittedDateString.length];
        splittedDate[0] = Integer.parseInt(splittedDateString[0]);
        splittedDate[1] = Integer.parseInt(splittedDateString[1]);
        splittedDate[2] = Integer.parseInt(splittedDateString[2]);

        return splittedDate;
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        if(dist>=1000){
            int distInInt = (int) dist;
            dist = (int)distInInt;
        } else if(dist>=100){
            dist = (((int)(dist*10))/10.0);
        } else {
            dist = (((int)(dist * 100)) / 100.0);
        }
        return dist;
    }

    public static void LogD(String title, String message) {
        if(AirbitzApplication.DEBUG_LOGGING)
            Log.d(title, message);
    }

}
