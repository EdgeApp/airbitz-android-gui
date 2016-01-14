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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.CurrentLocationManager;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2/13/14.
 */
public class Common {

    public static final String TAG = Common.class.getSimpleName();

    private static final double BORDER_THICKNESS = 0.03;

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
            CoreAPI.debugLevel(1, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    CoreAPI.debugLevel(1, "Error closing asset " + name);
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

    private static String getVersion(Context ctx) {
        String version = "version error";
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            version = info.versionName + " " + String.valueOf(info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            CoreAPI.debugLevel(1, "Common.java getVersion error" + e.toString());
        }
        return version;
    }

    public static String evaluateTextFile(Context ctx, int resId) {
        String text = readRawTextFile(ctx, resId);
        Map<String, String> tags = new LinkedHashMap<String, String>();
        tags.put("[[abtag APP_TITLE]]", ctx.getString(R.string.app_name));
        tags.put("[[abtag APP_STORE_LINK]]", ctx.getString(R.string.appstore_link));
        tags.put("[[abtag PLAY_STORE_LINK]]", ctx.getString(R.string.playstore_link));
        tags.put("[[abtag APP_HOMEPAGE]]", ctx.getString(R.string.app_homepage));
        tags.put("[[abtag APP_LOGO_WHITE_LINK]]", ctx.getString(R.string.logo_white_link));
        tags.put("[[abtag APP_DESIGNED_BY]]", ctx.getString(R.string.designed_by));
        tags.put("[[abtag APP_COMPANY_LOCATION]]", ctx.getString(R.string.company_location));
        tags.put("[[abtag APP_VERSION]]", getVersion(ctx));
        tags.put("[[abtag REQUEST_FOOTER]]", ctx.getString(R.string.request_footer));
        tags.put("[[abtag REQUEST_FOOTER_LINK_TITLE]]", ctx.getString(R.string.request_footer_link_title));
        tags.put("[[abtag REQUEST_FOOTER_LINK]]", ctx.getString(R.string.request_footer_link));
        tags.put("[[abtag REQUEST_FOOTER_CONTACT]]", ctx.getString(R.string.request_footer_contact));

        for (Map.Entry<String, String> e : tags.entrySet()) {
            text = text.replace(e.getKey(), e.getValue());
        }
        return text;
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
            CoreAPI.debugLevel(1, "createFileFromString failed for " + name);
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
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(activity);
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
        builder.show();
    }

    public static Bitmap AddWhiteBorder(Bitmap inBitmap) {
        Bitmap imageBitmap = Bitmap.createBitmap((int) (inBitmap.getWidth() * (1 + BORDER_THICKNESS * 2)),
                (int) (inBitmap.getHeight() * (1 + BORDER_THICKNESS * 2)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawPaint(p);
        canvas.drawBitmap(inBitmap, (int) (inBitmap.getWidth() * BORDER_THICKNESS), (int) (inBitmap.getHeight() * BORDER_THICKNESS), null);
        return getRoundedCornerBitmap(imageBitmap);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        BitmapShader shader;
        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

        // rect contains the bounds of the shape
        // radius is the radius in pixels of the rounded corners
        // paint contains the shader that will texture the shape
        canvas.drawRoundRect(rectF, 16, 16, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Map<String, String> splitQuery(Uri uri) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    // Snagged from http://stackoverflow.com/a/29281284
    public static void addStatusBarPadding(Activity activity, View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            view.setPadding(0, getStatusBarHeight(activity), 0, 0);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= 21) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    static final int NOTIFICATION_DURATION = 5000;

    public static void disabledNotification(final Activity activity, int viewRes) {
        disabledNotification(activity, activity.findViewById(viewRes));
    }

    public static void disabledNotification(final Activity activity, View view) {
        boolean enabled = CurrentLocationManager.locationEnabled(activity);
        if (!enabled) {
            if (AirbitzApplication.getLocationWarn() && activity != null) {
                Snackbar bar = Snackbar.make(view,
                            R.string.fragment_business_enable_location_services, Snackbar.LENGTH_LONG)
                        .setDuration(NOTIFICATION_DURATION)
                        .setActionTextColor(Color.YELLOW)
                        .setAction(R.string.location_enable, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    activity.startActivity(intent);
                                }
                            });
                View snackview = bar.getView();
                TextView textView = (TextView) snackview.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                bar.show();
                AirbitzApplication.setLocationWarn(false);
            }
        } else {
            AirbitzApplication.setLocationWarn(true);
        }
    }

    public static void networkTimeoutSnack(final Activity activity, View view) {
        if (null == activity || null == view) {
            return;
        }
        Snackbar bar = Snackbar.make(view,
                    R.string.fragment_directory_detail_timeout_retrieving_data, Snackbar.LENGTH_LONG)
                .setDuration(NOTIFICATION_DURATION);
        View snackview = bar.getView();
        TextView textView = (TextView) snackview.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        bar.show();
    }

    public static void noLocationSnack(final Activity activity, View view) {
        if (null == activity || null == view) {
            return;
        }
        Snackbar bar = Snackbar.make(view, R.string.no_location_found, Snackbar.LENGTH_LONG)
                .setDuration(NOTIFICATION_DURATION);
        View snackview = bar.getView();
        TextView textView = (TextView) snackview.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        bar.show();
    }
}
