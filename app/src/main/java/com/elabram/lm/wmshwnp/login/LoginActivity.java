package com.elabram.lm.wmshwnp.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.checkin.CheckinV1Activity;
import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.utilities.AppInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGGED;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.isOnline;

public class LoginActivity extends AppCompatActivity implements LoginView {

    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.etPassword)
    EditText etPassword;

    @BindView(R.id.containerMain)
    RelativeLayout containerMain;

    @BindView(R.id.buttonLogin)
    Button buttonLogin;

    private LoginPresenterImpl presenter;
    private Activity mActivity = LoginActivity.this;
    private ProgressDialog progressDialog;

    private String TAG = LoginActivity.class.getSimpleName();
    private String imei;
    private AlertDialog adVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        // Init
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        LoginRepository loginRepository = new LoginRepository(this);
        presenter = new LoginPresenterImpl(this, loginRepository);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Method
        checkingPermission();
        presenter.retrofitCheckVersion();
        buttonLogin.setOnClickListener(view -> login());
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

    private void dismissAlert() {
        if (adVersion != null && adVersion.isShowing()) {
            adVersion.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        dismissAlert();
    }

    @Override
    public void dialogCheckVersion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                } else {
                    ActivityCompat.finishAffinity(this);
                }
            }
        });

    }

    @Override
    public void showToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HardwareIds")
    public String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "Please allow the all permission to generate your account for login", Toast.LENGTH_SHORT).show();
                //Log.e(TAG, "getDeviceId: " + "Permission Deactivated");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 333);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            return null;
        }

        //noinspection ConstantConditions
        return telephonyManager.getDeviceId().trim();
    }

    private void login() {
        if (validate()) {
            if (isOnline(this)) {
                checkingPermission();
                presenter.login();
            } else {
                showToast("Check your internet connection");
            }
        }
    }

    private void checkingPermission() {
        int PERMISSION_ALL = 15;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };

        if (!AppInfo.hasPermissions(mActivity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(mActivity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkingPermission();

        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, 0);
        boolean loggin = preferences.getBoolean(PREFS_LOGGED, false);
        if (loggin) {
            gotoCheckinV1Activity();
        }

    }

    @Override
    public void showDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    @Override
    public void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public String getTextUser() {
        return etUsername.getText().toString();
    }

    @Override
    public String getTextPass() {
        return etPassword.getText().toString();
    }

    @Override
    public String getImei() {
        return getDeviceId();
    }

    @Override
    public String getVersion() {
        return getVersionInfo();
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(containerMain, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void gotoCheckinV1Activity() {
        dismissDialog();
        startActivity(new Intent(mActivity, CheckinV1Activity.class));
        this.finish();
    }

    private boolean validate() {
        boolean valid = true;

        if (getTextUser().length() <= 0) {
            etUsername.setError("email is required");
            valid = false;
        }

        if (getTextPass().length() <= 0) {
            etPassword.setError("password is required");
            valid = false;
        }

        return valid;
    }
}
