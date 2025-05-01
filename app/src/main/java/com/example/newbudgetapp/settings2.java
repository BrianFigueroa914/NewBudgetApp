package com.example.newbudgetapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class settings2 extends AppCompatActivity {

    //nested broadcast receiver class
    public static class settings2Receive extends BroadcastReceiver {
        String channelID = "repeated_notifications_channel";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("broadcast receiver", "Alarm frequency: " + frequency);

            createNotificationChannel(context);
            createNotification(context);

            //Rescheduling alarm
            Calendar alarmCalendar = Calendar.getInstance();
            if (settings2.frequency == 0) {
                alarmCalendar.add(Calendar.HOUR_OF_DAY, 12);
            }
            alarmCalendar.add(Calendar.DAY_OF_YEAR, settings2.frequency);

            Intent alarmIntent = new Intent(context, settings2Receive.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    settings2.alarmRequestCode,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            AlarmManager updateAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            updateAlarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmCalendar.getTimeInMillis(),
                    alarmPendingIntent
            );
        }

        private void createNotification(Context context) {
            String name = "Scheduled Update Reminders";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        private void createNotificationChannel(Context context) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("REMINDER")
                    .setContentText("Update your spending info!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManagerCompat.notify(settings2.alarmRequestCode, builder.build());
        }
    }

    MaterialAutoCompleteTextView autoCompleteTextView;
    TextInputLayout optionTextInput;
    Button confirmButton;
    ImageButton settings2BackBtn;
    static int frequency;
    static int alarmRequestCode = 1045;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);

        optionTextInput = findViewById(R.id.inputLayout);
        autoCompleteTextView = findViewById(R.id.optionInput);
        confirmButton = findViewById(R.id.setAlarmBtn);

        settings2BackBtn = findViewById(R.id.settings2BackBtn);

        //button to set alarm
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoCompleteTextView.getText().toString().isEmpty()) {
                    optionTextInput.setError("Please select an option");
                }
                else {
                    String selectedValue = autoCompleteTextView.getText().toString();
                    Log.d("on click", "selected value: " + selectedValue);
                    frequency = getinputValue(selectedValue);
                    setAlarm(getApplicationContext(), frequency);
                    Toast.makeText(settings2.this, "Option saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //button to go back to settings home page
        settings2BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settings2.this, settingsHome.class));
            }
        });

    }

    private int getinputValue(String string) {
        switch(string) {
            case "Everyday":
                frequency = 0;
                break;
            case "Every other day":
                frequency = 1;
                break;
            case "Every 2 days":
                frequency = 2;
                break;
            case "Every 3 days":
                frequency = 3;
                break;
            case "Every 4 days":
                frequency = 4;
                break;
            case "Weekly":
                frequency = 7;
                break;
        }
        return frequency;
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Context applicationContext, int frequency) {
        long interval;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(applicationContext, settings2Receive.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                alarmRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        if (frequency == 0) {
            interval = AlarmManager.INTERVAL_DAY;
        }
        else {
            interval = AlarmManager.INTERVAL_DAY * frequency;
        }
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                interval,
                pendingIntent
        );
        Log.d("setalarm method", "Alarm frequency: " + frequency);
    }
}
