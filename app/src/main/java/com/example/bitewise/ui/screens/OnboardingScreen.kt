package com.example.bitewise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.bitewise.ui.theme.BiteWiseGreen



//Data class to hold title, emoji, and description
data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String
)

//Slides logic/data......................................................................................
@Composable
fun OnboardingScreen(navController: NavController? = null){
    val pages = listOf(
        OnboardingPage(
            title = "Track Your Nutrition",
            description = "Log your meals and monitor your daily calorie intake with ease.",
            emoji = "🍎" // Apple Emoji
        ),
        OnboardingPage(
            title = "Achieve Your Goals",
            description = "Whether you want to lose, gain, or maintain weight, we've got you covered.",
            emoji = "📈" // Graph Emoji
        ),
        OnboardingPage(
            title = "Build Streaks",
            description = "Stay motivated with daily streak tracking and milestone rewards.",
            emoji = "🔥" // Fire Emoji
        )
    )

    // Page Slider settings
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }


        //Dots Indicator
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) BiteWiseGreen else Color.LightGray
                val width = if (pagerState.currentPage == iteration) 32.dp else 10.dp
                // Active dot is wider, inactive is a small circle

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                        .size(width = width, height = 10.dp)
                )
            }
        }

        // Main Button (Next / Get Started)
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    navController?.navigate("register") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BiteWiseGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Skip Button
        //Box to ensure the layout height stays consistent whether Skip is shown or hidden
        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
            if (pagerState.currentPage < 2) {
                Text(
                    text = "Skip",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            navController?.navigate("register") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                        .padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


//Slides Contents...................................................................................
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // The green circle container.
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(BiteWiseGreen),
                contentAlignment = Alignment.Center
            ) {
                // The Emoji text inside
                Text(
                    text = page.emoji,
                    fontSize = 60.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(navController = null)
}