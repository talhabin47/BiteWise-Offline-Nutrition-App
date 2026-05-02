package com.example.bitewise.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen
import java.time.LocalDate
import java.time.LocalTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// NEW: Database & Network Imports
import com.example.bitewise.data.BiteWiseDatabase
import com.example.bitewise.network.NetworkConnectivityObserver
import com.example.bitewise.network.NetworkStatus
import kotlinx.coroutines.flow.flowOf

//data class for meal items
data class MealItem(
    val icon: String,
    val name: String,
    val time: String,
    val kcal: Int
)

//helper to draw the meal card
@Composable
fun MealCard(meal: MealItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon inside a soft background circle for a cleaner look
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = meal.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name on top
                Text(text = meal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))

                // Time and Calories neatly stacked below the name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = meal.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "  •  ", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Text(text = "${meal.kcal} kcal", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = BiteWiseGreen)
                }
            }
        }
    }
}

//the UI for homescreen
@Composable
fun HomeScreen(navController: NavController? = null)
{//The home screen composable starts from here

    // Core Tools & Database Connections
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    // Safely load the User database
    val userFlow = if (isPreview) flowOf(null) else BiteWiseDatabase.getDatabase(context).userDao().getUser()
    val userProfile by userFlow.collectAsState(initial = null)

    // Safely load the Food Log database!
    val foodLogDao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).foodLogDao()
    val allLogs by (foodLogDao?.getAllLogs() ?: flowOf(emptyList())).collectAsState(initial = emptyList())

    // Safely load the Network Observer
    val networkObserver = remember { NetworkConnectivityObserver(context) }
    val networkStatus by networkObserver.observe().collectAsState(initial = NetworkStatus.Unavailable)
    val isOnline = networkStatus == NetworkStatus.Available

    //The variables
    //Dynamic Greeting based on time - varaibles
    val currentHour = LocalTime.now().hour

    // Extract just the first name from the database
    val firstName = userProfile?.name?.split(" ")?.firstOrNull() ?: ""
    val greetingName = if (firstName.isNotEmpty()) ", $firstName" else ""

    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning$greetingName! \uD83D\uDC4B"
        in 12..16 -> "Good Afternoon$greetingName! \uD83D\uDC4B"
        else -> "Good Evening$greetingName! \uD83D\uDC4B"
    }

    //Dynamic date - variables
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))

    //25 fact checked nutrition tips
    val nutritionFacts = listOf(
        "Eating protein-rich foods can boost your metabolism by 15-30% for several hours. This is called the thermic effect of food (TEF) - your body uses more energy to digest protein compared to fats or carbs!",
        "Eating protein within 2 hours after exercise helps maximize muscle recovery and growth.",
        "Aim for 25-30g of fiber daily to support digestive health and maintain steady blood sugar.",
        "Eating a variety of colorful vegetables ensures you get a wide range of essential vitamins and antioxidants.",
        "Using smaller plates can help naturally reduce portion sizes without leaving you feeling deprived.",
        "Sometimes thirst is confused with hunger. Drinking a glass of water before meals can aid digestion and prevent overeating.",
        "Unsaturated fats from avocados, nuts, and olive oil are essential for cellular function and brain health.",
        "Watch out for hidden added sugars in condiments and sauces, which are linked to increased risk of heart disease.",
        "Whole, unprocessed foods contain more intact nutrients and fiber compared to their heavily processed counterparts.",
        "Eating slowly and chewing thoroughly gives your brain the 20 minutes it needs to register fullness signals.",
        "Poor sleep spikes 'ghrelin' (the hunger hormone) and lowers 'leptin' (the fullness hormone), leading to increased cravings.",
        "Your brain is composed of about 60% fat. Consuming Omega-3 fatty acids is crucial for brain health and memory.",
        "Iron from plant-based sources (non-heme iron) is better absorbed when paired with Vitamin C, like spinach with lemon juice.",
        "Dark chocolate with 70% or higher cocoa content is packed with antioxidants and can improve blood flow.",
        "Green tea contains a powerful antioxidant called EGCG, which has been shown to boost metabolism slightly.",
        "Muscle tissue burns more calories at rest than fat tissue, making strength training excellent for long-term weight management.",
        "Potassium-rich foods like bananas, potatoes, and spinach help regulate fluid balance and muscle contractions.",
        "Vitamin D is one of the few vitamins your body can produce on its own when exposed to sunlight.",
        "Skipping meals can cause your blood sugar to drop, leading to fatigue and poor decision-making later in the day.",
        "Caffeine can improve physical performance by mobilizing fatty acids from fat tissues for energy.",
        "Nuts are calorie-dense but highly nutritious. A small handful daily is linked to better heart health.",
        "Eggs are one of the best sources of choline, a nutrient that is incredibly important for brain development.",
        "Oats contain a specific type of soluble fiber called beta-glucan, which is known to help lower cholesterol.",
        "Calcium is not just for bones; it is also required for your heart, muscles, and nerves to function properly.",
        "Spices like cinnamon and turmeric have strong anti-inflammatory properties and can easily be added to daily meals."
    )

    //Picks a random fact every time the user navigates to the homescreen
    val randomFact = remember { nutritionFacts.random() }

    // Filter the database logs so we ONLY see foods logged today!
    val todaysLogs = remember(allLogs) {
        val today = LocalDate.now()
        allLogs.filter { log ->
            val logDate = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            logDate == today && !log.isDeleted // Ensure deleted items don't appear
        }
    }

    // Convert the database logs into the MealItem format for the UI cards
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val todaysMeals = todaysLogs.take(5).map { log ->
        val logTime = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalTime()
        val icon = when {
            log.category.contains("Breakfast") -> "🍳"
            log.category.contains("Lunch") -> "🥗"
            log.category.contains("Dinner") -> "🍽️"
            else -> "🍎"
        }
        MealItem(icon = icon, name = log.name, time = logTime.format(timeFormatter), kcal = log.calories)
    }

    //Calorie Math (Now fully dynamic based on your real logs!)
    val targetCalories = userProfile?.dailyCalorieGoal ?: 2000
    val caloriesEaten = todaysLogs.sumOf { it.calories } // Adds up everything from today!
    val caloriesRemaining = maxOf(0, targetCalories - caloriesEaten) // Prevents negative remaining calories

    //Prevents a "Divide by Zero" crash when the database is empty, and caps at 1.0 so bar doesn't overflow
    val progressPercent = if (targetCalories > 0) {
        (caloriesEaten.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // NEW: THE STREAK ALGORITHM
    val currentStreak = remember(allLogs) {
        if (allLogs.isEmpty()) return@remember 0

        // 1. Extract all unique calendar days the user has ever logged food (excluding template favorites)
        val loggedDates = allLogs.filter { it.timestamp > 0 }.map { log ->
            Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var streak = 0
        var dateToCheck = today

        // 2. Check if the streak is alive (Did they log today or yesterday?)
        if (loggedDates.contains(today)) {
            streak = 1
            dateToCheck = yesterday
        } else if (loggedDates.contains(yesterday)) {
            streak = 1
            dateToCheck = yesterday.minusDays(1)
        } else {
            // They missed today AND yesterday. Streak is dead.
            return@remember 0
        }

        // 3. Count backwards through history!
        while (loggedDates.contains(dateToCheck)) {
            streak++
            dateToCheck = dateToCheck.minusDays(1)
        }

        streak
    }

    // Dynamic streak text formatting
    val streakText = "$currentStreak Day${if (currentStreak == 1) "" else "s"}"
    val streakSubtext = when (currentStreak) {
        0 -> "Log a meal to start!"
        1 -> "Off to a good start! 🔥"
        else -> "Keep it up! 🎉"
    }

    //background color
    val backgroundColor = Color(0xFFF9F9F9)


//UI contents
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ){//The column for homescreen start here
        Box(
            modifier = Modifier.fillMaxWidth()
        ){//the invisible box to hold the green section items starts here
            //the green box itself
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(BiteWiseGreen)
            )

            //column for contents inside the screen section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp)
            )
            {//the green section contents starts here
                //The header/ greeting / date / day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                )//the first row that holds the greeting and internet indicator starts here
                {
                    Column {
                        Text(text = greeting, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = currentDate, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                    }

                    // Smart Offline Indicator (Only shows if there is no internet)
                    if (!isOnline && !isPreview) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }//the first row that holds the greeting and internet indicator ends here

                Spacer(modifier = Modifier.height(32.dp))


                //The tracker card inside the green section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp)
                )
                {//the calorie tracker card starts here
                    Column(modifier = Modifier.padding(24.dp))
                    {//the calorie tracker card column starts/
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        )
                        {// the row that shows the calories eaten and the remaining starts here
                            //the calories today (left)
                            Column {
                                Text(text = "Calories Today", color = Color.Gray, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(text = "$caloriesEaten", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Text(text = "of $targetCalories kcal", color = Color.Gray, fontSize = 14.sp)
                            }

                            //the calories remaining (right)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Remaining", color = Color.Gray, fontSize = 14.sp)
                                Text(text = "$caloriesRemaining", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BiteWiseGreen)
                                Text(text = "kcal", color = Color.Gray, fontSize = 14.sp)
                            }
                        }//the row that shows the calories eaten and the remaining ends here

                        Spacer(modifier = Modifier.height(20.dp))

                        //calorie progression bar
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = BiteWiseGreen,
                            trackColor = Color(0xFFE0E0E0),
                            strokeCap = StrokeCap.Round
                        )
                    }//the calorie tracker card column ends
                }//the calorie tracker card ends here
            }//the green section contents ends here
        }//the invisible box to hold the green section items ends here

        Spacer(modifier = Modifier.height(16.dp))

        //The streak card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light Orange
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFCC80).copy(alpha = 0.5f))
        )
        {//The streak card starts from here
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {//The row for fire symbol and streak information starts here

                //Streak symbol (Changes to Gray if streak is 0!)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (currentStreak > 0) Color(0xFFF57C00) else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (currentStreak > 0) "🔥" else "🧊", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                //Streak info (Now 100% Dynamic!)
                Column {
                    Text(text = "Current Streak", color = Color.Gray, fontSize = 14.sp)
                    Text(text = streakText, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                    Text(text = streakSubtext, fontSize = 12.sp, color = if (currentStreak > 0) Color(0xFFE65100) else Color.Gray)
                }
            }//The row for fire symbol and streak information ends here
        }//The streak card end here


        Spacer(modifier = Modifier.height(16.dp))

        //Did you know? card

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)), // Very Light Green
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BiteWiseGreen.copy(alpha = 0.2f))
        )
        {//The did you know card starts here
            Column(modifier = Modifier.padding(20.dp))
            {//The column of the did you know card starts here
                Row(verticalAlignment = Alignment.CenterVertically)
                {//the row for the Apple emoji, title, and description starts here
                    //the Apple emoji
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍏", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(text = "Did You Know? 💡", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                }//the row for the Apple emoji, title, and description ends here

                Spacer(modifier = Modifier.height(12.dp))

                //Did you know info texts/ randomly changes as assigned in the nutritionFacts list
                Text(
                    text = randomFact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }//The column of the did you know card ends here
        }//The did you know card ends here

        Spacer(modifier = Modifier.height(24.dp))

        //Recent meals section
        Column(modifier = Modifier.padding(horizontal = 24.dp))
        {//recent meals column starts here
            //"Recent Meals" text
            Text(
                text = "Recent Meals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of recent logged meals! (Or a friendly message if empty)
            if (todaysMeals.isEmpty()) {
                Text(
                    text = "No meals logged yet today! Time to eat! 😋",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                todaysMeals.forEach { meal ->
                    MealCard(meal = meal)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //Log-food button
            Button(
                onClick = {
                    navController?.navigate("log_food") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C8D5E)),
                shape = RoundedCornerShape(12.dp)
            )
            {
                Icon(Icons.Default.Add, contentDescription = "Log Food", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Log Food", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }//recent meals column ends here

        Spacer(modifier = Modifier.height(100.dp))

    }//The column for homescreen ends here
}//The home screen composable ends here

@Preview(showBackground = true, heightDp = 2000)
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}