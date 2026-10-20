package com.example.sortai.ui.screens.recycler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
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
import com.example.sortai.data.MaterialLot
import com.example.sortai.data.PickupRecord
import com.example.sortai.data.RecyclerBid
import com.example.sortai.model.AppLanguage
import com.example.sortai.model.EWasteCategory
import com.example.ui.theme.EcoGreenPrimary

@Composable
fun RecyclerHomeScreen(
    lots: List<MaterialLot>,
    allBids: List<RecyclerBid>,
    pickups: List<PickupRecord>,
    language: AppLanguage,
    onSubmitBid: (lotId: String, pricePerKg: Double, pickupDate: String) -> Unit,
    onAcceptBid: (lotId: String, bidId: String) -> Unit,
    onCompletePickup: (pickupId: String, verifiedWeight: Double, purity: Double) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf<EWasteCategory?>(null) }
    var bidTargetLot by remember { mutableStateOf<MaterialLot?>(null) }
    var activePickupToVerify by remember { mutableStateOf<PickupRecord?>(null) }

    val filteredLots = lots.filter { lot ->
        val matchesCategory = selectedCategoryFilter == null || lot.category == selectedCategoryFilter?.name
        val matchesTab = when (selectedTab) {
            0 -> lot.status == "OPEN_FOR_BIDS" || lot.status == "BID_ACCEPTED"
            1 -> lot.status == "SCHEDULED" || pickups.any { it.lotId == lot.lotId && it.status == "SCHEDULED" }
            else -> true
        }
        matchesCategory && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Recycler Verified Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "अधिकृत रीसाइक्लर पोर्टल" else "Authorized Recycler B2B",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "CPCB Authorization • Verified Buyer",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ACTIVE BUYER",
                        color = EcoGreenPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section Tabs: 0 = Marketplace Lots, 1 = Scheduled Pickups, 2 = Completed Payouts
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = EcoGreenPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (language == AppLanguage.HINDI) "नीलामी लॉट (${lots.count { it.status == "OPEN_FOR_BIDS" }})" else "Marketplace (${lots.count { it.status == "OPEN_FOR_BIDS" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (language == AppLanguage.HINDI) "पिकअप (${pickups.count { it.status == "SCHEDULED" }})" else "Pickups (${pickups.count { it.status == "SCHEDULED" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (language == AppLanguage.HINDI) "लेनदेन (${pickups.count { it.status == "COMPLETED" }})" else "History (${pickups.count { it.status == "COMPLETED" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                label = { Text("All Categories", fontSize = 11.sp) }
            )
            EWasteCategory.entries.filter { it.crateNumber > 0 }.forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                    label = { Text(if (language == AppLanguage.HINDI) cat.hindiName else cat.englishName, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Lots list
                if (filteredLots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "कोई सक्रिय लॉट उपलब्ध नहीं है" else "No active marketplace lots found in this category",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredLots) { lot ->
                            val lotBids = allBids.filter { it.lotId == lot.lotId }
                            LotMarketCard(
                                lot = lot,
                                bids = lotBids,
                                language = language,
                                onPlaceBidClick = { bidTargetLot = lot },
                                onAcceptBidClick = { bidId -> onAcceptBid(lot.lotId, bidId) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Scheduled Pickups
                val activePickups = pickups.filter { it.status == "SCHEDULED" }
                if (activePickups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "कोई निर्धारित पिकअप नहीं है" else "No scheduled pickups pending inspection",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(activePickups) { pickup ->
                            PickupCard(
                                pickup = pickup,
                                language = language,
                                onVerifyClick = { activePickupToVerify = pickup }
                            )
                        }
                    }
                }
            }
            2 -> {
                // History & Payments
                val completedPickups = pickups.filter { it.status == "COMPLETED" }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(completedPickups) { pickup ->
                        CompletedPaymentCard(pickup = pickup, language = language)
                    }
                }
            }
        }
    }

    // Bid Placement Dialog
    bidTargetLot?.let { lot ->
        var offeredPrice by remember { mutableStateOf((lot.reservePricePerKg * 1.05).toInt().toString()) }
        var pickupWindow by remember { mutableStateOf("Tomorrow, 11:00 AM") }

        AlertDialog(
            onDismissRequest = { bidTargetLot = null },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "बोली लगाएं: ${lot.lotId}" else "Submit Recycler Bid: ${lot.lotId}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${lot.totalWeightKg} kg • Reserve: ₹${lot.reservePricePerKg}/kg",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = offeredPrice,
                        onValueChange = { offeredPrice = it },
                        label = { Text("Offered Price (₹ / kg)") },
                        singleLine = true,
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("bid_price_input")
                    )
                    OutlinedTextField(
                        value = pickupWindow,
                        onValueChange = { pickupWindow = it },
                        label = { Text("Pickup Window") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = offeredPrice.toDoubleOrNull() ?: (lot.reservePricePerKg * 1.05)
                        onSubmitBid(lot.lotId, price, pickupWindow)
                        bidTargetLot = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    modifier = Modifier.testTag("submit_bid_confirm_button")
                ) {
                    Text("Submit Bid")
                }
            },
            dismissButton = {
                TextButton(onClick = { bidTargetLot = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Purity Verification Sheet
    activePickupToVerify?.let { pickup ->
        PurityVerificationSheet(
            pickup = pickup,
            language = language,
            onComplete = { weight, purity ->
                onCompletePickup(pickup.pickupId, weight, purity)
                activePickupToVerify = null
            },
            onDismiss = { activePickupToVerify = null }
        )
    }
}

@Composable
fun LotMarketCard(
    lot: MaterialLot,
    bids: List<RecyclerBid>,
    language: AppLanguage,
    onPlaceBidClick: () -> Unit,
    onAcceptBidClick: (String) -> Unit
) {
    val category = EWasteCategory.fromName(lot.category)
    val highestBid = bids.maxByOrNull { it.pricePerKg }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, category.color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(category.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${category.crateNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) category.hindiName else category.englishName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(text = lot.lotId, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = if (lot.status == "OPEN_FOR_BIDS") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (lot.status == "OPEN_FOR_BIDS") "OPEN FOR BIDS" else "BID ACCEPTED",
                        color = if (lot.status == "OPEN_FOR_BIDS") EcoGreenPrimary else Color(0xFFEF6C00),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics: Weight, Base Price, Highest Offer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Lot Weight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${String.format("%.1f", lot.totalWeightKg)} kg", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text(text = "Base Price", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${lot.reservePricePerKg.toInt()}/kg", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text(text = "Highest Bid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (highestBid != null) "₹${highestBid.pricePerKg.toInt()}/kg" else "No bids",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EcoGreenPrimary
                    )
                }
            }

            // Bids List
            if (bids.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bids Received (${bids.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                bids.forEach { bid ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = bid.recyclerName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "₹${bid.pricePerKg.toInt()}/kg (Total ₹${String.format("%,.0f", bid.offeredTotal)}) • ${bid.proposedPickupDate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (lot.status == "OPEN_FOR_BIDS") {
                            Button(
                                onClick = { onAcceptBidClick(bid.bidId) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("accept_bid_${bid.bidId}")
                            ) {
                                Text("Accept", fontSize = 11.sp)
                            }
                        } else if (bid.status == "ACCEPTED") {
                            Text("ACCEPTED", color = EcoGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (lot.status == "OPEN_FOR_BIDS") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlaceBidClick,
                    colors = ButtonDefaults.buttonColors(containerColor = category.color),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("place_bid_button_${lot.lotId}")
                ) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (language == AppLanguage.HINDI) "इस लॉट पर बोली लगाएं" else "Place Recycler Bid", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PickupCard(
    pickup: PickupRecord,
    language: AppLanguage,
    onVerifyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
                Column {
                    Text(
                        text = "Pickup #${pickup.pickupId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(text = pickup.recyclerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SCHEDULED",
                        color = Color(0xFFE65100),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Scheduled: ${pickup.scheduledDate} • Est Weight: ${pickup.measuredWeightKg} kg",
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onVerifyClick,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("verify_pickup_button_${pickup.pickupId}")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "कांटे पर तौलें एवं शुद्धता जांचें" else "Weigh & Verify Purity",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CompletedPaymentCard(
    pickup: PickupRecord,
    language: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Pickup ${pickup.pickupId} • Completed", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = pickup.recyclerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "PAID (DEMO)",
                        color = EcoGreenPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Verified Weight: ${pickup.measuredWeightKg} kg • Purity: ${pickup.purityPercent}%", fontSize = 12.sp)
                Text(text = "₹${String.format("%,.0f", pickup.finalAmount)}", fontWeight = FontWeight.Bold, color = EcoGreenPrimary, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ref: ${pickup.paymentReference} (Simulated — No Real Money Transferred)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
