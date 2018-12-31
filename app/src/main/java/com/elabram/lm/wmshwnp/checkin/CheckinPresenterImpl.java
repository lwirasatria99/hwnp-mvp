package com.elabram.lm.wmshwnp.checkin;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.rest.ApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.user_fullname;

public class CheckinPresenterImpl implements CheckinPresenter {

    private static final String TAG = CheckinPresenterImpl.class.getSimpleName();
    private CheckinView view;
    private CheckinRepository repository;
    private Disposable disposable;

    //private String timeZoneId;
    private String rawOffset;
    private JSONObject jsonObject;

    CheckinPresenterImpl(CheckinView view, CheckinRepository checkinRepository) {
        this.view = view;
        this.repository = checkinRepository;
    }

    @Override
    public void logout() {
        repository.clearPreferencesLogin();

        // Clear Activity And Fragment
        view.hideDialogProfile();
        view.hideDialogLogout();

        //assert getFragmentManager() != null;
        //getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        view.backToLoginActivity();
    }

    @Override
    public void retrofitCheckinStatus() {
        Call<ResponseBody> call = new ApiClient().getApiService().loadStatusCheckin(token);
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String contentResponse = new String(response.body().bytes());
                        JSONObject jsonObject = new JSONObject(contentResponse);
                        String response_code = jsonObject.getString("response_code");
                        String message = jsonObject.getString("message");
                        Log.e(TAG, "onResponse: CheckinStatus -> " + message);
                        switch (response_code) {
                            case "401":
                                view.showSnackbar(message);
                                break;
                            case "200":
                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                //Log.e(TAG, "onResponse: Data CheckinStatus -> " + jsonObject1.toString());

                                String realtime_date = jsonObject1.getString("is_date");
                                String jTimeFirst = jsonObject1.getString("time_first");
                                String location_first = jsonObject1.getString("location_first");

                                String time_last = jsonObject1.getString("time_last");
                                String location_last = jsonObject1.getString("location_last");

                                String j_remarkFirst = jsonObject1.getString("remark_first");
                                String j_remarkLast = jsonObject1.getString("remark_last");

                                // Date Checkin View
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat read = new SimpleDateFormat("dd MMMM yyyy");
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat write = new SimpleDateFormat("EEE, dd MMM yyyy");
                                Date date;
                                String s_dayformat = null;
                                try {
                                    date = read.parse(realtime_date);
                                    s_dayformat = write.format(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                view.setTextDate(s_dayformat);

                                String replaceFirst = jTimeFirst.replace(".", ":");
                                String replaceLast = time_last.replace(".", ":");
                                // ~

                                // Time Start
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat readTime1 = new SimpleDateFormat("HH.mm");

                                // Time start exception
                                Date checkTime1 = null;
                                try {
                                    checkTime1 = new SimpleDateFormat("HH.mm").parse("08.00");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // Time start parsing
                                Date dateTime1 = null;
                                if (!jTimeFirst.isEmpty()) {
                                    try {
                                        dateTime1 = readTime1.parse(jTimeFirst);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (!jTimeFirst.isEmpty()) {
                                    assert dateTime1 != null;
                                    if (dateTime1.after(checkTime1)) {
                                        view.setColorStart(R.color.red); // red
                                    } else {
                                        view.setColorStart(R.color.blue); // blue
                                    }
                                    view.setTextStartTime(replaceFirst);
                                    view.showContainerTime();
                                } else {
                                    view.setTextStartTime("-");
                                    view.hideContainerTime();
                                }

                                // Time Last
                                if (!time_last.isEmpty())
                                    view.setTextEndTime(replaceLast);
                                else
                                    view.setTextEndTime("-");

                                // First Location
                                if (!location_first.isEmpty())
                                    view.setTextFirstLocation(location_first);
                                //else
                                //    tvFirstLocation.setText("(-)");

                                // Last Location
                                if (!location_last.isEmpty())
                                    view.setTextLastLocation(location_last);
                                //else
                                //    tvLastLocation.setText("(-)");


                                // Remark First
                                if (!j_remarkFirst.isEmpty() && !j_remarkFirst.equals("null")) {
                                    view.showlinearRemarkFirst();
                                    view.setTextRemarkFirst(j_remarkFirst);
                                } else {
                                    view.hidelinearRemarkFirst();
                                }

                                // Remark Last
                                if (!j_remarkLast.isEmpty() && !j_remarkLast.equals("null")) {
                                    view.showlinearRemarkLast();
                                    view.setTextRemarkLast(j_remarkLast);
                                } else {
                                    view.hidelinearRemarkLast();
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
                Log.e(TAG, "onFailure Checkin Status: " + t.getCause());
                view.showToast(t.getMessage());
                Crashlytics.log(user_fullname + " " + t.getCause());
            }
        });
    }

    @Override
    public void retrofitReadClient() {
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
                                String message = jsonObject.getString("message");
                                switch (response_code) {
                                    case "401":
                                        view.showSnackbar(message);
                                        break;
                                    case "200":
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                        String urlImage = jsonObject1.getString("cus_logo");
                                        if (!urlImage.equals("https://elabram.com/hris/")) {
                                            view.picassoClient(urlImage);
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

    @Override
    public void onDestroy() {
        view = null;
        disposable.dispose();
    }

    @Override
    public void retrofitCheckVersion() {
        Call<ResponseBody> call = new ApiClient().getApiService().checkVersion();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        //noinspection ConstantConditions
                        String mResponse = new String(response.body().bytes());
                        //Log.e(TAG, "onResponse: CheckVersion " + mResponse);
                        JSONObject jsonObject = new JSONObject(mResponse);
                        String response_code = jsonObject.getString("response_code");
                        String message = jsonObject.getString("message");
                        switch (response_code) {
                            case "401":
                                view.showSnackbar(message);
                                break;
                            case "200":
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String s_version = jsonObject1.getString("version");
                                    if (!s_version.equals(view.getVersion())) {
                                        view.showDialogVersion();
                                    }
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

    @Override
    public void retrofitGoogleTimezone(double lat, double lng) {
        String coordinate = lat + "," + lng;
        String apiKey = repository.getMapApi();

        Call<ResponseBody> call = new ApiClient().getApiService()
                .cekTimeZone(
                        coordinate,
                        repository.getTimestamp(),
                        apiKey);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String responseContent = new String(response.body().bytes());
                        Log.e(TAG, "onResponse Timezone: " + responseContent);

                        jsonObject = new JSONObject(responseContent);
                        //timeZoneName = jsonObject.getString("timeZoneName");
                        //timeZoneId = jsonObject.getString("timeZoneId");
                        rawOffset = jsonObject.getString("rawOffset");

                        Log.e(TAG, "onResponse: timeZoneId "+getTimeZoneId());

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    String s_gmt = getGMT();
                    Log.e(TAG, "onResponse: Timezone -> " + s_gmt);

                    repository.putString("s_gmt", s_gmt);
                    try {
                        repository.putString("s_timezone_id", getTimeZoneId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Timezone " + t.getMessage());
            }
        });
    }

    @Override
    public String getGMT() {
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
    public String getTimeZoneId() throws JSONException {
        return jsonObject.getString("timeZoneId");
    }

    @Override
    public void startTracking() throws JSONException {
        repository.startLiveTracking(getGMT(),
                getTimeZoneId(),
                view.getLat(),
                view.getLong());
    }
}
