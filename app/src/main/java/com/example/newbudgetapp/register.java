package com.example.newbudgetapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class register extends AppCompatActivity {
    //global variables
    private boolean passwordShowing = false;
    private boolean confirmPasswordShowing = false;

    public static int MAX_USER_CHARS = 50;
    public static int MAX_EMAIL_CHARS = 50;
    public static int MAX_PHONENUMBER_CHARS = 10;
    public static int MIN_PASSWORD_CHARS = 8; //8 chars



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        final EditText phone = findViewById(R.id.phoneNumber);
        final AppCompatButton signUpBtn = findViewById(R.id.signUpBtn);
        final TextView acctSignIn = findViewById(R.id.acctSignInBtn);
        final TextView errorMsg = findViewById(R.id.text_input_error);

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

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                //variables for input validation
                String strUsername =  username.getText().toString();
                String strEmail =  email.getText().toString();
                String strPassword =  password.getText().toString();
                String strConfirmPassword =  confirmPassword.getText().toString();
                String strPhone =  phone.getText().toString();

                //input validation
                if (!validateInput(strUsername, strEmail, strPassword, strConfirmPassword, strPhone)){
                    //if validation fails errorMsg will update with appropriate response
                }
                else {
                    errorMsg.setText("Account successfully created continue to Login");
                    finish();
                }


            }

            // Helper method to validate input
            private boolean validateInput(String tempUsername, String tempEmail, String tempPassword, String tempConfirmPassword, String tempPhone) {
                if (tempUsername.isEmpty() || tempEmail.isEmpty() || tempPassword.isEmpty() || tempConfirmPassword.isEmpty() || tempPhone.isEmpty()) {
                    errorMsg.setText("Please fill in all fields.");
                    return false;
                }
                else if (tempUsername.length() > MAX_USER_CHARS) {
                    errorMsg.setText("Username must be less than 50 characters.");
                    return false;
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tempEmail).matches() || tempEmail.length() > MAX_EMAIL_CHARS) {   //Checks email format
                    errorMsg.setText("Invalid email format or email exceeds 50 characters.");
                    return false;
                }
                else if (tempPhone.length() != MAX_PHONENUMBER_CHARS) {
                    errorMsg.setText("Phone number must be exactly 10 digits.");
                    return false;
                }
                else if (tempPassword.length() < MIN_PASSWORD_CHARS || tempConfirmPassword.length() < MIN_PASSWORD_CHARS) {
                    errorMsg.setText("Password must be at least 8 characters.");
                    return false;
                }
                else if (!tempPassword.equals(tempConfirmPassword)) {
                    errorMsg.setText( "Passwords do not match.");
                    return false;
                }
                else {
                    return true;
                }
            }
        });

        acctSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sends you back to the activity that launched this activity (login_page)
                finish();
            }
        });



    }
}