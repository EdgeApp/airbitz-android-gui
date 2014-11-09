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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.airbitz.R;
import com.airbitz.api.tABC_CC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * Created on 2/13/14.
 */
public class Common {

    public static final String TAG = Common.class.getSimpleName();

    public static double metersToMiles(double meters) {
        return meters * (1.0 / 1609.344);
    }

    public static double milesToFeet(double miles) {
        return (miles * 5280);
    }

    public static String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.d(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public static String createTempFileFromString(String name, String data) {
        String strDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp";
        File file = new File(strDir);
        if (!file.exists())
            file.mkdirs();
        String strFile = strDir + "/" + name;
        try {
            FileOutputStream fos = new FileOutputStream(strFile);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(data);
            out.flush();
            out.close();
            return strFile;
        } catch (Throwable t) {
            Log.d(TAG, "createFileFromString failed for " + name);
            return null;
        }
    }

    public static LinkedHashMap<String, Uri> GetMatchedContactsList(Context context, String searchTerm) {
        LinkedHashMap<String, Uri> contactList = new LinkedHashMap<String, Uri>();
        ContentResolver cr = context.getContentResolver();
        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        Cursor cur;
        if (searchTerm == null) {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, null, null, null);
        } else {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, ContactsContract.Contacts.DISPLAY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + searchTerm + "%"), null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        }
        if (cur != null) {
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
        }
        return contactList;
    }

    public static String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();

        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "" + e.getMessage());
        }

        return result.toString();
    }

    public static File getDir() {
        String rootDir = "";

        if (Environment.isExternalStorageEmulated()) {
            rootDir = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DCIM;
        } else {
            rootDir = Environment.getDataDirectory().toString() + "/" + Environment.DIRECTORY_DCIM;
        }
        File imageDirectory = new File(rootDir);

        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs();
        }

        return imageDirectory;
    }


    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        if (dist >= 1000) {
            int distInInt = (int) dist;
            dist = (int) distInInt;
        } else if (dist >= 100) {
            dist = (((int) (dist * 10)) / 10.0);
        } else {
            dist = (((int) (dist * 100)) / 100.0);
        }
        return dist;
    }

    public static boolean isBadWalletName(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static void alertBadWalletName(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
        builder.setMessage(activity.getResources().getString(R.string.error_invalid_wallet_name_description))
                .setTitle(activity.getString(R.string.error_invalid_wallet_name_title))
                .setCancelable(false)
                .setNeutralButton(activity.getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String errorMap(Context context, tABC_CC code) {
        if (code == tABC_CC.ABC_CC_AccountAlreadyExists) {
            return context.getString(R.string.server_error_account_already_exists);
        }
        else if (code == tABC_CC.ABC_CC_AccountDoesNotExist) {
            return context.getString(R.string.server_error_account_does_not_exists);
        }
        else if (code == tABC_CC.ABC_CC_BadPassword) {
            return context.getString(R.string.server_error_bad_password);
        }
        else if (code == tABC_CC.ABC_CC_WalletAlreadyExists) {
            return context.getString(R.string.server_error_wallet_exists);
        }
        else if (code == tABC_CC.ABC_CC_InvalidWalletID) {
            return context.getString(R.string.server_error_invalid_wallet);
        }
        else if (code == tABC_CC.ABC_CC_URLError) {
            return context.getString(R.string.string_connection_error_server);
        }
        else if (code == tABC_CC.ABC_CC_ServerError) {
            return context.getString(R.string.server_error_no_connection);
        }
        else if (code == tABC_CC.ABC_CC_NoRecoveryQuestions) {
            return context.getString(R.string.server_error_no_recovery_questions);
        }
        else if (code == tABC_CC.ABC_CC_NotSupported) {
            return context.getString(R.string.server_error_not_supported);
        }
        else if (code == tABC_CC.ABC_CC_InsufficientFunds) {
            return context.getString(R.string.server_error_insufficient_funds);
        }
        else if (code == tABC_CC.ABC_CC_Synchronizing) {
            return context.getString(R.string.server_error_synchronizing);
        }
        else if (code == tABC_CC.ABC_CC_NonNumericPin) {
            return context.getString(R.string.server_error_non_numeric_pin);
        }
        else if (code == tABC_CC.ABC_CC_PinExpired) {
            return context.getString(R.string.server_error_pin_expired);
        }
        else {
            return context.getString(R.string.server_error_other);
        }
    }
}
