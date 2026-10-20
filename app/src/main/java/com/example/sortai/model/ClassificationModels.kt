package com.example.sortai.model

enum class ScanMode(val displayName: String, val hindiDisplayName: String) {
    WHOLE_DEVICE("Whole Device", "पूरा उपकरण (फोन, टीवी, चार्जर)"),
    SEPARATED_COMPONENT("Separated Component", "अलग किया हुआ पुर्जा (बोर्ड, मोटर, सेल)")
}

enum class HazardLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    companion object {
        val HIGH_PRIORITY = CRITICAL
    }
}

data class ComponentBreakdown(
    val name: String,
    val category: EWasteCategory,
    val estimatedSharePercent: Int
)

enum class InferenceSource(val label: String) {
    GEMINI_3_5_FLASH("Gemini 3.5 Flash Multimodal Vision"),
    OFFLINE_RULE_ENGINE("SortAI Local On-Device Heuristic"),
    MANUAL_REVIEW("Manual Collector Override")
}

data class InferenceResult(
    val category: EWasteCategory,
    val confidence: Float,
    val detectedLabel: String,
    val mode: ScanMode,
    val isHazard: Boolean,
    val hazardLevel: HazardLevel,
    val hazardWarning: String,
    val hazardWarningHindi: String,
    val components: List<ComponentBreakdown> = emptyList(),
    val requiresHumanReview: Boolean = false,
    val inferenceSource: InferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
    val modelLatencyMs: Long = 0L,
    val recommendedCrate: EWasteCategory = category
)
