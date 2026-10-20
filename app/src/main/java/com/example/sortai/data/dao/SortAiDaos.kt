package com.example.sortai.data.dao

import androidx.room.*
import com.example.sortai.data.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {
    @Query("SELECT * FROM scanned_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ScannedItem>>

    @Query("SELECT * FROM scanned_items WHERE crateId = :crateId ORDER BY timestamp DESC")
    fun getItemsForCrate(crateId: Int): Flow<List<ScannedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScannedItem): Long

    @Query("SELECT COUNT(*) FROM scanned_items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM scanned_items")
    suspend fun clearAll()
}

@Dao
interface CrateDao {
    @Query("SELECT * FROM crates ORDER BY crateNumber ASC")
    fun getAllCrates(): Flow<List<CrateEntity>>

    @Query("SELECT * FROM crates WHERE crateNumber = :crateNumber")
    suspend fun getCrate(crateNumber: Int): CrateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCrate(crate: CrateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrates(crates: List<CrateEntity>)

    @Query("UPDATE crates SET currentWeightKg = :newWeight, itemCount = :newCount, lastUpdated = :timestamp WHERE crateNumber = :crateNumber")
    suspend fun updateCrateCapacity(crateNumber: Int, newWeight: Double, newCount: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE crates SET currentWeightKg = 0.0, itemCount = 0, lastUpdated = :timestamp WHERE crateNumber = :crateNumber")
    suspend fun resetCrate(crateNumber: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface MaterialLotDao {
    @Query("SELECT * FROM material_lots ORDER BY createdAt DESC")
    fun getAllLots(): Flow<List<MaterialLot>>

    @Query("SELECT * FROM material_lots WHERE status = :status ORDER BY createdAt DESC")
    fun getLotsByStatus(status: String): Flow<List<MaterialLot>>

    @Query("SELECT * FROM material_lots WHERE lotId = :lotId")
    suspend fun getLotById(lotId: String): MaterialLot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: MaterialLot)

    @Query("UPDATE material_lots SET status = :status, acceptedBidId = :bidId WHERE lotId = :lotId")
    suspend fun updateLotStatus(lotId: String, status: String, bidId: String? = null)
}

@Dao
interface RecyclerBidDao {
    @Query("SELECT * FROM recycler_bids WHERE lotId = :lotId ORDER BY pricePerKg DESC")
    fun getBidsForLot(lotId: String): Flow<List<RecyclerBid>>

    @Query("SELECT * FROM recycler_bids ORDER BY submittedAt DESC")
    fun getAllBids(): Flow<List<RecyclerBid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: RecyclerBid)

    @Query("UPDATE recycler_bids SET status = :status WHERE bidId = :bidId")
    suspend fun updateBidStatus(bidId: String, status: String)

    @Query("UPDATE recycler_bids SET status = 'REJECTED' WHERE lotId = :lotId AND bidId != :acceptedBidId")
    suspend fun rejectOtherBidsForLot(lotId: String, acceptedBidId: String)
}

@Dao
interface PickupDao {
    @Query("SELECT * FROM pickups ORDER BY scheduledDate DESC")
    fun getAllPickups(): Flow<List<PickupRecord>>

    @Query("SELECT * FROM pickups WHERE pickupId = :pickupId")
    suspend fun getPickupById(pickupId: String): PickupRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickup(pickup: PickupRecord)

    @Query("UPDATE pickups SET status = :status, measuredWeightKg = :weight, purityPercent = :purity, finalAmount = :finalAmount, paymentReference = :paymentRef, completedAt = :completedAt WHERE pickupId = :pickupId")
    suspend fun completePickup(
        pickupId: String,
        status: String,
        weight: Double,
        purity: Double,
        finalAmount: Double,
        paymentRef: String,
        completedAt: Long = System.currentTimeMillis()
    )
}

@Dao
interface AuditEventDao {
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<AuditEvent>>

    @Insert
    suspend fun insertEvent(event: AuditEvent)
}
