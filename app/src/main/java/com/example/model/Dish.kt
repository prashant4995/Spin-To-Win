package com.example.model

enum class Dish(
    val title: String,
    val nativeTitle: String,
    val subtitle: String,
    val tag: String,
    val description: String,
    val highlights: List<String>,
    val emoji: String
) {
    KOTHIMBIR_VADI(
        title = "Kothimbir Vadi",
        nativeTitle = "कोथिंबीर वडी",
        subtitle = "Crispy Savory Cilantro Snack",
        tag = "Savory & Crispy",
        description = "Authentic Maharashtrian delicacy made from aromatic fresh coriander greens, spiced gram flour & toasted sesame seeds, delicately steamed and fried to golden perfection.",
        highlights = listOf("Fresh Coriander", "Toasted Sesame", "Golden Crisp", "Served with Mint Chutney"),
        emoji = "🌿"
    ),
    MODAK(
        title = "Modak",
        nativeTitle = "उकडीचे मोदक",
        subtitle = "Sweet Steamed / Fried Delicacy",
        tag = "Sweet & Festive",
        description = "Divine festival dumplings handcrafted from tender rice dough, packed with luscious grated fresh coconut, pure organic jaggery, fragrant cardamom, and royal saffron.",
        highlights = listOf("Fresh Coconut & Jaggery", "Royal Kesar Saffron", "Aromatic Elaichi", "Topped with Pure Ghee"),
        emoji = "✨"
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
    val userName: String,
    val claimCode: String,
    val timestamp: Long = System.currentTimeMillis()
)
