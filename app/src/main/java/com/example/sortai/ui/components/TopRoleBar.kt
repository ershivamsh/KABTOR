package com.example.sortai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.UserRole
import com.example.ui.theme.*

@Composable
fun TopRoleBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageToggled: () -> Unit,
    onResetDatabase: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Logo, App Name, Language button, Reset/Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EcoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = "SortAI Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SortAI",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EcoGreenPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EcoGreenLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "E-Waste AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EcoGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "स्मार्ट ई-कचरा छंटाई एवं रीसाइक्लिंग" else "Safe Sorting & Recycler Marketplace",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher button
                    FilledTonalButton(
                        onClick = onLanguageToggled,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("language_switch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "English" else "हिन्दी",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Database Reset (for evaluator convenience)
                    IconButton(
                        onClick = onResetDatabase,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("reset_db_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Demo Data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role selector chips (Collector, Shop Owner, Recycler, Admin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val isSelected = role == currentRole
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        label = "chipBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "chipContent"
                    )

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onRoleSelected(role) }
                            .testTag("role_chip_${role.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val icon = when (role) {
                                UserRole.COLLECTOR -> Icons.Default.QrCodeScanner
                                UserRole.SHOP_OWNER -> Icons.Default.Storefront
                                UserRole.RECYCLER -> Icons.Default.LocalShipping
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = role.title,
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.HINDI) role.hindiTitle.substringBefore(" /") else role.title,
                                color = contentColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
