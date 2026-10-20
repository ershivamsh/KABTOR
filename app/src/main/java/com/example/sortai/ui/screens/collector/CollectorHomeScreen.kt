package com.example.sortai.ui.screens.collector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortai.data.CrateEntity
import com.example.sortai.data.ScannedItem
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.EWasteCategory
import com.example.sortai.ui.components.CrateCard
import com.example.ui.theme.EcoGreenLight
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun CollectorHomeScreen(
    crates: List<CrateEntity>,
    recentItems: List<ScannedItem>,
    language: AppLanguage,
    onNavigateToScan: () -> Unit,
    onPublishLotClick: (Int) -> Unit
) {
    val totalWeight = crates.sumOf { it.currentWeightKg }
    val readyCratesCount = crates.count { it.currentWeightKg >= it.thresholdKg }
    val estimatedEarnings = crates.sumOf { crate ->
        val cat = EWasteCategory.fromId(crate.crateNumber)
        crate.currentWeightKg * cat.benchmarkPricePerKg
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Stats Overview Row
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "दैनिक संग्रह अवलोकन" else "Collector Daily Overview",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatTile(
                            label = if (language == AppLanguage.HINDI) "कुल वजन" else "Total Scrap",
                            value = "${String.format("%.1f", totalWeight)} kg",
                            subtext = "${recentItems.size} items scanned"
                        )
                        StatTile(
                            label = if (language == AppLanguage.HINDI) "अनुमानित मूल्य" else "Est. Value",
                            value = "₹${String.format("%,.0f", estimatedEarnings)}",
                            subtext = "Fair scrap rate"
                        )
                        StatTile(
                            label = if (language == AppLanguage.HINDI) "तैयार क्रेट" else "Lots Ready",
                            value = "$readyCratesCount Crates",
                            subtext = "Met threshold",
                            isHighlight = readyCratesCount > 0
                        )
                    }
                }
            }
        }

        // Primary Scan Button Hero
        item {
            Button(
                onClick = onNavigateToScan,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("scan_ewaste_primary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "नया ई-कचरा स्कैन करें" else "Scan Electronic Waste",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Crates Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "रंग-कोडित क्रेट इन्वेंट्री (6 श्रेणियां)" else "Color-Coded Crate Inventory",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${crates.size} active crates",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(crates) { crate ->
            CrateCard(
                crate = crate,
                language = language,
                onPublishLotClick = onPublishLotClick
            )
        }

        // Recent Scans Stream
        if (recentItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "हाल ही में छांटे गए उपकरण" else "Recent Sorted Items",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(recentItems.take(5)) { item ->
                ScannedItemRow(item = item, language = language)
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    subtext: String,
    isHighlight: Boolean = false
) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) EcoGreenPrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(text = subtext, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ScannedItemRow(item: ScannedItem, language: AppLanguage) {
    val category = EWasteCategory.fromName(item.category)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (category.isHazardous) Icons.Default.Warning else Icons.Default.Memory,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.confirmedClass,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${category.crateLabel} • ${item.weightKg} kg • ${(item.confidence * 100).toInt()}% conf",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = category.color,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = category.crateLabel,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
