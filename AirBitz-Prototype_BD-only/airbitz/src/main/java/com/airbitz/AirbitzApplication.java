package com.airbitz;

import android.app.Application;

/**
 * Created by tom on 6/17/14.
 * Holds App statics for login info during the app lifecycle
 */
public class AirbitzApplication extends Application {

    public static boolean TEST = true;   // TODO Set to false for deployed application!!!!!!!

    private static Login airbitzLogin = new Login();
    private static int mLastNavTab = 0;

    public static boolean isLoggedIn() {
        return airbitzLogin.getUsername() != null;
    }

    public static void Login(String uname, String password) {
        if(TEST) {
            airbitzLogin.setUsername("tb2");
            airbitzLogin.setPassword("Aaaaaaaa1@");

//            airbitzLogin.setUsername("matt104");
//            airbitzLogin.setPassword("O@21grapes");
        } else if(uname!=null && password!=null) {
            airbitzLogin.setUsername(uname);
            airbitzLogin.setPassword(password);
        }

        //TODO setup auto logout based on Settings. App being killed automatically forgets login,
        // like on reboot or force close.
    }

    public static void Logout() {
        airbitzLogin = new Login();
    }

    public static String getUsername() {return airbitzLogin.getUsername(); }
    public static String getPassword() {return airbitzLogin.getPassword(); }
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
