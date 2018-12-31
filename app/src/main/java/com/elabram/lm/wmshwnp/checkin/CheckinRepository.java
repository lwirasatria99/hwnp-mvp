package com.elabram.lm.wmshwnp.checkin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.utilities.GPSTracker;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;

class CheckinRepository {

    private Context mContext;

    CheckinRepository(Context mContext) {
        this.mContext = mContext;
    }

    void clearPreferencesLogin() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    void putString(String key, String value) {
        SharedPreferences preferences = mContext.getSharedPreferences("PREFS_TIMEZONE", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    String getMapApi() {
        return mContext.getString(R.string.map_api);
    }

    String getTimestamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }

    void startLiveTracking(String gmt,
                           String timeZoneId,
                           String myLat,
                           String myLong) {
        /*
             1 * 1000 = 1 second
             1 * 60 * 1000 = 1 minute
             60 * 60 * 1000 = 60 minute
        */
        //long intervalMinute = 60 * 1000;
        long intervalMinute = 30 * 60 * 1000; // 60 Minute
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, GPSTracker.class);
        intent.putExtra("isStart", "true");
        intent.putExtra("gmt", gmt);
        intent.putExtra("timezone_id", timeZoneId);
        intent.putExtra("a_lat", myLat);
        intent.putExtra("a_long", myLong);
        mContext.startService(intent);

        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMinute, pendingIntent);
    }
}
