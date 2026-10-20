package com.example.sortai.ui.screens.recycler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sortai.data.PickupRecord
import com.example.sortai.model.AppLanguage
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun PurityVerificationSheet(
    pickup: PickupRecord,
    language: AppLanguage,
    onComplete: (verifiedWeightKg: Double, purityPercent: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var measuredWeight by remember { mutableStateOf(pickup.measuredWeightKg.toString()) }
    var puritySlider by remember { mutableFloatStateOf(98f) }
    var inspectionNotes by remember { mutableStateOf("No foreign metallic contamination. Circuit board grade verified.") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "सामग्री शुद्धता एवं वजन सत्यापन" else "Purity & Weight Verification",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Pickup ${pickup.pickupId} • ${pickup.recyclerName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Measured Weight on Physical Scale
                OutlinedTextField(
                    value = measuredWeight,
                    onValueChange = { measuredWeight = it },
                    label = { Text(if (language == AppLanguage.HINDI) "कांटे पर तोला गया वास्तविक वजन (kg)" else "Verified Scale Weight (kg)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Scale, contentDescription = null) },
                    trailingIcon = { Text("kg", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verified_weight_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Purity Grade Slider
                Text(
                    text = "${if (language == AppLanguage.HINDI) "शुद्धता रेटिंग" else "Material Purity"}: ${puritySlider.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Slider(
                    value = puritySlider,
                    onValueChange = { puritySlider = it },
                    valueRange = 80f..100f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = EcoGreenPrimary, activeTrackColor = EcoGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inspectionNotes,
                    onValueChange = { inspectionNotes = it },
                    label = { Text(if (language == AppLanguage.HINDI) "निरीक्षण विवरण / टिप्पणी" else "Inspection Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Settlement Preview
                val weightVal = measuredWeight.toDoubleOrNull() ?: pickup.measuredWeightKg
                val purityFactor = puritySlider / 100.0
                val estimatedTotal = (pickup.finalAmount / pickup.measuredWeightKg) * weightVal * purityFactor

                Surface(
                    color = EcoGreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "अंतिम भुगतान राशि (डेमो)" else "Final Verified Payout (Demo)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format("%,.0f", estimatedTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenPrimary
                            )
                        }

                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SIMULATED UPI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val finalWeight = measuredWeight.toDoubleOrNull() ?: pickup.measuredWeightKg
                        onComplete(finalWeight, puritySlider.toDouble())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("complete_pickup_button")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "सत्यापित करें और भुगतान दर्ज करें" else "Complete & Record Payout",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
