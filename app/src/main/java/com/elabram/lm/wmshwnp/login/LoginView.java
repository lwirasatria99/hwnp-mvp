package com.elabram.lm.wmshwnp.login;

public interface LoginView {

    String getTextUser();

    String getTextPass();

    String getImei();

    String getVersion();

    void showSnackbar(String message);

    void gotoCheckinV1Activity();

    void dialogCheckVersion();

    void showToast(String message);

    void showDialog();

    void dismissDialog();

}
