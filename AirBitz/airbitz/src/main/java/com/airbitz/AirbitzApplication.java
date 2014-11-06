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

package com.airbitz;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

/**
 * Created by tom on 6/17/14.
 * Holds App statics for login info during the app lifecycle
 */
public class AirbitzApplication extends Application {

    public static String PREFS = "com.airbitz.prefs";
    public static String LOGIN_NAME = "com.airbitz.login_name";

    private static Login airbitzLogin = new Login();
    private static long mBackgroundedTime = 0;
    private static Context mContext;
    private static int mLastNavTab = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static boolean isLoggedIn() {
        return airbitzLogin.getUsername() != null;
    }

    public static boolean isDebugging() {
        ApplicationInfo appInfo = mContext.getApplicationInfo();
        return (appInfo != null && ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0));
    }

    public static void Login(String uname, char[] password) {
        if (uname != null) {
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

    public static String getUsername() {
        return airbitzLogin.getUsername();
    }

    public static String getPassword() {
        if(airbitzLogin.getPassword()==null) {
            return null;
        } else {
            return String.valueOf(airbitzLogin.getPassword());
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setLastNavTab(int tab) {
        mLastNavTab = tab;
    }

    public static int getLastNavTab() {
        return mLastNavTab;
    }

    public static void setBackgroundedTime(long time) {
        mBackgroundedTime = time;
    }

    public static long getmBackgroundedTime() {
        return mBackgroundedTime;
    }

    private static class Login {
        private String mUsername = null;
        private char[] mPassword = null;

        public String getUsername() {
            return mUsername;
        }

        public void setUsername(String name) {
            mUsername = name;
        }

        public char[] getPassword() {
            return mPassword;
        }

        public void setPassword(char[] word) {
            mPassword = word;
        }
    }
}
