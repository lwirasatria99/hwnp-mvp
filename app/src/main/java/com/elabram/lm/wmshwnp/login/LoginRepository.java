package com.elabram.lm.wmshwnp.login;

import android.content.Context;
import android.content.SharedPreferences;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;

class LoginRepository {

    private Context mContext;

    LoginRepository(Context mContext) {
        this.mContext = mContext;
    }

    void putString(String key, String value) {
        SharedPreferences sPreferences = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString(key, value).apply();
    }

    void putBoolean(String key, boolean value) {
        SharedPreferences sPreferences = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(key, value).apply();
    }
}
