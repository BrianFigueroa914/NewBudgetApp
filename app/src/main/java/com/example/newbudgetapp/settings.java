package com.example.newbudgetapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class settings extends AppCompatActivity {

    //nested broadcast receiver class
    public static class settingsReceive extends BroadcastReceiver {
        int billNotificationID = 1;
        String billChannelID = "scheduled_notification_channel";
        static String titleExtra = "titleExtra";
        static String messageExtra = "messageExtra";

        @Override
        public void onReceive(Context context, Intent intent) {
            billNotificationID++;
            createBillsNotificationChannel(context);
            createBillNotification(context, intent);
        }

        private void createBillNotification(Context context, Intent intent) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, billChannelID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(intent.getStringExtra(titleExtra))
                    .setContentText(intent.getStringExtra(messageExtra))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManagerCompat.notify(billNotificationID, builder.build());
        }

        private void createBillsNotificationChannel(Context context) {
            String name = "Bill Reminders";
            String description = "Description of channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(billChannelID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    Button submitButton;
    ImageButton nextPageButton;
    TextInputEditText title;
    TextInputEditText message;
    TimePicker timePicker;
    DatePicker datePicker;
    int requestCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        submitButton = findViewById(R.id.submitButton);
        nextPageButton = findViewById(R.id.settings2btn);
        title = findViewById(R.id.titleET);
        message = findViewById(R.id.messageET);

        timePicker = findViewById(R.id.timePicker);
        datePicker = findViewById(R.id.datePicker);

        //button to set reminder
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCode++;
                setReminder(
                        getApplicationContext(),
                        title,
                        message,
                        timePicker,
                        datePicker
                );
                Toast.makeText(settings.this, "Reminder set!", Toast.LENGTH_SHORT).show();
            }
        });

        //button to go to next page
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settings.this, settings2.class);
                startActivity(intent);
            }
        });
    }

    private void setReminder(
            Context billApplicationContext,
            TextInputEditText title,
            TextInputEditText message,
            TimePicker timePicker,
            DatePicker datePicker
    ) {
        Intent billIntent = new Intent(billApplicationContext, settingsReceive.class);
        billIntent.putExtra(settings.settingsReceive.titleExtra, title.getText().toString());
        billIntent.putExtra(settingsReceive.messageExtra, message.getText().toString());

        PendingIntent billPendingIntent = PendingIntent.getBroadcast(
                billApplicationContext,
                requestCode,
                billIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        AlarmManager billAlarmManager = (AlarmManager) billApplicationContext.getSystemService(Context.ALARM_SERVICE);
        long time = getTime(timePicker, datePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (billAlarmManager != null && billAlarmManager.canScheduleExactAlarms()) {
                billAlarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        billPendingIntent
                );

            }
        }

    }

    private long getTime(TimePicker timePicker, DatePicker datePicker) {
        int minute = timePicker.getMinute();
        int hour = timePicker.getHour();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar billCalendar = Calendar.getInstance();
        billCalendar.set(year, month, day, hour, minute);
        return billCalendar.getTimeInMillis();
    }

}
