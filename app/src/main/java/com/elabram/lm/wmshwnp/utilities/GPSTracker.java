package com.elabram.lm.wmshwnp.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.elabram.lm.wmshwnp.rest.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;


public class GPSTracker extends Service implements LocationListener {

    private Context mContext = this;

    private String TAG = GPSTracker.class.getSimpleName();

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second

    // Declaring logo_indosat Location Manager
    protected LocationManager locationManager;
    private Disposable disposable;

    private String s_gmt;
    private String s_timezone_id;
    private String s_lat, s_long;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    @SuppressWarnings("unused")
    public GPSTracker() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLocation() {

        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            assert locationManager != null;
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.e(TAG, "getLocation: No Network & No GPS");
            } else {

                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {

                    // Need Permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            }, 15);
                        }
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    //Log.e(TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.e(TAG, "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Have to check server time
     * if >= 17.00 stop service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        s_gmt = intent.getStringExtra("gmt");
//        s_timezone_id = intent.getStringExtra("timezone_id");
//        s_lat = intent.getStringExtra("a_lat");
//        s_long = intent.getStringExtra("a_long");

        s_lat = String.valueOf(getLocation().getLatitude());
        s_long = String.valueOf(getLocation().getLongitude());

        SharedPreferences preferences = getSharedPreferences("PREFS_TIMEZONE", 0);
        s_timezone_id = preferences.getString("s_timezone_id", "");
        s_gmt = preferences.getString("s_gmt", "");

        // use timezone
        rxLiveTracking();

        return super.onStartCommand(intent, flags, startId);
    }

    private HashMap<String, String> getParamsLiveTracking() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token); // OK
        params.put("lat", s_lat); // OK
        params.put("long", s_long); // OK
        params.put("timezone", s_gmt);
        params.put("timezone_id", s_timezone_id);
        //Log.e(TAG, "getParamsLive: " + params);
        return params;
    }

    private void rxLiveTracking() {
        Observable<ResponseBody> call = new ApiClient().getApiService().liveTracking(getParamsLiveTracking());
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (responseBody != null) {
                            try {
                                String mResponse = responseBody.string();
                                //Log.e(TAG, "onNext: LiveTrack -> "+mResponse);
                                //noinspection unused
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String message = jsonObject.getString("message");
                                Log.e(TAG, "onNext: LiveTrack -> "+message);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError LiveTracking Cause: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    @SuppressWarnings("unused")
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     */
    @SuppressWarnings("unused")
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Showing Alert Message
        alertDialog.show();
    }

    @SuppressWarnings("unused")
    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

}
