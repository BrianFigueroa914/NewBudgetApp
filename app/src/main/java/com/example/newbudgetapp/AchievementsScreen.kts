package com.example.mybudgettracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgettracker.ui.theme.MybudgettrackerTheme

data class Achievement(
    val title: String,
    val unlockedPercentage: Double
)

@Composable
fun AchievementsScreen(modifier: Modifier = Modifier) {
    val achievements = listOf(
        Achievement("Save \$100 ✅", 70.5),
        Achievement("Save \$500 💵", 25.7),
        Achievement("Save \$1,000 🎉", 10.1),
        Achievement("Save \$5,000 🏆", 7.0),
        Achievement("Save \$10,000 💎", 2.6),
        Achievement(" \"No Spend Day\"  🚫🛍️", 50.7),
        Achievement("No eating out for 7 days 🍱", 36.6),
        Achievement("No impulse purchases for a week 🚫💳", 7.8),
        Achievement("Have less than 3 subscription services 📉", 3.0),
        Achievement("Build an emergency fund 💼", 0.5),
        Achievement("Pay off a debt 💳", 3.8),
        Achievement("No Spend Weekend 💳", 4.8),
        Achievement("Spend \$0 on entertainment for 7 days❌", 36.8),
        Achievement("No online shopping for 14 days 💳", 44.8),
        Achievement("Spend less than \$10/day for a week 💳", 25.8),
        Achievement("No Spend Week 💳", 1.1),
        Achievement("Buy Nothing New for a Week 💳", 4.5),
        Achievement("Buy Nothing New for a Month 💳", 2.3),
        Achievement("Buy Nothing New for a Day 💳", 1.9),
        Achievement("Buy Nothing New for 2 Months 💳", 0.8),
        Achievement("Reach a savings rate of 20% of your income 📉", 2.2)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            itemsIndexed(achievements) { index, achievement ->
                val backgroundColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(achievement.title)
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Achievement"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(achievement.unlockedPercentage)}% of users have unlocked this",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AchievementsPreview() {
    MybudgettrackerTheme {
        AchievementsScreen()
    }
}
