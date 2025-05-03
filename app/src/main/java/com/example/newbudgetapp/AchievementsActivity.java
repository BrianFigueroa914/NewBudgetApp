package com.example.newbudgetapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AchievementsActivity extends AppCompatActivity {

    private static class Achievement {
        String title, subtitle;
        boolean highlighted;

        Achievement(String title, String subtitle, boolean highlighted) {
            this.title = title;
            this.subtitle = subtitle;
            this.highlighted = highlighted;
        }
    }

    private final Achievement[] achievements = new Achievement[]{
            new Achievement("Save $100 ✅", "70.5% of users have unlocked this", true),
            new Achievement("Save $500 💵", "25.7% of users have unlocked this", false),
            new Achievement("Save $1,000 🎉", "10.1% of users have unlocked this", true),
            new Achievement("Save $5,000 🏆", "7.0% of users have unlocked this", false),
            new Achievement("Save $10,000 💎", "2.6% of users have unlocked this", false),
            new Achievement("No Spend Day 🚫🛍️", "50.7% of users have unlocked this", false),
            new Achievement("No eating out for 7 days 🍱", "36.6% of users have unlocked this", true),
            new Achievement("No impulse purchases for a week 🚫💳", "7.8% of users have unlocked this", false),
            new Achievement("Have less than 3 subscription services 📉", "33.0% of users have unlocked this", true),
            new Achievement("Have less than 2 subscription services ✅", "27.5% of users have unlocked this", true),
            new Achievement("Have less than 1 subscription services 💵", "5.7% of users have unlocked this", false),
            new Achievement("Build an emergency fund ", "10.1% of users have unlocked this", true),
            new Achievement("Pay off a debt 🏆", "7.0% of users have unlocked this", false),
            new Achievement("No Spend Weekend 💎", "2.6% of users have unlocked this", false),
            new Achievement("Spend $0 on entertainment for 7 days 🚫🛍️", "50.7% of users have unlocked this", false),
            new Achievement("No online shopping for 14 days ✅", "70.5% of users have unlocked this", true),
            new Achievement("Spend less than $10/day for a week ✅", "70.5% of users have unlocked this", true),
            new Achievement("Buy Nothing New for a Week ✅", "70.5% of users have unlocked this", true),
            new Achievement("Buy Nothing New for a Month ✅", "70.5% of users have unlocked this", true),
            new Achievement("Buy Nothing New for a Day ✅", "70.5% of users have unlocked this", true),
            new Achievement("Buy Nothing New for 2 Months ✅", "70.5% of users have unlocked this", true),
            new Achievement("Reach a savings rate of 20% of your income ✅", "70.5% of users have unlocked this", true),

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        LinearLayout layout = findViewById(R.id.achievementsLayout);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Achievement a : achievements) {
            LinearLayout card = (LinearLayout) inflater.inflate(R.layout.achievement_card, null);

            TextView title = card.findViewById(R.id.title);
            TextView subtitle = card.findViewById(R.id.subtitle);
            ImageView star = card.findViewById(R.id.star);

            title.setText(a.title);
            subtitle.setText(a.subtitle);
            star.setImageResource(android.R.drawable.btn_star_big_off);

            if (a.highlighted) {
                card.setBackgroundColor(0xFFECECEC); // light gray
            }

            layout.addView(card);
        }
    }
}
