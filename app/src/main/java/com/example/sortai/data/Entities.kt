package com.example.sortai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_items")
data class ScannedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val collectorId: String = "collector_01",
    val category: String,
    val predictedClass: String,
    val confirmedClass: String,
    val confidence: Float,
    val isHazard: Boolean,
    val hazardNote: String,
    val crateId: Int,
    val weightKg: Double,
    val quantity: Int = 1,
    val scanMode: String,
    val inferenceSource: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isConfirmed: Boolean = true,
    val sampleImageName: String = ""
)

@Entity(tableName = "crates")
data class CrateEntity(
    @PrimaryKey
    val crateNumber: Int,
    val categoryName: String,
    val currentWeightKg: Double,
    val thresholdKg: Double,
    val itemCount: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "material_lots")
data class MaterialLot(
    @PrimaryKey
    val lotId: String,
    val category: String,
    val totalWeightKg: Double,
    val reservePricePerKg: Double,
    val status: String = "OPEN_FOR_BIDS", // OPEN_FOR_BIDS, BID_ACCEPTED, PICKUP_SCHEDULED, COLLECTED, COMPLETED
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedBidId: String? = null
)

@Entity(tableName = "recycler_bids")
data class RecyclerBid(
    @PrimaryKey
    val bidId: String,
    val lotId: String,
    val recyclerId: String,
    val recyclerName: String,
    val pricePerKg: Double,
    val offeredTotal: Double,
    val proposedPickupDate: String,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pickups")
data class PickupRecord(
    @PrimaryKey
    val pickupId: String,
    val lotId: String,
    val bidId: String,
    val recyclerName: String,
    val scheduledDate: String,
    val measuredWeightKg: Double,
    val purityPercent: Double = 98.0,
    val finalAmount: Double,
    val status: String = "SCHEDULED", // SCHEDULED, COLLECTED, PURITY_VERIFIED, COMPLETED
    val paymentReference: String = "",
    val completedAt: Long? = null
)

@Entity(tableName = "audit_events")
data class AuditEvent(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    val eventType: String,
    val details: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
