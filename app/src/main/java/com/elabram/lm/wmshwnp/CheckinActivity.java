package com.elabram.lm.wmshwnp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.change_password.ChangePasswordActivity;
import com.elabram.lm.wmshwnp.db.AttendanceModel;
import com.elabram.lm.wmshwnp.db.DBHelper;
import com.elabram.lm.wmshwnp.login.LoginActivity;
import com.elabram.lm.wmshwnp.model.Office;
import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.utilities.AppInfo;
import com.elabram.lm.wmshwnp.utilities.GPSTracker;
import com.elabram.lm.wmshwnp.utilities.WordUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.isOnline;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_address;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_mobile;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_nip;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_phone;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.position;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_email;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_fullname;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.dateNow;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isAfternoon;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isDay;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isEarly;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isEvening;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isEvening1;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isLate1;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isMorning;
import static com.elabram.lm.wmshwnp.utilities.TimeGreetings.isOntime;

public class CheckinActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.buttonCheckin)
    Button buttonRecord;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.dataLayout)
    LinearLayout dataLayout;

    @BindView(R.id.date)
    TextView tvDate;

    @BindView(R.id.time_start)
    TextView tvStart;

    @BindView(R.id.time_end)
    TextView tvEnd;

    @BindView(R.id.tvFirstLocation)
    TextView tvFirstLocation;

    @BindView(R.id.tvLastLocation)
    TextView tvLastLocation;

    @BindView(R.id.relativeEnabled)
    RelativeLayout rel_online;

    @BindView(R.id.relativeDisabled)
    RelativeLayout rel_offline;

    @BindView(R.id.buttonRefresh)
    ImageView buttonRefresh;

    @BindDrawable(R.drawable.bg_confirm_white)
    Drawable bg_confirm;

    @BindView(R.id.buttonOutRadius)
    Button buttonOutOfRadius;

    @BindView(R.id.progressBarPosition)
    ProgressBar progressBarPosition;

    @BindView(R.id.digitalClock)
    TextView tvDigitalClock;

    private String TAG = CheckinActivity.class.getSimpleName();

    // Map Variable
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private double myLat;
    private double myLong;
    private LatLng myLatLong;

    //private String task_id;
    //private String location_name;
    //private String lat_, long_;

    private ArrayList<Office> offices;
    float[] resultApi = new float[1];
    private String site_name;
    private ProgressDialog progressDialog;

    // used
    private String cache_timeZone_name;
    private String s_timeStamp;
    private String s_gmt;

    @BindView(R.id.iv_logo_client)
    ImageView iv_logo_client;

    @BindView(R.id.iv_profile)
    CircularImageView iv_profile_main;

    private AlertDialog dialogProfile;
    private AlertDialog dialogLogout;
    private AlertDialog adVersion;
    //    private RelativeLayout relativeDate;
    private AlertDialog adGreetings;
    private Disposable disposable;
    private String s_type_attendance;
    private ImageView ivGreeting;

    private TextView tvGreetings;

    private String message_greetings;
    private Drawable img_attendance;
    private long delayShow;
    private String timeZoneId;
    private String rawOffset;

    private DBHelper db;
    private ArrayList<AttendanceModel> attendanceModels = new ArrayList<>();

    private Runnable updater;
    private Handler timerHandler;
    private String serverTime;

    Handler mHandler = new Handler();
    TextView mText;
    int mHour;
    int mMinute;
    int mSecond;
    private Runnable mUpdate;

    private String part1_time;
    private String part2_time;
    private String part3_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin2);
        ButterKnife.bind(this);

        getSharedUserDetail();
        initiateProfilePicture();
        retrofitReadClient();

        retrofitRealTime();
        //updateTime();

        Long tsLong = System.currentTimeMillis() / 1000;
        s_timeStamp = tsLong.toString();

        offices = new ArrayList<>();

        progressDialog = new ProgressDialog(this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        buildGoogleApiClient();
        buttonRecord.setVisibility(View.GONE);
        progressBarPosition.setVisibility(View.VISIBLE);

        loadSiteAndStatus();
        retrofitCheckVersion();

        GPSTracker gpsTracker = new GPSTracker(this);
        myLat = gpsTracker.getLatitude();
        myLong = gpsTracker.getLongitude();
        myLatLong = new LatLng(myLat, myLong);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                setMarker(locationResult);
                locationChanged(locationResult);
            }
        };

        refreshConnectionClick();

        setGreetingsFirstTime();

//        else if (isBirthday(dateNow())) {
//            s_type_attendance = "birthday";
//            img_attendance = getResources().getDrawable(R.drawable.greetings_birthday);
//        }

        // Testing Database
//        db = new DBHelper(this);
//
//        db.insertAttendance("08.00", "17.00", "Thamrin A", "Thamrin B"
//        , "+7", "Asia/Jakarta");
//
//        attendanceModels.addAll(db.getAllAttendance());
//
//        for (int i = 0; i < attendanceModels.size(); i++) {
//            Log.e(TAG, "onCreate Database Time Start: "+attendanceModels.get(i).getPlace_start());
//            Log.e(TAG, "onCreate Database Time End: "+attendanceModels.get(i).getPlace_end());
//        }

    }

    // Greetings #1
    private void setGreetingsFirstTime() {
        delayShow = 3000;

        if (isMorning(dateNow())) {
            s_type_attendance = "morning";
            img_attendance = getResources().getDrawable(R.drawable.good_morning);
        } else if (isDay(dateNow())) {
            s_type_attendance = "day";
            img_attendance = getResources().getDrawable(R.drawable.good_day);
        } else if (isAfternoon(dateNow())) {
            s_type_attendance = "afternoon";
            img_attendance = getResources().getDrawable(R.drawable.good_afternoon);
        } else if (isEvening(dateNow())) {
            s_type_attendance = "evening";
            img_attendance = getResources().getDrawable(R.drawable.good_evening);
        } else if (isEvening1(dateNow())) {
            s_type_attendance = "evening";
            img_attendance = getResources().getDrawable(R.drawable.good_evening);
        }
        retrofitShowGreeting();
        //tvGreetings.setVisibility(View.INVISIBLE);
    }

    // Greetings #2
    private void setGreetingsRecord() {
        delayShow = 5000;

        if (isEarly(dateNow())) {
            s_type_attendance = "early";
            img_attendance = getResources().getDrawable(R.drawable.greetings_early);
        } else if (isOntime(dateNow())) {
            s_type_attendance = "ontime";
            img_attendance = getResources().getDrawable(R.drawable.greetings_on_time);
        } else if (isLate1(dateNow())) {
            s_type_attendance = "late";
            img_attendance = getResources().getDrawable(R.drawable.greetings_late);
        }
        retrofitShowGreeting();
        //tvGreetings.setVisibility(View.VISIBLE);
    }

    private void showGreetings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_greetings, null);
        initView(view);

        builder.setView(view);
        adGreetings = builder.create();
        Objects.requireNonNull(adGreetings.getWindow()).getAttributes().windowAnimations = R.style.showingDialogAnimation;
        adGreetings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        adGreetings.show();

        Log.e(TAG, "showGreetings: Delay " + delayShow);

        if (delayShow == 5000) {
            tvGreetings.setVisibility(View.VISIBLE);
            ivGreeting.setImageDrawable(img_attendance);
            tvGreetings.setText(message_greetings);
        } else {
            ivGreeting.setImageDrawable(img_attendance);
            tvGreetings.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(() -> adGreetings.dismiss(), delayShow);
    }

    private void startDialogProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);

        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_id = view.findViewById(R.id.tv_id);
        TextView tv_position = view.findViewById(R.id.tv_position);
        TextView tv_email = view.findViewById(R.id.tv_email);
        TextView tv_mobile = view.findViewById(R.id.tv_mobile);
        TextView tv_phone = view.findViewById(R.id.tv_phone);
        ImageView iv_profile = view.findViewById(R.id.iv_profile);

        LinearLayout linear_logout = view.findViewById(R.id.linear_logout);
        LinearLayout linear_change = view.findViewById(R.id.linear_change_password);
        LinearLayout linear_feedback = view.findViewById(R.id.linear_feedback);
        LinearLayout linear_about = view.findViewById(R.id.linear_about);

        builder.setView(view);
        dialogProfile = builder.create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        Objects.requireNonNull(dialogProfile.getWindow()).setBackgroundDrawable(inset);
        dialogProfile.show();

        setDetailsForm(tv_name, tv_id, tv_position, tv_email, tv_mobile, tv_phone, iv_profile);

        linear_about.setOnClickListener(view1 -> startActivity(new Intent(this, AboutActivity.class)));

        linear_change.setOnClickListener(view1 -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        linear_feedback.setOnClickListener(view1 -> startActivity(new Intent(this, FeedbackActivity.class)));

        linear_logout.setOnClickListener(view1 -> dialogLogout());
    }

    private void dialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_delete, null);

        TextView buttonNo = view.findViewById(R.id.No);
        TextView buttonYes = view.findViewById(R.id.Yes);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        builder.setView(view);
        dialogLogout = builder.create();
        //noinspection ConstantConditions
        dialogLogout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLogout.show();

        String s_logout = "Are you sure you want to logout ?";
        tvMessage.setText(s_logout);

        buttonNo.setOnClickListener(view1 -> dialogLogout.cancel());

        buttonYes.setOnClickListener(view12 -> logout());
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        // Clear Activity And Fragment
        dialogProfile.dismiss();
        dialogLogout.dismiss();
        //assert getFragmentManager() != null;
        //getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setDetailsForm(TextView tv_name, TextView tv_id, TextView tv_position,
                                TextView tv_email, TextView tv_mobile, TextView tv_phone, ImageView iv_profile) {

        tv_name.setText(WordUtils.capitalize(user_fullname));
        tv_id.setText(mem_nip);
        tv_position.setText(position);
        tv_email.setText(user_email);
        tv_mobile.setText(mem_mobile);
        tv_phone.setText(mem_phone);

        // Profile Picture
        if (mem_image.equals("https://elabram.com/hris/files/employee/") || mem_image.isEmpty()) {
            VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.profile_default_picture, null);
            iv_profile.setImageDrawable(vectorDrawableCompat);
        } else {
            Picasso.with(this)
                    .load(mem_image)
                    .resize(200, 200)
                    .fit()
                    .noFade()
                    .into(iv_profile);
        }

        // ID
        if (mem_nip.length() == 0) {
            tv_id.setText("-");
        }

        // Position
        if (position.length() == 0) {
            tv_position.setText("-");
        }

        // Email
        if (user_email.length() == 0) {
            tv_email.setText("-");
        }

        // Mobile
        if (mem_mobile.length() == 0) {
            tv_mobile.setText("-");
        }

        // Telephone
        if (mem_phone.length() == 0) {
            tv_phone.setText("-");
        }
    }

    private void initiateProfilePicture() {
        if (mem_image.equals("https://elabram.com/hris/files/employee/") || mem_image.isEmpty()) {
            VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.profile_default_picture, null);
            iv_profile_main.setImageDrawable(vectorDrawableCompat);
        } else {
            Picasso.with(this)
                    .load(mem_image)
                    .resize(200, 200)
                    .noFade()
                    .into(iv_profile_main);
        }
    }

    private void refreshConnectionClick() {
        buttonRefresh.setOnClickListener(view -> loadSiteAndStatus());
    }

    private void setMarker(LocationResult locationResult) {
        map.clear();

        Double latNow = locationResult.getLastLocation().getLatitude();
        Double longNow = locationResult.getLastLocation().getLongitude();

//        for (Location location : locationResult.getLocations()) {
//            Double latNow = location.getLatitude();
//            Double longNow = location.getLongitude();

        /* First Camera Position */
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latNow, longNow))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        for (int k = 0; k < offices.size(); k++) {
            Office office = offices.get(k);
            Double d_lat = Double.valueOf(office.getOc_lat());
            Double d_long = Double.valueOf(office.getOc_long());
            Double d_radius = Double.valueOf(office.getOc_radius());

            // Check the distance "MyLocation" to "OfficeLocation"
            Location.distanceBetween(latNow, longNow, d_lat, d_long, resultApi);

            // Convert to String & Double
            String s_distanceToOffice = String.valueOf(resultApi[0]);
            Double d_distanceToOffice = Double.parseDouble(s_distanceToOffice);

            if (d_distanceToOffice < 200) {
                //Log.e(TAG, "locationChanged: Distance Marker " + s_distanceToOffice);

                LatLng latLngTask = new LatLng(d_lat, d_long);
                map.addMarker(new MarkerOptions()
                        .position(latLngTask)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                );

                int stroke_c = 0xffff0000;
                int transp = 0x44ff0000;
                map.addCircle(new CircleOptions()
                        .center(latLngTask)
                        .radius(d_radius)
                        .fillColor(transp)
                        .strokeWidth(2)
                        .strokeColor(stroke_c)
                );
                stopLocationUpdates();
            }
        }
//        }
    }

    private void loadSiteAndStatus() {
        if (AppInfo.isOnline(this)) {
            rel_online.setVisibility(View.VISIBLE);
            showProgress();
            retrofitSite();
            retrofitCheckinStatus();
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            stopLocationUpdates();
        }

        mHandler.removeCallbacks(mUpdate);

        if (map != null)
            map.clear();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void requestLocationUpdates() {
        // 120000 = 2 minute
        // 5 minute = 5 * 60 * 1000
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // two minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    private void locationChanged(LocationResult locationResult) {

        for (@SuppressWarnings("unused") Location location : locationResult.getLocations()) {

            // Initialize
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();

            // Mock Checking
            boolean isMock;
            if (Build.VERSION.SDK_INT >= 18) {
                isMock = location.isFromMockProvider();
            } else {
                isMock = !Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
            }

            for (int k = 0; k < offices.size(); k++) {
                Office office = offices.get(k);
                Double d_lat = Double.valueOf(office.getOc_lat());
                Double d_long = Double.valueOf(office.getOc_long());
                Double d_radius = Double.valueOf(office.getOc_radius());

                // Check the distance "MyLocation" to "OfficeLocation"
                Location.distanceBetween(lat, lng, d_lat, d_long, resultApi);

                // Convert to String & Double
                String s_distanceToOffice = String.valueOf(resultApi[0]);
                Double d_distanceToOffice = Double.parseDouble(s_distanceToOffice);
                //Log.e(TAG, "locationChanged: Distance " + s_distanceToOffice);

                if (d_distanceToOffice < d_radius) {
                    site_name = office.getOc_site();
                    progressBarPosition.setVisibility(View.GONE);
                    buttonRecord.setVisibility(View.VISIBLE);
                    buttonOutOfRadius.setVisibility(View.GONE);
                    if (isMock) {
                        processTheMock();
                    } else {
                        processNoMock(lat, lng);
                    }
                    break;
                } else {
                    progressBarPosition.setVisibility(View.GONE);
                    buttonRecord.setVisibility(View.GONE);
                    buttonOutOfRadius.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private String checkTimezoneGMT() {
        String timeZoneGMT = "";

        if (rawOffset != null) {
            int i_rawOffset = Integer.parseInt(rawOffset);
            int i_gmt = i_rawOffset / 3600;
            if (i_gmt > 0) {
                timeZoneGMT = "+" + i_gmt;
            } else {
                timeZoneGMT = String.valueOf(i_gmt);
            }
        }

        return timeZoneGMT;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocationClient != null) {
            stopLocationUpdates();
            map.clear();
        }

        mHandler.removeCallbacks(mUpdate);

        dismissAlert();

        if (disposable != null) {
            disposable.dispose();
        }

        //timerHandler.removeCallbacks(updater);
    }

    private void retrofitTimezone(double lat, double lng) {
        String coordinate = lat + "," + lng;
        String apiKey = getString(R.string.map_api);

        Call<ResponseBody> call = new ApiClient().getApiService().cekTimeZone(coordinate, s_timeStamp, apiKey);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String responseContent = new String(response.body().bytes());
                        //Log.e(TAG, "onResponse: Timezone " + responseContent);

                        JSONObject jsonObject = new JSONObject(responseContent);
                        cache_timeZone_name = jsonObject.getString("timeZoneName");
                        timeZoneId = jsonObject.getString("timeZoneId");
                        rawOffset = jsonObject.getString("rawOffset");

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "onResponse: Timezone else " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Timezone " + t.getMessage());
            }
        });
    }

    private void processNoMock(Double lat, Double lng) {
        buttonRecord.setOnClickListener(view -> {
            if (isOnline(this)) {
                s_gmt = checkTimezoneGMT();
                if (!s_gmt.isEmpty()) {
                    retrofitCheckin();
                } else {
                    retrofitTimezone(lat, lng);
                    Snackbar.make(rootView, "Please try again", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar snackbar = Snackbar.make(rootView, "Please check your internet connection", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void processTheMock() {
        Log.e(TAG, "locationChanged: " + "Yes Mock");

        Crashlytics.log(user_fullname + " " + "FAKE GPS");
        Snackbar snackbar = Snackbar.make(rootView,
                "We detected the use of fake GPS application. As logo_indosat consequence, " +
                        "we will report this illegal action to HR to be processed accordingly.",
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.show();
        buttonRecord.setEnabled(false);

        retrofitReportFakeGPS();
    }

    private void retrofitReportFakeGPS() {
        Observable<ResponseBody> observable = new ApiClient().getApiService().fakeGPS(getParamsFakeGPS());
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            Log.e(TAG, "onNext Fake GPS: " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError Fake GPS: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private HashMap<String, String> getParamsFakeGPS() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        return params;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    private void myLocation() {
        mapFragment.getMapAsync(googleMap -> {
            map = googleMap;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(myLatLong));
                    map.animateCamera(CameraUpdateFactory.zoomTo(17));
                }
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLng(myLatLong));
                map.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });
    }

    private void showProgress() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void retrofitReadClient() {
        Observable<ResponseBody> call = new ApiClient().getApiService().listLogo(token);
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
                                JSONObject jsonObject = new JSONObject(mResponse);

                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        String urlImage = jsonObject1.getString("cus_logo");

                                        if (!urlImage.equals("https://elabram.com/hris/")) {
                                            Picasso.with(CheckinActivity.this)
                                                    .load(urlImage)
                                                    .fit()
                                                    .into(iv_logo_client);
                                        }
                                        break;
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void retrofitShowGreeting() {
        Observable<ResponseBody> call = new ApiClient().getApiService().listGreeting(getParamsGreeting());
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
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                            message_greetings = jsonObject1.getString("message");
                                            Log.e(TAG, "onNext message greetings: " + message_greetings);
                                        }

                                        break;
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError Retrofit Greeting Cause: %" + e.getCause());
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(CheckinActivity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        showGreetings();
                    }
                });
    }

    private HashMap<String, String> getParamsGreeting() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("type", s_type_attendance);
        Log.e(TAG, "getParamsGreeting: " + params);
        return params;
    }

    private void retrofitCheckVersion() {
        Call<ResponseBody> call = new ApiClient().getApiService().checkVersion();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        //noinspection ConstantConditions
                        String mResponse = new String(response.body().bytes());
//                        Log.e(TAG, "onResponse: CheckVersion " + mResponse);
                        JSONObject jsonObject = new JSONObject(mResponse);

                        String response_code = jsonObject.getString("response_code");
                        switch (response_code) {
                            case "401":
                                String message = jsonObject.getString("message");
                                Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                                snackbar.show();
                                break;
                            case "200":
                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                String s_version = jsonObject1.getString("version");

                                if (!s_version.equals(getVersionInfo())) {
                                    dialogCheckVersion();
                                }
                                break;
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Version " + t.getMessage());
            }
        });
    }

    private String getVersionInfo() {
        //int versionCode = -1;
        String versionName = null;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            //Log.e(TAG, "getVersionInfo: "+versionName );
            //versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    private void retrofitCheckinStatus() {
        Call<ResponseBody> call = new ApiClient().getApiService().loadStatusCheckin(token);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                dismissProgress();
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String contentResponse = new String(response.body().bytes());
                        Log.e(TAG, "onResponse Status Checkin: " + contentResponse);
                        JSONObject jsonObject = new JSONObject(contentResponse);
                        String response_code = jsonObject.getString("response_code");
                        switch (response_code) {
                            case "401":
                                String message = jsonObject.getString("message");
                                Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                                snackbar.show();
                                break;
                            case "200":
                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                String realtime_date = jsonObject1.getString("is_date");
                                String time_first = jsonObject1.getString("time_first");
                                String location_first = jsonObject1.getString("location_first");

                                String time_last = jsonObject1.getString("time_last");
                                String location_last = jsonObject1.getString("location_last");

                                tvDate.setText(realtime_date);

                                String replaceFirst = time_first.replace(".", ":");
                                String replaceLast = time_last.replace(".", ":");

                                if (!time_first.isEmpty())
                                    tvStart.setText(replaceFirst);
                                else
                                    tvStart.setText("-");

                                if (!time_last.isEmpty())
                                    tvEnd.setText(replaceLast);
                                else
                                    tvEnd.setText("-");

                                if (!location_first.isEmpty())
                                    tvFirstLocation.setText(location_first);
                                //else
                                //    tvFirstLocation.setText("(-)");

                                if (!location_last.isEmpty())
                                    tvLastLocation.setText(location_last);
                                //else
                                //    tvLastLocation.setText("(-)");

                                break;
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(CheckinActivity.this, "Please check your internet & try again", Toast.LENGTH_SHORT).show();
                dismissProgress();
                Crashlytics.log(user_fullname + " " + t.getCause());
            }
        });
    }

    private void retrofitCheckinStatusAfterClick() {
        Call<ResponseBody> call = new ApiClient().getApiService().loadStatusCheckin(token);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                dismissProgress();
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String contentResponse = new String(response.body().bytes());
                        Log.e(TAG, "onResponse Status Checkin after click: " + contentResponse);
                        JSONObject jsonObject = new JSONObject(contentResponse);
                        String response_code = jsonObject.getString("response_code");
                        switch (response_code) {
                            case "401":
                                String message = jsonObject.getString("message");
                                Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                                snackbar.show();
                                break;
                            case "200":
                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                String time_first = jsonObject1.getString("time_first");
                                String time_last = jsonObject1.getString("time_last");

                                if (!time_first.isEmpty() && !time_last.isEmpty() && time_first.equals(time_last)) {
                                    setGreetingsRecord();
                                }

                                break;
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(CheckinActivity.this, "Please check your internet & try again", Toast.LENGTH_SHORT).show();
                dismissProgress();
                Crashlytics.log(user_fullname + " " + t.getCause());
            }
        });
    }

    private void retrofitRealTime() {
        Observable<ResponseBody> call = new ApiClient().getApiService().realtime();
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
                                String response = responseBody.string();
                                Log.e(TAG, "onNext Realtime: " + response);

                                JSONObject jsonObject = new JSONObject(response);
                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        serverTime = jsonObject1.getString("serverTime");
                                        Log.e(TAG, "onNext serverTime:" + serverTime);
                                        String time = serverTime.substring(11);

                                        String[] parts = time.split(":");
                                        part1_time = parts[0];
                                        part2_time = parts[1];
                                        part3_time = parts[2];

                                        Log.e(TAG, "onNext Time subs:" + time);
                                        Log.e(TAG, "onNext Time Hour:" + part1_time);
                                        Log.e(TAG, "onNext Time Minute:" + part2_time);
                                        Log.e(TAG, "onNext Time Second:" + part3_time);
                                        //tvDigitalClock.setText(serverTime);
                                        break;
                                }


                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError Realtime: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {
                        updateTime();
                    }
                });
    }

    private void updateTime() {

        ///String log2 = part1_time +":"+part2_time+":"+part3_time;
        mHour = Integer.parseInt(part1_time);
        mMinute = Integer.parseInt(part2_time);
        mSecond = Integer.parseInt(part3_time);

//        Log.e(TAG, "onNext U Time Hour:" + part1_time);
//        Log.e(TAG, "onNext U Time Minute:" + part2_time);
//        Log.e(TAG, "onNext U Time Second:" + part3_time);


//        timerHandler = new Handler();
//        updater = () -> {
//            retrofitRealTime();
//            tvDigitalClock.setText(serverTime);
//            timerHandler.postDelayed(updater,1000);
//        };
//        timerHandler.post(updater);

        mUpdate = new Runnable() {
            @Override
            public void run() {
                //mMinute += 1;
                mSecond += 1;
                // just some checks to keep everything in order
                if (mSecond >= 60) {
                    mSecond = 0;
                    mMinute += 1;
                }

                if (mMinute >= 60) {
                    mMinute = 0;
                    mHour += 1;
                }
                if (mHour >= 24) {
                    mHour = 0;
                }

                // or call your method
                //mText.setText(mHour + ":" + mMinute);

                String s_mHour;
                String s_mMinute;
                String s_mSecond;

//                if (mHour < 10) {
//                    s_mHour = "0" + String.valueOf(mHour);
                s_mHour = String.valueOf(mHour);
                s_mMinute = String.valueOf(mMinute);
                s_mSecond = String.valueOf(mSecond);
//                }
//                if (mMinute < 10) {
//                    s_mMinute = "0"+ String.valueOf(mMinute);
//                }
//                if (mSecond < 10) {
//                    s_mSecond = "0"+ String.valueOf(mSecond);
//                }
                String logTime = s_mHour + ":" + s_mMinute + ":" + s_mSecond;
                tvDigitalClock.setText(logTime);
                //Log.e(TAG, "run time: " + mHour + ":" + mMinute + ":" + mSecond);
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.post(mUpdate);
    }

    private void retrofitCheckin() {
        showProgress();

        Observable<ResponseBody> call = new ApiClient().getApiService().checkin(getParamsCheckin());
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
                            parseJSONCheckin(responseBody);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgress();
                        Toast.makeText(CheckinActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Crashlytics.log(user_fullname + " " + e.getCause());
                    }

                    @Override
                    public void onComplete() {
                        dismissProgress();
                    }
                });
    }

    private HashMap<String, String> getParamsCheckin() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("location", site_name);
        params.put("timezone", s_gmt);
        params.put("timezone_id", timeZoneId);
        Log.e(TAG, "getParamsCheckin: " + params);
        return params;
    }

    private void parseJSONCheckin(ResponseBody responseBody) {
        try {
            String mResponse = responseBody.string();
            JSONObject jsonObject = new JSONObject(mResponse);
            String s_response_code = jsonObject.getString("response_code");
            String s_message = jsonObject.getString("message");

            if (s_response_code.equals("200")) {
                retrofitCheckinStatusAfterClick();
                retrofitCheckinStatus();
            } else {
                Toast.makeText(this, s_message, Toast.LENGTH_SHORT).show();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void retrofitSite() {
        Call<ResponseBody> call = new ApiClient().getApiService().siteList(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String content = new String(response.body().bytes());
//                        Log.e(TAG, "onResponse: Retrofit Site List " + content);
                        JSONObject jsonObject = new JSONObject(content);

                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String lat = jsonObject1.getString("lat");
                            String lng = jsonObject1.getString("long");
                            String radius = jsonObject1.getString("radius");
                            //noinspection unused
                            String city = jsonObject1.getString("city");
                            String site_name = jsonObject1.getString("site_name");

                            Office office = new Office();
                            office.setOc_lat(lat);
                            office.setOc_long(lng);
                            office.setOc_site(site_name);
                            office.setOc_radius(radius);
                            offices.add(office);
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Site " + t.getCause());
            }
        });
    }

    private void dialogCheckVersion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_version, null);

        TextView tvUpdate = view.findViewById(R.id.tvUpdate);
        TextView tvNoThanks = view.findViewById(R.id.tvNoThanks);

        builder.setView(view);
        adVersion = builder.create();
        //noinspection ConstantConditions
        adVersion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        adVersion.setCancelable(false);

        if (!isFinishing())
            adVersion.show();

        // Go To Playstore
        tvUpdate.setOnClickListener(view1 -> {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        // Exit the apps
        tvNoThanks.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= 21) {
                dismissAlert();
                finishAndRemoveTask();
            } else {
                dismissAlert();
                if (Build.VERSION.SDK_INT >= 16) {
                    finishAffinity();
                } else {
                    ActivityCompat.finishAffinity(this);
                }
            }
        });

    }

    private void dismissAlert() {
        if (adVersion != null && adVersion.isShowing()) {
            adVersion.dismiss();
        }
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(AppInfo.PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_image = preferences.getString("mem_image", "");

        mem_nip = preferences.getString("mem_nip", "");
        mem_mobile = preferences.getString("mem_mobile", "");
        mem_phone = preferences.getString("mem_phone", "");

        mem_address = preferences.getString("mem_address", "");
        position = preferences.getString("position", "");
        user_fullname = preferences.getString("name", "");
        user_email = preferences.getString("email", "");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrofitTimezone(myLat, myLong);
        //Log.e(TAG, "onResume: timezone " + cache_timeZone_name);

        if (mGoogleApiClient != null && mFusedLocationClient != null) {
            requestLocationUpdates();
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        /* Permission */
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /* Default Map Setting */
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        retrofitTimezone(myLat, myLong);
        //Log.e(TAG, "onMapReady: " + cache_timeZone_name);
    }


    @OnClick({R.id.iv_profile, R.id.fabAttendance, R.id.fabRefresh, R.id.fabMyLocation, R.id.relative_date,
            R.id.linear_first_record, R.id.linear_last_record})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_profile:
                startDialogProfile();
                break;
            case R.id.fabAttendance:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
            case R.id.fabRefresh:
                finish();
                startActivity(getIntent());
                break;
            case R.id.fabMyLocation:
                myLocation();
                break;
            case R.id.relative_date:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
            case R.id.linear_first_record:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
            case R.id.linear_last_record:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
        }
    }

    private void initView(View view) {
        ivGreeting = view.findViewById(R.id.ivGreeting);
        tvGreetings = view.findViewById(R.id.tvGreetings);
    }

}
