package com.example.appqlcv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // Thời gian hiển thị SplashScreen (3 giây)
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);

        // Tạo một animation để xoay ProgressBar
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(SPLASH_DURATION);
        progressBar.startAnimation(rotateAnimation);

        // Chuyển đến SignInActivity sau khi SplashScreen kết thúc
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkIfUserIsLoggedIn();
            }
        }, SPLASH_DURATION);
    }

    private void checkIfUserIsLoggedIn() {
        // Kiểm tra đăng nhập bằng Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            navigateToHomeActivity();
        } else {
            navigateToSignInActivity();
        }
    }

    private void navigateToSignInActivity() {
        Intent intent = new Intent(SplashScreen.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}