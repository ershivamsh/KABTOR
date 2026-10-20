package com.example.sortai.service

import com.example.sortai.model.EWasteCategory
import com.example.sortai.model.HazardLevel
import com.example.sortai.model.ScanMode

data class DemoScrapItem(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val mode: ScanMode,
    val expectedCategory: EWasteCategory,
    val defaultWeightKg: Double,
    val hazardLevel: HazardLevel,
    val hazardNoteEn: String,
    val hazardNoteHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val componentsSummary: String
)

object DemoScrapDataset {
    val items = listOf(
        DemoScrapItem(
            id = "demo_phone",
            title = "Discarded Android Smartphone",
            hindiTitle = "पुराना टचस्क्रीन स्मार्टफोन",
            mode = ScanMode.WHOLE_DEVICE,
            expectedCategory = EWasteCategory.PCB,
            defaultWeightKg = 0.22,
            hazardLevel = HazardLevel.HIGH_PRIORITY,
            hazardNoteEn = "Contains embedded Li-Ion pouch battery. Do not puncture or bend frame.",
            hazardNoteHi = "अंदर लिथियम-आयन बैटरी है। फोन को न मोड़ें और न ही बैटरी को छेदें।",
            descriptionEn = "Whole smartphone with cracked front glass, motherboard, camera sensor, and internal cell.",
            descriptionHi = "टूटे हुए डिस्प्ले वाला पूरा स्मार्टफोन, जिसमें मदरबोर्ड और आंतरिक बैटरी शामिल है।",
            componentsSummary = "PCB (40%), Lithium Cell (30%), Display/Glass (20%), Plastics (10%)"
        ),
        DemoScrapItem(
            id = "demo_laptop_pcb",
            title = "Laptop High-Grade Motherboard",
            hindiTitle = "लैपटॉप हाई-ग्रेड मदरबोर्ड (PCB)",
            mode = ScanMode.SEPARATED_COMPONENT,
            expectedCategory = EWasteCategory.PCB,
            defaultWeightKg = 0.45,
            hazardLevel = HazardLevel.NONE,
            hazardNoteEn = "Non-hazardous. Contains gold/palladium contacts. Keep completely dry.",
            hazardNoteHi = "सुरक्षित। इसमें सोने और तांबे के कॉन्टैक्ट्स हैं। सूखा रखें।",
            descriptionEn = "Multi-layer printed circuit board with CPU socket, soldered ICs, and copper grounding plane.",
            descriptionHi = "सोने और तांबे की परतों वाला मल्टी-लेयर सर्किट बोर्ड।",
            componentsSummary = "FR4 Substrate (55%), Copper/Gold Traces (25%), ICs & Solder (20%)"
        ),
        DemoScrapItem(
            id = "demo_swollen_battery",
            title = "Swollen Lithium-Ion Battery Pack",
            hindiTitle = "फूली हुई लिथियम-आयन बैटरी",
            mode = ScanMode.SEPARATED_COMPONENT,
            expectedCategory = EWasteCategory.BATTERY,
            defaultWeightKg = 0.18,
            hazardLevel = HazardLevel.CRITICAL,
            hazardNoteEn = "CRITICAL FIRE HAZARD: Battery cell is swollen. Never dismantle, compress, or incinerate. Place immediately in Red Crate 2 with sand/vermiculite lining.",
            hazardNoteHi = "अति संवेदनशील आग का खतरा: बैटरी में गैस भर चुकी है। इसे न खोलें और न दबाएं। तुरंत लाल क्रेट 2 में रखें।",
            descriptionEn = "Damaged 3.7V rechargeable lithium polymer cell exhibiting dangerous gas expansion.",
            descriptionHi = "क्षतिग्रस्त लिथियम सेल, गैस के दबाव से फूला हुआ।",
            componentsSummary = "Cobalt/Lithium Chemistry (60%), Flammable Organic Solvent (25%), Aluminum Housing (15%)"
        ),
        DemoScrapItem(
            id = "demo_ac_motor",
            title = "Appliance Induction Motor with Copper Stator",
            hindiTitle = "तांबे की वाइंडिंग वाली इलेक्ट्रिक मोटर",
            mode = ScanMode.SEPARATED_COMPONENT,
            expectedCategory = EWasteCategory.COPPER_MOTOR,
            defaultWeightKg = 4.80,
            hazardLevel = HazardLevel.LOW,
            hazardNoteEn = "Heavy material. Safe to handle with protective gloves. Do not drop on feet.",
            hazardNoteHi = "भारी वस्तु। दस्ताने पहनकर उठाएं। पैरों पर गिरने से बचाएं।",
            descriptionEn = "Heavy electric induction motor stator densely wound with high-purity enameled copper wire.",
            descriptionHi = "प्योर तांबे के तारों से बंधी भारी इलेक्ट्रिक मोटर।",
            componentsSummary = "Enameled Copper Winding (45%), Laminated Silicon Steel (50%), Cast Housing (5%)"
        ),
        DemoScrapItem(
            id = "demo_copper_cables",
            title = "Bundled Copper Power Wires & Data Cables",
            hindiTitle = "तांबे के बिजली तार और केबल का गुच्छा",
            mode = ScanMode.SEPARATED_COMPONENT,
            expectedCategory = EWasteCategory.CABLE,
            defaultWeightKg = 1.95,
            hazardLevel = HazardLevel.NONE,
            hazardNoteEn = "Non-hazardous. STRICT NOTICE: Do not burn outer PVC insulation. Stripping must be mechanical.",
            hazardNoteHi = "सुरक्षित। सूचना: प्लास्टिक को कभी न जलाएं। मशीन से छिलाई करें।",
            descriptionEn = "Flexible stranded copper conducting core insulated with thermoplastic PVC coating.",
            descriptionHi = "तांबे के बहु-तंत्रीय तार, पीवीसी कवर के साथ।",
            componentsSummary = "Refined Copper Conductor (60%), PVC Insulative Sheath (40%)"
        ),
        DemoScrapItem(
            id = "demo_crt_tv",
            title = "Legacy CRT Cathode Ray Television",
            hindiTitle = "पुराना सीआरटी (CRT) टीवी बॉक्स",
            mode = ScanMode.WHOLE_DEVICE,
            expectedCategory = EWasteCategory.DISPLAY,
            defaultWeightKg = 12.50,
            hazardLevel = HazardLevel.HIGH_PRIORITY,
            hazardNoteEn = "HIGH HAZARD: Funnel glass contains toxic lead oxide and internal vacuum under high tension. Do not smash tube.",
            hazardNoteHi = "गंभीर खतरा: इसके कांच में जहरीला सीसा (Lead) और वैक्यूम है। स्क्रीन को कभी न फोड़ें।",
            descriptionEn = "Bulky television chassis with high-voltage flyback transformer, phosphor screen, and deflector yoke.",
            descriptionHi = "पुराना टीवी, जिसके अंदर कांच का वैक्यूम ट्यूब और भारी ट्रांसफार्मर है।",
            componentsSummary = "Leaded Funnel Glass (50%), Deflection Coil Copper (15%), Chassis Plastic (25%), Power PCB (10%)"
        ),
        DemoScrapItem(
            id = "demo_plastics",
            title = "Computer Monitor Plastic Bezel & Casing",
            hindiTitle = "कंप्यूटर मॉनिटर प्लास्टिक बॉडी",
            mode = ScanMode.SEPARATED_COMPONENT,
            expectedCategory = EWasteCategory.PLASTIC_GLASS,
            defaultWeightKg = 1.60,
            hazardLevel = HazardLevel.NONE,
            hazardNoteEn = "Flame-retardant ABS/PC polymers. Keep separate from domestic food packaging plastics.",
            hazardNoteHi = "सुरक्षित। यह इंजीनियरिंग प्लास्टिक (ABS) है। साधारण कचरे से अलग रखें।",
            descriptionEn = "Molded thermoplastic casing from desktop computing equipment without electronic hardware.",
            descriptionHi = "कंप्यूटर का बाहरी प्लास्टिक कवर।",
            componentsSummary = "Polycarbonate / ABS Polymer (95%), Metal Thread Inserts (5%)"
        ),
        DemoScrapItem(
            id = "demo_unknown_canister",
            title = "Unlabeled Corroded Metal Canister",
            hindiTitle = "बिना लेबल का जंग लगा डिब्बा / धातु",
            mode = ScanMode.WHOLE_DEVICE,
            expectedCategory = EWasteCategory.UNKNOWN_REVIEW,
            defaultWeightKg = 0.85,
            hazardLevel = HazardLevel.MEDIUM,
            hazardNoteEn = "Unknown contents or industrial capacitor. Place in Review zone for multimeter/expert check.",
            hazardNoteHi = "अज्ञात वस्तु। इसे रिव्यू जोन में रखें और विशेषज्ञ से जांच कराएं।",
            descriptionEn = "Heavily oxidized cylindrical component with unreadable serial numbers and sealed electrolyte seals.",
            descriptionHi = "जंग लगा पुराना धातु का डिब्बा, जिसकी पहचान नहीं हो सकी।",
            componentsSummary = "Unknown Electrolyte (50%), Steel Case (50%)"
        )
    )
}
