package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.UserRole
import com.example.sortai.ui.SortAiViewModel
import com.example.sortai.ui.components.TopRoleBar
import com.example.sortai.ui.screens.admin.AdminDashboardScreen
import com.example.sortai.ui.screens.collector.CollectorHomeScreen
import com.example.sortai.ui.screens.collector.ScanScreen
import com.example.sortai.ui.screens.recycler.RecyclerHomeScreen
import com.example.sortai.ui.screens.shop.ShopOwnerScreen
import com.example.ui.theme.EcoGreenPrimary
import com.example.ui.theme.SortAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SortAiTheme {
                val viewModel: SortAiViewModel = viewModel()
                SortAiMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SortAiMainApp(viewModel: SortAiViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val collectorScreenState by viewModel.collectorScreenState.collectAsStateWithLifecycle()
    val crates by viewModel.crates.collectAsStateWithLifecycle()
    val scannedItems by viewModel.scannedItems.collectAsStateWithLifecycle()
    val lots by viewModel.lots.collectAsStateWithLifecycle()
    val bids by viewModel.bids.collectAsStateWithLifecycle()
    val pickups by viewModel.pickups.collectAsStateWithLifecycle()
    val auditEvents by viewModel.auditEvents.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Handle back button when in scan sub-screen
    BackHandler(enabled = currentRole == UserRole.COLLECTOR && collectorScreenState == "SCAN") {
        viewModel.navigateToCollectorHome()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopRoleBar(
                currentRole = currentRole,
                onRoleSelected = { role ->
                    viewModel.selectRole(role)
                },
                currentLanguage = currentLanguage,
                onLanguageToggled = {
                    viewModel.toggleLanguage()
                },
                onResetDatabase = {
                    viewModel.resetDatabase()
                }
            )
        },
        bottomBar = {
            if (currentRole == UserRole.COLLECTOR) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("collector_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = collectorScreenState == "HOME",
                        onClick = { viewModel.navigateToCollectorHome() },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                        label = { Text(if (currentLanguage == AppLanguage.HINDI) "इन्वेंट्री" else "Inventory") },
                        modifier = Modifier.testTag("nav_home_button")
                    )
                    NavigationBarItem(
                        selected = collectorScreenState == "SCAN",
                        onClick = { viewModel.navigateToScan() },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        label = { Text(if (currentLanguage == AppLanguage.HINDI) "स्कैन करें" else "Snap & Sort") },
                        modifier = Modifier.testTag("nav_scan_button")
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.COLLECTOR -> {
                    if (collectorScreenState == "SCAN") {
                        ScanScreen(
                            visionEngine = viewModel.visionEngine,
                            language = currentLanguage,
                            onItemConfirmed = { category, weight, name, conf, hazard, note, source ->
                                viewModel.recordScannedItem(
                                    category = category,
                                    weightKg = weight,
                                    confirmedName = name,
                                    confidence = conf,
                                    isHazard = hazard,
                                    hazardNote = note,
                                    source = source
                                )
                            },
                            onSpeak = { text ->
                                viewModel.speakSafety(text)
                            }
                        )
                    } else {
                        CollectorHomeScreen(
                            crates = crates,
                            recentItems = scannedItems,
                            language = currentLanguage,
                            onNavigateToScan = { viewModel.navigateToScan() },
                            onPublishLotClick = { crateNumber ->
                                viewModel.publishLotFromCrate(crateNumber)
                            }
                        )
                    }
                }

                UserRole.SHOP_OWNER -> {
                    ShopOwnerScreen(
                        crates = crates,
                        lots = lots,
                        language = currentLanguage,
                        onPublishLot = { crateNumber ->
                            viewModel.publishLotFromCrate(crateNumber)
                        }
                    )
                }

                UserRole.RECYCLER -> {
                    RecyclerHomeScreen(
                        lots = lots,
                        allBids = bids,
                        pickups = pickups,
                        language = currentLanguage,
                        onSubmitBid = { lotId, price, date ->
                            viewModel.submitBid(lotId, price, date)
                        },
                        onAcceptBid = { lotId, bidId ->
                            viewModel.acceptBid(lotId, bidId)
                        },
                        onCompletePickup = { pickupId, weight, purity ->
                            viewModel.verifyPickup(pickupId, weight, purity)
                        }
                    )
                }

                UserRole.ADMIN -> {
                    AdminDashboardScreen(
                        crates = crates,
                        lots = lots,
                        pickups = pickups,
                        auditEvents = auditEvents,
                        language = currentLanguage,
                        onResetDatabase = {
                            viewModel.resetDatabase()
                        }
                    )
                }
            }
        }
    }
}
