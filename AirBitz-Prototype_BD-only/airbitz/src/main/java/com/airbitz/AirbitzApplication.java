package com.airbitz;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by tom on 6/17/14.
 * Holds App statics for login info during the app lifecycle
 */
public class AirbitzApplication extends Application {

    public static final boolean DEBUG_LOGGING = true;

    public static boolean AUTOLOGIN = false;
    public static String autologinName = "tbtestnet"; public static String autologinPassword = "Aaaaaaaa1@";

    public static String PREFS = "com.airbitz.prefs";
    public static String LOGIN_NAME = "com.airbitz.login_name";
    private static Login airbitzLogin = new Login();
    private static Context mContext;
    private static int mLastNavTab = 0;

    @Override
    public void onCreate(){
        super.onCreate();
        mContext=getApplicationContext();
    }

    public static boolean isLoggedIn() {
        return airbitzLogin.getUsername() != null;
    }

    public static void Login(String uname, String password) {
        if(uname!=null && password!=null) {
            airbitzLogin.setUsername(uname);
            airbitzLogin.setPassword(password);
            SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
            editor.putString(LOGIN_NAME, uname);
            editor.apply();
        }

        //TODO setup auto logout based on Settings. App being killed automatically forgets login,
        // like on reboot or force close.
    }

    public static void Logout() {
        airbitzLogin = new Login();
    }

    public static String getUsername() {return airbitzLogin.getUsername(); }
    public static String getPassword() {return airbitzLogin.getPassword(); }
    public static Context getContext() {return mContext; }
    public static void setLastNavTab(int tab) {mLastNavTab = tab;}
    public static int getLastNavTab() {return mLastNavTab;}

    private static class Login {
        private String mUsername = null;
        private String mPassword = null;

        public String getUsername() {return mUsername; }
        public void setUsername(String name) { mUsername = name; }
        public String getPassword() {return mPassword; }
        public void setPassword(String word) { mPassword = word; }
    }
}
