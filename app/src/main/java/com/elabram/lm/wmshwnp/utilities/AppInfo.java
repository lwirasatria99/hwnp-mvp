package com.elabram.lm.wmshwnp.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 *
 * Created by lalu.mahendra on 03-May-18.
 */

public class AppInfo {

    //public static String PRE_URL = "http://elabramdev.com/wms-api-lumen/api";
    public static String PRE_URL = "http://178.128.97.164/wms-api-lumen/api/";

    public static String token;
    public static String mem_id;
    public static String mem_nip;
    public static String mem_mobile;
    public static String mem_phone;
    public static String mem_address;
    public static String position;
    public static String user_fullname;
    public static String user_email;
    public static String mem_image;

    public static String PREFS_LOGIN = "login";
    public static String PREFS_LOGGED = "logged";

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //noinspection RedundantIfStatement
        if (netInfo == null || !netInfo.isConnected() ||!netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public void errorResponse(VolleyError error, Activity c){
        String message;
        if (error instanceof NetworkError) {
            message = "Cannot connect to Internet...Please check your connection!";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        } else if (error instanceof ServerError) {
            message = "The server could not be found. Please try again after some time!!";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        } else if (error instanceof AuthFailureError) {
            message = "Your Token Is Expired...Please Logout And Login Back To Get New One!";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        } else if (error instanceof ParseError) {
            message = "Parsing error! Please try again after some time!!";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        } else //noinspection ConstantConditions
            if (error instanceof NoConnectionError) {
            message = "Cannot connect to Internet ...Please check your connection!";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        } else if (error instanceof TimeoutError) {
            message = "Connection TimeOut! Please check your internet connection.";
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
        }
    }
}
