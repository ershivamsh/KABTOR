package com.example.sortai.ui.screens.collector

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortai.model.*
import com.example.sortai.service.DemoScrapDataset
import com.example.sortai.service.DemoScrapItem
import com.example.sortai.service.VisionEngine
import com.example.ui.theme.EcoGreenPrimary
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    visionEngine: VisionEngine,
    language: AppLanguage,
    onItemConfirmed: (EWasteCategory, Double, String, Float, Boolean, String, InferenceSource) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var scanMode by remember { mutableStateOf(ScanMode.WHOLE_DEVICE) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentResult by remember { mutableStateOf<InferenceResult?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isAnalyzing = true
            coroutineScope.launch {
                val result = visionEngine.analyzeScrap(bitmap = bitmap, mode = scanMode)
                currentResult = result
                isAnalyzing = false

                // Announce safety instruction in selected language
                val spokenAlert = if (language == AppLanguage.HINDI) {
                    if (result.isHazard) "सावधान! ${result.hazardWarningHindi} इसे ${result.category.crateLabel} में रखें"
                    else "${result.category.hindiName} पहचाना गया। इसे ${result.category.crateLabel} में रखें"
                } else {
                    if (result.isHazard) "Warning! ${result.hazardWarning} Place in ${result.category.crateLabel}"
                    else "${result.category.englishName} identified. Place in ${result.category.crateLabel}"
                }
                onSpeak(spokenAlert)
            }
        }
    }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isAnalyzing = true
                val bitmap = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                } catch (e: Exception) {
                    null
                }
                capturedBitmap = bitmap
                val result = visionEngine.analyzeScrap(bitmap = bitmap, mode = scanMode)
                currentResult = result
                isAnalyzing = false

                val spokenAlert = if (language == AppLanguage.HINDI) {
                    "${result.category.hindiName} पहचाना गया। इसे ${result.category.crateLabel} में रखें"
                } else {
                    "${result.category.englishName} identified. Place in ${result.category.crateLabel}"
                }
                onSpeak(spokenAlert)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Whole Device vs Component
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "स्कैनिंग मोड चुनें" else "Select Scanning Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScanMode.entries.forEach { mode ->
                            val isSelected = mode == scanMode
                            Surface(
                                color = if (isSelected) EcoGreenPrimary else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { scanMode = mode }
                                    .testTag("scan_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (mode == ScanMode.WHOLE_DEVICE) Icons.Default.DevicesOther else Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.HINDI) mode.hindiDisplayName.substringBefore(" ") else mode.displayName,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Camera & Upload Actions Hero
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(EcoGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI) "ई-कचरे की फोटो खींचें" else "Snap E-Waste to Sort",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = if (language == AppLanguage.HINDI) "AI स्वचालित रूप से सही क्रेट और सुरक्षा चेतावनी बताएगा" else "AI automatically detects category, hazards & target crate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_camera_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.HINDI) "कैमरा खोलें" else "Take Photo")
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("pick_gallery_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.HINDI) "गैलरी से चुनें" else "Upload")
                        }
                    }
                }
            }
        }

        // Demo Scrap Testbed (Interactive instant classification)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "डेमो स्क्रैप टेस्टबेड" else "Instant Scrap Testbed",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "बिना कैमरे के सीधे परीक्षण के लिए नमूना चुनें" else "Tap any sample to simulate real-world e-waste classification",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(DemoScrapDataset.items) { item ->
                        DemoScrapCard(
                            item = item,
                            language = language,
                            onClick = {
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val result = visionEngine.evaluateDemoItem(item)
                                    currentResult = result
                                    isAnalyzing = false

                                    val spokenAlert = if (language == AppLanguage.HINDI) {
                                        if (result.isHazard) "चेतावनी: ${result.hazardWarningHindi} क्रेट ${result.category.crateNumber} में रखें"
                                        else "${result.category.hindiName}। क्रेट ${result.category.crateNumber} में रखें"
                                    } else {
                                        if (result.isHazard) "Caution: ${result.hazardWarning}. Place in ${result.category.crateLabel}"
                                        else "${result.category.englishName}. Place in ${result.category.crateLabel}"
                                    }
                                    onSpeak(spokenAlert)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Loading overlay when analyzing
    if (isAnalyzing) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "AI विश्लेषण जारी है..." else "Analyzing E-Waste...",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = EcoGreenPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "सुरक्षा खतरों और क्रेट श्रेणी का मूल्यांकन हो रहा है" else "Running computer vision & evaluating hazard policy...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    // Result dialog on classification complete
    currentResult?.let { result ->
        ScanResultDialog(
            result = result,
            language = language,
            onConfirm = { chosenCat, weight, overrideLabel ->
                onItemConfirmed(
                    chosenCat,
                    weight,
                    overrideLabel,
                    result.confidence,
                    result.isHazard,
                    result.hazardWarning,
                    result.inferenceSource
                )
                currentResult = null
            },
            onSpeakSafety = { textToSpeak ->
                onSpeak(textToSpeak)
            },
            onDismiss = { currentResult = null }
        )
    }
}

@Composable
fun DemoScrapCard(
    item: DemoScrapItem,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(200.dp)
            .border(1.dp, item.expectedCategory.color.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("demo_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = item.expectedCategory.color,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.expectedCategory.crateLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (item.hazardLevel != HazardLevel.NONE) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hazard",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (language == AppLanguage.HINDI) item.hindiTitle else item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.defaultWeightKg} kg • ${item.componentsSummary}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.HINDI) "परीक्षण करें" else "Test Scan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.expectedCategory.color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = item.expectedCategory.color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
