package com.example.appqlcv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private String sEmail;
    private String sPassword;
    private String sFullName;
    private String key1;
    private EditText email;
    private EditText password;
    private EditText name;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        Button signUpButton = findViewById(R.id.signupbtn);
        TextView signInTextView = findViewById(R.id.sign_in);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail() && validatePassword() && validateName()) {
                    sEmail = email.getText().toString().trim();
                    sPassword = password.getText().toString().trim();
                    auth.createUserWithEmailAndPassword(sEmail, sPassword)
                            .addOnCompleteListener(SignUpActivity.this, task -> {
                                ProgressBar progressBar = new ProgressBar(SignUpActivity.this);
                                progressBar.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    auth.getCurrentUser().sendEmailVerification()
                                            .addOnSuccessListener(aVoid -> {
                                                firebaseUser();
                                                Toast.makeText(SignUpActivity.this, "Sign Up Success!", Toast.LENGTH_SHORT).show();
                                                Toast.makeText(SignUpActivity.this, "Please Verify your email!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignUpActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void firebaseUser() {
        sEmail = email.getText().toString().trim();
        sPassword = password.getText().toString().trim();
        sFullName = name.getText().toString().trim();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        UserData user = new UserData(sFullName, sEmail, " ", sPassword);
        DatabaseReference ref = database.getReference("Users");
        String key = ref.push().getKey();
        if (key != null) {
            DatabaseReference userRef = ref.child(currentUserId);
            userRef.setValue(user);
        }
    }

    private boolean validateName() {
        String sName = name.getText().toString().trim();
        if (sName.isEmpty()) {
            name.setError("Name cannot be blank");
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String pass = password.getText().toString().trim();
        // Password must contain 11 characters, at least one uppercase letter, one lowercase letter, one digit, and one special character
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*([^a-zA-Z\\d\\s])).{11,20}$";
        if (pass.isEmpty()) {
            password.setError("Password cannot be blank");
            return false;
        } else if (!pass.matches(passwordPattern)) {
            password.setError("Password consists of 11-20 characters, at least one uppercase letter, one lowercase letter, a number, and a special character");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String mail = email.getText().toString().trim();
        if (mail.isEmpty()) {
            email.setError("Email cannot be blank");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Email address is not valid");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }
}
