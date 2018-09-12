package com.elabram.lm.wmsmobile;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.elabram.lm.wmsmobile.model.Office;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.AppInfo;
import com.elabram.lm.wmsmobile.utilities.GPSTracker;
import com.elabram.lm.wmsmobile.utilities.WordUtils;
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
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.isOnline;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_address;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_mobile;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_nip;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_phone;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.position;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.user_email;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.user_fullname;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.dateNow;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isAfternoon;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isDay;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isEarly;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isEvening;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isEvening1;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isLate1;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isMorning;
import static com.elabram.lm.wmsmobile.utilities.TimeGreetings.isOntime;

public class CheckinV1Activity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    private String s_timeStamp;
    private String s_gmt;

    @BindView(R.id.iv_logo_client)
    ImageView iv_logo_client;

    @BindView(R.id.iv_profile)
    CircularImageView iv_profile_main;

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
    private String timeZoneId;
    private String rawOffset;

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

    //private CircularImageView iv_profile_dialog;
    private ImageView iv_profile_dialog;

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

    /**
     * Stop Service in Background X
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e(TAG, "onCreate: ");

        checkingPermission();

        setContentView(R.layout.activity_checkin_v1);
        ButterKnife.bind(this);

        getSharedUserDetail();
        retrofitReadClient();

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

        locationCall();

        refreshConnectionClick();
        setGreetingsFirstTime();

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

        retrofitReadProfile(iv_profile_main);
    }

    private void locationCall() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                setMarker(locationResult);
                locationChanged(locationResult);
            }
        };
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

        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_id = view.findViewById(R.id.tv_id);
        TextView tv_position = view.findViewById(R.id.tv_position);
        TextView tv_email = view.findViewById(R.id.tv_email);
        TextView tv_mobile = view.findViewById(R.id.tv_mobile);
        TextView tv_phone = view.findViewById(R.id.tv_phone);

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

        setDataProfile(tv_name, tv_id, tv_position, tv_email, tv_mobile, tv_phone);

        linear_about.setOnClickListener(view1 -> startActivity(new Intent(this, AboutActivity.class)));

        linear_change.setOnClickListener(view1 -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        linear_feedback.setOnClickListener(view1 -> startActivity(new Intent(this, FeedbackActivity.class)));

        linear_logout.setOnClickListener(view1 -> dialogLogout());

        iv_change_profile.setOnClickListener(view1 -> startDialogGalleryCamera());
        iv_profile_dialog.setOnClickListener(view1 -> startDialogGalleryCamera());

    }

    private void startDialogRemark(Uri bitmapURI) {
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

        relSubmit.setOnClickListener(view1 -> {
            s_remark = etRemark.getText().toString();
            Log.e(TAG, "startDialogRemark: " + s_remark);

            if (s_remark.length() <= 0) {
                etRemark.setError("Remark is required!");
            } else {
                retrofitCheckin(bitmapURI);
            }
        });

        relCancel.setOnClickListener(view1 -> dialogRemark.cancel());
    }

//    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        Matrix matrix = new Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        Bitmap resizedBitmap = Bitmap.createBitmap(
//                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
//        return resizedBitmap;
//    }

//    private Bitmap getAspectRatio(Bitmap bm) {
//        float aspectRatio = bm.getWidth() /
//                (float) bm.getHeight();
//        int width = 400;
//        int height = Math.round(width / aspectRatio);
//
//        Bitmap bitmap = Bitmap.createScaledBitmap(bm, width, height, false);
//        //bm.recycle();
//
//        // if using heigh
//        //int height = 480;
//        //int width = Math.round(height * aspectRatio);
//        return bitmap;
//    }

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
                                Log.e(TAG, "onNext Profile: " + mResponse);
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String response_code = jsonObject.getString("response_code");
                                switch (response_code) {
                                    case "401":
                                        String message = jsonObject.getString("message");
                                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "200":
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        s_url_image = jsonObject1.getString("user_image");
                                        Log.e(TAG, "onNext Profile: " + s_url_image);

                                        if (s_url_image.isEmpty()) {
                                            ivProfile.setImageDrawable(ContextCompat.getDrawable(
                                                    CheckinV1Activity.this, R.drawable.profile_default_picture));
                                        } else {
                                            glideURL(s_url_image, ivProfile);
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
                        Log.e(TAG, "onError Profile: " + e.getCause());
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(CheckinV1Activity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CheckinV1Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete Profile: ");
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

    private void setDataProfile(TextView tv_name, TextView tv_id, TextView tv_position,
                                TextView tv_email, TextView tv_mobile, TextView tv_phone) {

        tv_name.setText(WordUtils.capitalize(user_fullname));
        tv_id.setText(mem_nip);
        tv_position.setText(position);
        tv_email.setText(user_email);
        tv_mobile.setText(mem_mobile);
        tv_phone.setText(mem_phone);


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

            try {
                Bitmap compressor = new Compressor(this).compressToBitmap(file1);

                ExifInterface exif = new ExifInterface(realPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Matrix matrix = new Matrix();
                if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                    matrix.postRotate(180);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    matrix.postRotate(90);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    matrix.postRotate(270);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                }

                Uri bitmapURI = getImageUri(this, compressor);
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
                        Bitmap compressor = new Compressor(this).compressToBitmap(file1);

                        ExifInterface exif = new ExifInterface(selectedImagePath);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        Matrix matrix = new Matrix();
                        if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                            matrix.postRotate(180);
                            compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                                    compressor.getHeight(), matrix, true);
                        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                            matrix.postRotate(90);
                            compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                                    compressor.getHeight(), matrix, true);
                        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                            matrix.postRotate(270);
                            compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                                    compressor.getHeight(), matrix, true);
                        }

                        Uri bitmapURI = getImageUri(this, compressor);
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
                Bitmap compressor = new Compressor(this).compressToBitmap(file1);

                ExifInterface exif = new ExifInterface(realPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Matrix matrix = new Matrix();
                if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                    matrix.postRotate(180);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    matrix.postRotate(90);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    matrix.postRotate(270);
                    compressor = Bitmap.createBitmap(compressor, 0, 0, compressor.getWidth(),
                            compressor.getHeight(), matrix, true);
                }

                Uri bitmapURI = getImageUri(this, compressor);

                if (in_area.equals("N")) {
                    startDialogRemark(bitmapURI);
                } else {
                    retrofitCheckin(bitmapURI);
                }

                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
            showProgress();
            retrofitReadSiteList();
            retrofitCheckinStatus();
            retrofitReadContract();
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mUpdate);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

            // count the office
            if (offices.size() > 0) {
                for (int k = 0; k < offices.size(); k++) {
                    Office office = offices.get(k);

                    Double d_lat = Double.valueOf(office.getOc_lat());
                    Double d_long = Double.valueOf(office.getOc_long());
                    Double d_radius = Double.valueOf(office.getOc_radius());

                    // Check the distance "MyLocation" to "OfficeLocation"
                    Location.distanceBetween(lat, lng, d_lat, d_long, resultApi);
                    String s_distanceToOffice = String.valueOf(resultApi[0]);
                    Double d_distanceToOffice = Double.parseDouble(s_distanceToOffice);
                    Log.e(TAG, "locationChanged: Distance -> " + d_distanceToOffice);

                    // checkin in radius
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
                            processNoMock(lat, lng, "In Radius", site_name);
                        }
                        stopLocationUpdates();
                        break;
                    }
                    // checkin out radius
                    else if (d_distanceToOffice > d_radius) {
                        progressBarPosition.setVisibility(View.GONE);
                        buttonOutOfRadius.setVisibility(View.GONE);
                        buttonRecord.setVisibility(View.VISIBLE);

                        if (isMock) {
                            processTheMock();
                        } else {
                            retrofitGoogleGeocoding(lat, lng);
                            in_area = "N";
                            site_name = new_address;
                            Log.e(TAG, "locationChanged: New Address -> " + site_name);
                            processNoMock(lat, lng, "Out Radius", site_name);
                        }
                    }

                }

            }
            // no office
            else {
                buttonOutOfRadius.setVisibility(View.GONE);
                buttonRecord.setVisibility(View.VISIBLE);

                // checking fake gps
                if (isMock) {
                    processTheMock();
                } else {
                    retrofitGoogleGeocoding(lat, lng);
                    in_area = "Y";
                    site_name = new_address;
                    processNoMock(lat, lng, "No Office", site_name);
                }
                break;
            }
        }
        stopLocationUpdates();
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

        Glide.get(this).clearMemory();

        if (mFusedLocationClient != null) {
            stopLocationUpdates();
            map.clear();
        }

        mHandler.removeCallbacks(mUpdate);

        dismissAlert();

        if (disposable != null) {
            disposable.dispose();
        }

    }

    private void processNoMock(Double lat, Double lng, String status, String new_address) {
        Log.e(TAG, "processNoMock: " + status);
        buttonRecord.setOnClickListener(view -> {
            // checking online
            if (isOnline(this)) {
                s_gmt = checkTimezoneGMT();

                // checkin timezone
                if (!s_gmt.isEmpty()) {
                    Log.e(TAG, "processNoMock: CheckinClick -> " + new_address);
                    // take photo & checkin
                    takePhoto(new_address);
                } else {
                    retrofitGoogleTimezone(lat, lng);
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

        Uri dirUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".com.elabram.lm.wmsmobile", f);
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

    private void retrofitGoogleTimezone(double lat, double lng) {
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
                        //Log.e(TAG, "onResponse Timezone: " + responseContent);

                        JSONObject jsonObject = new JSONObject(responseContent);
                        //cache_timeZone_name = jsonObject.getString("timeZoneName");
                        timeZoneId = jsonObject.getString("timeZoneId");
                        rawOffset = jsonObject.getString("rawOffset");

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    s_gmt = checkTimezoneGMT();
                    Log.e(TAG, "onResponse: Timezone -> "+s_gmt);

                    SharedPreferences preferences = getSharedPreferences("PREFS_TIMEZONE", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("s_gmt", s_gmt);
                    editor.putString("s_timezone_id", timeZoneId);
                    editor.apply();

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
                            //Log.e(TAG, "onResponse: Geocoding -> " + j_formatted_address);
                        }

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
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
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

                                @SuppressLint("SimpleDateFormat") SimpleDateFormat read = new SimpleDateFormat("dd MMMM yyyy");
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat write = new SimpleDateFormat("EEEE, dd MMMM yyyy");
                                Date date;
                                String s_dayformat = null;
                                try {
                                    date = read.parse(realtime_date);
                                    s_dayformat = write.format(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                tvDate.setText(s_dayformat);

                                String replaceFirst = time_first.replace(".", ":");
                                String replaceLast = time_last.replace(".", ":");
                                // ~

                                // Time Start
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat readTime1 = new SimpleDateFormat("HH.mm");
                                Date checkTime1 = null;
                                try {
                                    checkTime1 = new SimpleDateFormat("HH.mm").parse("08.00");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date dateTime1 = null;
                                try {
                                    dateTime1 = readTime1.parse(time_first);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (!time_first.isEmpty()) {

                                    assert dateTime1 != null;
                                    if (dateTime1.after(checkTime1)) {
                                        Log.e(TAG, "onResponse: RED");
                                        tvStartTime.setTextColor(Color.RED);
                                    } else {
                                        Log.e(TAG, "onResponse: BLUE");
                                        tvStartTime.setTextColor(getResources().getColor(R.color.blue));
                                    }


                                    tvStartTime.setText(replaceFirst);
                                    linearTimePlace.setVisibility(View.VISIBLE);
                                } else {
                                    tvStartTime.setText("-");
                                    linearTimePlace.setVisibility(View.INVISIBLE);
                                }

                                // Time Last
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
                Log.e(TAG, "onFailure Checkin Status: " + t.getCause());
                Toast.makeText(CheckinV1Activity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
                                        //Log.e(TAG, "onNext serverTime:" + serverTime);
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

                if (mHour >= 17) {
                    stopLiveTracking();
                }

                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.post(mUpdate);
    }

    private void retrofitCheckin(Uri fileUri) {
        showProgress();
        Log.e(TAG, "retrofitCheckin: Site name -> " + site_name);

        String filePath = getRealPathFromUri(fileUri);
        //noinspection ConstantConditions
        File file = new File(filePath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);

        RequestBody requestToken = RequestBody.create(MediaType.parse("multipart/form-data"), token);
        RequestBody requestTimezone = RequestBody.create(MediaType.parse("multipart/form-data"), s_gmt);
        RequestBody requestTimezoneId = RequestBody.create(MediaType.parse("multipart/form-data"), timeZoneId);
        RequestBody requestLocationName = RequestBody.create(MediaType.parse("multipart/form-data"), site_name);
        RequestBody requestInArea = RequestBody.create(MediaType.parse("multipart/form-data"), in_area);
        RequestBody requestRemark = RequestBody.create(MediaType.parse("multipart/form-data"), s_remark);

        Observable<ResponseBody> call = new ApiClient().getApiService().recordWithPhoto(
                requestToken,
                requestLocationName,
                requestTimezone,
                requestTimezoneId,
                requestInArea,
                requestRemark,
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
                retrofitCheckinStatus();
                startLiveTracking();
            } else {
                Toast.makeText(this, s_message, Toast.LENGTH_SHORT).show();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void retrofitReadSiteList() {
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

        // Timezone
        retrofitGoogleTimezone(myLat, myLong);

        // Map
        if (mGoogleApiClient != null && mFusedLocationClient != null) {
            requestLocationUpdates();
        } else {
            buildGoogleApiClient();
        }

        retrofitRealTime();
    }

    void startLiveTracking() {
        /*
             1 * 1000 = 1 second
             1 * 60 * 1000 = 1 minute
             60 * 60 * 1000 = 60 minute
        */
        long intervalMinute = 60 * 60 * 1000; // 60 Minute
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, GPSTracker.class);
        intent.putExtra("isStart", "true");
        intent.putExtra("gmt", s_gmt);
        intent.putExtra("timezone_id", timeZoneId);
        intent.putExtra("a_lat", String.valueOf(myLat));
        intent.putExtra("a_long", String.valueOf(myLong));
        startService(intent);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMinute, pendingIntent);
    }

    void stopLiveTracking() {
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

        retrofitGoogleTimezone(myLat, myLong);
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

}
