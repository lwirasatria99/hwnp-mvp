package com.elabram.lm.wmshwnp.checkin;

import android.support.annotation.ColorRes;
import android.widget.ImageView;

import com.elabram.lm.wmshwnp.BaseView;

interface CheckinView extends BaseView {

    // setText
    void setTextDate(String value);

    void setTextStartTime(String value);
    void setTextEndTime(String value);

    void setTextFirstLocation(String value);
    void setTextLastLocation(String value);

    void setTextRemarkFirst(String value);
    void setTextRemarkLast(String value);

    // setColor
    void setColorStart(@ColorRes int colorStart);

    // show hide
    void showlinearRemarkFirst();
    void hidelinearRemarkFirst();

    void showlinearRemarkLast();
    void hidelinearRemarkLast();

    void showContainerTime();
    void hideContainerTime();

    void hideDialogProfile();
    void hideDialogLogout();

    void showDialogVersion();

    void backToLoginActivity();

    void picassoClient(String url);

    // getString
    String getLat();

    String getLong();

}
