package com.example.newbudgetapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class register extends AppCompatActivity {
    //global variables
    private boolean passwordShowing = false;
    private boolean confirmPasswordShowing = false;
    public static int MIN_PASSWORD_CHARS = 8; //8 chars
    private String userID;
    FirebaseAuth mAuth;
    FirebaseFirestore budgetData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        budgetData = FirebaseFirestore.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrationPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //variables
        final EditText username = findViewById(R.id.username);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText confirmPassword = findViewById(R.id.confirmPassword);
        final ImageView showPassword = findViewById(R.id.showPasswordIcon);
        final ImageView secondShowPassword = findViewById(R.id.secondShowPasswordIcon);
        final AppCompatButton signUpBtn = findViewById(R.id.signUpBtn);
        final TextView acctSignIn = findViewById(R.id.acctSignInBtn);

        //methods
        //show or hide password methods
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
        secondShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmPasswordShowing) {
                    confirmPasswordShowing = false;
                    confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    secondShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
                } else {
                    confirmPasswordShowing = true;
                    confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    secondShowPassword.setImageResource(R.drawable.baseline_block_24);

                }
            }
        });

        //Create an account button
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                //variables for input validation
                String strUsername = String.valueOf(username.getText());
                String strEmail =  String.valueOf(email.getText());
                String strPassword =  String.valueOf(password.getText());
                String strConfirmPassword =  String.valueOf(confirmPassword.getText());

                //input validation
                if (TextUtils.isEmpty(strEmail)) {
                    Toast.makeText(register.this, "Please enter email", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(strPassword)) {
                    Toast.makeText(register.this, "Please enter password", Toast.LENGTH_SHORT).show();
                }
                else if (strPassword.length() < MIN_PASSWORD_CHARS) {
                    Toast.makeText(register.this, "Password must be at least " + MIN_PASSWORD_CHARS + " characters long", Toast.LENGTH_SHORT).show();
                }
                else if (!strPassword.equals(strConfirmPassword)) {
                    Toast.makeText(register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Proceed with Firebase registration
                    mAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            userID = user.getUid(); //Get UID for registered user

                                            //Create User object
                                            User userData = new User(strUsername, strEmail);

                                            // Add data to Firestore
                                            budgetData.collection("Users").document(userID)
                                                    .set(userData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(register.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                Toast.makeText(register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                    else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        //Return to login page method
        acctSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sends you back to the activity that launched this activity (login_page)
                finish();
            }
        });
    }
}