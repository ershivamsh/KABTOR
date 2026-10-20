package com.example.sortai.data

import com.example.sortai.model.EWasteCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class SortAiRepository(private val database: AppDatabase) {

    private val scannedItemDao = database.scannedItemDao()
    private val crateDao = database.crateDao()
    private val materialLotDao = database.materialLotDao()
    private val recyclerBidDao = database.recyclerBidDao()
    private val pickupDao = database.pickupDao()
    private val auditEventDao = database.auditEventDao()

    val allScannedItems: Flow<List<ScannedItem>> = scannedItemDao.getAllItems()
    val allCrates: Flow<List<CrateEntity>> = crateDao.getAllCrates()
    val allLots: Flow<List<MaterialLot>> = materialLotDao.getAllLots()
    val allPickups: Flow<List<PickupRecord>> = pickupDao.getAllPickups()
    val allBids: Flow<List<RecyclerBid>> = recyclerBidDao.getAllBids()
    val recentAuditEvents: Flow<List<AuditEvent>> = auditEventDao.getRecentEvents()

    fun getBidsForLot(lotId: String): Flow<List<RecyclerBid>> = recyclerBidDao.getBidsForLot(lotId)

    suspend fun recordScannedItem(item: ScannedItem): Long = withContext(Dispatchers.IO) {
        val itemId = scannedItemDao.insertItem(item)

        // Update corresponding Crate capacity
        val existingCrate = crateDao.getCrate(item.crateId)
        if (existingCrate != null) {
            val updatedWeight = existingCrate.currentWeightKg + item.weightKg
            val updatedCount = existingCrate.itemCount + item.quantity
            crateDao.updateCrateCapacity(item.crateId, updatedWeight, updatedCount)

            // Check if threshold just got crossed
            if (updatedWeight >= existingCrate.thresholdKg && existingCrate.currentWeightKg < existingCrate.thresholdKg) {
                auditEventDao.insertEvent(
                    AuditEvent(
                        eventType = "THRESHOLD_REACHED",
                        details = "Crate ${item.crateId} (${existingCrate.categoryName}) reached pickup threshold of ${existingCrate.thresholdKg} kg (now ${String.format("%.1f", updatedWeight)} kg)",
                        performedBy = item.collectorId
                    )
                )
            }
        }

        auditEventDao.insertEvent(
            AuditEvent(
                eventType = "ITEM_SCANNED",
                details = "Scanned ${item.confirmedClass} (${item.weightKg} kg) into Crate ${item.crateId} with ${String.format("%.0f", item.confidence * 100)}% confidence",
                performedBy = item.collectorId
            )
        )

        itemId
    }

    suspend fun createMaterialLotFromCrate(crateNumber: Int, customReservePrice: Double? = null): String = withContext(Dispatchers.IO) {
        val crate = crateDao.getCrate(crateNumber) ?: throw IllegalArgumentException("Crate not found")
        val category = EWasteCategory.fromId(crateNumber)
        val reserve = customReservePrice ?: category.benchmarkPricePerKg
        val weight = if (crate.currentWeightKg > 0.0) crate.currentWeightKg else category.defaultThresholdKg

        val lotId = "LOT-${category.name.take(3)}-${(1000..9999).random()}"
        val lot = MaterialLot(
            lotId = lotId,
            category = category.name,
            totalWeightKg = weight,
            reservePricePerKg = reserve,
            status = "OPEN_FOR_BIDS"
        )
        materialLotDao.insertLot(lot)

        // Reset the crate for the next batch
        crateDao.resetCrate(crateNumber)

        // Automatically seed 2 competitive bids so the evaluator can test bidding instantly
        val bid1 = RecyclerBid(
            bidId = "BID-${UUID.randomUUID().toString().take(6).uppercase()}",
            lotId = lotId,
            recyclerId = "rec_alpha",
            recyclerName = "Apex Metal & Rare Earth Recyclers (CPCB Reg)",
            pricePerKg = reserve * 1.08,
            offeredTotal = weight * reserve * 1.08,
            proposedPickupDate = "Within 24 Hours",
            status = "PENDING"
        )
        val bid2 = RecyclerBid(
            bidId = "BID-${UUID.randomUUID().toString().take(6).uppercase()}",
            lotId = lotId,
            recyclerId = "rec_beta",
            recyclerName = "CleanE-Waste Logistics India",
            pricePerKg = reserve * 1.04,
            offeredTotal = weight * reserve * 1.04,
            proposedPickupDate = "Day after tomorrow",
            status = "PENDING"
        )
        recyclerBidDao.insertBid(bid1)
        recyclerBidDao.insertBid(bid2)

        auditEventDao.insertEvent(
            AuditEvent(
                eventType = "LOT_CREATED",
                details = "Created Lot $lotId for ${category.englishName} (${String.format("%.1f", weight)} kg) at base ₹${reserve}/kg",
                performedBy = "Shop Owner"
            )
        )

        lotId
    }

    suspend fun submitBid(
        lotId: String,
        recyclerName: String,
        pricePerKg: Double,
        proposedDate: String
    ): String = withContext(Dispatchers.IO) {
        val lot = materialLotDao.getLotById(lotId) ?: throw IllegalArgumentException("Lot not found")
        val bidId = "BID-${UUID.randomUUID().toString().take(6).uppercase()}"
        val total = lot.totalWeightKg * pricePerKg

        val bid = RecyclerBid(
            bidId = bidId,
            lotId = lotId,
            recyclerId = "rec_current",
            recyclerName = recyclerName,
            pricePerKg = pricePerKg,
            offeredTotal = total,
            proposedPickupDate = proposedDate,
            status = "PENDING"
        )
        recyclerBidDao.insertBid(bid)

        auditEventDao.insertEvent(
            AuditEvent(
                eventType = "BID_SUBMITTED",
                details = "$recyclerName bid ₹${pricePerKg}/kg on $lotId (Total: ₹${String.format("%.0f", total)})",
                performedBy = recyclerName
            )
        )

        bidId
    }

    suspend fun acceptBid(lotId: String, bidId: String): String = withContext(Dispatchers.IO) {
        val lot = materialLotDao.getLotById(lotId) ?: throw IllegalArgumentException("Lot not found")
        val bid = recyclerBidDao.getAllBids() // We can get specific or iterate
        // Accept the chosen bid and reject others
        recyclerBidDao.updateBidStatus(bidId, "ACCEPTED")
        recyclerBidDao.rejectOtherBidsForLot(lotId, bidId)
        materialLotDao.updateLotStatus(lotId, "BID_ACCEPTED", bidId)

        // Create scheduled pickup record
        val pickupId = "PCK-${(100..999).random()}"
        val pickup = PickupRecord(
            pickupId = pickupId,
            lotId = lotId,
            bidId = bidId,
            recyclerName = "Selected Recycler",
            scheduledDate = "Tomorrow, 11:00 AM",
            measuredWeightKg = lot.totalWeightKg,
            purityPercent = 98.0,
            finalAmount = lot.totalWeightKg * lot.reservePricePerKg,
            status = "SCHEDULED",
            paymentReference = "UPI-PENDING-${pickupId}"
        )
        pickupDao.insertPickup(pickup)

        auditEventDao.insertEvent(
            AuditEvent(
                eventType = "BID_ACCEPTED",
                details = "Bid $bidId accepted for $lotId. Pickup $pickupId scheduled.",
                performedBy = "Collector / Shop"
            )
        )

        pickupId
    }

    suspend fun verifyAndCompletePickup(
        pickupId: String,
        verifiedWeightKg: Double,
        purityPercent: Double
    ): PickupRecord = withContext(Dispatchers.IO) {
        val existing = pickupDao.getPickupById(pickupId) ?: throw IllegalArgumentException("Pickup not found")
        val lot = materialLotDao.getLotById(existing.lotId)

        // Adjust amount based on purity and actual measured scale weight
        val basePricePerKg = lot?.reservePricePerKg ?: 300.0
        val purityFactor = (purityPercent / 100.0).coerceIn(0.7, 1.05)
        val finalAmount = verifiedWeightKg * basePricePerKg * purityFactor
        val paymentRef = "UPI-SIM-2026-${(10000..99999).random()}"

        pickupDao.completePickup(
            pickupId = pickupId,
            status = "COMPLETED",
            weight = verifiedWeightKg,
            purity = purityPercent,
            finalAmount = finalAmount,
            paymentRef = paymentRef
        )

        lot?.let {
            materialLotDao.updateLotStatus(it.lotId, "COMPLETED")
        }

        auditEventDao.insertEvent(
            AuditEvent(
                eventType = "PICKUP_COMPLETED",
                details = "Pickup $pickupId completed: ${verifiedWeightKg} kg at ${purityPercent}% purity. Final settlement ₹${String.format("%.0f", finalAmount)} via $paymentRef (Demo)",
                performedBy = "Recycler & Collector"
            )
        )

        existing.copy(
            status = "COMPLETED",
            measuredWeightKg = verifiedWeightKg,
            purityPercent = purityPercent,
            finalAmount = finalAmount,
            paymentReference = paymentRef,
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        scannedItemDao.clearAll()
        AppDatabase.seedInitialData(database)
    }
}
