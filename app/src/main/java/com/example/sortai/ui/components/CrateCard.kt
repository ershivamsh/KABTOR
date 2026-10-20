package com.example.sortai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortai.data.CrateEntity
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.EWasteCategory
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun CrateCard(
    crate: CrateEntity,
    language: AppLanguage,
    onPublishLotClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val category = EWasteCategory.fromId(crate.crateNumber)
    val progress = (crate.currentWeightKg / crate.thresholdKg).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "crateProgress")
    val isThresholdReached = crate.currentWeightKg >= crate.thresholdKg

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isThresholdReached) 2.dp else 1.dp,
                color = if (isThresholdReached) category.color else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("crate_card_${crate.crateNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Crate Badge + Category Name + Hazard Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(category.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (category.crateNumber > 0) "#${category.crateNumber}" else "!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) category.hindiName else category.englishName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${category.crateLabel} • ₹${category.benchmarkPricePerKg.toInt()}/kg benchmark",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (category.isHazardous) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Hazardous",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "HAZARDOUS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Capacity & Threshold Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", crate.currentWeightKg)} kg / ${String.format("%.1f", crate.thresholdKg)} kg",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${(progress * 100).toInt()}% fill • ${crate.itemCount} items",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = category.color,
                trackColor = category.color.copy(alpha = 0.15f)
            )

            // If threshold reached, display lot creation call-to-action
            if (isThresholdReached && onPublishLotClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(category.color.copy(alpha = 0.10f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ready",
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "थ्रेशोल्ड पूरा! लॉट बनाएं" else "Ready to Create Lot",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = category.color
                        )
                    }

                    FilledTonalButton(
                        onClick = { onPublishLotClick(crate.crateNumber) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("publish_lot_button_${crate.crateNumber}")
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "लॉट बनाएं" else "Publish Lot",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
