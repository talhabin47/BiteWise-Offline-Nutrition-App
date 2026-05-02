package com.example.bitewise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bitewise.ui.theme.BiteWiseGreen

//data class for the article catalogs
data class ArticleItem(
    val title: String,
    val description: String,
    val readTime: String,
    val icon: ImageVector,
    val iconTint: Color,
    val backgroundColor: Color,
    val url: String
)

//data class for quick tips item
data class TipItem(
    val emoji: String,
    val title: String,
    val description: String
)

//helper to draw the articles card
@Composable
fun ArticleCard(article: ArticleItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = article.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circular Icon Background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = article.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = article.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = article.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = article.readTime, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }
    }
}

//helper to draw the tips card
@Composable
fun TipCard(tip: TipItem) {
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
            verticalAlignment = Alignment.Top
        ) {
            Text(text = tip.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = tip.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = tip.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}






@Composable
fun EducationScreen(navController: NavController? = null)
{//The education screen composable starts here

    //handler to open websites in phone browser
    val uriHandler = LocalUriHandler.current

    //Data for the articles
    val featuredArticles = listOf(
        ArticleItem(
            title = "Benefits of Protein",
            description = "Discover how protein helps build muscle, supports metabolism, and keeps you feeling full longer.",
            readTime = "5 min read",
            icon = Icons.Outlined.FitnessCenter,
            iconTint = Color(0xFF1976D2), // Blue
            backgroundColor = Color(0xFFE3F2FD),
            url = "https://www.hsph.harvard.edu/nutritionsource/what-should-you-eat/protein/"
        ),
        ArticleItem(
            title = "Healthy Carbohydrates",
            description = "Learn about complex carbs, fiber, and how they provide sustained energy throughout your day.",
            readTime = "4 min read",
            icon = Icons.Outlined.Eco,
            iconTint = Color(0xFF388E3C), // Green
            backgroundColor = Color(0xFFE8F5E9),
            url = "https://www.hsph.harvard.edu/nutritionsource/carbohydrates/"
        ),
        ArticleItem(
            title = "Importance of Hydration",
            description = "Understanding how proper hydration affects your metabolism, energy levels, and overall health.",
            readTime = "3 min read",
            icon = Icons.Outlined.WaterDrop,
            iconTint = Color(0xFF0288D1), // Light Blue
            backgroundColor = Color(0xFFE1F5FE),
            url = "https://www.hsph.harvard.edu/nutritionsource/water/"
        ),
        ArticleItem(
            title = "Boosting Your Metabolism",
            description = "Science-backed strategies to naturally increase your metabolic rate and burn more calories.",
            readTime = "6 min read",
            icon = Icons.Outlined.Bolt,
            iconTint = Color(0xFFF57F17), // Orange/Yellow
            backgroundColor = Color(0xFFFFF9C4),
            url = "https://www.healthline.com/nutrition/10-ways-to-boost-metabolism"
        ),
        ArticleItem(
            title = "Heart-Healthy Eating",
            description = "Foods and habits that support cardiovascular health and reduce risk of heart disease.",
            readTime = "5 min read",
            icon = Icons.Outlined.FavoriteBorder,
            iconTint = Color(0xFFD32F2F), // Red
            backgroundColor = Color(0xFFFFEBEE),
            url = "https://www.mayoclinic.org/diseases-conditions/heart-disease/in-depth/heart-healthy-diet/art-20047702"
        ),
        ArticleItem(
            title = "Nutrition for Brain Health",
            description = "Essential nutrients that support cognitive function, memory, and mental clarity.",
            readTime = "7 min read",
            icon = Icons.Outlined.Psychology,
            iconTint = Color(0xFF7B1FA2), // Purple
            backgroundColor = Color(0xFFF3E5F5),
            url = "https://www.health.harvard.edu/healthbeat/foods-linked-to-better-brainpower"
        )
    )

    //data for the quick tips
    val quickTips = listOf(
        TipItem("⏰", "Meal Timing Matters", "Eating protein within 2 hours after exercise helps maximize muscle recovery and growth."),
        TipItem("🌾", "Fiber is Your Friend", "Aim for 25-30g of fiber daily to support digestive health and maintain steady blood sugar."),
        TipItem("🎨", "Colorful Plates", "Eating a variety of colorful vegetables ensures you get a wide range of essential vitamins and antioxidants."),
        TipItem("🍽️", "Portion Control", "Using smaller plates can help naturally reduce portion sizes without leaving you feeling deprived."),
        TipItem("🚰", "Hydration Masking", "Sometimes thirst is confused with hunger. Drinking a glass of water before meals can aid digestion and prevent overeating."),
        TipItem("🥑", "Don't Fear Healthy Fats", "Unsaturated fats from avocados, nuts, and olive oil are essential for cellular function and brain health."),
        TipItem("🔍", "Read Labels Carefully", "Watch out for hidden added sugars in condiments and sauces, which are linked to increased risk of heart disease."),
        TipItem("🥦", "Prioritize Whole Foods", "Whole, unprocessed foods contain more intact nutrients and fiber compared to their heavily processed counterparts."),
        TipItem("🧘", "Mindful Eating", "Eating slowly and chewing thoroughly gives your brain the 20 minutes it needs to register fullness signals."),
        TipItem("😴", "Sleep Affects Appetite", "Poor sleep spikes 'ghrelin' (the hunger hormone) and lowers 'leptin' (the fullness hormone), leading to increased cravings.")
    )

    //variable to hold background color
    val backgroundColor = Color(0xFFF9F9F9)

    //The UI components of the education screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    )
    {//the education screen UI column starts here


        //The green section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(BiteWiseGreen)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        )
        {//the green section starts here
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    //The logo and food science text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = "Book", tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Food Science",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    //the description text (Learn about nutrition..........)
                    Text(
                        text = "Learn about nutrition and healthy eating",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
        }//the green section ends here

        Spacer(modifier = Modifier.height(24.dp))

        //Featured articles sections
        Column(modifier = Modifier.padding(horizontal = 20.dp))
        {//The articles column start here
            Text(
                text = "Featured Articles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Loop through the list to generate cards
            featuredArticles.forEach { article ->
                ArticleCard(article = article, onClick = {
                    uriHandler.openUri(article.url)
                })
            }
        }//The list of articles ends here

        Spacer(modifier = Modifier.height(16.dp))

        //QUICK TIPS
        Column(modifier = Modifier.padding(horizontal = 24.dp))
        {//the list of quick tips starts here

            //The Quck Nutrition Tips text
            Text(
                text = "Quick Nutrition Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loop through the tips list to generate cards
            quickTips.forEach { tip ->
                TipCard(tip = tip)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }//the list of quick tips ends here

        Spacer(modifier = Modifier.height(32.dp))

        //Copyright footer
        Text(
            text = "© 2026 BiteWise. All rights reserved.",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(100.dp))

    }//the education screen UI column ends here
}//The education screen composable ends here

@Preview(showBackground = true, heightDp = 2000)
@Composable
fun EducationScreenPreview(){
    EducationScreen()
}

/* References
 * * FEATURED ARTICLES (URLs used in ArticleItem data class):
 * 1. Harvard Health Publishing. (2021, March 6). Foods linked to better brainpower.
 * Harvard Health. https://www.health.harvard.edu/healthbeat/foods-linked-to-better-brainpower
 * 2. Harvard T.H. Chan School of Public Health. (n.d.). Carbohydrates.
 * The Nutrition Source. Retrieved February 24, 2026, from https://www.hsph.harvard.edu/nutritionsource/carbohydrates/
 * 3. Harvard T.H. Chan School of Public Health. (n.d.). Protein.
 * The Nutrition Source. Retrieved February 24, 2026, from https://www.hsph.harvard.edu/nutritionsource/what-should-you-eat/protein/
 * 4. Harvard T.H. Chan School of Public Health. (n.d.). Water.
 * The Nutrition Source. Retrieved February 24, 2026, from https://www.hsph.harvard.edu/nutritionsource/water/
 * 5. Mayo Clinic Staff. (2022, April 28). Heart-healthy diet: 8 steps to prevent heart disease.
 * Mayo Clinic. https://www.mayoclinic.org/diseases-conditions/heart-disease/in-depth/heart-healthy-diet/art-20047702
 * 6. McDonell, K. (2023, February 20). 10 easy ways to boost your metabolism (backed by science).
 * Healthline. https://www.healthline.com/nutrition/10-ways-to-boost-metabolism
 *
 * QUICK NUTRITION TIPS (Scientific backing for static TipItem content):
 * - Mindful Eating (20-Minute Rule):
 * Andrade, A. M., Greene, G. W., & Melanson, K. J. (2008). Eating slowly led to decreases in energy
 * intake within meals in healthy women. Journal of the American Dietetic Association, 108(7), 1186–1191.
 * https://doi.org/10.1016/j.jada.2008.04.026
 *
 * - Sleep Affects Appetite (Ghrelin/Leptin):
 * Taheri, S., Lin, L., Austin, D., Young, T., & Mignot, E. (2004). Short sleep duration is associated
 * with reduced leptin, elevated ghrelin, and increased body mass index. PLoS Medicine, 1(3), e62.
 * https://doi.org/10.1371/journal.pmed.0010062
 *
 * - General Guidelines (Fiber, Hydration, Portion Control):
 * U.S. Department of Agriculture & U.S. Department of Health and Human Services. (2020).
 * Dietary guidelines for Americans, 2020-2025 (9th ed.). https://www.dietaryguidelines.gov/
 * ============================================================================
 */