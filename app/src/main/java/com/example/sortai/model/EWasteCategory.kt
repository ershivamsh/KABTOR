package com.example.sortai.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class EWasteCategory(
    val crateNumber: Int,
    val crateLabel: String,
    val englishName: String,
    val hindiName: String,
    val color: Color,
    val hexCode: String,
    val defaultThresholdKg: Double,
    val benchmarkPricePerKg: Double,
    val isHazardous: Boolean,
    val safeHandlingEnglish: String,
    val safeHandlingHindi: String
) {
    PCB(
        crateNumber = 1,
        crateLabel = "Crate 1",
        englishName = "Printed Circuit Boards",
        hindiName = "सर्किट बोर्ड (PCB)",
        color = CratePcbGreen,
        hexCode = "#2E7D32",
        defaultThresholdKg = 15.0,
        benchmarkPricePerKg = 340.0,
        isHazardous = false,
        safeHandlingEnglish = "Keep dry. Do not break or burn boards to avoid toxic fumes.",
        safeHandlingHindi = "सूखा रखें। जहरीले धुएं से बचने के लिए बोर्ड को न तोड़ें और न जलाएं।"
    ),
    BATTERY(
        crateNumber = 2,
        crateLabel = "Crate 2",
        englishName = "Batteries (Hazardous)",
        hindiName = "खतरनाक बैटरी",
        color = CrateBatteryRed,
        hexCode = "#C62828",
        defaultThresholdKg = 10.0,
        benchmarkPricePerKg = 110.0,
        isHazardous = true,
        safeHandlingEnglish = "CRITICAL: Do not puncture or dismantle. Tape exposed terminals to prevent short circuit.",
        safeHandlingHindi = "चेतावनी: बैटरी को न खोलें और न तोड़ें। शॉर्ट सर्किट से बचने के लिए टर्मिनलों पर टेप लगाएं।"
    ),
    COPPER_MOTOR(
        crateNumber = 3,
        crateLabel = "Crate 3",
        englishName = "Copper Coils, Transformers & Motors",
        hindiName = "तांबे की कॉइल, ट्रांसफार्मर और मोटर",
        color = CrateCopperOrange,
        hexCode = "#EF6C00",
        defaultThresholdKg = 25.0,
        benchmarkPricePerKg = 480.0,
        isHazardous = false,
        safeHandlingEnglish = "Heavy item. Avoid dropping. Store with winding insulation intact.",
        safeHandlingHindi = "भारी सामान। गिरने से बचाएं। वाइंडिंग सुरक्षित रखें।"
    ),
    CABLE(
        crateNumber = 4,
        crateLabel = "Crate 4",
        englishName = "Cables and Wires",
        hindiName = "केबल और बिजली के तार",
        color = CrateCableYellow,
        hexCode = "#F9A825",
        defaultThresholdKg = 20.0,
        benchmarkPricePerKg = 220.0,
        isHazardous = false,
        safeHandlingEnglish = "Coil cleanly. Do not burn plastic insulation; burning releases toxic dioxins.",
        safeHandlingHindi = "तारों को मोड़कर रखें। प्लास्टिक इन्सुलेशन को न जलाएं; यह हानिकारक धुआं छोड़ता है।"
    ),
    DISPLAY(
        crateNumber = 5,
        crateLabel = "Crate 5",
        englishName = "Display Panels (CRT / LCD / OLED)",
        hindiName = "डिस्प्ले पैनल और स्क्रीन",
        color = CrateDisplayBlue,
        hexCode = "#1565C0",
        defaultThresholdKg = 18.0,
        benchmarkPricePerKg = 75.0,
        isHazardous = true,
        safeHandlingEnglish = "Handle with care to prevent glass shattering. Older CRT displays may contain lead/mercury.",
        safeHandlingHindi = "कांच टूटने से बचाएं। पुराने डिस्प्ले में सीसा और पारा हो सकता है।"
    ),
    PLASTIC_GLASS(
        crateNumber = 6,
        crateLabel = "Crate 6",
        englishName = "Glass & Engineered Plastics",
        hindiName = "प्लास्टिक बॉडी और कांच",
        color = CratePlasticPurple,
        hexCode = "#6A1B9A",
        defaultThresholdKg = 30.0,
        benchmarkPricePerKg = 35.0,
        isHazardous = false,
        safeHandlingEnglish = "Segregate flame-retardant computer casings from clean domestic plastics.",
        safeHandlingHindi = "इलेक्ट्रॉनिक प्लास्टिक केसिंग को साधारण प्लास्टिक से अलग रखें।"
    ),
    UNKNOWN_REVIEW(
        crateNumber = 0,
        crateLabel = "Review",
        englishName = "Unknown / Needs Review",
        hindiName = "जांच की आवश्यकता",
        color = CrateReviewGrey,
        hexCode = "#546E7A",
        defaultThresholdKg = 5.0,
        benchmarkPricePerKg = 50.0,
        isHazardous = false,
        safeHandlingEnglish = "Place in segregated staging area. Requires manual inspection before sorting.",
        safeHandlingHindi = "अलग सुरक्षित स्थान पर रखें। छांटने से पहले किसी विशेषज्ञ से जांच कराएं।"
    );

    companion object {
        fun fromId(id: Int): EWasteCategory = entries.firstOrNull { it.crateNumber == id } ?: UNKNOWN_REVIEW
        fun fromName(name: String): EWasteCategory = entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNKNOWN_REVIEW
    }
}
