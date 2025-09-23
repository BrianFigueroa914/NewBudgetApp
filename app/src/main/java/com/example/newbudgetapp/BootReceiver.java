package com.example.newbudgetapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("NotificationPreferences", Context.MODE_PRIVATE);
            sharedPreferences.edit().apply();
            int frequency = sharedPreferences.getInt("notification_preferences", 0);
            Log.d("sharedpreferencesboot", "Alarm frequency: " + frequency);
            settings2.setAlarm(context, frequency);

        }
    }
}
