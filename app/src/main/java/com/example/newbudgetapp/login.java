package com.example.newbudgetapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    private boolean passwordShowing = false;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences; // SharedPreferences to store credentials

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Variables
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final ImageView showPassword = findViewById(R.id.showPasswordIcon);
        final CheckBox rememberMeCheckbox = findViewById(R.id.rememberMeCheckBox);
        final TextView forgotPasswordText = findViewById(R.id.forgotPassword);
        final TextView loginBtn = findViewById(R.id.loginBtn);
        final TextView registerBtn = findViewById(R.id.registerBtn);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");

        // Populate credentials if "Remember me" was checked before
        if (!savedEmail.isEmpty()) {
            email.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
        }

        // Show/hide password
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordShowing) {
                    passwordShowing = false;
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
                } else {
                    passwordShowing = true;
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPassword.setImageResource(R.drawable.baseline_block_24);
                }
            }
        });

        // Send forgot password email
        forgotPasswordText.setOnClickListener(v -> {
            String strEmail = email.getText().toString().trim();

            if (strEmail.isEmpty()) {
                Toast.makeText(login.this, "Please enter your email to reset password.", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(strEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            Toast.makeText(login.this, "Password reset email sent! Check your inbox.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(login.this, "Failed to send reset email. Check if the email is correct.", Toast.LENGTH_SHORT).show();
                    });
        });

        // Handle Firebase login
        loginBtn.setOnClickListener(v -> {
            String strEmail = email.getText().toString().trim();
            String strPass = password.getText().toString().trim();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (strEmail.isEmpty() || strPass.isEmpty()) {
                Toast.makeText(login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Put email if remember is checked
            if (rememberMeCheckbox.isChecked()) {
                editor.putString("email", strEmail);
                editor.apply();
            } else {
                editor.remove("email");
                editor.apply();
            }

            mAuth.signInWithEmailAndPassword(strEmail, strPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Login successful
                                startActivity(new Intent(login.this, DashboardActivity.class));
                                finish();
                            } else {
                                //Login failed
                                Toast.makeText(login.this, "Login failed. Check email/password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Go to register screen
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(login.this, RegisterActivity.class));
        });
    }
}
