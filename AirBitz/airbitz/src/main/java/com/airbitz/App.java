package com.airbitz;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.airbitz.shared.app.HaloApp;


/**
 * Created by dannyroa on 4/2/14.
 */
public class App extends HaloApp {

    private static int displayWidth;
    private static int displayHeight;

    @Override public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        App.setDisplayWidth(size.x);
        App.setDisplayHeight(size.y);
    }

    public static void setDisplayWidth(int displayWidth) {
        App.displayWidth = displayWidth;
    }

    public static int getDisplayWidth() {
        return displayWidth;
    }

    public static void setDisplayHeight(int displayHeight) {
        App.displayHeight = displayHeight;
    }

    public static int getDisplayHeight() {
        return displayHeight;
    }


}
