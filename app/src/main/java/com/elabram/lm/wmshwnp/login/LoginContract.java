package com.elabram.lm.wmshwnp.login;

public interface LoginContract {

    interface Presenter {
        void retrofitLogin();

        void retrofitCheckVersion();

        void onDestroy();

        void login();
    }
}
