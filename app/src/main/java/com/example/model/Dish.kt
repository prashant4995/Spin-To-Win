package com.example.model

enum class Dish(
    val title: String,
    val nativeTitle: String,
    val subtitle: String,
    val tag: String,
    val description: String,
    val highlights: List<String>,
    val emoji: String,
    val pricePerUnit: Int // Price in Indian Rupees (₹)
) {
    MODAK(
        title = "Modak",
        nativeTitle = "उकडीचे मोदक",
        subtitle = "Sweet Festive Delicacy",
        tag = "Sweet",
        description = "Authentic steamed sweet dumpling stuffed with fresh coconut & organic jaggery.",
        highlights = listOf("Steamed Modak", "Pure Ghee", "Fresh Coconut"),
        emoji = "✨",
        pricePerUnit = 40
    ),
    KHANDVI(
        title = "Khandvi",
        nativeTitle = "खांडवी",
        subtitle = "Spiced Gram Flour Rolls",
        tag = "Savory",
        description = "Melt-in-mouth spiced gram flour rolls tempered with mustard seeds & grated coconut.",
        highlights = listOf("Khandvi Rolls", "Steamed Tadka", "Sesame & Coconut"),
        emoji = "🟡",
        pricePerUnit = 30
    ),
    COMBO_PLATE(
        title = "Festive Combo",
        nativeTitle = "उत्सव कॉम्बो थाळी",
        subtitle = "Modak + Khandvi Plate",
        tag = "Festive Combo",
        description = "Festive special platter pairing authentic sweet Modak with savory spiced Khandvi.",
        highlights = listOf("Festive Combo", "Modak + Khandvi", "Best Value"),
        emoji = "🍱",
        pricePerUnit = 55
    )
}

sealed interface AppScreen {
    data object Registration : AppScreen
    data object SpinWheel : AppScreen
    data object RewardResult : AppScreen
    data object History : AppScreen
}

data class SpinResult(
    val isWin: Boolean,
    val wonDish: Dish?,
    val quantity: Int = 1,
    val userName: String,
    val isSold: Boolean = false,
    val amountPaid: Int = 0,
    val isPaidViaQr: Boolean = false,
    val isDirectCheckout: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

