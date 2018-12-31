package com.elabram.lm.wmshwnp;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    @BindView(R.id.tv_description)
    TextView tv_description;

    @BindView(R.id.tv_version)
    TextView tv_version;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.iv_logo_client)
    ImageView iv_logo_client;

    private String versionName;
    private Disposable disposable;
    private String isContractActive;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        retrofitReadContract();

        String wmsDescription = "" +
                "<p>WMS Mobile is brought to you by Elabram Systems, a company providing outsourcing and recruitment solutions, covering all level of expertise in Engineering and Telecommunication industry across Asia Pacific.</p>" +
                "<p>WMS Mobile is an integrated employee self-service solution that helps employees stay connected to their company’s information at any time, from anywhere.<p/>" +
                "<p>All information and features are the same as in WMS website but we give you the efficiency for checking, inputting and approving Timesheet, Overtime, Travel Request and Claim whenever and wherever you are.</p>";

        tv_description.setText(Html.fromHtml(wmsDescription));

        getVersionInfo();
        tv_version.setText("Version " + versionName);

        retrofitReadClient();

        iv_back.setOnClickListener(view -> finish());

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
                                Log.e(TAG, "onNext Contract: "+mResponse);
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
                        Log.e(TAG, "onError Retrofit Contract: " + e.getCause());
                        if (e instanceof SocketTimeoutException) {
                            Toast.makeText(AboutActivity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AboutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
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
        AlertDialog adVersion = builder.create();
        //noinspection ConstantConditions
        adVersion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        adVersion.setCancelable(false);

        if (!isFinishing())
            adVersion.show();

        tvOK.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= 21) {
                adVersion.dismiss();
                finishAndRemoveTask();
            } else {
                adVersion.dismiss();
                if (Build.VERSION.SDK_INT >= 16) {
                    finishAffinity();
                } else {
                    ActivityCompat.finishAffinity(this);
                }
            }
        });
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
                                            Picasso.with(AboutActivity.this)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    private void getVersionInfo() {
        //int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            //Log.e(TAG, "getVersionInfo: "+versionName );
            //versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
