package com.skylark.tripadviser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.Date;

public class SplashScreen extends AppCompatActivity {

    private TextView tvFuel;
    private TextView tvTrades;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_splash_screen);

        tvFuel = findViewById(R.id.tvFuel);
        tvTrades = findViewById(R.id.tvTrades);
        logo = findViewById(R.id.logo);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.swipe_down);
        logo.startAnimation(animation);
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.swipe_right);
        tvFuel.startAnimation(animation1);
        Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.swipe_left);
        tvTrades.startAnimation(animation2);

        SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);

        final String isLogin = pref.getString("isLogin", "false");
        final String defaultDate = new Date().toString();
        final Date lastLoginTime = new Date(pref.getString("lastLoginTime", defaultDate));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if(isLogin.equals("true")) {
                    if(getDifference(lastLoginTime, new Date()) <= 30) {
                        intent = new Intent(SplashScreen.this, HomeActivity.class);
                    }
                    else {
                        intent = new Intent(SplashScreen.this, LoginActivity.class);
                    }
                }
                else {
                    intent = new Intent(SplashScreen.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 2*1000);

    }

    public int getDifference(Date startDate, Date endDate) {
        long different = endDate.getTime() - startDate.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long elapsedDays = different / daysInMilli;
        return (int) elapsedDays;
    }


}
