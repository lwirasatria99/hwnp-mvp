package com.elabram.lm.wmshwnp;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.change_password.ChangePasswordActivity;
import com.elabram.lm.wmshwnp.login.LoginActivity;
import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.utilities.AppInfo;
import com.elabram.lm.wmshwnp.utilities.WordUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_address;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_mobile;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_nip;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_phone;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.position;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_email;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_fullname;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.iv_logo_client)
    ImageView iv_logo_client;

    @BindView(R.id.relative_attendance_record)
    RelativeLayout relative_attendance_record;

    @BindView(R.id.relative_mobile_attendance)
    RelativeLayout relative_mobile_attendance;

    @BindView(R.id.container)
    LinearLayout rootView;

    @BindView(R.id.iv_profile)
    ImageView iv_profile_main;

    private AlertDialog dialogProfile;
    private AlertDialog dialogLogout;
    private AlertDialog adVersion;
    private Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        getSharedUserDetail();
        Crashlytics.log(TAG + " " + user_email);

        if (AppInfo.isOnline(this)) {
            Log.e(TAG, "onCreate: from App Connected");
        } else {
            Log.e(TAG, "onCreate: from App No Connection");
        }

        retrofitCheckVersion();

        retrofitReadClient();

        relative_attendance_record.setOnClickListener(view -> startActivity(new Intent(this, AttendanceRecordActivity.class)));

        relative_mobile_attendance.setOnClickListener(view -> startActivity(new Intent(this, CheckinActivity.class)));

        // Profile Picture
        if (mem_image.equals("https://elabram.com/hris/files/employee/") || mem_image.isEmpty()) {
            iv_profile_main.setImageResource(R.drawable.profile_default_picture);
        } else {
            Picasso.with(this)
                    .load(mem_image)
                    .fit()
                    .into(iv_profile_main);
        }

        openDialogProfile();
    }

    private void openDialogProfile() {
        iv_profile_main.setOnClickListener(view -> dialogProfile());
    }

    private void dialogProfile() {
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

        builder.setView(view);
        dialogProfile = builder.create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        //noinspection ConstantConditions
        dialogProfile.getWindow().setBackgroundDrawable(inset);
        dialogProfile.show();

        setDetailsForm(tv_name, tv_id, tv_position, tv_email, tv_mobile, tv_phone, iv_profile);

//        fab_about.setOnClickListener(view1 -> startActivity(new Intent(this, AboutActivity.class)));

        linear_logout.setOnClickListener(view1 -> dialogLogout());

        linear_change.setOnClickListener(view1 -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        linear_feedback.setOnClickListener(view1 -> startActivity(new Intent(this, FeedbackActivity.class)));
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

    private void retrofitCheckVersion() {
        Call<ResponseBody> call = new ApiClient().getApiService().checkVersion();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        //noinspection ConstantConditions
                        String mResponse = new String(response.body().bytes());
                        Log.e(TAG, "onResponse: CheckVersion " + mResponse);
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

    private void dismissAlert() {
        if (adVersion != null && adVersion.isShowing()) {
            adVersion.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissAlert();

        if (disposable != null)
            disposable.dispose();
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
                finishAndRemoveTask();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                } else {
                    ActivityCompat.finishAffinity(this);
                }
            }
        });

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

    private void setDetailsForm(TextView tv_name, TextView tv_id, TextView tv_position, TextView tv_email, TextView tv_mobile, TextView tv_phone, ImageView iv_profile) {
        tv_name.setText(WordUtils.capitalize(user_fullname));
        tv_id.setText(mem_nip);
        tv_position.setText(position);
        tv_email.setText(user_email);
        tv_mobile.setText(mem_mobile);
        tv_phone.setText(mem_phone);

        // Profile Picture
        if (mem_image.equals("https://elabram.com/hris/files/employee/") || mem_image.isEmpty()) {
            iv_profile.setImageResource(R.drawable.profile_default_picture);
        } else {
            Picasso.with(this)
                    .load(mem_image)
                    .fit()
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

    public void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_nip = preferences.getString("mem_nip", "");
        mem_mobile = preferences.getString("mem_mobile", "");
        mem_phone = preferences.getString("mem_phone", "");
        mem_image = preferences.getString("mem_image", "");
        mem_address = preferences.getString("mem_address", "");
        position = preferences.getString("position", "");
        user_fullname = preferences.getString("name", "");
        user_email = preferences.getString("email", "");
        Log.e(TAG, "getSharedUserDetail: ProfileImage " + mem_image);
    }

//    BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = item -> {
//        Fragment fragment;
//        switch (item.getItemId()) {
//            case R.id.navigation_home:
//                fragment = new MainHomeFragment();
//                replaceFragment(fragment);
//                return true;
//            case R.id.navigation_dashboard:
//                fragment = new MainAboutFragment();
//                replaceFragment(fragment);
//                return true;
//            case R.id.navigation_notifications:
//                fragment = new MainProfileFragment();
//                replaceFragment(fragment);
//                return true;
//        }
//        return false;
//    };

//    private void replaceFragment(Fragment fragment) {
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.frameContainer, fragment);
//        transaction.commit();
//    }

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
                                            Picasso.with(MainActivity.this)
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

}
