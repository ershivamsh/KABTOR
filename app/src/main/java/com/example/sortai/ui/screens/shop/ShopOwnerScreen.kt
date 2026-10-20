package com.example.sortai.ui.screens.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortai.data.CrateEntity
import com.example.sortai.data.MaterialLot
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.EWasteCategory
import com.example.sortai.ui.components.CrateCard
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun ShopOwnerScreen(
    crates: List<CrateEntity>,
    lots: List<MaterialLot>,
    language: AppLanguage,
    onPublishLot: (Int) -> Unit
) {
    val readyCrates = crates.filter { it.currentWeightKg >= it.thresholdKg }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Shop Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "संकलन केंद्र इन्वेंटरी प्रबंधन" else "Collection Centre Operations",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "STATION #04 - ACTIVE",
                                color = EcoGreenPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "जैसे ही कोई क्रेट अपने थ्रेशोल्ड (निर्धारित वजन) तक पहुंचती है, आप यहां से तुरंत नीलामी लॉट बना सकते हैं।"
                        else
                            "When verified accumulated scrap crosses category thresholds, package and publish material lots directly to certified recyclers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Ready Crates Alert
        if (readyCrates.isNotEmpty()) {
            item {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = EcoGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "${readyCrates.size} क्रेट बिक्री के लिए तैयार!" else "${readyCrates.size} Crates Ready for Recycler Bidding!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = EcoGreenPrimary
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "नीचे दिए गए बटन से लॉट प्रकाशित करें" else "Click 'Publish Lot' on any eligible crate below",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // All Crates with Threshold indicators
        item {
            Text(
                text = if (language == AppLanguage.HINDI) "क्रेट क्षमता एवं थ्रेशोल्ड स्थिति" else "Crate Capacities & Threshold Tracking",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(crates) { crate ->
            CrateCard(
                crate = crate,
                language = language,
                onPublishLotClick = onPublishLot
            )
        }
    }
}
