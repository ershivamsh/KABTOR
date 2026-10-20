package com.example.sortai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sortai.data.dao.*
import com.example.sortai.model.EWasteCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ScannedItem::class,
        CrateEntity::class,
        MaterialLot::class,
        RecyclerBid::class,
        PickupRecord::class,
        AuditEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scannedItemDao(): ScannedItemDao
    abstract fun crateDao(): CrateDao
    abstract fun materialLotDao(): MaterialLotDao
    abstract fun recyclerBidDao(): RecyclerBidDao
    abstract fun pickupDao(): PickupDao
    abstract fun auditEventDao(): AuditEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sortai_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val crateDao = database.crateDao()
            val lotDao = database.materialLotDao()
            val bidDao = database.recyclerBidDao()
            val auditDao = database.auditEventDao()

            // Initialize all 6 standard crates + 1 review crate
            val defaultCrates = EWasteCategory.entries.map { cat ->
                val initialWeight = when (cat) {
                    EWasteCategory.PCB -> 12.8
                    EWasteCategory.COPPER_MOTOR -> 21.0
                    EWasteCategory.CABLE -> 8.5
                    EWasteCategory.BATTERY -> 4.2
                    EWasteCategory.DISPLAY -> 9.0
                    EWasteCategory.PLASTIC_GLASS -> 14.0
                    EWasteCategory.UNKNOWN_REVIEW -> 1.5
                }
                val initialItems = when (cat) {
                    EWasteCategory.PCB -> 18
                    EWasteCategory.COPPER_MOTOR -> 7
                    EWasteCategory.CABLE -> 12
                    EWasteCategory.BATTERY -> 6
                    EWasteCategory.DISPLAY -> 3
                    EWasteCategory.PLASTIC_GLASS -> 11
                    EWasteCategory.UNKNOWN_REVIEW -> 2
                }
                CrateEntity(
                    crateNumber = cat.crateNumber,
                    categoryName = cat.name,
                    currentWeightKg = initialWeight,
                    thresholdKg = cat.defaultThresholdKg,
                    itemCount = initialItems
                )
            }
            crateDao.insertAllCrates(defaultCrates)

            // Seed an active PCB Material Lot that reached threshold
            val pcbLot = MaterialLot(
                lotId = "LOT-PCB-8801",
                category = EWasteCategory.PCB.name,
                totalWeightKg = 28.5,
                reservePricePerKg = 320.0,
                status = "OPEN_FOR_BIDS"
            )
            lotDao.insertLot(pcbLot)

            // Seed bids on that lot
            val bid1 = RecyclerBid(
                bidId = "BID-EMR-01",
                lotId = pcbLot.lotId,
                recyclerId = "rec_01",
                recyclerName = "EcoMetal Refiners Pvt Ltd (CPCB Reg #418)",
                pricePerKg = 360.0,
                offeredTotal = 28.5 * 360.0,
                proposedPickupDate = "Tomorrow, 10:30 AM",
                status = "PENDING"
            )
            val bid2 = RecyclerBid(
                bidId = "BID-GCE-02",
                lotId = pcbLot.lotId,
                recyclerId = "rec_02",
                recyclerName = "GreenCircuit Extractor Labs",
                pricePerKg = 345.0,
                offeredTotal = 28.5 * 345.0,
                proposedPickupDate = "Thursday, 2:00 PM",
                status = "PENDING"
            )
            bidDao.insertBid(bid1)
            bidDao.insertBid(bid2)

            // Seed a Copper Lot with an accepted bid in progress for demonstration
            val copperLot = MaterialLot(
                lotId = "LOT-COP-4402",
                category = EWasteCategory.COPPER_MOTOR.name,
                totalWeightKg = 32.0,
                reservePricePerKg = 450.0,
                status = "BID_ACCEPTED",
                acceptedBidId = "BID-IND-03"
            )
            lotDao.insertLot(copperLot)

            val bid3 = RecyclerBid(
                bidId = "BID-IND-03",
                lotId = copperLot.lotId,
                recyclerId = "rec_03",
                recyclerName = "Indo-Pure Smelters & Copper",
                pricePerKg = 510.0,
                offeredTotal = 32.0 * 510.0,
                proposedPickupDate = "Today, 4:00 PM",
                status = "ACCEPTED"
            )
            bidDao.insertBid(bid3)

            database.pickupDao().insertPickup(
                PickupRecord(
                    pickupId = "PCK-991",
                    lotId = copperLot.lotId,
                    bidId = bid3.bidId,
                    recyclerName = bid3.recyclerName,
                    scheduledDate = "Today, 4:00 PM",
                    measuredWeightKg = 32.0,
                    purityPercent = 98.5,
                    finalAmount = 32.0 * 510.0,
                    status = "SCHEDULED",
                    paymentReference = "UPI-SIM-2026-98124"
                )
            )

            auditDao.insertEvent(
                AuditEvent(
                    eventType = "SYSTEM_INITIALIZED",
                    details = "SortAI system initialized with 6 standardized crates & demo marketplace lots",
                    performedBy = "System"
                )
            )
        }
    }
}
