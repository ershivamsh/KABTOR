package com.example.sortai.ui.screens.collector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.example.sortai.model.*
import com.example.sortai.ui.components.SafetyAlertBanner
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun ScanResultDialog(
    result: InferenceResult,
    language: AppLanguage,
    onConfirm: (category: EWasteCategory, weightKg: Double, overrideClass: String) -> Unit,
    onSpeakSafety: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(result.category) }
    var weightInput by remember { mutableStateOf("0.5") }
    var isOverriding by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Title & Close
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "AI विश्लेषण परिणाम" else "AI Classification",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Crate Destination Hero Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = selectedCategory.color.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, selectedCategory.color, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = selectedCategory.color,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (selectedCategory) {
                                            EWasteCategory.PCB -> Icons.Default.Memory
                                            EWasteCategory.BATTERY -> Icons.Default.BatteryAlert
                                            EWasteCategory.COPPER_MOTOR -> Icons.Default.Bolt
                                            EWasteCategory.CABLE -> Icons.Default.Cable
                                            EWasteCategory.DISPLAY -> Icons.Default.Tv
                                            EWasteCategory.PLASTIC_GLASS -> Icons.Default.Layers
                                            EWasteCategory.UNKNOWN_REVIEW -> Icons.AutoMirrored.Filled.HelpOutline
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = selectedCategory.crateLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedCategory.color
                            )

                            Text(
                                text = if (language == AppLanguage.HINDI) selectedCategory.hindiName else selectedCategory.englishName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = selectedCategory.color,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${(result.confidence * 100).toInt()}% Confidence",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = result.detectedLabel,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Safety Warning Banner
                if (result.isHazard || selectedCategory.isHazardous) {
                    item {
                        SafetyAlertBanner(
                            hazardLevel = result.hazardLevel,
                            messageEn = result.hazardWarning,
                            messageHi = result.hazardWarningHindi,
                            language = language,
                            onSpeakClick = {
                                val message = if (language == AppLanguage.HINDI) result.hazardWarningHindi else result.hazardWarning
                                onSpeakSafety(message)
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Component breakdown (if whole device or has breakdown)
                if (result.components.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "संभावित घटक संरचना" else "Estimated Component Composition",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            result.components.forEach { comp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = comp.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${comp.estimatedSharePercent}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = comp.category.color
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Weight Input Field
                item {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text(if (language == AppLanguage.HINDI) "वजन दर्ज करें (किलोग्राम)" else "Measured Weight (kg)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Scale, contentDescription = null)
                        },
                        trailingIcon = {
                            Text("kg", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weight_input_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Override Category Expandable
                item {
                    TextButton(
                        onClick = { isOverriding = !isOverriding },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isOverriding) Icons.Default.ExpandLess else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOverriding) {
                                if (language == AppLanguage.HINDI) "बदलाव छुपाएं" else "Hide Category Selection"
                            } else {
                                if (language == AppLanguage.HINDI) "गलत पहचान? श्रेणी बदलें" else "Wrong Category? Change Crate"
                            },
                            fontSize = 12.sp
                        )
                    }

                    if (isOverriding) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EWasteCategory.entries.forEach { cat ->
                                Surface(
                                    color = if (selectedCategory == cat) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = cat }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cat.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == AppLanguage.HINDI) cat.hindiName else cat.englishName,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Actions: Confirm & Sort Button
                item {
                    Button(
                        onClick = {
                            val weight = weightInput.toDoubleOrNull() ?: 0.5
                            onConfirm(selectedCategory, weight, selectedCategory.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedCategory.color),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_sort_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "${selectedCategory.crateLabel} में सुरक्षित रखें" else "Place in ${selectedCategory.crateLabel}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val weight = weightInput.toDoubleOrNull() ?: 0.5
                            onConfirm(EWasteCategory.UNKNOWN_REVIEW, weight, "Sent to Review")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "जांच के लिए रिव्यू में भेजें" else "Send to Review Staging",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
