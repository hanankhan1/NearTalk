package com.example.neartalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class activity_splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivLogo);
        TextView text = findViewById(R.id.tvAppName);

        // Load animations
        Animation zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Start animations
        logo.startAnimation(zoomOut);
        text.startAnimation(fadeOut);

        // Delay and move to LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(activity_splash.this, LoginActivity.class));
            finish();
        }, 1500); // matches animation duration
    }
}
