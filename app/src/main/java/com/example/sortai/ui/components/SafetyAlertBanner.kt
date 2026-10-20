package com.example.sortai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Warning
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
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.HazardLevel

@Composable
fun SafetyAlertBanner(
    hazardLevel: HazardLevel,
    messageEn: String,
    messageHi: String,
    language: AppLanguage,
    onSpeakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hazardLevel == HazardLevel.NONE) return

    val isCritical = hazardLevel == HazardLevel.CRITICAL
    val bgColor = if (isCritical) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
    val borderColor = if (isCritical) Color(0xFFC62828) else Color(0xFFF57F17)
    val iconColor = if (isCritical) Color(0xFFC62828) else Color(0xFFF57F17)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("safety_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isCritical) Icons.Default.Dangerous else Icons.Default.Warning,
                contentDescription = "Safety Alert",
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCritical) {
                        if (language == AppLanguage.HINDI) "अति संवेदनशील चेतावनी!" else "CRITICAL SAFETY HAZARD"
                    } else {
                        if (language == AppLanguage.HINDI) "सुरक्षा निर्देश" else "HANDLING CAUTION"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) messageHi else messageEn,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Audio Speech Button
            IconButton(
                onClick = onSpeakClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(borderColor.copy(alpha = 0.15f))
                    .testTag("speak_alert_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speak Safety Instruction",
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
