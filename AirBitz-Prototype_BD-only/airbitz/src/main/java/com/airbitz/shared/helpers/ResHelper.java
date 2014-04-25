package com.airbitz.shared.helpers;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.airbitz.shared.app.HaloApp;

/**
 * Created by dannyroa on 3/21/14.
 */
public class ResHelper {

    public static String getQuantityString(int resId, int quantity, Object... args) {
        return HaloApp.getInstance().getResources().getQuantityString(resId, quantity, args);
    }

    public static String getStringByResId(int resId) {
        return HaloApp.getInstance().getString(resId);
    }

    public static String getStringByResId(int resId, Object... args) {
        return HaloApp.getInstance().getString(resId, args);
    }

    public static int getInteger(int resId) {
        return HaloApp.getInstance().getResources().getInteger(resId);
    }

    public static Drawable getDrawable(int resId) {
        return HaloApp.getInstance().getResources().getDrawable(resId);
    }

    public static float convertDpToPx(int value) {

        Resources r = HaloApp.getInstance().getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.getDisplayMetrics());
    }
}
