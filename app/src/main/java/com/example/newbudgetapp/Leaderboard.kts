package com.example.mybudgettracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgettracker.ui.theme.MybudgettrackerTheme

data class LeaderboardUser(
    val username: String,
    val ageGroup: String,
    val savings: Double,
    val achievementsUnlocked: Int,
    val utilization: Double
) {
    val overallScore: Double
        get() {
            val savingsWeight = 0.5
            val achievementWeight = 100
            val utilizationPenalty = 1000

            return ((savings * savingsWeight) +
                    (achievementsUnlocked * achievementWeight) -
                    (utilization * utilizationPenalty)).toDouble()
        }
}

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val users = listOf(
        LeaderboardUser("Alex", "18–24", 1200.0, 5, 0.75),
        LeaderboardUser("Jordan", "18–24", 900.0, 4, 0.60),
        LeaderboardUser("Taylor", "18–24", 750.0, 6, 0.30),
        LeaderboardUser("Ivan", "18–24", 3000.0, 8, 0.85),
        LeaderboardUser("Kris", "18–24", 1800.0, 6, 0.45),
        LeaderboardUser("Taha", "18–24", 2200.0, 3, 0.55),
        LeaderboardUser("Morgan", "18–24", 1500.0, 4, 0.40),
        LeaderboardUser("Brian", "18–24", 2600.0, 7, 0.60),
        LeaderboardUser("Sky", "18–24", 1300.0, 2, 0.70),
        LeaderboardUser("Drew", "18–24", 1000.0, 5, 0.65),
        LeaderboardUser("Cameron", "18–24", 1600.0, 4, 0.50),
        LeaderboardUser("Reese", "18–24", 800.0, 6, 0.35),
        LeaderboardUser("Remie", "18–24", 2100.0, 7, 0.40),
        LeaderboardUser("Kendall", "18–24", 1400.0, 3, 0.25),
        LeaderboardUser("Peyton", "18–24", 900.0, 1, 0.85),
        LeaderboardUser("Riley", "18–24", 1700.0, 5, 0.50),
        LeaderboardUser("Sherifat", "18–24", 2400.0, 6, 0.70),
        LeaderboardUser("Kevin", "18–24", 2000.0, 4, 0.45),
        LeaderboardUser("Quinn", "18–24", 950.0, 2, 0.30),
        LeaderboardUser("Frankie", "18–24", 1100.0, 3, 0.60)
    ).sortedByDescending { it.overallScore }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Leaderboard Rankings 18–24",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            itemsIndexed(users) { index, user ->
                val backgroundColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface

                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> ""
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "#${index + 1} $medal - ${user.username}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Age Group: ${user.ageGroup}")
                        Text(text = "Score: ${"%.1f".format(user.overallScore)}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardPreview() {
    MybudgettrackerTheme {
        LeaderboardScreen()
    }
}
