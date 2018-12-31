package com.elabram.lm.wmshwnp.checkin;

import com.elabram.lm.wmshwnp.BasePresenter;

import org.json.JSONException;

public interface CheckinPresenter extends BasePresenter {

    void retrofitCheckinStatus();

    void retrofitReadClient();

    void retrofitGoogleTimezone(double lat, double lng);

    String getGMT();

    String getTimeZoneId() throws JSONException;

    void startTracking() throws JSONException;

    void logout();


}
