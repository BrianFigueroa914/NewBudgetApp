package com.example.newbudgetapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class settingsHome extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings_home);

        ImageButton toDashboard = findViewById(R.id.homeBtn);
        ImageButton toSettings = findViewById(R.id.billRemindersBtn);
        ImageButton toSettings2 = findViewById(R.id.scheduleUpdatesBtn);
        Button resetGraph = findViewById(R.id.resetGraphBtn);
        Button logOut = findViewById(R.id.signOutBtn);

        toDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, DashboardActivity.class));
            }
        });

        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, settings.class));
            }
        });

        toSettings2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settingsHome.this, settings2.class));
            }
        });

        //------------------------------
        //logic for reset graph button here :)










        //--------------------------------


        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(settingsHome.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to the login activity
            Intent intent = new Intent(settingsHome.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish the current activity
        });

    }
}
