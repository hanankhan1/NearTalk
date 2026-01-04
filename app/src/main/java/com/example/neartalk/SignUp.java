package com.example.neartalk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignup;
    private TextView tvGoLogin;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        scrollView = findViewById(R.id.signscroll);
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        tvGoLogin.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Snackbar.make(scrollView, "All fields required", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Snackbar.make(scrollView, "Passwords do not match", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Snackbar.make(scrollView, "Password must be at least 6 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            auth.signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();

                        } else {
                            Snackbar.make(scrollView,
                                    task.getException().getMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
