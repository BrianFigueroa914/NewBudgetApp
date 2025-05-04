package com.example.newbudgetapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Comparator;

public class LeaderboardActivity extends AppCompatActivity {

    private static class LeaderboardUser {
        String name, ageGroup;
        int achievementsUnlocked;
        double savings;
        double utilization;

        LeaderboardUser(String name, String ageGroup, double savings, int achievementsUnlocked, double utilization) {
            this.name = name;
            this.ageGroup = ageGroup;
            this.savings = savings;
            this.achievementsUnlocked = achievementsUnlocked;
            this.utilization = utilization;
        }

        double getScore() {
            double savingsWeight = 0.5;
            double achievementWeight = 100;
            double utilizationPenalty = 1000;

            return (savings * savingsWeight) +
                    (achievementsUnlocked * achievementWeight) -
                    (utilization * utilizationPenalty);
        }
    }

    private final LeaderboardUser[] users = new LeaderboardUser[]{
            new LeaderboardUser("Alex", "18–24", 1200.0, 5, 0.75),
            new LeaderboardUser("Jordan", "18–24", 900.0, 4, 0.60),
            new LeaderboardUser("Taylor", "18–24", 750.0, 6, 0.30),
            new LeaderboardUser("Ivan", "18–24", 9999.0, 8, 0.85),
            new LeaderboardUser("Kris", "18–24", 1800.0, 6, 0.45),
            new LeaderboardUser("Taha", "18–24", 2200.0, 3, 0.55),
            new LeaderboardUser("Morgan", "18–24", 1500.0, 4, 0.40),
            new LeaderboardUser("Brian", "18–24", 2600.0, 7, 0.60),
            new LeaderboardUser("Tammy", "18–24", 1300.0, 2, 0.70),
            new LeaderboardUser("Drew", "18–24", 1000.0, 5, 0.65),
            new LeaderboardUser("Cameron", "18–24", 1600.0, 4, 0.50),
            new LeaderboardUser("Reese", "18–24", 800.0, 6, 0.35),
            new LeaderboardUser("Remie", "18–24", 2100.0, 7, 0.40),
            new LeaderboardUser("Kendall", "18–24", 1400.0, 3, 0.25),
            new LeaderboardUser("Peyton", "18–24", 900.0, 1, 0.85),
            new LeaderboardUser("Riley", "18–24", 1700.0, 5, 0.50),
            new LeaderboardUser("Sherifat", "18–24", 2400.0, 6, 0.70),
            new LeaderboardUser("Kevin", "18–24", 2000.0, 4, 0.45),
            new LeaderboardUser("Quinn", "18–24", 950.0, 2, 0.30),
            new LeaderboardUser("Frankie", "18–24", 1100.0, 3, 0.60)

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        LinearLayout layout = findViewById(R.id.leaderboardLayout);
        LayoutInflater inflater = LayoutInflater.from(this);

        // Sort users by score descending
        Arrays.sort(users, Comparator.comparingDouble(LeaderboardUser::getScore).reversed());

        for (int i = 0; i < users.length; i++) {
            LeaderboardUser u = users[i];
            LinearLayout card = (LinearLayout) inflater.inflate(R.layout.leaderboard_card, null);

            TextView name = card.findViewById(R.id.name);
            TextView stats = card.findViewById(R.id.stats);

            String medal = "";
            if (i == 0) medal = "\uD83E\uDD47 "; // Gold
            else if (i == 1) medal = "\uD83E\uDD48 "; // Silver
            else if (i == 2) medal = "\uD83E\uDD49 "; // Bronze

            name.setText("#" + (i + 1) + " " + medal + u.name);
            stats.setText("Age Group: " + u.ageGroup + "\nScore: " + (int) u.getScore());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 24); // 24dp bottom spacing
            card.setLayoutParams(params);
            layout.addView(card);
        }
    }
}
