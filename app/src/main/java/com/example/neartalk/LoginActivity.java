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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoSignup, tvForgotPassword;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, DashBoard.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        scrollView = findViewById(R.id.login_scrollview);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignup = findViewById(R.id.tvGoSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(scrollView, "Email & Password required", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        startActivity(new Intent(this, Profile.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Snackbar.make(scrollView, e.getMessage(), Snackbar.LENGTH_LONG).show());
        });

        tvGoSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignUp.class)));

        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Snackbar.make(scrollView, "Enter your email first", Snackbar.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Snackbar.make(scrollView,
                                "Password reset email sent. Check your inbox.",
                                Snackbar.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Snackbar.make(scrollView,
                                e.getMessage(),
                                Snackbar.LENGTH_LONG).show());
    }
}