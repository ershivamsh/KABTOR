package com.example.sortai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sortai.data.*
import com.example.sortai.model.*
import com.example.sortai.service.SpeechService
import com.example.sortai.service.VisionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SortAiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = SortAiRepository(database)
    val visionEngine = VisionEngine()
    val speechService = SpeechService(application)

    private val _currentRole = MutableStateFlow(UserRole.COLLECTOR)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _collectorScreenState = MutableStateFlow("HOME") // "HOME" or "SCAN"
    val collectorScreenState: StateFlow<String> = _collectorScreenState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val crates: StateFlow<List<CrateEntity>> = repository.allCrates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedItems: StateFlow<List<ScannedItem>> = repository.allScannedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lots: StateFlow<List<MaterialLot>> = repository.allLots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bids: StateFlow<List<RecyclerBid>> = repository.allBids
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pickups: StateFlow<List<PickupRecord>> = repository.allPickups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditEvents: StateFlow<List<AuditEvent>> = repository.recentAuditEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectRole(role: UserRole) {
        _currentRole.value = role
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.ENGLISH) {
            AppLanguage.HINDI
        } else {
            AppLanguage.ENGLISH
        }
    }

    fun navigateToScan() {
        _collectorScreenState.value = "SCAN"
    }

    fun navigateToCollectorHome() {
        _collectorScreenState.value = "HOME"
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun recordScannedItem(
        category: EWasteCategory,
        weightKg: Double,
        confirmedName: String,
        confidence: Float,
        isHazard: Boolean,
        hazardNote: String,
        source: InferenceSource
    ) {
        viewModelScope.launch {
            val item = ScannedItem(
                category = category.name,
                predictedClass = category.englishName,
                confirmedClass = confirmedName.ifBlank { category.englishName },
                confidence = confidence,
                isHazard = isHazard,
                hazardNote = hazardNote,
                crateId = category.crateNumber,
                weightKg = weightKg,
                scanMode = "Scanned",
                inferenceSource = source.label
            )
            repository.recordScannedItem(item)
            _feedbackMessage.value = "Sorted $confirmedName (${String.format("%.1f", weightKg)} kg) into ${category.crateLabel}"
            _collectorScreenState.value = "HOME"
        }
    }

    fun publishLotFromCrate(crateNumber: Int) {
        viewModelScope.launch {
            try {
                val lotId = repository.createMaterialLotFromCrate(crateNumber)
                _feedbackMessage.value = "Created marketplace lot $lotId! Open for recycler bids."
            } catch (e: Exception) {
                _feedbackMessage.value = "Could not create lot: ${e.message}"
            }
        }
    }

    fun submitBid(lotId: String, pricePerKg: Double, pickupDate: String) {
        viewModelScope.launch {
            try {
                repository.submitBid(
                    lotId = lotId,
                    recyclerName = "Apex Metal Recyclers (Verified)",
                    pricePerKg = pricePerKg,
                    proposedDate = pickupDate
                )
                _feedbackMessage.value = "Bid of ₹$pricePerKg/kg submitted successfully!"
            } catch (e: Exception) {
                _feedbackMessage.value = "Error submitting bid: ${e.message}"
            }
        }
    }

    fun acceptBid(lotId: String, bidId: String) {
        viewModelScope.launch {
            try {
                val pickupId = repository.acceptBid(lotId, bidId)
                _feedbackMessage.value = "Bid accepted! Pickup #$pickupId scheduled."
            } catch (e: Exception) {
                _feedbackMessage.value = "Error accepting bid: ${e.message}"
            }
        }
    }

    fun verifyPickup(pickupId: String, verifiedWeight: Double, purity: Double) {
        viewModelScope.launch {
            try {
                val completed = repository.verifyAndCompletePickup(pickupId, verifiedWeight, purity)
                _feedbackMessage.value = "Pickup completed! Simulated payment of ₹${String.format("%,.0f", completed.finalAmount)} recorded."
            } catch (e: Exception) {
                _feedbackMessage.value = "Error completing pickup: ${e.message}"
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetDatabase()
            _feedbackMessage.value = "Demo database reset to initial prototype state."
        }
    }

    fun speakSafety(text: String) {
        speechService.speak(text, _currentLanguage.value)
    }

    override fun onCleared() {
        super.onCleared()
        speechService.shutdown()
    }
}
