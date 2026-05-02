package com.example.bitewise.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Database Imports
import com.example.bitewise.data.BiteWiseDatabase
import com.example.bitewise.data.FoodLogEntity
import com.example.bitewise.data.FoodCatalog
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

//Data classes
data class FoodItem(
    val name: String,
    val portion: String,
    val calories: Int,
    val initialFavorite: Boolean = false,
    val category: String = ""
)

//Helpers: Card for Searching and Favoriting
@Composable
fun FoodCard(
    food: FoodItem,
    onAddClick: (Boolean) -> Unit,
    onFavoriteClick: (Boolean) -> Unit = {}
) {
    var isFavorite by remember(food.name, food.initialFavorite) { mutableStateOf(food.initialFavorite) }

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
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = food.portion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${food.calories} kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        onFavoriteClick(isFavorite)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFC107) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { onAddClick(isFavorite) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Food",
                        tint = BiteWiseGreen
                    )
                }
            }
        }
    }
}

// Card for items you ALREADY logged today (with a delete button!)
@Composable
fun LoggedFoodCard(
    log: FoodLogEntity,
    onDeleteClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val logTime = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalTime()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)), // Very light green background
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                // Shows the category and the exact time you logged it!
                Text(text = "${log.category} • ${logTime.format(timeFormatter)}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${log.calories} kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BiteWiseGreen)
                Spacer(modifier = Modifier.width(16.dp))

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = Color(0xFFE53935) // Red
                    )
                }
            }
        }
    }
}


//The main UI of the log food screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodScreen(navController: NavController? = null)
{//The Log Food Screen Composable starts here

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current // NEW: We need this to control the keyboard!
    val coroutineScope = rememberCoroutineScope()
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    // Database Connections
    val dao = if (isPreview) null else BiteWiseDatabase.getDatabase(context).foodLogDao()

    // Load all foods from your JSON file
    val fullFoodCatalog = remember {
        if (isPreview) emptyList() else FoodCatalog.getFoodsFromJSON(context)
    }

    // Group the foods by category
    val foodDatabaseByCategory = remember(fullFoodCatalog) {
        fullFoodCatalog.groupBy { it.category }
    }

    // Live Stream of Favorites from the Database (Hides deleted items!)
    val rawFavorites by (dao?.getFavoriteFoods() ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    val favoriteFoodsFromDB = rawFavorites.filter { !it.isDeleted }.distinctBy { it.name }.map {
        FoodItem(it.name, it.portion, it.calories, true, it.category)
    }

    // Live Stream of ALL logs, filtered for today! (Hides deleted items!)
    val allLogs by (dao?.getAllLogs() ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    val todaysLogs = remember(allLogs) {
        val today = LocalDate.now()
        allLogs.filter { log ->
            // Hide Favorite Templates! (Because we gave them negative timestamps)
            if (log.timestamp < 0) return@filter false

            val logDate = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            logDate == today && !log.isDeleted
        }
    }

    //Variables
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFFF9F9F9)

    //UI contents
    // NEW: Added pointerInput to detect taps anywhere on the screen to close the keyboard
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    )
    {//the box for the log food screen starts here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        )
        {//the column for the log food screen starts here
            Spacer(modifier = Modifier.height(40.dp))

            //header
            Text(
                text = "Log Food",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 200+ foods...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            focusManager.clearFocus() // Close keyboard when clearing search
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true, // NEW: Prevents the text box from expanding!
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // NEW: Changes "Enter" to "Search"
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }), // NEW: Closes keyboard on Search
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = BiteWiseGreen
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // THE SEARCH ENGINE LOGIC
            if (searchQuery.isNotEmpty()) {
                // If the user is typing, show search results!
                Text(text = "Search Results", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                val searchResults = fullFoodCatalog.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    if (searchResults.isEmpty()) {
                        item { Text(text = "No foods found matching '$searchQuery'", color = Color.Gray) }
                    } else {
                        items(searchResults) { food ->
                            FoodCard(
                                food = food,
                                onAddClick = { isFav ->
                                    coroutineScope.launch {
                                        dao?.insertFoodLog(
                                            FoodLogEntity(
                                                name = food.name, portion = food.portion, calories = food.calories,
                                                category = food.category.ifEmpty { "Snacks 🍎" }, isFavorite = isFav,
                                                timestamp = System.currentTimeMillis(), // POSITIVE = Real Meal
                                                isSynced = false
                                            )
                                        )
                                        focusManager.clearFocus() // Close keyboard when adding a food
                                        Toast.makeText(context, "${food.name} logged!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFavoriteClick = { isFav ->
                                    coroutineScope.launch {
                                        if (isFav) {
                                            dao?.insertFoodLog(
                                                FoodLogEntity(
                                                    name = food.name, portion = food.portion, calories = food.calories,
                                                    category = food.category.ifEmpty { "Snacks 🍎" }, isFavorite = true,
                                                    timestamp = -System.currentTimeMillis(), // NEGATIVE = Template Only
                                                    isSynced = false
                                                )
                                            )
                                            Toast.makeText(context, "${food.name} added to Favorites!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            dao?.removeFavorite(food.name)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // 1. CATEGORIES SECTION
                    item {
                        Text(text = "Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(foodDatabaseByCategory.keys.toList()) { categoryName ->
                                Button(
                                    onClick = {
                                        selectedCategory = categoryName
                                        showDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BiteWiseGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(text = categoryName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // 2. TODAY'S LOG SECTION
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = BiteWiseGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Today's Log", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (todaysLogs.isEmpty()) {
                        item {
                            Text(
                                text = "You haven't logged any food yet today.",
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                    } else {
                        items(todaysLogs) { log ->
                            LoggedFoodCard(
                                log = log,
                                onDeleteClick = {
                                    coroutineScope.launch {
                                        // Soft Delete Magic!
                                        dao?.insertFoodLog(log.copy(isDeleted = true, isSynced = false))
                                        Toast.makeText(context, "Log deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // 3. FAVORITES SECTION
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Favorites", tint = Color(0xFFFFC107))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Your Favorites", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (favoriteFoodsFromDB.isEmpty()) {
                        item {
                            Text(
                                text = "No favorites yet. Tap the star on foods to save them here!",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(favoriteFoodsFromDB) { food ->
                            FoodCard(
                                food = food,
                                onAddClick = { isFav ->
                                    coroutineScope.launch {
                                        dao?.insertFoodLog(
                                            FoodLogEntity(
                                                name = food.name, portion = food.portion, calories = food.calories,
                                                category = food.category.ifEmpty { "Snacks 🍎" }, isFavorite = isFav,
                                                timestamp = System.currentTimeMillis(), // POSITIVE = Real Meal
                                                isSynced = false
                                            )
                                        )
                                        Toast.makeText(context, "${food.name} logged!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFavoriteClick = { isFav ->
                                    coroutineScope.launch {
                                        if (isFav) {
                                            dao?.insertFoodLog(
                                                FoodLogEntity(
                                                    name = food.name, portion = food.portion, calories = food.calories,
                                                    category = food.category.ifEmpty { "Snacks 🍎" }, isFavorite = true,
                                                    timestamp = -System.currentTimeMillis(), // NEGATIVE = Template Only
                                                    isSynced = false
                                                )
                                            )
                                            Toast.makeText(context, "${food.name} added to Favorites!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            dao?.removeFavorite(food.name)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }


            //the dialogue box pop up feature for listing the food according to the category
            if (showDialog)
            {//if show dialog starts here
                Dialog(
                    onDismissRequest = { showDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                )
                {//dialog box starts here
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.8f)
                            .clip(RoundedCornerShape(24.dp)),
                        color = backgroundColor
                    )
                    {//the surface for the dialog box starts here
                        Column(modifier = Modifier.fillMaxSize())
                        {//column of the dialog box starts here
                            // Dialog Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(16.dp)
                            )
                            {
                                Text(
                                    text = selectedCategory,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )

                                IconButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                                }
                            }

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                            {
                                val foodsToShow = foodDatabaseByCategory[selectedCategory] ?: emptyList()
                                items(foodsToShow) { food ->
                                    FoodCard(
                                        food = food,
                                        onAddClick = { isFav ->
                                            coroutineScope.launch {
                                                dao?.insertFoodLog(
                                                    FoodLogEntity(
                                                        name = food.name,
                                                        portion = food.portion,
                                                        calories = food.calories,
                                                        category = selectedCategory,
                                                        isFavorite = isFav,
                                                        timestamp = System.currentTimeMillis(), // POSITIVE = Real Meal
                                                        isSynced = false
                                                    )
                                                )
                                                Toast.makeText(context, "${food.name} logged!", Toast.LENGTH_SHORT).show()
                                                showDialog = false
                                            }
                                        },
                                        onFavoriteClick = { isFav ->
                                            coroutineScope.launch {
                                                if (isFav) {
                                                    dao?.insertFoodLog(
                                                        FoodLogEntity(
                                                            name = food.name, portion = food.portion, calories = food.calories,
                                                            category = selectedCategory, isFavorite = true,
                                                            timestamp = -System.currentTimeMillis(), // NEGATIVE = Template Only
                                                            isSynced = false
                                                        )
                                                    )
                                                    Toast.makeText(context, "${food.name} added to Favorites!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    dao?.removeFavorite(food.name)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }//column of the dialog box ends here
                    }//the surface for the dialog box ends here
                }//the dialog box ends here
            }//if show dialog ends here

        }//the column for the log food screen ends here
    }//the box for the log food screen ends here

}//The Log Food Screen Composable ends here


@Preview (showBackground = true)
@Composable
fun LogFoodScreenPreview(){
    LogFoodScreen()
}