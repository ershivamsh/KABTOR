package com.example.sortai.service

import com.example.sortai.model.*

object OfflineVisionClassifier {

    fun classify(
        queryOrLabel: String,
        mode: ScanMode
    ): InferenceResult {
        val q = queryOrLabel.lowercase().trim()

        // 1. Check for critical battery hazards
        if (q.contains("swollen") || q.contains("bloated") || q.contains("bulging") || q.contains("flammable")) {
            return InferenceResult(
                category = EWasteCategory.BATTERY,
                confidence = 0.96f,
                detectedLabel = "Swollen Lithium-Ion Cell (Thermal Hazard)",
                mode = mode,
                isHazard = true,
                hazardLevel = HazardLevel.CRITICAL,
                hazardWarning = "CRITICAL FIRE HAZARD: Gas pressure detected in battery. Do not puncture, bend, or crush. Keep in sand-lined Red Crate 2.",
                hazardWarningHindi = "अति संवेदनशील आग का खतरा! बैटरी फूली हुई है। इसे न खोलें और न दबाएं। तुरंत लाल क्रेट 2 में रखें।",
                components = listOf(
                    ComponentBreakdown("Lithium Polymer Cell", EWasteCategory.BATTERY, 85),
                    ComponentBreakdown("Protection Circuit Board", EWasteCategory.PCB, 15)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 42L,
                recommendedCrate = EWasteCategory.BATTERY
            )
        }

        // 2. Check for standard batteries
        if (q.contains("battery") || q.contains("cell") || q.contains("li-ion") || q.contains("accumulator") || q.contains("powerbank")) {
            return InferenceResult(
                category = EWasteCategory.BATTERY,
                confidence = 0.92f,
                detectedLabel = "Rechargeable Lithium/Lead-Acid Battery",
                mode = mode,
                isHazard = true,
                hazardLevel = HazardLevel.CRITICAL,
                hazardWarning = "Hazardous Material: Lithium / toxic acid inside. Tape terminals and place in Red Crate 2.",
                hazardWarningHindi = "खतरनाक सामग्री: इसमें लिथियम और एसिड है। टर्मिनलों पर टेप लगाकर लाल क्रेट 2 में रखें।",
                components = listOf(
                    ComponentBreakdown("Electrochemical Core", EWasteCategory.BATTERY, 90),
                    ComponentBreakdown("Outer Casing", EWasteCategory.PLASTIC_GLASS, 10)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 38L,
                recommendedCrate = EWasteCategory.BATTERY
            )
        }

        // 3. Check for Printed Circuit Boards (PCBs)
        if (q.contains("pcb") || q.contains("motherboard") || q.contains("circuit") || q.contains("board") || q.contains("ram") || q.contains("chip")) {
            return InferenceResult(
                category = EWasteCategory.PCB,
                confidence = 0.94f,
                detectedLabel = "Printed Circuit Board (Gold/Copper Bearing)",
                mode = mode,
                isHazard = false,
                hazardLevel = HazardLevel.NONE,
                hazardWarning = "High-value scrap. Keep dry and avoid breaking boards.",
                hazardWarningHindi = "कीमती स्क्रैप। बोर्ड को सूखा रखें और टूटने न दें।",
                components = listOf(
                    ComponentBreakdown("FR4 Glass-Epoxy Board", EWasteCategory.PCB, 60),
                    ComponentBreakdown("Gold/Copper Traces & ICs", EWasteCategory.PCB, 40)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 35L,
                recommendedCrate = EWasteCategory.PCB
            )
        }

        // 4. Check for Copper Coils, Transformers, Motors
        if (q.contains("motor") || q.contains("copper") || q.contains("coil") || q.contains("transformer") || q.contains("stator") || q.contains("rotor") || q.contains("winding")) {
            return InferenceResult(
                category = EWasteCategory.COPPER_MOTOR,
                confidence = 0.91f,
                detectedLabel = "Heavy Copper Induction Motor / Transformer",
                mode = mode,
                isHazard = false,
                hazardLevel = HazardLevel.LOW,
                hazardWarning = "Heavy material. Wear safety shoes and gloves. Place in Orange Crate 3.",
                hazardWarningHindi = "भारी धातु। दस्ताने पहनें और सावधानी से नारंगी क्रेट 3 में रखें।",
                components = listOf(
                    ComponentBreakdown("Enameled Copper Winding", EWasteCategory.COPPER_MOTOR, 45),
                    ComponentBreakdown("Silicon Steel Laminations", EWasteCategory.COPPER_MOTOR, 55)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 40L,
                recommendedCrate = EWasteCategory.COPPER_MOTOR
            )
        }

        // 5. Check for Cables and Wires
        if (q.contains("cable") || q.contains("wire") || q.contains("cord") || q.contains("lead") || q.contains("harness")) {
            return InferenceResult(
                category = EWasteCategory.CABLE,
                confidence = 0.93f,
                detectedLabel = "Insulated Copper Wire Bundles",
                mode = mode,
                isHazard = false,
                hazardLevel = HazardLevel.NONE,
                hazardWarning = "Do not burn PVC insulation to recover copper. Place in Yellow Crate 4.",
                hazardWarningHindi = "तांबा निकालने के लिए तार को कभी न जलाएं। पीली क्रेट 4 में रखें।",
                components = listOf(
                    ComponentBreakdown("High-Purity Copper Core", EWasteCategory.CABLE, 65),
                    ComponentBreakdown("PVC Insulation", EWasteCategory.PLASTIC_GLASS, 35)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 30L,
                recommendedCrate = EWasteCategory.CABLE
            )
        }

        // 6. Check for Displays (LCD, CRT, Screen)
        if (q.contains("display") || q.contains("screen") || q.contains("lcd") || q.contains("crt") || q.contains("tv") || q.contains("monitor")) {
            val isCrt = q.contains("crt")
            return InferenceResult(
                category = EWasteCategory.DISPLAY,
                confidence = 0.89f,
                detectedLabel = if (isCrt) "CRT Leaded Display Tube" else "Flat Panel Display Screen (LCD/OLED)",
                mode = mode,
                isHazard = isCrt,
                hazardLevel = if (isCrt) HazardLevel.CRITICAL else HazardLevel.MEDIUM,
                hazardWarning = if (isCrt) "HAZARD: Leaded funnel glass and vacuum bulb. Never smash. Place in Blue Crate 5." else "Fragile glass. Handle gently to avoid lacerations.",
                hazardWarningHindi = if (isCrt) "चेतावनी: इसके कांच में जहरीला सीसा है। स्क्रीन को न फोड़ें। नीली क्रेट 5 में रखें।" else "कांच की स्क्रीन। सावधानी से नीली क्रेट 5 में रखें।",
                components = listOf(
                    ComponentBreakdown("Glass Substrate", EWasteCategory.DISPLAY, 50),
                    ComponentBreakdown("Driver Electronics", EWasteCategory.PCB, 20),
                    ComponentBreakdown("Plastic / Metal Frame", EWasteCategory.PLASTIC_GLASS, 30)
                ),
                requiresHumanReview = isCrt,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 45L,
                recommendedCrate = EWasteCategory.DISPLAY
            )
        }

        // 7. Check for Plastics
        if (q.contains("plastic") || q.contains("casing") || q.contains("bezel") || q.contains("housing") || q.contains("chassis")) {
            return InferenceResult(
                category = EWasteCategory.PLASTIC_GLASS,
                confidence = 0.90f,
                detectedLabel = "Engineered Flame-Retardant Plastics",
                mode = mode,
                isHazard = false,
                hazardLevel = HazardLevel.NONE,
                hazardWarning = "Segregate from domestic plastic. Place in Purple Crate 6.",
                hazardWarningHindi = "साधारण घरेलू प्लास्टिक से अलग रखें। बैंगनी क्रेट 6 में रखें।",
                components = listOf(
                    ComponentBreakdown("ABS/Polycarbonate Casings", EWasteCategory.PLASTIC_GLASS, 100)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 28L,
                recommendedCrate = EWasteCategory.PLASTIC_GLASS
            )
        }

        // 8. Whole device phone or tablet
        if (q.contains("phone") || q.contains("smartphone") || q.contains("tablet")) {
            return InferenceResult(
                category = EWasteCategory.PCB,
                confidence = 0.91f,
                detectedLabel = "Smartphone (Whole Assembly)",
                mode = mode,
                isHazard = true,
                hazardLevel = HazardLevel.MEDIUM,
                hazardWarning = "Device contains internal Li-Ion battery. Do not crush. High-grade gold PCB recovery value.",
                hazardWarningHindi = "उपकरण में अंदर बैटरी है। इसे न दबाएं। अंदर कीमती सर्किट बोर्ड है।",
                components = listOf(
                    ComponentBreakdown("High-Grade Motherboard", EWasteCategory.PCB, 40),
                    ComponentBreakdown("Lithium-Ion Pouch", EWasteCategory.BATTERY, 30),
                    ComponentBreakdown("Display & Cover Glass", EWasteCategory.DISPLAY, 20),
                    ComponentBreakdown("Chassis Plastics", EWasteCategory.PLASTIC_GLASS, 10)
                ),
                requiresHumanReview = false,
                inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
                modelLatencyMs = 50L,
                recommendedCrate = EWasteCategory.PCB
            )
        }

        // Default: Unknown / Needs Review
        return InferenceResult(
            category = EWasteCategory.UNKNOWN_REVIEW,
            confidence = 0.52f,
            detectedLabel = "Unidentified Scrap Item ($queryOrLabel)",
            mode = mode,
            isHazard = false,
            hazardLevel = HazardLevel.LOW,
            hazardWarning = "Confidence below threshold. Place in Gray Review Zone for manual inspection.",
            hazardWarningHindi = "पहचान संदिग्ध है। छांटने से पहले ग्रे रिव्यू क्षेत्र में रखें।",
            components = emptyList(),
            requiresHumanReview = true,
            inferenceSource = InferenceSource.OFFLINE_RULE_ENGINE,
            modelLatencyMs = 25L,
            recommendedCrate = EWasteCategory.UNKNOWN_REVIEW
        )
    }
}
