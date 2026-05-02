package com.example.bitewise.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen
import kotlin.math.roundToInt

// Database Imports
import com.example.bitewise.data.BiteWiseDatabase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

//Helper to draw the cards
@Composable
fun EditGoalOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
)
{ // The goal card drawer function starts from here
    val borderColor = if (isSelected) BiteWiseGreen else Color.LightGray
    val backgroundColor = if (isSelected) BiteWiseGreen.copy(alpha = 0.05f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
} // The goal card drawer function ends here

@Composable
fun EditProfileScreen(navController: NavController? = null)
{ //The Edit Profile Composable Starts Here

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    // Database Connection
    val userDao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).userDao()
    val userProfile by (userDao?.getUser() ?: flowOf(null)).collectAsState(initial = null)

    // Variables to store information for this current page
    var gender by remember { mutableStateOf("Male") }
    var ageInput by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Loss") }
    var activityLevel by remember { mutableStateOf("Moderate (exercise 3-5 days/week)") }

    // Autofill the form fields the moment the database loads!
    LaunchedEffect(userProfile) {
        userProfile?.let { user ->
            gender = user.gender
            ageInput = user.age.toString()

            // NEW: Convert Double to Int first, so we drop the ".0" that was breaking the backspace!
            height = user.height.toInt().toString()
            weight = user.weight.toInt().toString()

            goal = user.goal
            activityLevel = user.activityLevel
        }
    }

    //The activity level drop down menu data/variables
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val activityOptions = listOf(
        "Sedentary (little or no exercise)",
        "Light (exercise 1-3 days/week)",
        "Moderate (exercise 3-5 days/week)",
        "Active (exercise 6-7 days/week)"
    )

    //Toggle to change/custom calories
    var isCustomCalories by remember { mutableStateOf(false) }
    var customCalorieInput by remember { mutableStateOf("") }

    // Age Validation
    val parsedAge = ageInput.toIntOrNull() ?: 0
    val isAgeValid = parsedAge in 15..100

    val parsedHeight = height.toDoubleOrNull() ?: 0.0
    val isHeightValid = parsedHeight in 120.0..250.0

    val parsedWeight = weight.toDoubleOrNull() ?: 0.0
    val isWeightValid = parsedWeight in 30.0..300.0

    val parsedCustomCal = customCalorieInput.toIntOrNull() ?: 0
    val isCustomCalValid = !isCustomCalories || parsedCustomCal in 1000..10000

    // Updated Form Validation to include Age!
    val isFormValid = isHeightValid && isWeightValid && isAgeValid && isCustomCalValid

    //A function to calculate the required calorie intake by using all the general information collected from the user.
    fun calculateCalories(): Int { //The REQUIRED CALORIES CALCULATION starts from here
        val h = height.toDoubleOrNull() ?: 0.0
        val w = weight.toDoubleOrNull() ?: 0.0

        //Returns 0 if the inputs are empty.
        if (h == 0.0 || w == 0.0 || parsedAge == 0) return 0

        //Basal Metabolic Rate (BMR) calculation - (Mifflin-St Jeor) - NOW USES PARSED AGE!
        val bmr = if(gender == "Male")
        {
            (10 * w) + (6.25 * h) - (5 * parsedAge) + 5  //Male BMR
        }
        else
        {
            (10 * w) + (6.25 * h) - (5 * parsedAge) - 161 //Female BMR
        }

        //Total Daily Energy Expenditure (TDEE) calculation
        val multiplier = when {
            activityLevel.startsWith("Sedentary") -> 1.2
            activityLevel.startsWith("Light") -> 1.375
            activityLevel.startsWith("Moderate") -> 1.55
            else -> 1.725
        }
        val tdee = bmr * multiplier

        //Adjustment of TDEE depending the on the goal, loss, gain, or maintenance
        val result = when (goal) {
            "Loss" -> tdee - 500
            "Gain" -> tdee + 500
            else -> tdee
        }

        return result.roundToInt()
    } //The REQUIRED CALORIES CALCULATION ends here

    val recommendedCalories = calculateCalories() //the result of the calculation recommended calories is stored here in this variable.

    //UI contents
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {//The UI column starts from here
        Spacer(modifier = Modifier.height(16.dp))

        //The back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //The Edit Profile Header
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Update your physical details and goals",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Age Field!
        Text(
            text = "Age (years)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ageInput,
            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) ageInput = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent
            )
        )
        if (ageInput.isNotEmpty() && !isAgeValid) {
            Text(
                text = "Please enter a valid age (15 - 100)",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Height Field
        Text(
            text = "Height (cm)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) height = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent
            )
        )
        if (height.isNotEmpty() && !isHeightValid) {
            Text(
                text = "Please enter a valid height (120 - 250 cm)",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Weight Field
        Text(
            text = "Current Weight (kg)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) weight = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent
            )
        )
        if (weight.isNotEmpty() && !isWeightValid) {
            Text(
                text = "Please enter a valid weight (30 - 300 kg)",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Weight Goals Selection Sections
        Text(
            text = "What is your goal?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        //#1 Weight Loss
        EditGoalOptionCard(
            title = "Weight Loss",
            subtitle = "Lose weight gradually",
            icon = "📉",
            isSelected = goal == "Loss",
            onClick = {
                goal = "Loss"
                focusManager.clearFocus()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        //#2 Weight Maintenance
        EditGoalOptionCard(
            title = "Maintenance",
            subtitle = "Maintain current weight",
            icon = "➖",
            isSelected = goal == "Maintenance",
            onClick = {
                goal = "Maintenance"
                focusManager.clearFocus()
            }
        )

        //#3 Weight Gain
        Spacer(modifier = Modifier.height(8.dp))

        EditGoalOptionCard(
            title = "Weight Gain",
            subtitle = "Build muscle and mass",
            icon = "📈",
            isSelected = goal == "Gain",
            onClick = {
                goal = "Gain"
                focusManager.clearFocus()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        //Activity drop down menu
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = activityLevel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        isDropdownExpanded = true
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Activity")
                    }
                }
            )
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                activityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            activityLevel = option
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        //The required calories result card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        {//The result card starts from here (Green Card)
            Column(modifier = Modifier.padding(20.dp))
            {//The result column starts here
                Text(text = "Recommended Daily Calories", fontWeight = FontWeight.SemiBold, color = Color.Black)

                //The calorie intake green result text
                Text(
                    text = "$recommendedCalories kcal",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = BiteWiseGreen
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                //Toggle for custom calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                { //The toggle row starts from here
                    Text(text = "Set custom calorie goal", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isCustomCalories,
                        onCheckedChange = {
                            isCustomCalories = it
                            focusManager.clearFocus()
                            if(!it) customCalorieInput = ""
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BiteWiseGreen)
                    )
                }//The toggle row ends here

                Spacer(modifier = Modifier.height(8.dp))

                //The input field for custom calories (only works when the toggle is on)
                OutlinedTextField(
                    value = if (isCustomCalories) customCalorieInput else recommendedCalories.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                            customCalorieInput = newValue
                        }
                    },
                    enabled = isCustomCalories,
                    label = { Text("Daily Calorie Target (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.White,
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                if (isCustomCalories && customCalorieInput.isNotEmpty() && !isCustomCalValid) {
                    Text(
                        text = "Goal must be between 1000 - 10000 kcal",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //Information Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                )
                {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Updating your profile will recalculate your daily calorie targets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

            }//The result column ends here
        }//The result card ends here (Green Card)

        Spacer(modifier = Modifier.height(32.dp))

        //The Done Button
        Button(
            onClick = {
                val finalCalories = if (isCustomCalories && customCalorieInput.isNotEmpty()) {
                    customCalorieInput.toInt()
                } else {
                    recommendedCalories
                }

                userProfile?.let { currentUser ->
                    coroutineScope.launch {
                        val updatedUser = currentUser.copy(
                            age = parsedAge,
                            height = parsedHeight,
                            weight = parsedWeight,
                            goal = goal,
                            activityLevel = activityLevel,
                            dailyCalorieGoal = finalCalories,
                            isSynced = false
                        )
                        userDao?.insertUser(updatedUser)
                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        navController?.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = BiteWiseGreen,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Done",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFormValid) Color.White else Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(100.dp))

    }//The UI column end here
}//The Edit Profile Composable Ends Here

@Preview (showBackground = true, heightDp = 1300)
@Composable
fun EditProfileScreenPreview(){
    EditProfileScreen()
}