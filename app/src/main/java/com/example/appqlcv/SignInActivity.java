package com.example.appqlcv;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private final String FILE_EMAIL = "myFile";
    private String text;
    private String sEmail;
    private String sPassword;
    private EditText email;
    private EditText password;
    private FirebaseAuth auth;
    private GoogleSignInClient client;
    private FirebaseDatabase database;
    private ActivityResultLauncher<Intent> launcher;
    private final String TAG = "GoogleSignInActivity";
    private static int RC_SIGN_IN = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        database = FirebaseDatabase.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.signipbtn);

        TextView signup = findViewById(R.id.sign_up);
        CheckBox checkBox = findViewById(R.id.remember);
        TextView forGot = findViewById(R.id.quenmk);
        TextView logInGG = findViewById(R.id.lggoogle);
        auth = FirebaseAuth.getInstance();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                // Xử lý kết quả trả về từ hoạt động con
                // Process the result data
            }
        });

        sharedPreferences = getSharedPreferences(FILE_EMAIL, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        sEmail = sharedPreferences.getString("sEmail", "");
        sPassword = sharedPreferences.getString("sPassword", "");
        checkBox.setChecked(sharedPreferences.contains("checked") && sharedPreferences.getBoolean("checked", false));
        email.setText(sEmail);
        password.setText(sPassword);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sEmail = email.getText().toString().trim();
                sPassword = password.getText().toString().trim();
                if (checkBox.isChecked()) {
                    editor.putBoolean("checked", true);
                    editor.apply();
                    storedDataUsingSharedpref(sEmail, sPassword);

                    if (!validateEmail() || !validatePassword()) {
                        // Handle validation error
                    } else {
                        loginUser();
                    }
                } else {
                    if (!validateEmail() || !validatePassword()) {
                        // Handle validation error
                    } else {
                        getSharedPreferences(FILE_EMAIL, MODE_PRIVATE).edit().clear().commit();
                        loginUser();
                    }
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        forGot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this, options);

        logInGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = client.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void storedDataUsingSharedpref(String sEmail, String sPassword) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sEmail", sEmail);
        editor.putString("sPassword", sPassword);
        editor.apply();
    }

    private void loginUser() {
        sEmail = email.getText().toString().trim();
        sPassword = password.getText().toString().trim();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress_dialog);
        builder.setCancelable(false);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        auth.signInWithEmailAndPassword(sEmail, sPassword)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(SignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            // Địa chỉ email người dùng không hợp lệ
                            Toast.makeText(SignInActivity.this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // Mật khẩu người dùng không hợp lệ
                            Toast.makeText(SignInActivity.this, "Mật khẩu không hợp lệ", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            // Các ngoại lệ khác
                            Toast.makeText(SignInActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
    }

    private boolean validateEmail() {
        String emailInput = email.getText().toString().trim();
        if (emailInput.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = password.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Toast.makeText(SignInActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String uid = user.getUid();
                        String userName = user.getDisplayName();
                        String userEmail = user.getEmail();
                        String userPhotoUrl = user.getPhotoUrl().toString();
                        UserData newUser = new UserData(userName, userEmail, userPhotoUrl, uid);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(uid)
                                .setValue(newUser);
                        // Start Home activity after successful sign-in
                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                        Toast.makeText(SignInActivity.this, "Sign in with Google successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
