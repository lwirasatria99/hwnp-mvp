package com.elabram.lm.wmshwnp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elabram.lm.wmshwnp.CheckinActivity;
import com.elabram.lm.wmshwnp.AttendanceRecordActivity;
import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.rest.ApiClient;
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

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

/**
 * Class : Home Fragment
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainHomeFragment extends Fragment {

    private static final String TAG = MainHomeFragment.class.getSimpleName();
    private Activity mActivity;

    private String fullname;
    private String fullemail;
    private String user_image;

    @BindView(R.id.relativeMonthly)
    RelativeLayout relativeMonthly;
    @BindView(R.id.rootView)
    RelativeLayout rootView;

    @BindView(R.id.ivLogoClient)
    ImageView ivLogoClient;
    private Disposable disposable;

    public MainHomeFragment() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        fullname = preferences.getString("name", "");
        fullemail = preferences.getString("user_email", "");
        mem_image = preferences.getString("mem_image", "");
        token = preferences.getString("token", "");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home2, container, false);
        ButterKnife.bind(this, view);

        TextView tvUsername = view.findViewById(R.id.tvName);
        TextView tvUseremail = view.findViewById(R.id.tvAuthority);
        ImageView ivUserpic = view.findViewById(R.id.ivProfil);

        getSharedUserDetail();
        retrofitReadClient();
//        SharedPreferences preferences = mActivity.getSharedPreferences("LOGO", 0);
//        String urlImage = preferences.getString("url", "");
//        if (!urlImage.equals("https://elabram.com/hris/")) {
//            Picasso.with(mActivity)
//                    .load(urlImage)
//                    .fit()
//                    .into(ivLogoClient);
//        }

        if (mem_image.equals("https://elabram.com/hris/files/employee/")) {
            ivUserpic.setImageResource(R.drawable.profile_default_picture);
        } else {
            Picasso.with(mActivity)
                    .load(mem_image)
                    .fit()
                    .into(ivUserpic);
        }
        tvUsername.setText(fullname);
        tvUseremail.setText(fullemail);

//        RelativeLayout buttonOvertime = view.findViewById(R.id.buttonOvertime);
//        RelativeLayout buttonTimesheet = view.findViewById(R.id.buttonTimesheet);
//        RelativeLayout buttonTravel = view.findViewById(R.id.buttonTravel);
//        RelativeLayout buttonClaim = view.findViewById(R.id.buttonClaim);
        RelativeLayout buttonCheckin = view.findViewById(R.id.buttonCheckin);

//        if (!role.equals("2")) {
//            buttonMonthlyReport.setVisibility(View.GONE);
//            buttonCheckin.setVisibility(View.GONE);
//        }

//        buttonOvertime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickOvertime();
//            }
//        });
//
//        buttonTimesheet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickTimesheet();
//            }
//        });
//
//        buttonTravel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickTravel();
//            }
//        });
//
//        buttonClaim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickClaim();
//            }
//        });
//
        relativeMonthly.setOnClickListener(view1 -> startActivity(new Intent(mActivity, AttendanceRecordActivity.class)));

        buttonCheckin.setOnClickListener(view1 -> startActivity(new Intent(mActivity, CheckinActivity.class)));

        return view;
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

//    private void clickOvertime() {
//        startActivity(new Intent(mActivity, OvertimeActivity2.class));
//    }
//
//    private void clickTimesheet() {
//        startActivity(new Intent(mActivity, TimesheetActivity.class));
//    }
//
//    private void clickTravel() {
//        startActivity(new Intent(mActivity, TravelActivity.class));
//    }
//
//    private void clickClaim() {
//        startActivity(new Intent(mActivity, ClaimActivity.class));
//    }
//
//    private void clickMonthlyReport() {
//        startActivity(new Intent(mActivity, MonthlyReportActivity.class));
//    }

}
