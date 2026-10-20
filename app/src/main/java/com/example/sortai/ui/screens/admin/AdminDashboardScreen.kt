package com.example.sortai.ui.screens.admin

import androidx.compose.foundation.background
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
import com.example.sortai.data.AuditEvent
import com.example.sortai.data.CrateEntity
import com.example.sortai.data.MaterialLot
import com.example.sortai.data.PickupRecord
import com.example.sortai.model.AppLanguage
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun AdminDashboardScreen(
    crates: List<CrateEntity>,
    lots: List<MaterialLot>,
    pickups: List<PickupRecord>,
    auditEvents: List<AuditEvent>,
    language: AppLanguage,
    onResetDatabase: () -> Unit
) {
    val totalVolumeKg = crates.sumOf { it.currentWeightKg } + lots.sumOf { it.totalWeightKg }
    val totalSettledAmount = pickups.filter { it.status == "COMPLETED" }.sumOf { it.finalAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Platform Overview Banner
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
                            text = if (language == AppLanguage.HINDI) "SortAI प्लेटफॉर्म प्रशासन" else "Platform Governance & Audit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SYSTEM HEALTH: OK",
                                color = EcoGreenPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdminStat("Total Diverted", "${String.format("%.1f", totalVolumeKg)} kg")
                        AdminStat("Lots Traded", "${lots.size}")
                        AdminStat("Completed Pickups", "${pickups.count { it.status == "COMPLETED" }}")
                        AdminStat("Simulated GMV", "₹${String.format("%,.0f", totalSettledAmount)}")
                    }
                }
            }
        }

        // Authorized Recycler Compliance Section
        item {
            Text(
                text = if (language == AppLanguage.HINDI) "अधिकृत रीसाइक्लर सत्यापन स्थिति" else "Authorised Recycler Compliance Directory",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val recyclers = listOf(
                RecyclerEntry("EcoMetal Refiners Pvt Ltd", "CPCB/EW/2024/418", "VERIFIED", "PCBs, Copper, Gold"),
                RecyclerEntry("CleanE-Waste Logistics India", "SPCB/REG/2025/102", "VERIFIED", "Cables, Plastics"),
                RecyclerEntry("GreenCircuit Extractor Labs", "CPCB/EW/2023/889", "VERIFIED", "High-Grade Electronics"),
                RecyclerEntry("Sunlight Battery Recyclers", "CPCB/BAT/2026/054", "AUDIT PENDING", "Lead-Acid, Lithium-Ion")
            )

            recyclers.forEach { r ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = r.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (r.status == "VERIFIED") {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = EcoGreenPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(text = "Auth ID: ${r.authId} • Categories: ${r.categories}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = if (r.status == "VERIFIED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = r.status,
                                color = if (r.status == "VERIFIED") EcoGreenPrimary else Color(0xFFE65100),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Audit Event Stream
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "सिस्टम ऑडिट एवं सुरक्षा लॉग्स" else "System Audit & Safety Event Trail",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(text = "${auditEvents.size} events", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(auditEvents) { event ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    event.eventType.contains("HAZARD") -> Color(0xFFFFEBEE)
                                    event.eventType.contains("LOT") || event.eventType.contains("BID") -> Color(0xFFE3F2FD)
                                    else -> EcoGreenPrimary.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                event.eventType.contains("HAZARD") -> Icons.Default.Warning
                                event.eventType.contains("LOT") || event.eventType.contains("BID") -> Icons.Default.Gavel
                                else -> Icons.Default.History
                            },
                            contentDescription = null,
                            tint = if (event.eventType.contains("HAZARD")) Color(0xFFC62828) else EcoGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = event.eventType, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = event.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(text = event.performedBy, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Demo Reset Controls
        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onResetDatabase,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_reset_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "डेमो डेटा रीसेट करें (प्रारंभिक अवस्था)" else "Reset Demo Data to Initial Prototype State",
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class RecyclerEntry(
    val name: String,
    val authId: String,
    val status: String,
    val categories: String
)

@Composable
fun AdminStat(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
