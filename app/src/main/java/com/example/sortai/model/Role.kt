package com.example.sortai.model

enum class UserRole(
    val title: String,
    val hindiTitle: String,
    val description: String,
    val iconName: String
) {
    COLLECTOR(
        title = "Collector",
        hindiTitle = "कबाड़ीवाला / संग्राहक",
        description = "Snap e-waste, receive safety & crate instructions, log items",
        iconName = "Scanner"
    ),
    SHOP_OWNER(
        title = "Shop Owner",
        hindiTitle = "दुकानदार / केंद्र प्रबंधक",
        description = "Monitor crate threshold capacities and publish material lots",
        iconName = "Storefront"
    ),
    RECYCLER(
        title = "Recycler",
        hindiTitle = "अधिकृत रिसाइकलर",
        description = "Discover categorized lots, submit bids, and verify pickups",
        iconName = "LocalShipping"
    ),
    ADMIN(
        title = "Administrator",
        hindiTitle = "प्रशासक",
        description = "Audit compliance, verify recyclers, view platform stats",
        iconName = "AdminPanelSettings"
    )
}

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val localeTag: String
) {
    ENGLISH("en", "English", "en-US"),
    HINDI("hi", "हिन्दी", "hi-IN")
}
