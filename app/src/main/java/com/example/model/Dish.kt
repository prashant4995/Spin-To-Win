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
    KHANDVI(
        title = "Khandvi",
        nativeTitle = "खांडवी",
        subtitle = "Silky Melt-in-Mouth Spiced Gram Flour Rolls",
        tag = "Savory & Steamed",
        description = "Authentic delicate rolled bites handcrafted from spiced gram flour and fresh sour buttermilk, seasoned with mustard seeds, golden sesame, fresh coconut, and fresh coriander.",
        highlights = listOf("Silky Steamed Rolls", "Fresh Grated Coconut", "Mustard & Sesame Tadka", "Spiced Buttermilk"),
        emoji = "🟡",
        pricePerUnit = 30
    ),
    MODAK(
        title = "Modak",
        nativeTitle = "उकडीचे मोदक",
        subtitle = "Sweet Steamed / Fried Delicacy",
        tag = "Sweet & Festive",
        description = "Divine festival dumplings handcrafted from tender rice dough, packed with luscious grated fresh coconut, pure organic jaggery, fragrant cardamom, and royal saffron.",
        highlights = listOf("Fresh Coconut & Jaggery", "Royal Kesar Saffron", "Aromatic Elaichi", "Topped with Pure Ghee"),
        emoji = "✨",
        pricePerUnit = 30
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
    val timestamp: Long = System.currentTimeMillis()
)

