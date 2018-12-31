package com.elabram.lm.wmshwnp.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.AboutActivity;
import com.elabram.lm.wmshwnp.AttendanceRecordActivity;
import com.elabram.lm.wmshwnp.change_password.ChangePasswordActivity;
import com.elabram.lm.wmshwnp.FeedbackActivity;
import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.login.LoginActivity;
import com.elabram.lm.wmshwnp.model.Office;
import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.utilities.AppInfo;
import com.elabram.lm.wmshwnp.utilities.Connectivity;
import com.elabram.lm.wmshwnp.utilities.GPSTracker;
import com.elabram.lm.wmshwnp.utilities.MyJobService;
import com.elabram.lm.wmshwnp.utilities.WordUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

public class CheckinV1Activity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        CheckinView {

    /**
     * Photo
     */
    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;
    protected static final int CAMERA_RECORD = 2;
    //private Intent pictureActionIntent = null;
    Bitmap bitmap;

    String selectedImagePath;

    @BindView(R.id.buttonCheckin)
    Button buttonRecord;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.dataLayout)
    RelativeLayout dataLayout;

    @BindView(R.id.tvDate)
    TextView tvDate;

    @BindView(R.id.tvFirstTime)
    TextView tvStartTime;

    @BindView(R.id.tvLastTime)
    TextView tvEnd;

    @BindView(R.id.tvFirstLocation)
    TextView tvFirstLocation;

    @BindView(R.id.tvLastLocation)
    TextView tvLastLocation;

    @BindView(R.id.tvFirstRemark)
    TextView tvFirstRemark;

    @BindView(R.id.tvLastRemark)
    TextView tvLastRemark;

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

    @BindView(R.id.tvDigitalClock)
    TextView tvDigitalClock;

    private String TAG = CheckinV1Activity.class.getSimpleName();

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
    private String site_name = "";
    private ProgressDialog progressDialog;

    // used
//    private String cache_timeZone_name;

    private String s_gmt;

    @BindView(R.id.iv_logo_client)
    ImageView iv_logo_client;

    @BindView(R.id.iv_profile)
    CircularImageView iv_profile_main;

    @BindView(R.id.linearRemarkFirst)
    LinearLayout linearRemarkFirst;

    @BindView(R.id.linearRemarkLast)
    LinearLayout linearRemarkLast;

    @BindView(R.id.linearTimePlace)
    LinearLayout linearTimePlace;

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

//    private DBHelper db;
//    private ArrayList<AttendanceModel> attendanceModels = new ArrayList<>();
//    private Runnable updater;
//    private Handler timerHandler;

    private String serverTime;

    Handler mHandler = new Handler();
    int mHour;
    int mMinute;
    int mSecond;
    private Runnable mUpdate;

    private String part1_time;
    private String part2_time;
    private String part3_time;

    private CircularImageView iv_profile_dialog;

    private String s_url_image;
    private String isContractActive;

//    private String sh_url_image = "";
//    private String sh_image = "";
//    private byte[] byte_image;

    private String in_area = "";
    private String j_formatted_address;
    private String new_address = "";
    private String s_remark = "";
    private AlertDialog dialogRemark;
    //private String j_place_id;

    private TextView tv_name;
    private TextView tv_id;
    private TextView tv_position;
    private TextView tv_email;
    private TextView tv_mobile;
    private TextView tv_phone;

    private String site_id = "";
    private String site_lat = "";
    private String site_long = "";

    Context mContext = CheckinV1Activity.this;
    CheckinPresenterImpl presenter;
    CheckinRepository repository;
    private GPSTracker gpsTracker;

    /**
     * Check Stop Service
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        setContentView(R.layout.activity_checkin_v1);
        ButterKnife.bind(this);

        // Initialize
//        Long tsLong = System.currentTimeMillis() / 1000;
//        s_timeStamp = tsLong.toString();

        offices = new ArrayList<>();

        progressDialog = new ProgressDialog(this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        buildGoogleApiClient();

        gpsTracker = new GPSTracker(this);
        myLat = gpsTracker.getLatitude();
        myLong = gpsTracker.getLongitude();
        myLatLong = new LatLng(myLat, myLong);

        mLocationCallback = new LocationCallback();

        repository = new CheckinRepository(this);
        presenter = new CheckinPresenterImpl(this, repository);

        // Method
        buttonRecord.setVisibility(View.GONE);
        progressBarPosition.setVisibility(View.VISIBLE);

        checkingPermission();
        getSharedUserDetail();
        presenter.retrofitReadClient();

        loadSiteAndStatus();
        refreshConnectionClick();
        presenter.retrofitCheckVersion();

        showProgress();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                retrofitListSite(locationResult);
            }
        };

        retrofitReadContract();

        setGreetingsFirstTime();
        retrofitReadProfile(iv_profile_main);

        //myJobScheduler();
        // mobileCountryCode
        // mobileNetworkCode
        //getDeviceData();
        // #Greeting Birthday
//        else if (isBirthday(dateNow())) {
//            s_type_attendance = "birthday";
//            img_attendance = getResources().getDrawable(R.drawable.greetings_birthday);
//        }
//        ~

        // #Testing Database
//        db = new DBHelper(this);
//        db.insertAttendance("08.00", "17.00", "Thamrin A", "Thamrin B"
//        , "+7", "Asia/Jakarta");
//        attendanceModels.addAll(db.getAllAttendance());
//
//        for (int i = 0; i < attendanceModels.size(); i++) {
//            Log.e(TAG, "onCreate Database Time Start: "+attendanceModels.get(i).getPlace_start());
//            Log.e(TAG, "onCreate Database Time End: "+attendanceModels.get(i).getPlace_end());
//        }
//        ~

//        SharedPreferences settings = getSharedPreferences("PREFS_PHOTO", 0);
//        sh_image = settings.getString("sh_image", "");
//        sh_url_image = settings.getString("sh_url_image", "");
//        byte[] byte_image = Base64.decode(sh_image, Base64.NO_WRAP);

//        if (!sh_url_image.isEmpty()) {
//            Bitmap bmp = BitmapFactory.decodeByteArray(byte_image, 0, byte_image.length);
//            glideBitmap(bmp, iv_profile_main);
//        }
    }

    private void myJobScheduler() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");

        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-unique-tag")
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 60))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .setExtras(myExtrasBundle)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    private void getDeviceData() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(this, "Please allow the all permission to generate your account for login", Toast.LENGTH_SHORT).show();
                //Log.e(TAG, "getDeviceId: " + "Permission Deactivated");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 333);

            }
        }

        String carrierName = "";
        int mcc = 0;
        int mnc = 0;

        if (telephonyManager != null) {
            // MCC, MNC
            String mnc_mcc = telephonyManager.getNetworkOperator();
            if (!mnc_mcc.isEmpty()) {
                mcc = Integer.parseInt(mnc_mcc.substring(0, 3));
                mnc = Integer.parseInt(mnc_mcc.substring(3));
                Log.e(TAG, "getMCC: " + mcc);
                Log.e(TAG, "getMNC: " + mnc);
            }

            // carrier
            carrierName = telephonyManager.getNetworkOperatorName();
            Log.e(TAG, "getCarrierName: " + carrierName);
        }

        // radioType
        NetworkInfo info = Connectivity.getNetworkInfo(this);
        //int type = info.getType(); // wifi or radio
        int subType = info.getSubtype(); // which is what u want
        Log.e(TAG, "getSubtype: " + subType);
        String radioType = null;

        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
                radioType = "cdma";
                break;
            case TelephonyManager.NETWORK_TYPE_GSM:
                radioType = "gsm";
                break;
            //more checks you are interested in ie
            //case TelephonyManager.NETWORK_TYPE_***
        }
        Log.e(TAG, "getRadioType: " + radioType);

        // cell tower
        int cellId = 0;
        int lac = 0;
        assert telephonyManager != null;
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            final GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
            if (location != null) {
                Log.e(TAG, "getDeviceData: LAC: " + location.getLac() + " CID: " + location.getCid());
                cellId = location.getLac();
                lac = location.getCid();
            }
        }

//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        assert wifiManager != null;
//        WifiInfo wInfo = wifiManager.getConnectionInfo();
//        @SuppressLint("HardwareIds") String macAddress = wInfo.getMacAddress();

        String macAddress = getMacAddr();

        // convert to json
        convertToJSON(mcc,
                mnc,
                radioType,
                carrierName,
                cellId,
                lac,
                macAddress);
    }

    // get mac address
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private void convertToJSON(int mcc, int mnc, String radioType, String carrierName, int cellId, int lac, String macAddress) {
        try {
            String response;
            JSONObject json = new JSONObject();
            json.put("homeMobileCountryCode", mcc);
            json.put("homeMobileNetworkCode", mnc);
            json.put("radioType", radioType);
            json.put("carrier", carrierName);
            json.put("considerIp", "true");

            JSONArray array1 = new JSONArray();
            JSONObject item1 = new JSONObject();
            item1.put("cellId", cellId);
            item1.put("locationAreaCode", lac);
            item1.put("mobileCountryCode", mcc);
            item1.put("mobileNetworkCode", mnc);
            array1.put(item1);
            json.put("cellTowers", array1);

            JSONArray array2 = new JSONArray();
            JSONObject item2 = new JSONObject();
            item2.put("macAddress", macAddress);// required
            item2.put("signalStrength", "");
            item2.put("age", "");
            item2.put("channel", "");
            item2.put("signalToNoiseRatio", "");
            array2.put(item2);
            json.put("wifiAccessPoints", array2);

            response = json.toString();
            Log.e(TAG, "convertToJSON: " + response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        //Log.e(TAG, "showGreetings: Delay " + delayShow);

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

    /**
     * Profile
     */
    private void startDialogProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);

        tv_name = view.findViewById(R.id.tv_name);
        tv_id = view.findViewById(R.id.tv_id);
        tv_position = view.findViewById(R.id.tv_position);
        tv_email = view.findViewById(R.id.tv_email);
        tv_mobile = view.findViewById(R.id.tv_mobile);
        tv_phone = view.findViewById(R.id.tv_phone);

        iv_profile_dialog = view.findViewById(R.id.iv_profile_dialog);
        ImageView iv_change_profile = view.findViewById(R.id.ivChangePicture);

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

        retrofitReadProfile(iv_profile_dialog);

        linear_about.setOnClickListener(view1 -> startActivity(new Intent(this, AboutActivity.class)));

        linear_change.setOnClickListener(view1 -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        linear_feedback.setOnClickListener(view1 -> startActivity(new Intent(this, FeedbackActivity.class)));

        linear_logout.setOnClickListener(view1 -> dialogLogout());

        //iv_change_profile.setOnClickListener(view1 -> startDialogGalleryCamera());
        //iv_profile_dialog.setOnClickListener(view1 -> startDialogGalleryCamera());

    }

    @SuppressLint("ClickableViewAccessibility")
    private void startDialogRemark(Uri bitmapURI, String status_report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_remark, null);

        EditText etRemark = view.findViewById(R.id.etRemark);
        RelativeLayout relSubmit = view.findViewById(R.id.relative_choose);
        RelativeLayout relCancel = view.findViewById(R.id.relative_cancel);

        builder.setView(view);
        dialogRemark = builder.create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        Objects.requireNonNull(dialogRemark.getWindow()).setBackgroundDrawable(inset);
        dialogRemark.show();

        etRemark.setOnTouchListener((viewEt, motionEvent) -> {
            viewEt.setFocusable(true);
            viewEt.setFocusableInTouchMode(true);
            return false;
        });

        etRemark.setHint(status_report);

        relSubmit.setOnClickListener(view1 -> {
            s_remark = etRemark.getText().toString();
            if (s_remark.length() <= 0 && in_area.equals("N")) {
                etRemark.setError("Remark is required!");
            } else {
                try {
                    retrofitCheckin(bitmapURI);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        relCancel.setOnClickListener(view1 -> dialogRemark.cancel());
    }

    private void retrofitReadProfile(ImageView ivProfile) {
        Observable<ResponseBody> call = new ApiClient().getApiService().readProfile(token);
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
                                //Log.e(TAG, "onNext Profile: " + mResponse);
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        //Log.e(TAG, "onNext: Data Pofile -> " + jsonObject1.toString());
                                        s_url_image = jsonObject1.getString("user_image");
                                        Log.e(TAG, "onNext Profile URL: " + s_url_image);

//                                        if (s_url_image.isEmpty()) {
//                                            ivProfile.setImageDrawable(ContextCompat.getDrawable(
//                                                    CheckinV1Activity.this, R.drawable.profile_default_picture));
//                                        } else {
//                                            glideURL(s_url_image, ivProfile);
//                                        }

                                        String j_username = jsonObject1.getString("user_fullname");
                                        String j_id = jsonObject1.getString("mem_nip");
                                        String j_position = jsonObject1.getString("user_position");
                                        String j_email = jsonObject1.getString("user_email");
                                        String j_mobile = jsonObject1.getString("user_mobile");
                                        String j_phone = jsonObject1.getString("user_phone");

                                        setDataProfile(
                                                j_username, j_id, j_position,
                                                j_email, j_mobile, j_phone
                                        );

                                        break;
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError Profile: " + e.getCause());
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(CheckinV1Activity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CheckinV1Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        //Log.e(TAG, "onComplete Profile: ");
                    }
                });
    }

    private void retrofitReadContract() {
        Observable<ResponseBody> call = new ApiClient().getApiService().readContract(token);
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
                                Log.e(TAG, "onNext Contract: " + mResponse);
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        //contract_message = jsonObject.getString("message");
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        isContractActive = jsonObject1.getString("is_contract_active");
                                        break;
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgress();
                        Log.e(TAG, "onError Contract: " + e.getCause());
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(CheckinV1Activity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CheckinV1Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissProgress();
                        if (isContractActive.equals("false")) {
                            startDialogContract();
                        }
                    }
                });
    }

    private void startDialogContract() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_contract_out, null);

        TextView tvOK = view.findViewById(R.id.tvOK);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        if (!isFinishing())
            dialog.show();

        tvOK.setOnClickListener(view1 -> {
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

    private void checkingPermission() {
        int PERMISSION_ALL = 16;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA
        };

        if (!AppInfo.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private void startDialogGalleryCamera() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery", (arg0, arg1) -> {
            Intent pictureActionIntent;

            pictureActionIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(
                    pictureActionIntent,
                    GALLERY_PICTURE);

        });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Uri dirUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".com.elabram.lm.wmsmobile", f);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, dirUri);

                    startActivityForResult(intent, CAMERA_REQUEST);

                });
        myAlertDialog.show();
    }

    private void dialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_delete, null);

        RelativeLayout buttonNo = view.findViewById(R.id.relative_no);
        RelativeLayout buttonYes = view.findViewById(R.id.relative_yes);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        builder.setView(view);
        dialogLogout = builder.create();
        //noinspection ConstantConditions
        dialogLogout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLogout.show();

        String s_logout = "Are you sure you want to logout ?";
        tvMessage.setText(s_logout);

        buttonNo.setOnClickListener(view1 -> dialogLogout.cancel());

        buttonYes.setOnClickListener(view12 -> presenter.logout());
    }

    private void setDataProfile(String s_username, String s_id, String s_position,
                                String s_email, String s_mobile, String s_phone) {

        if (tv_name != null) {
            tv_name.setText(WordUtils.capitalize(s_username));
            tv_id.setText(s_id);
            tv_position.setText(s_position);
            tv_email.setText(s_email);
            tv_mobile.setText(s_mobile);
            tv_phone.setText(s_phone);

            // ID
            if (s_id.length() == 0) {
                tv_id.setText("-");
            }

            // Position
            if (s_position.length() == 0) {
                tv_position.setText("-");
            }

            // Email
            if (s_email.length() == 0) {
                tv_email.setText("-");
            }

            // Mobile
            if (s_mobile.length() == 0) {
                tv_mobile.setText("-");
            }

            // Telephone
            if (s_phone.length() == 0) {
                tv_phone.setText("-");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bitmap = null;
        selectedImagePath = null;

        // Camera Profile
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

            File f = new File(Environment.getExternalStorageDirectory().toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {
                Toast.makeText(getBaseContext(),
                        "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            String realPath = f.getAbsolutePath();
            File file1 = new File(realPath);
            Log.e(TAG, "onActivityResult: Size Before -> " + file1.length() / 1024);

            try {
                Bitmap compressorBitmap;

                File compressorFile = new Compressor(this).compressToFile(file1);
                InputStream input = getContentResolver().openInputStream(Uri.fromFile(compressorFile));

                ExifInterface exif;

                if (Build.VERSION.SDK_INT > 23) {
                    assert input != null;
                    exif = new ExifInterface(input);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Log.e(TAG, "onActivityResult: Orientation 23 -> " + orientation);

                    // File to Bitmap
                    compressorBitmap = BitmapFactory.decodeFile(compressorFile.getAbsolutePath());
                    //Log.e(TAG, "onActivityResult: Path -> "+compressorFile.getAbsolutePath());
                    Log.e(TAG, "onActivityResult: Size After -> " + compressorFile.length() / 1024);
                } else {
                    compressorBitmap = new Compressor(this).compressToBitmap(file1);

                    exif = new ExifInterface(realPath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Log.e(TAG, "onActivityResult: Orientation -> " + orientation);

                    if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                        compressorBitmap = rotateImage(compressorBitmap, 180);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        compressorBitmap = rotateImage(compressorBitmap, 90);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        compressorBitmap = rotateImage(compressorBitmap, 270);
                    }
                }

                Uri bitmapURI = getImageUri(this, compressorBitmap);
                retrofitAddPicture(bitmapURI);

                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // Gallery Profile
        else if (resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            if (data != null) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};

                assert selectedImage != null;
                Cursor c = getContentResolver().query(selectedImage, filePath,
                        null, null, null);

                //noinspection ConstantConditions
                c.moveToFirst();

                int columnIndex = c.getColumnIndex(filePath[0]);
                selectedImagePath = c.getString(columnIndex);
                c.close();

                File file1;

                if (selectedImagePath != null) {
                    Log.e(TAG, "onActivityResult Selected ImagePath: " + selectedImagePath);
                    file1 = new File(selectedImagePath);

                    try {
                        Bitmap compressorBitmap;

                        File compressorFile = new Compressor(this).compressToFile(file1);
                        InputStream input = getContentResolver().openInputStream(Uri.fromFile(compressorFile));

                        ExifInterface exif;

                        if (Build.VERSION.SDK_INT > 23) {
                            assert input != null;
                            exif = new ExifInterface(input);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                            Log.e(TAG, "onActivityResult: Orientation 23 -> " + orientation);

                            // File to Bitmap
                            compressorBitmap = BitmapFactory.decodeFile(compressorFile.getAbsolutePath());
                        } else {
                            compressorBitmap = new Compressor(this).compressToBitmap(file1);

                            exif = new ExifInterface(selectedImagePath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                            Log.e(TAG, "onActivityResult: Orientation -> " + orientation);

                            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                                compressorBitmap = rotateImage(compressorBitmap, 180);
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                                compressorBitmap = rotateImage(compressorBitmap, 90);
                            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                compressorBitmap = rotateImage(compressorBitmap, 270);
                            }
                        }

                        Uri bitmapURI = getImageUri(this, compressorBitmap);
                        retrofitAddPicture(bitmapURI);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }

        }
        // Camera Record
        else if (resultCode == RESULT_OK && requestCode == CAMERA_RECORD) {

            File f = new File(Environment.getExternalStorageDirectory().toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("record.jpg")) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {
                Toast.makeText(getBaseContext(),
                        "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            String realPath = f.getAbsolutePath();
            File file1 = new File(realPath);

            try {
                Bitmap compressorBitmap;

                File compressorFile = new Compressor(this).compressToFile(file1);
                InputStream input = getContentResolver().openInputStream(Uri.fromFile(compressorFile));

                ExifInterface exif;

                if (Build.VERSION.SDK_INT > 23) {
                    assert input != null;
                    exif = new ExifInterface(input);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Log.e(TAG, "onActivityResult: Orientation 23 -> " + orientation);

                    // File to Bitmap
                    compressorBitmap = BitmapFactory.decodeFile(compressorFile.getAbsolutePath());

                } else {
                    compressorBitmap = new Compressor(this).compressToBitmap(file1);

                    exif = new ExifInterface(realPath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Log.e(TAG, "onActivityResult: Orientation -> " + orientation);

                    if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                        compressorBitmap = rotateImage(compressorBitmap, 180);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        compressorBitmap = rotateImage(compressorBitmap, 90);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        compressorBitmap = rotateImage(compressorBitmap, 270);
                    }
                }

                Uri bitmapURI = getImageUri(this, compressorBitmap);

                if (in_area.equals("N")) {
                    startDialogRemark(bitmapURI, "Required");
                } else {
                    //retrofitCheckin(bitmapURI);
                    startDialogRemark(bitmapURI, "Optional");
                }

                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * ~
     */

    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private String getRealPathFromUri(final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void retrofitAddPicture(Uri fileUri) {
        showProgress();
        String filePath = getRealPathFromUri(fileUri);
        //noinspection ConstantConditions
        File file = new File(filePath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("mem_image", file.getName(), requestFile);

        RequestBody requestToken = RequestBody.create(MediaType.parse("multipart/form-data"), token);

        Observable<ResponseBody> call = new ApiClient().getApiService().addProfile(requestToken, body);
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
                                Log.e(TAG, "onNext Add Profile: " + response);

                                JSONObject jsonObject = new JSONObject(response);
                                String response_code = jsonObject.getString("response_code");
                                String message = jsonObject.getString("message");
                                if (response_code.equals("200")) {
                                    Toast.makeText(CheckinV1Activity.this, "Photo Successfully uploaded ", Toast.LENGTH_SHORT).show();

                                    glideBitmap(fileUri, iv_profile_dialog);
                                    glideBitmap(fileUri, iv_profile_main);

                                } else {
                                    Toast.makeText(CheckinV1Activity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgress();
                        Toast.makeText(CheckinV1Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onError Add Profile: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissProgress();
                    }
                });
    }

    private void glideBitmap(Uri bitmap, ImageView ivProfile) {
        Glide.with(CheckinV1Activity.this)
                .load(bitmap)
                .apply(new RequestOptions()
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.profile_default_picture)
                        .dontAnimate())
                .into(ivProfile);
    }
    // ~

    private void refreshConnectionClick() {
        buttonRefresh.setOnClickListener(view -> loadSiteAndStatus());
    }

    private void setMarker(LocationResult locationResult) {
        map.clear();

        Double latNow = locationResult.getLastLocation().getLatitude();
        Double longNow = locationResult.getLastLocation().getLongitude();

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
            } else {
                stopLocationUpdates();
            }
        }
    }

    private void loadSiteAndStatus() {
        if (AppInfo.isOnline(this)) {
            rel_online.setVisibility(View.VISIBLE);
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFusedLocationClient != null) {
            stopLocationUpdates();
            map.clear();
        }

        if (disposable != null) {
            disposable.dispose();
        }

        dismissAlert();
        Glide.get(this).clearMemory();
        mHandler.removeCallbacks(mUpdate);

        presenter.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mFusedLocationClient != null) {
            stopLocationUpdates();
        }

        if (map != null)
            map.clear();

        mHandler.removeCallbacks(mUpdate);
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void requestLocationUpdates() {
        // 120000 = 2 minute
        // 5 minute = 5 * 60 * 1000
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // two minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @SuppressLint("SetTextI18n")
    private void locationChanged(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {

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
            Log.e(TAG, "locationChanged Office Total: " + offices.size());

//            if (offices.size() > 0) {
            for (int k = 0; k < offices.size(); k++) {
                Office office = offices.get(k);

                Double d_lat = Double.valueOf(office.getOc_lat());
                Double d_long = Double.valueOf(office.getOc_long());
                Double d_radius = Double.valueOf(office.getOc_radius());

                // Check the distance "MyLocation" to "OfficeLocation"
                Location.distanceBetween(lat, lng, d_lat, d_long, resultApi);
                String s_distanceToOffice = String.valueOf(resultApi[0]);
                Double d_distanceToOffice = Double.parseDouble(s_distanceToOffice);
                //Log.e(TAG, "locationChanged: Distance -> " + d_distanceToOffice);

                // check-in in radius
                if (d_distanceToOffice < d_radius) {
                    buttonOutOfRadius.setVisibility(View.GONE);
                    progressBarPosition.setVisibility(View.GONE);
                    buttonRecord.setVisibility(View.VISIBLE);

                    // checking fake gps
                    if (isMock) {
                        processTheMock();
                    } else {
                        in_area = "Y";

                        site_name = office.getOc_site();
                        site_id = office.getOc_id();
                        site_lat = office.getOc_lat();
                        site_long = office.getOc_long();

                        processNoMock(lat, lng, "In Radius", site_name);
                    }
                    stopLocationUpdates();
                    break;
                }
                // checkin out radius
                else if (d_distanceToOffice > d_radius) {
                    progressBarPosition.setVisibility(View.GONE);
                    buttonOutOfRadius.setVisibility(View.VISIBLE);
                    buttonRecord.setVisibility(View.GONE);
                }
            }

        }
        // no office
//            else {
//                progressBarPosition.setVisibility(View.GONE);
//                buttonOutOfRadius.setVisibility(View.GONE);
//                buttonRecord.setVisibility(View.GONE);
//                Snackbar.make(rootView, "No site found !, Please refresh to try again", Snackbar.LENGTH_INDEFINITE).show();
//            }
//        }
        stopLocationUpdates();
    }

    private void processNoMock(Double lat, Double lng, String status, String new_address) {
        Log.e(TAG, "processNoMock: " + status);

        buttonRecord.setOnClickListener(view -> {
            // checking online
            if (isOnline(this)) {
                s_gmt = presenter.getGMT();

                // checkin timezone
                if (!s_gmt.isEmpty()) {
                    Log.e(TAG, "processNoMock: CheckinClick -> " + new_address);
                    // take photo & checkin
                    takePhoto(new_address);
                } else {
                    presenter.retrofitGoogleTimezone(lat, lng);
                    Snackbar.make(rootView, "Please try again", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(rootView, "Please check your internet connection", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void takePhoto(String new_name) {
        site_name = new_name;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "record.jpg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri dirUri = FileProvider.getUriForFile(this, "com.elabram.lm.wmshwnp", f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, dirUri);

        startActivityForResult(intent, CAMERA_RECORD);
    }


    // Fake GPS
    private void processTheMock() {
        Log.e(TAG, "locationChanged: " + "Yes Mock");

        Crashlytics.log(user_fullname + " " + "FAKE GPS");
        Snackbar.make(rootView,
                "We detected the use of fake GPS application. As logo_indosat consequence, " +
                        "we will report this illegal action to HR to be processed accordingly.",
                Snackbar.LENGTH_INDEFINITE
        ).show();

        buttonRecord.setVisibility(View.GONE);
        buttonOutOfRadius.setVisibility(View.GONE);

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

    // Google
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void retrofitGoogleGeolocation() {
        String apiKey = getString(R.string.map_api);

        Call<ResponseBody> call = new ApiClient().getApiService().cekGeolocation(apiKey, "");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String responseContent = new String(response.body().bytes());
                        JSONObject jsonObject = new JSONObject(responseContent);
                        //Log.e(TAG, "onResponse: Geocoding -> "+ jsonObject.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            JSONObject jsonData0 = jsonArray.getJSONObject(0);
                            //String j_formatted_address = jsonObject1.getString("formatted_address");
                            j_formatted_address = jsonData0.getString("formatted_address");
                            //j_place_id = jsonData0.getString("place_id");
                            //Log.e(TAG, "onResponse: Geocoding -> " + j_formatted_address);
                        }

                        //Log.e(TAG, "onResponse: Place ID -> "+j_place_id);
                        //retrofitGoogleGeocodingPlaces(j_place_id);

                        new_address = j_formatted_address;

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "onResponse: Geocoding -> " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Geocoding -> " + t.getMessage());
            }
        });
    }

    private void retrofitGoogleGeocoding(double lat, double lng) {
        String coordinate = lat + "," + lng;
        String apiKey = getString(R.string.map_api);

        Call<ResponseBody> call = new ApiClient().getApiService().cekLocationName(coordinate, apiKey);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String responseContent = new String(response.body().bytes());
                        JSONObject jsonObject = new JSONObject(responseContent);
                        //Log.e(TAG, "onResponse: Geocoding -> "+ jsonObject.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            JSONObject jsonData0 = jsonArray.getJSONObject(0);
                            //String j_formatted_address = jsonObject1.getString("formatted_address");
                            j_formatted_address = jsonData0.getString("formatted_address");
                            //j_place_id = jsonData0.getString("place_id");
                            //Log.e(TAG, "onResponse: Geocoding -> " + j_formatted_address);
                        }

                        //Log.e(TAG, "onResponse: Place ID -> "+j_place_id);
                        //retrofitGoogleGeocodingPlaces(j_place_id);

                        new_address = j_formatted_address;

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "onResponse: Geocoding -> " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Geocoding -> " + t.getMessage());
            }
        });
    }

//    Geocoding plus
//    private void retrofitGoogleGeocodingPlaces(String placeId) {
//        String apiKey = getString(R.string.map_api);
//
//        Call<ResponseBody> call = new ApiClient().getApiService().cekLocationNamePlaces(placeId, apiKey);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.body() != null) {
//                    try {
//                        //noinspection ConstantConditions
//                        String responseContent = new String(response.body().bytes());
//                        JSONObject jsonObject = new JSONObject(responseContent);
//                        JSONObject jsonObjectResult = jsonObject.getJSONObject("result");
//
//                        String name = jsonObjectResult.getString("name");
//                        Log.e(TAG, "onResponse: Name -> " + name);
//
//                        //new_address = name;
//
//
//                    } catch (IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    Log.e(TAG, "onResponse: Geocoding -> " + response.errorBody());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "onFailure: Geocoding -> " + t.getMessage());
//            }
//        });
//    }

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
                                            Picasso.with(CheckinV1Activity.this)
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
                        Log.e(TAG, "onError Client: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void glideURL(String s_url_image, ImageView ivProfile) {
        Glide.with(this)
                .load(s_url_image)
                .apply(new RequestOptions()
                        .circleCrop()
                        .dontAnimate()
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.profile_default_picture)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .priority(Priority.HIGH))
                .into(ivProfile);
    }

//    private void picassoProfile(String s_url_image, ImageView ivProfile) {
//        Picasso.with(this)
//                .load(s_url_image)
//                .noFade()
//                .centerInside()
//                .resize(400, 400)
//                .into(new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                        byte[] imageInByte = baos.toByteArray();
//                        String saveThis = Base64.encodeToString(imageInByte, Base64.NO_WRAP);
//
//                        Log.e(TAG, "onBitmapLoaded: " + s_url_image);
//                        Log.e(TAG, "onBitmapLoaded: " + sh_url_image);
//
//                        if (!sh_url_image.equals(s_url_image) || sh_url_image.isEmpty()) {
//
//                            SharedPreferences preferences = getSharedPreferences("PREFS_PHOTO", 0);
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putString("sh_url_image", s_url_image);
//                            editor.putString("sh_image", saveThis);
//                            editor.apply();
//
//                            SharedPreferences settings = getSharedPreferences("PREFS_PHOTO", 0);
//                            sh_image = settings.getString("sh_image", "");
//                            sh_url_image = settings.getString("sh_url_image", "");
//                            byte_image = Base64.decode(sh_image, Base64.NO_WRAP);
//
//                            Bitmap bmp = BitmapFactory.decodeByteArray(byte_image, 0, byte_image.length);
//                            ivProfile.setImageBitmap(bmp);
//                        }
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                    }
//                });
//    }

    private HashMap<String, String> getParamsGreeting() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("type", s_type_attendance);
        //Log.e(TAG, "getParamsGreeting: " + params);
        return params;
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
                                            //Log.e(TAG, "onNext message greetings: " + message_greetings);
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
                            Toast.makeText(CheckinV1Activity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        showGreetings();
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
                        //Log.e(TAG, "onResponse Status Checkin after click: " + contentResponse);
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
                Toast.makeText(CheckinV1Activity.this, "Please check your internet & try again", Toast.LENGTH_SHORT).show();
                dismissProgress();
                Crashlytics.log(user_fullname + " " + t.getCause());
            }
        });
    }

    private void retrofitRealTime() {
        mHandler.removeCallbacks(mUpdate); // testing
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
                                //Log.e(TAG, "onNext Realtime: " + response);

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

                                        String time = serverTime.substring(11);
                                        String[] parts = time.split(":");
                                        part1_time = parts[0];
                                        part2_time = parts[1];
                                        part3_time = parts[2];

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
                        mHandler.removeCallbacks(mUpdate);
                    }

                    @Override
                    public void onComplete() {
                        startDigitalClock();
                    }
                });
    }

    private void startDigitalClock() {
        mHour = Integer.parseInt(part1_time);
        mMinute = Integer.parseInt(part2_time);
        mSecond = Integer.parseInt(part3_time);

        mUpdate = new Runnable() {
            @Override
            public void run() {

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

                String s_mHour;
                String s_mMinute;
                String s_mSecond;

//                s_mHour = String.valueOf(mHour);
//                s_mMinute = String.valueOf(mMinute);
//                s_mSecond = String.valueOf(mSecond);

                if (mHour < 10) {
                    s_mHour = "0" + String.valueOf(mHour);
                } else {
                    s_mHour = String.valueOf(mHour);
                }

                if (mMinute < 10) {
                    s_mMinute = "0" + String.valueOf(mMinute);
                } else {
                    s_mMinute = String.valueOf(mMinute);
                }

                if (mSecond < 10) {
                    s_mSecond = "0" + String.valueOf(mSecond);
                } else {
                    s_mSecond = String.valueOf(mSecond);
                }

                String logTime = s_mHour + ":" + s_mMinute + ":" + s_mSecond;
                tvDigitalClock.setText(logTime);

                //if (mHour > 17) {
                //stopLiveTracking();
                //  stopLocationUpdates();
                //}

                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.post(mUpdate);
    }

    private void retrofitCheckin(Uri fileUri) throws JSONException {
        showProgress();
        Log.e(TAG, "retrofitCheckin: Site name -> " + site_name);

        String filePath = getRealPathFromUri(fileUri);
        //noinspection ConstantConditions
        File file = new File(filePath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);

        // add lat long
        RequestBody requestToken = RequestBody.create(MediaType.parse("multipart/form-data"), token);
        RequestBody requestTimezone = RequestBody.create(MediaType.parse("multipart/form-data"), s_gmt);
        RequestBody requestTimezoneId = RequestBody.create(MediaType.parse("multipart/form-data"), presenter.getTimeZoneId());
        RequestBody requestLocationName = RequestBody.create(MediaType.parse("multipart/form-data"), site_name);
        RequestBody requestInArea = RequestBody.create(MediaType.parse("multipart/form-data"), in_area);
        RequestBody requestRemark = RequestBody.create(MediaType.parse("multipart/form-data"), s_remark);

        RequestBody requestSiteId = RequestBody.create(MediaType.parse("multipart/form-data"), site_id);
        RequestBody requestLat = RequestBody.create(MediaType.parse("multipart/form-data"), site_lat);
        RequestBody requestLng = RequestBody.create(MediaType.parse("multipart/form-data"), site_long);

        Observable<ResponseBody> call = new ApiClient().getApiService().recordWithPhoto(
                requestToken,
                requestLocationName,
                requestTimezone,
                requestTimezoneId,
                requestInArea,
                requestRemark,
                requestSiteId,
                requestLat,
                requestLng,
                body);

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
                        Toast.makeText(CheckinV1Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Crashlytics.log(user_fullname + " " + e.getCause());

                        if (dialogRemark != null)
                            dialogRemark.dismiss();

                    }

                    @Override
                    public void onComplete() {
                        dismissProgress();

                        if (dialogRemark != null)
                            dialogRemark.dismiss();
                    }
                });
    }

    private void parseJSONCheckin(ResponseBody responseBody) {
        try {
            String mResponse = responseBody.string();
            JSONObject jsonObject = new JSONObject(mResponse);
            String s_response_code = jsonObject.getString("response_code");
            String s_message = jsonObject.getString("message");

            if (s_response_code.equals("200")) {
                retrofitCheckinStatusAfterClick();
                presenter.retrofitCheckinStatus();
                presenter.startTracking();
            } else {
                Toast.makeText(this, s_message, Toast.LENGTH_SHORT).show();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void retrofitListSite(LocationResult locationResult) {
        Call<ResponseBody> call = new ApiClient().getApiService().siteList(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String content = new String(response.body().bytes());
                        JSONObject jsonObject = new JSONObject(content);

                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        Log.e(TAG, "onResponse Site Total: " + jsonArray.length());
                        offices.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String lat = jsonObject1.getString("lat");
                            String lng = jsonObject1.getString("long");
                            String radius = jsonObject1.getString("radius");
                            //noinspection unused
                            String city = jsonObject1.getString("city");
                            String site_name = jsonObject1.getString("site_name");
                            String site_id = jsonObject1.getString("site_id");

                            Office office = new Office();
                            office.setOc_lat(lat);
                            office.setOc_long(lng);
                            office.setOc_site(site_name);
                            office.setOc_radius(radius);
                            office.setOc_id(site_id);
                            offices.add(office);
                        }

                        setMarker(locationResult);
                        locationChanged(locationResult);
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
        Log.e(TAG, "onResume: ");

        // Timezone
        presenter.retrofitGoogleTimezone(myLat, myLong);

        // Map
        if (mGoogleApiClient != null && mFusedLocationClient != null) {
            requestLocationUpdates();
        } else {
            buildGoogleApiClient();
        }

        presenter.retrofitCheckinStatus();
        retrofitRealTime();
    }

//    void startLiveTracking() {
//        /*
//             1 * 1000 = 1 second
//             1 * 60 * 1000 = 1 minute
//             60 * 60 * 1000 = 60 minute
//        */
//        //long intervalMinute = 60 * 1000;
//        long intervalMinute = 30 * 60 * 1000; // 60 Minute
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        Intent intent = new Intent(this, GPSTracker.class);
//        intent.putExtra("isStart", "true");
//        intent.putExtra("gmt", s_gmt);
//        intent.putExtra("timezone_id", timeZoneId);
//        intent.putExtra("a_lat", String.valueOf(myLat));
//        intent.putExtra("a_long", String.valueOf(myLong));
//        startService(intent);
//
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
//
//        assert alarmManager != null;
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMinute, pendingIntent);
//    }

    void stopLiveTracking() {
        Log.e(TAG, "stopLiveTracking: ");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, GPSTracker.class);
        stopService(intent);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setPadding(0, 500, 0, 0);

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

//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(new LatLng(myLat, myLong))
//                .zoom(17)
//                .build();
//        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        presenter.retrofitGoogleTimezone(myLat, myLong);
    }

    @OnClick({R.id.iv_profile, R.id.fabRefresh, R.id.fabMyLocation, R.id.frameDate, R.id.dataLayout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_profile:
                startDialogProfile();
                break;
//            case R.id.fabAttendance:
//                startActivity(new Intent(this, AttendanceRecordActivity.class));
//                break;
            case R.id.dataLayout:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
            case R.id.fabRefresh:
                finish();
                startActivity(getIntent());
//                stopLocationUpdates();
//                requestLocationUpdates();
//                retrofitListSite();
                //locationCall();
//                retrofitCheckinStatus();
                break;
            case R.id.fabMyLocation:
                myLocation();
                break;
            case R.id.frameDate:
                startActivity(new Intent(this, AttendanceRecordActivity.class));
                break;
//            case R.id.linear_first_record:
//                startActivity(new Intent(this, AttendanceRecordActivity.class));
//                break;
//            case R.id.linear_last_record:
//                startActivity(new Intent(this, AttendanceRecordActivity.class));
//                break;
        }
    }

    private void initView(View view) {
        ivGreeting = view.findViewById(R.id.ivGreeting);
        tvGreetings = view.findViewById(R.id.tvGreetings);
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getVersion() {
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

    @Override
    public void setTextDate(String value) {
        tvDate.setText(value);
    }

    @Override
    public void setTextStartTime(String value) {
        tvStartTime.setText(value);
    }

    @Override
    public void setTextEndTime(String value) {
        tvEnd.setText(value);
    }

    @Override
    public void setTextFirstLocation(String value) {
        tvFirstLocation.setText(value);
    }

    @Override
    public void setTextLastLocation(String value) {
        tvLastLocation.setText(value);
    }

    @Override
    public void setTextRemarkFirst(String value) {
        tvFirstRemark.setText(value);
    }

    @Override
    public void setTextRemarkLast(String value) {
        tvLastRemark.setText(value);
    }

    @Override
    public void setColorStart(@ColorInt int colorStart) {
        tvStartTime.setTextColor(ColorStateList.valueOf(colorStart));
    }

    @Override
    public void showlinearRemarkFirst() {
        linearRemarkFirst.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidelinearRemarkFirst() {
        linearRemarkFirst.setVisibility(View.GONE);
    }

    @Override
    public void showlinearRemarkLast() {
        linearRemarkLast.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidelinearRemarkLast() {
        linearRemarkLast.setVisibility(View.GONE);
    }

    @Override
    public void showContainerTime() {
        linearTimePlace.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideContainerTime() {
        linearTimePlace.setVisibility(View.GONE);
    }

    @Override
    public void hideDialogProfile() {
        if (dialogProfile != null)
            dialogProfile.dismiss();
    }

    @Override
    public void hideDialogLogout() {
        if (dialogLogout != null)
            dialogLogout.dismiss();
    }

    @Override
    public void showDialogVersion() {
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

    @Override
    public void backToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void picassoClient(String url) {
        Picasso.with(CheckinV1Activity.this)
                .load(url)
                .fit()
                .into(iv_logo_client);
    }

    @Override
    public String getLat() {
        return String.valueOf(gpsTracker.getLatitude());
    }

    @Override
    public String getLong() {
        return String.valueOf(gpsTracker.getLongitude());
    }
}
