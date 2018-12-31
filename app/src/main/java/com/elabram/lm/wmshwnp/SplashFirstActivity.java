package com.elabram.lm.wmshwnp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SplashFirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_first);

        int splashWaitingTime = 3000;

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, splashWaitingTime);
    }
}
