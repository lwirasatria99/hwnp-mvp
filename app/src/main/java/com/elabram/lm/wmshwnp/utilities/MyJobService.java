package com.elabram.lm.wmshwnp.utilities;

import android.content.SharedPreferences;
import android.util.Log;

import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();
    private Disposable disposable;

    private String s_timezone_id;
    private String s_gmt;
    private String s_lat;
    private String s_long;

    @Override
    public boolean onStartJob(JobParameters job) {
//        s_lat = String.valueOf(getLocation().getLatitude());
//        s_long = String.valueOf(getLocation().getLongitude());
//
//        SharedPreferences preferences = getSharedPreferences("PREFS_TIMEZONE", 0);
//        s_timezone_id = preferences.getString("s_timezone_id", "");
//        s_gmt = preferences.getString("s_gmt", "");

        // use timezone
//        rxLiveTracking();
        Log.e(TAG, "onStartJob: ");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private HashMap<String, String> getParamsLiveTracking() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token); // OK
        params.put("lat", s_lat); // OK
        params.put("long", s_long); // OK
        params.put("timezone", s_gmt);
        params.put("timezone_id", s_timezone_id);
        Log.e(TAG, "getParamsLive Jobservice: " + params);
        return params;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    private void rxLiveTracking() {
        Observable<ResponseBody> call = new ApiClient().getApiService().liveTracking(getParamsLiveTracking());
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
                                //Log.e(TAG, "onNext: LiveTrack -> "+mResponse);
                                //noinspection unused
                                JSONObject jsonObject = new JSONObject(mResponse);
                                String message = jsonObject.getString("message");
                                Log.e(TAG, "onNext: LiveTrack -> "+message);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError LiveTracking Cause: " + e.getCause());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
