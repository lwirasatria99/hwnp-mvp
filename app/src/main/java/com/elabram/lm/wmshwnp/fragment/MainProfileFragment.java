package com.elabram.lm.wmshwnp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.login.LoginActivity;
import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.utilities.WordUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_address;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_mobile;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_nip;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_phone;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.position;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;


/**
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainProfileFragment extends Fragment {

    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvNIK)
    TextView tvNIK;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvMobile)
    TextView tvMobile;
    @BindView(R.id.tvTelephone)
    TextView tvTelephone;
    @BindView(R.id.tvPosition)
    TextView tvPosition;
    @BindView(R.id.tvAuthority)
    TextView tvAuthority;

    @BindView(R.id.ivLogoClient)
    ImageView ivLogoClient;
    @BindView(R.id.ivProfil)
    ImageView ivProfile;

    @BindView(R.id.buttonLogout)
    RelativeLayout buttonLogout;
    @BindView(R.id.rootView)
    RelativeLayout rootView;

    private Activity mActivity;
    private AlertDialog dialogLogout;

    private static final String TAG = MainProfileFragment.class.getSimpleName();

    private String user_fullname;
    private String user_email;
    private Disposable disposable;
    //private ProgressDialog progressDialog;


    public MainProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil2, container, false);
        ButterKnife.bind(this, view);
        Fabric.with(mActivity, new Crashlytics());

//        progressDialog = new ProgressDialog(mActivity, R.style.AppThemeLoading);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Please Wait...");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getSharedUserDetail();
        setDetailsForm();
        retrofitReadClient();
//        SharedPreferences preferences = mActivity.getSharedPreferences("LOGO", 0);
//        String urlImage = preferences.getString("url", "");
//        if (!urlImage.equals("https://elabram.com/hris/")) {
//            Picasso.with(mActivity)
//                    .load(urlImage)
//                    .fit()
//                    .into(ivLogoClient);
//        }
        buttonLogout.setOnClickListener(view1 -> dialogOpenLogout());
    }

//    private void showDialog() {
//        if (progressDialog != null) {
//            progressDialog.show();
//        }
//    }

//    private void dismissDialog() {
//        if (progressDialog != null) {
//            progressDialog.dismiss();
//        }
//    }

    private void dialogOpenLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
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
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        // Clear Activity And Fragment
        dialogLogout.dismiss();
        assert getFragmentManager() != null;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = new Intent(mActivity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    private void setDetailsForm() {
        tvName.setText(WordUtils.capitalize(user_fullname));
        tvAuthority.setText(position);

        tvNIK.setText(mem_nip);
        tvPosition.setText(position);
        tvEmail.setText(user_email);
        tvMobile.setText(mem_mobile);
        tvTelephone.setText(mem_phone);

        // Profile Picture
        if (mem_image.equals("https://elabram.com/hris/files/employee/")) {
            ivProfile.setImageResource(R.drawable.profile_default_picture);
        } else {
            Picasso.with(mActivity)
                    .load(mem_image)
                    .fit()
                    .into(ivProfile);
        }

        // ID
        if (mem_nip.length() == 0) {
            tvNIK.setText("-");
        }

        // Position
        if (position.length() == 0) {
            tvPosition.setText("-");
        }

        // Email
        if (user_email.length() == 0) {
            tvEmail.setText("-");
        }

        // Mobile
        if (mem_mobile.length() == 0) {
            tvMobile.setText("-");
        }

        // Telephone
        if (mem_phone.length() == 0) {
            tvTelephone.setText("-");
        }
    }

    public void getSharedUserDetail() {
        SharedPreferences preferences = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_nip = preferences.getString("mem_nip", "");
        mem_mobile = preferences.getString("mem_mobile", "");
        mem_phone = preferences.getString("mem_phone", "");
        mem_image = preferences.getString("mem_image", "");
        mem_address = preferences.getString("mem_address", "");
        position = preferences.getString("position", "");
        user_fullname = preferences.getString("name", "");
        user_email = preferences.getString("email", "");
        Log.e(TAG, "getSharedUserDetail: ProfilImage " + mem_image);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
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
                                            Picasso.with(mActivity)
                                                    .load(urlImage)
                                                    .fit()
                                                    .into(ivLogoClient);
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
