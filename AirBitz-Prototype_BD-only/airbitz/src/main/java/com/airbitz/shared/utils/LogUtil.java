package com.airbitz.shared.utils;

import android.util.Log;

/**
 * Created by chris on 1/25/14.
 */
public class LogUtil {

    public static void logDebug(Class<?> theClass, String message) {
        Log.d(theClass.getName(), message);
    }

    public static void logWarning(Class<?> theClass, String message) {
        Log.w(theClass.getName(), message);
    }

    public static void logError(Class<?> theClass, String message) {
        Log.e(theClass.getName(), message);
    }

}
