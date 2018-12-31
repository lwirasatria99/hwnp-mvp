package com.elabram.lm.wmshwnp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elabram.lm.wmshwnp.adapter.AttendanceRecordAdapter;
import com.elabram.lm.wmshwnp.model.Monthly;
import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.utilities.AppInfo;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import static com.elabram.lm.wmshwnp.utilities.AppInfo.isOnline;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.mem_id;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_email;

public class AttendanceRecordActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = AttendanceRecordActivity.class.getSimpleName();

//    @BindView(R.id.linearMonth)
//    RelativeLayout linearMonth;

    @BindView(R.id.tvDatePeriode)
    TextView tvDatePeriode;

    @BindView(R.id.relative_change)
    RelativeLayout relative_change;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.listview)
    ListView listView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.tvNoData)
    TextView tvNoData;

    @BindView(R.id.relativeLoadMore)
    RelativeLayout relativeLoadMore;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    RatingBar ratingBar;

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;

    @BindView(R.id.relativeEnabled)
    RelativeLayout rel_online;

    @BindView(R.id.relativeDisabled)
    RelativeLayout rel_offline;

    @BindView(R.id.buttonRefresh)
    ImageView buttonRefresh;

    private static final int MAX_YEAR = 2099;
    private static final String[] Months = new String[]{"January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"};

    private ArrayList<Monthly> monthlies;
    private AttendanceRecordAdapter adapter;
    private String date_take;
    private String s_datefilter;
    private String lastDay;
    private String firstDay;
    private String realLastDay;
    private String formattedRealNumber;
    private String get_date_now;
    private String get_date_last;
    private RelativeLayout relativeDate;
    private String isContractActive;
    private Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_record);
        ButterKnife.bind(this);

        getSharedUserDetail();
        monthlies = new ArrayList<>();

        retrofitReadContract();

        adapter = new AttendanceRecordAdapter(this, monthlies);
        listView.setAdapter(adapter);

        tvNoData.setVisibility(View.GONE);
        relativeLoadMore.setVisibility(View.GONE);

        setInitialDate();

        cekInternet();

        buttonRefresh.setOnClickListener(view -> cekInternet());

        relative_change.setOnClickListener(view -> startActivity(new Intent(this, PerformanceChartActivity.class)));

        iv_back.setOnClickListener(view -> finish());

        initView();

        relativeDate.setOnClickListener(view -> showDateDialog(getDatePeriode()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }


    private void cekInternet() {
        if (AppInfo.isOnline(this)) {
            rel_online.setVisibility(View.VISIBLE);
            showProgress();
            retrofitListMonthly();
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_id = preferences.getString("mem_id", "");
        user_email = preferences.getString("user_email", "");
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
                            Toast.makeText(AttendanceRecordActivity.this, "Timeout / Please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AttendanceRecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void retrofitListMonthly() {
        if (monthlies != null) {
            monthlies.clear();
            adapter.notifyDataSetChanged();
        }

        if (firstDay == null) {
            firstDay = get_date_now;
        }
        if (realLastDay == null) {
            realLastDay = get_date_last;
        }

        showProgress();

        Call<ResponseBody> call = new ApiClient().getApiService().monthlyList(token, firstDay, realLastDay, mem_id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideProgress();
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String content = new String(response.body().bytes());
                        //Log.e(TAG, "onResponse: Monthly List " + content);
                        parseJSON(content);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideProgress();
                Toast.makeText(AttendanceRecordActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: "+t.getCause());
            }
        });
    }

    private void parseJSON(String content) throws JSONException {
        JSONObject jsonObject = new JSONObject(content);
        String response_code = jsonObject.getString("response_code");
        switch (response_code) {
            case "401":
                String message = jsonObject.getString("message");
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                break;
            case "200":
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray.length() == 0) {
                    tvNoData.setVisibility(View.VISIBLE);
                } else {
                    tvNoData.setVisibility(View.GONE);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    //Log.e(TAG, "parseJSON: JSON 200 "+jsonObject1);

                    String att_id_token = jsonObject1.getString("att_id_token");
                    String att_date = jsonObject1.getString("att_date");
                    String att_day = jsonObject1.getString("att_day");
                    String time_in = jsonObject1.getString("time_in");
                    String time_out = jsonObject1.getString("time_out");
                    //String project_name = jsonObject1.getString("project_name");
                    //String description = jsonObject1.getString("description");
                    String location_first = jsonObject1.getString("location_first");
                    String location_last = jsonObject1.getString("location_last");
                    String total_working_hour = jsonObject1.getString("total_working_hour");

                    String timezone1 = jsonObject1.getString("timezone_first");
                    String timezone2 = jsonObject1.getString("timezone_last");

                    Monthly monthly = new Monthly();
                    monthly.setTimezone1(timezone1);
                    monthly.setTimezone2(timezone2);
                    monthly.setAttDate(att_date);
                    monthly.setAttDay(att_day);
                    monthly.setTimeIn(time_in);
                    monthly.setTimeOut(time_out);
                    monthly.setLocationFirst(location_first);
                    monthly.setLocationLast(location_last);
                    monthly.setTotalWorkingHour(total_working_hour);

                    monthlies.add(monthly);
                    listView.setAdapter(adapter);
                }
                break;
        }
    }

    private void setInitialDate() {
        DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Date date = new Date();
        String s_datenow = dateFormat.format(date);

        if (getDatePeriode().equals("null")) {
            tvDatePeriode.setText(s_datenow);
        }

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-01");
        Date date1 = new Date();

        Calendar calendar = Calendar.getInstance();

        String get_day_last = String.valueOf(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int month = calendar.get(Calendar.MONTH) + 1;
        String get_month = null;
        if (month < 10) {
            get_month = "0" + month;
        }

        String get_year = String.valueOf(calendar.get(Calendar.YEAR));
        get_date_last = get_year + "-" + get_month + "-" + get_day_last;
        Log.e(TAG, "setInitialDate: Last " + get_date_last);
        get_date_now = dateFormat1.format(date1);
        Log.e(TAG, "setInitialDate: First " + get_date_now);
    }

    public void showProgress() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    public String getDatePeriode() {
        return tvDatePeriode.getText().toString();
    }

    private void showDateDialog(final String s_tvdateperiode) {
        String split_calendar[] = s_tvdateperiode.split("\\s+");
        String split_month = split_calendar[0];
        //String split_year = split_calendar[1];

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ActionThemeDialog);
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_month_picker, null);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        final RelativeLayout relativeCancel = dialogView.findViewById(R.id.relative_cancel);
        final RelativeLayout relativeChoose = dialogView.findViewById(R.id.relative_choose);
//        final ImageView imageCheck = dialogView.findViewById(R.id.imageCheck);
//        final ImageView imageCross = dialogView.findViewById(R.id.imageCross);

        Calendar cal = Calendar.getInstance();

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-B.ttf");
        monthPicker.setTypeface(type);
        yearPicker.setTypeface(type);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(Months.length - 1);
        monthPicker.setWrapSelectorWheel(true);
        monthPicker.setDisplayedValues(Months);
        //int month = cal.get(Calendar.MONTH);
        for (int i = 0; i < Months.length; i++) {
            if (Months[i].equalsIgnoreCase(split_month)) {
                monthPicker.setValue(i);
                break;
            }
        }
        //monthPicker.setValue();

        monthPicker.setFormatter(value -> {
            // TODO Auto-generated method stub
            return Months[value];
        });

        int yearMin = 1994;
        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(yearMin);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        relativeChoose.setOnClickListener(view ->
            setMonthYear(monthPicker, yearPicker, alertDialog)
        );

        relativeCancel.setOnClickListener(view -> alertDialog.dismiss());

    }

    private void setMonthYear(NumberPicker monthPicker, NumberPicker yearPicker, AlertDialog alertDialog) {
        onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
        //Log.e(TAG, "onClick Date Take: " + date_take);
        int realMonthNumber = monthPicker.getValue() + 1;
        //Log.e(TAG, "setMonthYear: Month " + realMonthNumber);

        // Date Convert
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-01", Locale.getDefault());
        Date dateFormat = null;
        try {
            dateFormat = inputFormat.parse(date_take);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        firstDay = outputFormat.format(dateFormat);
        //Log.e(TAG, "setMonthYear: source date " + firstDay);
        // End

        // Get The Last Day
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = null;
        try {
            convertedDate = inFormat.parse(firstDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(convertedDate);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDay = String.valueOf(c.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (realMonthNumber < 10) {
            formattedRealNumber = "0" + realMonthNumber;
        }
        realLastDay = yearPicker.getValue() + "-" + formattedRealNumber + "-" + lastDay;
        //Log.e(TAG, "setMonthYear: LastDay" + realLastDay);

        if (isOnline(this)) {
            retrofitListMonthly();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        alertDialog.dismiss();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        int month = monthOfYear + 1;

        String formattedMonth = "" + month;

        if (month < 10) {
            formattedMonth = "0" + month;
        }

        date_take = formattedMonth + " " + year;
        if (!date_take.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat origin = new SimpleDateFormat("MM yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat destination = new SimpleDateFormat("MMMM yyyy");
            Date newFormat = null;
            try {
                newFormat = origin.parse(date_take);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date_take = destination.format(newFormat);
            tvDatePeriode.setText(date_take);
        }

    }

    private void initView() {
        relativeDate = findViewById(R.id.relative_date);
    }
}
