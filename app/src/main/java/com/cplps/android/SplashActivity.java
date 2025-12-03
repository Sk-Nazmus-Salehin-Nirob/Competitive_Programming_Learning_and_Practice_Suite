package com.cplps.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.cplps.android.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionManager sessionManager = new SessionManager(SplashActivity.this);

                    Intent intent;
                    if (sessionManager.isLoggedIn()) {
                        // User is logged in, go to MainActivity
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    } else {
                        // User is not logged in, go to LoginActivity
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    }

                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    // If any error, go to login
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }
}
