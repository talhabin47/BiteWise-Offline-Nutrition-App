package com.example.bitewise.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitewise.ui.screens.*
import com.example.bitewise.ui.theme.BiteWiseGreen

//............................................................................................................
//APP BOTTOM NAVIGATION DATA..................................................................................

//Data class for each tab
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)
//The list of 5 navigation bar options
val bottomNavItems = listOf(
    BottomNavItem(title = "Home", route = "home", icon = Icons.Default.Home),
    BottomNavItem(title = "Log Food", route = "log_food", icon = Icons.Default.Add),
    BottomNavItem(title = "Goals", route = "goals", icon = Icons.Default.TrackChanges),
    BottomNavItem(title = "Learn", route = "education", icon = Icons.Default.MenuBook),
    BottomNavItem(title = "Profile", route = "profile", icon = Icons.Default.Person)
)

//............................................................................................................
//THE VISUAL BOTTOM BAR COMPOSABLE............................................................................
@Composable
fun BiteWiseBottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Only navigate if we aren't already on that screen
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to home to avoid building a massive backstack of tabs
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = if (item.title == "Log Food") Modifier.size(32.dp) else Modifier.size(24.dp)
                    )
                },
                label = { Text(text = item.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BiteWiseGreen,
                    selectedTextColor = BiteWiseGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = BiteWiseGreen.copy(alpha = 0.1f) // Light green background behind selected icon
                )
            )
        }
    }
}

//...................................................................................................
//APP NAVIGATION FUNCTION (HOST).......................................................................
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Track the current screen to know when to show the bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens SHOULD have the bottom bar visible
    val screensWithBottomBar = listOf("home", "log_food", "goals", "education", "profile")

    // Wrap the NavHost in a Scaffold
    Scaffold(
        bottomBar = {
            // Only showing the bar if the current route is one of the main 5 tabs
            if (currentRoute in screensWithBottomBar) {
                BiteWiseBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {

            // Splash Screen #0
            composable("splash") { SplashScreen(navController = navController) }

            // Onboarding Screen #1
            composable("onboarding") { OnboardingScreen(navController = navController) }

            // Login Screen #2
            composable("login") { LoginScreen(navController = navController) }

            // Forgot Password Screen #2.5
            composable("forgot_password") { ForgotPasswordScreen(navController = navController) }

            // Register Screen #3
            composable("register") { RegisterScreen(navController = navController) }

            // Profile Setup Screen #4 (UPDATED to catch the Name)
            composable(
                route = "profile_setup/{name}/{gender}/{age}",
                arguments = listOf(
                    navArgument("name") { type = NavType.StringType },
                    navArgument("gender") { type = NavType.StringType },
                    navArgument("age") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                // Unpack and decode the safe name
                val rawName = backStackEntry.arguments?.getString("name") ?: "No Name"
                val passedName = java.net.URLDecoder.decode(rawName, java.nio.charset.StandardCharsets.UTF_8.toString())

                val passedGender = backStackEntry.arguments?.getString("gender") ?: "Male"
                val passedAge = backStackEntry.arguments?.getInt("age") ?: 25

                ProfileSetupScreen(
                    navController = navController,
                    name = passedName,
                    gender = passedGender,
                    age = passedAge
                )
            }

            // Home Screen #5
            composable("home") { HomeScreen(navController = navController) }

            // Log Food Screen #6
            composable("log_food") { LogFoodScreen(navController = navController) }

            // Goal Screen #7
            composable("goals") { GoalsScreen(navController = navController) }

            // Education Screen #8
            composable("education") { EducationScreen(navController = navController) }

            // Profile Screen #9
            composable("profile") { ProfileScreen(navController = navController) }

            // Edit Profile Screen #10
            composable("edit_profile") { EditProfileScreen(navController = navController) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    val dummyNavController = rememberNavController()
    BiteWiseBottomNavigationBar(
        navController = dummyNavController,
        currentRoute = "home"
    )
}