package com.example.bitewise.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

// NEW: Database & Time Imports
import com.example.bitewise.data.BiteWiseDatabase
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

//Helper to draw the milestone cards
@Composable
fun MilestoneCard(emoji: String, title: String, subtitle: String, isAchieved: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAchieved) Color(0xFFFFF8E1) else Color.White // Light yellow if achieved
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (isAchieved) BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isAchieved) BiteWiseGreen else Color(0xFFE0E0E0))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isAchieved) "Achieved!" else "Locked",
                    color = if (isAchieved) Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}



//The UI for goals screen
@Composable
fun GoalsScreen(navController: NavController? = null)
{//The comparable for goals screen starts here

    // NEW: Core Tools & Database Connections
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    // Load User Profile (for the target goal)
    val userFlow = if (isPreview) flowOf(null) else BiteWiseDatabase.getDatabase(context).userDao().getUser()
    val userProfile by userFlow.collectAsState(initial = null)

    // Load all Food Logs (for the charts and math)
    val foodLogDao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).foodLogDao()
    val allLogs by (foodLogDao?.getAllLogs() ?: flowOf(emptyList())).collectAsState(initial = emptyList())

    // The Variables
    val dailyGoal = userProfile?.dailyCalorieGoal ?: 2000

    // NEW: The Time Travel State Tracker! (0 = This week, -1 = Last week, etc.)
    var weekOffset by remember { mutableStateOf(0) }

    // Logic to calculate the text for the currently selected week (e.g., "Mar 9 - Mar 15")
    val targetWeekDate = LocalDate.now().plusWeeks(weekOffset.toLong())
    val startOfWeek = targetWeekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = startOfWeek.plusDays(6)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val dateRangeText = "${startOfWeek.format(dateFormatter)} - ${endOfWeek.format(dateFormatter)}"

    // Weekly Chart Data Logic (Now relies on the weekOffset!)
    val (weekData, totalThisWeek, dailyAverage) = remember(allLogs, weekOffset) {
        val today = LocalDate.now()

        // Group all logs by their exact calendar date
        val logsByDate = allLogs.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        // Generate the 7 days (Mon-Sun) and sum up the calories for each
        val calculatedWeekData = (0..6).map { offset ->
            val date = startOfWeek.plusDays(offset.toLong())
            val dayName = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } // E.g., "Mon"
            val caloriesForDay = logsByDate[date]?.sumOf { it.calories } ?: 0
            Pair(dayName, caloriesForDay)
        }

        val total = calculatedWeekData.sumOf { it.second }

        // NEW: If past week, divide by 7. If current week, divide by what day of the week it is today!
        val daysToDivide = if (weekOffset < 0) 7 else today.dayOfWeek.value
        val average = if (total == 0) 0 else total / daysToDivide

        Triple(calculatedWeekData, total, average)
    }

    // Dynamic Streak Logic
    val currentStreak = remember(allLogs) {
        if (allLogs.isEmpty()) return@remember 0
        val loggedDates = allLogs.map { log ->
            Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        var streak = 0
        var dateToCheck = today

        if (loggedDates.contains(today)) {
            streak = 1
            dateToCheck = yesterday
        } else if (loggedDates.contains(yesterday)) {
            streak = 1
            dateToCheck = yesterday.minusDays(1)
        } else {
            return@remember 0
        }

        while (loggedDates.contains(dateToCheck)) {
            streak++
            dateToCheck = dateToCheck.minusDays(1)
        }
        streak
    }

    //Dynamic Message Logic based on average vs goal
    val diff = dailyAverage - dailyGoal
    val (msgBgColor, msgIcon, msgTitle, msgBody) = when {
        diff > 200 -> listOf(
            Color(0xFFE53935), "⚠️", "Over Goal",
            "Your average is $dailyAverage kcal. You are consistently over your daily goal. Try to cut back on high-calorie snacks."
        )
        diff < -200 -> listOf(
            Color(0xFFF57C00), "🔔", "Under Goal",
            "Your average is $dailyAverage kcal. You are eating significantly less than your goal. Make sure you're fueling your body enough!"
        )
        else -> listOf(
            BiteWiseGreen, "🏆", "Great Progress!",
            "Your average daily intake is $dailyAverage kcal. You're staying within your goal! Keep it up!"
        )
    }

    //The background color
    val backgroundColor = Color(0xFFF9F9F9)

    //UI contents

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    )
    {//The column for goals screen starts here
        //The green section itself
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(BiteWiseGreen)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        )
        {//The green section starts here
            //The header and the description
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Your Goals", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Track your progress and achievements", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }//The green section ends here

        Spacer(modifier = Modifier.height(24.dp))

        //The daily goal card
        Column(modifier = Modifier.padding(horizontal = 24.dp))
        {//The daily goal card column starts here
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            )
            {//The daily goal card starts from here
                Column(modifier = Modifier.padding(20.dp))
                {//A column to hold all the card contents starts here
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {//The row to hold the green logo and the daily goal text & the edit button starts here

                        //Container 1: The green logo and the Daily Goal, Calories Text (left)
                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            //The green logo goals screen
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            )
                            {
                                Icon(Icons.Default.TrackChanges, contentDescription = "Goal", tint = BiteWiseGreen)
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            //Daily Calorie Goal text and the calories in the middle
                            Column {
                                Text(text = "Daily Calorie Goal", color = Color.Gray, fontSize = 14.sp)
                                Text(text = "$dailyGoal kcal", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                            }
                        }

                        //Container 2: Edit Button (right)
                        IconButton(onClick = {
                            navController?.navigate("edit_profile") {
                                //Prevents multiple edit screens from opening if tapped fast
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = Color.Gray)
                        }
                    }//The row to hold the green logo and the daily goal text & the edit button ends here

                    Spacer(modifier = Modifier.height(16.dp))

                    //The info section inside the daily goal card
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F8F1)).padding(12.dp)
                    )
                    {
                        Text(
                            text = "This is your daily calorie target based on your goals and activity level.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }//A column to hold all the card contents starts here
            }//The daily goal card ends here
        }//The daily goal card column ends here

        Spacer(modifier = Modifier.height(24.dp))

        //The weekly calorie intake card/ the bar graph card
        Column(modifier = Modifier.padding(horizontal = 24.dp))
        {//The weekly calorie intake card/ the bar graph card COLUMN starts here
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            )
            {//The Weekly Calorie Intake/ the bar graph CARD starts here
                Column(modifier = Modifier.padding(20.dp))
                {//A column to hold the bar graph card contents starts here

                    //The header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {//Row to hold the logo, Weely Calorie Intake, and the kcal/day text starts here
                        //The logo (Weekly Calorie Intake)
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ShowChart, contentDescription = "Chart", tint = Color(0xFF1976D2))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        //The text "Weekly Calorie Intake" & kcal/day
                        Column {
                            Text(text = "Weekly Calorie Intake", color = Color.Gray, fontSize = 14.sp)
                            Text(text = "$dailyAverage kcal/day", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                        }
                    }//Row to hold the logo, Weely Calorie Intake, and the kcal/day text starts here

                    Spacer(modifier = Modifier.height(16.dp))

                    // NEW: The Time Travel UI Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { weekOffset-- }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Week", tint = Color.Gray)
                        }

                        Text(text = dateRangeText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BiteWiseGreen)

                        // Prevents the user from clicking into future weeks!
                        IconButton(
                            onClick = { weekOffset++ },
                            enabled = weekOffset < 0
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next Week",
                                tint = if (weekOffset < 0) Color.Gray else Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Total This Week and Daily Average cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp))
                    {//Row for total this week and daily average cards starts here

                        //Daily Average Card
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F8F1)).padding(16.dp), contentAlignment = Alignment.Center)
                        {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Total This Week", color = Color.Gray, fontSize = 12.sp)
                                Text(text = "$totalThisWeek kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BiteWiseGreen)
                            }
                        }

                        //Total This Week Card
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF9F9F9)).padding(16.dp), contentAlignment = Alignment.Center)
                        {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Daily Average", color = Color.Gray, fontSize = 12.sp)
                                Text(text = "$dailyAverage kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            }
                        }
                    }//Row for total this week and daily average cards ends here

                    Spacer(modifier = Modifier.height(24.dp))

                    //Custom bar chart
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp))
                    {//The box for custom bar chart starts here
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        )
                        {//Row for custom bar chart starts here

                            //#1 A dynamic ceiling for the chart height so it never overflows
                            val maxChartCal = maxOf(2500f, (dailyGoal * 1.5f), weekData.maxOf { it.second }.toFloat() * 1.2f)

                            //#2 Loop through weekData list (Mon to Sun)
                            weekData.forEach {//loop starts here
                                    (day, calories) ->

                                //#3 Logic to determine color and height
                                val isOverGoal = calories > dailyGoal
                                val barColor = if (isOverGoal) Color(0xFFE67C30) else Color(0xFF558B60) // Orange or Green
                                val fillPercentage = (calories / maxChartCal).coerceIn(0f, 1f)

                                //#4 Drawing of the actual bar and text for this specific days
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxHeight()
                                ) {//The column for bar drawing it starts here
                                    //Number at the top of the bar
                                    Text(
                                        text = calories.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    //The bar area
                                    Box(
                                        modifier = Modifier.weight(1f).width(28.dp),
                                        contentAlignment = Alignment.BottomCenter // Makes the bar grow UP from the bottom
                                    ) {
                                        // The actual colored bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(fillPercentage)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(barColor)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 3. The Day Label (Mon, Tue, etc)
                                    Text(text = day, fontSize = 12.sp, color = Color.Gray)
                                }//The column for the bar drawing ends here
                            }//the loop ends here
                        }//Row for custom bar chart ends here
                    }//The box for custom bar chart ends here

                    Spacer(modifier = Modifier.height(16.dp))

                    //Within goal and ver goal cards
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF9F9F9)).padding(12.dp))
                    {//the box to hold within goal and over goal cards starts here
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp))
                        {//Row for within goal and over goal cards starts here
                            //Within goal card
                            Row(verticalAlignment = Alignment.CenterVertically)
                            {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF558B60)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Within goal", fontSize = 14.sp, color = Color.DarkGray)
                            }

                            //Over goal card
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFE67C30)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Over goal", fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }//Row for within goal and over goal cards ends here
                    }//The box to hold within and over goal cards ends here
                }//A column to hold the bar graph card contents ends here
            }//The Weekly Calorie Intake/ the bar graph CARD ends here
        }//The weekly calorie intake card/ the bar graph card COLUMN ends here

        Spacer(modifier = Modifier.height(24.dp))

        //Streak milestone cards
        Column(modifier = Modifier.padding(horizontal = 24.dp))
        {
            //The text Streak Milestone
            Text(text = "Streak Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            // These automatically unlock based on your actual streak!
            MilestoneCard(emoji = "🔥", title = "1 Week Streak", subtitle = "7 consecutive days", isAchieved = currentStreak >= 7)
            MilestoneCard(emoji = "⭐", title = "2 Week Streak", subtitle = "14 consecutive days", isAchieved = currentStreak >= 14)
            MilestoneCard(emoji = "🏆", title = "1 Month Streak", subtitle = "30 consecutive days", isAchieved = currentStreak >= 30)
            MilestoneCard(emoji = "💎", title = "100 Day Streak", subtitle = "100 consecutive days", isAchieved = currentStreak >= 100)
        }

        Spacer(modifier = Modifier.height(16.dp))

        //A message box that provides message according to the current progress.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(msgBgColor as Color)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = msgIcon as String, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = msgTitle as String, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = msgBody as String, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Copyright Footer
        Text(
            text = "© 2026 BiteWise. All rights reserved.",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Spacer to clear the bottom navigation bar
        Spacer(modifier = Modifier.height(100.dp))
    } //The column for goals screen ends here
}//The goals screen composable ends here


@Preview(showBackground = true, heightDp = 2000)
@Composable
fun GoalsScreenPreview(){
    GoalsScreen()
}