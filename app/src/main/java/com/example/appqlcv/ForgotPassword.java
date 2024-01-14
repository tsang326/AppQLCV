package com.example.appqlcv;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private String sEmail;
    private EditText email;
    private FirebaseAuth auth;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        builder = new AlertDialog.Builder(this);
        email = findViewById(R.id.emailEt);
        Button forgot = findViewById(R.id.reset_mk);
        TextView signup = findViewById(R.id.sign_up);
        auth = FirebaseAuth.getInstance();

        signup.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassword.this, SignUpActivity.class);
            startActivity(intent);
        });

        forgot.setOnClickListener(view -> {
            if (!validateEmail()) {
                return;
            }

            sEmail = email.getText().toString().trim();
            auth.sendPasswordResetEmail(sEmail)
                    .addOnSuccessListener(aVoid -> {
                        builder.setTitle("Notification")
                                .setMessage("We've helped you reset your password. Please check your email again.")
                                .setCancelable(true)
                                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                                    Intent intent = new Intent(ForgotPassword.this, SignInActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ForgotPassword.this, "Email not registered account!", Toast.LENGTH_SHORT).show());
        });
    }

    private boolean validateEmail() {
        String mail = email.getText().toString().trim();
        if (mail.isEmpty()) {
            email.setError("Email cannot be blank");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Email address is not valid!");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }
}