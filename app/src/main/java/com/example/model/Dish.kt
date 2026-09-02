package com.example.model

/**
 * Quality grade options for festival delicacies (Modak, Khandvi, Festive combo).
 */
data class QualityOption(
    val id: String,
    val name: String,
    val nativeName: String,
    val badge: String,
    val price: Int, // Price per unit in ₹
    val shortDesc: String,
    val description: String,
    val ingredients: List<String>,
    val emoji: String = "✨"
)

enum class Dish(
    val title: String,
    val nativeTitle: String,
    val subtitle: String,
    val tag: String,
    val description: String,
    val highlights: List<String>,
    val emoji: String,
    val pricePerUnit: Int, // Base / Standard price
    val qualityOptions: List<QualityOption>
) {
    MODAK(
        title = "Modak",
        nativeTitle = "उकडीचे मोदक",
        subtitle = "Sweet Festive Delicacy",
        tag = "Sweet Prasad",
        description = "Authentic steamed sweet dumpling stuffed with fresh coconut & organic jaggery.",
        highlights = listOf("Steamed Modak", "Pure Ghee", "Fresh Coconut"),
        emoji = "✨",
        pricePerUnit = 40,
        qualityOptions = listOf(
            QualityOption(
                id = "modak_standard",
                name = "Traditional Steamed",
                nativeName = "पारंपारिक उकडीचे",
                badge = "⭐ Standard",
                price = 40,
                shortDesc = "Fresh Coconut & Organic Jaggery",
                description = "Authentic steamed rice flour dumplings filled with fresh grated coconut & organic Kolhapuri jaggery, scented with fragrant cardamom.",
                ingredients = listOf("Steamed Rice Dough", "Fresh Coconut", "Organic Jaggery", "Cardamom"),
                emoji = "🥥"
            ),
            QualityOption(
                id = "modak_premium",
                name = "Kesar Mawa Premium",
                nativeName = "केशर मावा स्पेशल",
                badge = "👑 Premium",
                price = 60,
                shortDesc = "Rich Khoya & Kashmiri Saffron",
                description = "Velvety fresh mawa/khoya infusion with genuine Kashmiri saffron threads, roasted almond slivers & crushed pistachios.",
                ingredients = listOf("Pure Mawa / Khoya", "Kashmiri Kesar", "Pistachio Slivers", "Nutmeg"),
                emoji = "🌟"
            ),
            QualityOption(
                id = "modak_royal",
                name = "Royal Pure Ghee & Dryfruit",
                nativeName = "शाही ड्रायफ्रूट तूप",
                badge = "✨ Royal Gold",
                price = 80,
                shortDesc = "Pure Gir Ghee, Cashews & Gold Saffron",
                description = "Exclusive festive Mahaprasad prepared with pure Gir Cow A2 Ghee, roasted cashews, almonds, raisins, saffron and edible silver vark.",
                ingredients = listOf("Gir Cow A2 Ghee", "Roasted Cashews & Almonds", "Gold Saffron", "Silver Vark"),
                emoji = "👑"
            )
        )
    ),
    KHANDVI(
        title = "Khandvi",
        nativeTitle = "खांडवी",
        subtitle = "Spiced Gram Flour Rolls",
        tag = "Savory Tadka",
        description = "Melt-in-mouth spiced gram flour rolls tempered with mustard seeds & grated coconut.",
        highlights = listOf("Khandvi Rolls", "Steamed Tadka", "Sesame & Coconut"),
        emoji = "🟡",
        pricePerUnit = 30,
        qualityOptions = listOf(
            QualityOption(
                id = "khandvi_standard",
                name = "Classic Traditional",
                nativeName = "पारंपारिक तडका",
                badge = "⭐ Standard",
                price = 30,
                shortDesc = "Mustard Tadka & Grated Coconut",
                description = "Silky rolled besan sheets delicately cooked in spiced buttermilk, tempered with mustard seeds, sesame, curry leaves & grated coconut.",
                ingredients = listOf("Gram Flour (Besan)", "Mustard Seeds", "Fresh Coconut", "Curry Leaves"),
                emoji = "🟡"
            ),
            QualityOption(
                id = "khandvi_premium",
                name = "Cheese Tadka Special",
                nativeName = "चीज तडका स्पेशल",
                badge = "👑 Premium",
                price = 45,
                shortDesc = "Melting Amul Cheese & Fresh Tadka",
                description = "Layered with grated Amul cheese, roasted white sesame, crunchy green chillies, aromatic coriander and tangy chaat masala.",
                ingredients = listOf("Amul Processed Cheese", "Roasted Sesame", "Green Chillies", "Fresh Coriander"),
                emoji = "🧀"
            ),
            QualityOption(
                id = "khandvi_royal",
                name = "Royal Dryfruit & Kesar Tadka",
                nativeName = "शाही ड्रायफ्रूट तडका",
                badge = "✨ Royal Gold",
                price = 60,
                shortDesc = "Roasted Cashews, Hing & Saffron Notes",
                description = "Gourmet preparation topped with golden roasted cashews, crushed peanuts, Kashmiri saffron strands and royal asafoetida tadka.",
                ingredients = listOf("Roasted Cashews", "Crushed Peanuts", "Kashmiri Kesar", "Royal Hing Tadka"),
                emoji = "👑"
            )
        )
    ),
    COMBO_PLATE(
        title = "Festive Combo",
        nativeTitle = "उत्सव कॉम्बो थाळी",
        subtitle = "Modak + Khandvi Plate",
        tag = "Festive Combo",
        description = "Festive special platter pairing authentic sweet Modak with savory spiced Khandvi.",
        highlights = listOf("Festive Combo", "Modak + Khandvi", "Best Value"),
        emoji = "🍱",
        pricePerUnit = 55,
        qualityOptions = listOf(
            QualityOption(
                id = "combo_standard",
                name = "Classic Utsav Platter",
                nativeName = "क्लासिक उत्सव ताट",
                badge = "⭐ Standard",
                price = 55,
                shortDesc = "2x Traditional Modak + 4x Classic Khandvi",
                description = "Authentic festive pairing of 2 fresh steamed coconut Modaks alongside 4 pieces of mustard-tempered classic Khandvi rolls.",
                ingredients = listOf("2x Steamed Modak", "4x Classic Khandvi", "Fresh Coconut Chutney"),
                emoji = "🍱"
            ),
            QualityOption(
                id = "combo_premium",
                name = "Deluxe Festive Thali",
                nativeName = "डिलक्स उत्सव थाळी",
                badge = "👑 Premium",
                price = 85,
                shortDesc = "2x Kesar Mawa Modak + 4x Cheese Khandvi",
                description = "Elevated festive combo with 2 rich Kesar Mawa Modaks, 4 cheesy spiced Khandvi rolls, and sweet-tangy festive dip.",
                ingredients = listOf("2x Kesar Mawa Modak", "4x Cheese Khandvi", "Festive Chutney Trio"),
                emoji = "🌟"
            ),
            QualityOption(
                id = "combo_royal",
                name = "Royal Shahi Mahaprasad",
                nativeName = "शाही महाप्रसाद थाळी",
                badge = "✨ Royal Gold",
                price = 115,
                shortDesc = "2x Royal Dryfruit Modak + 4x Royal Khandvi + Pure Ghee",
                description = "Grand Shahi offering with 2 Royal Pure Ghee Dryfruit Modaks, 4 Royal Cashew Tadka Khandvi, pure Gir Ghee cup & roasted dryfruits.",
                ingredients = listOf("2x Royal Dryfruit Modak", "4x Royal Khandvi", "Pure Gir Ghee Cup", "Dryfruits"),
                emoji = "👑"
            )
        )
    );

    val defaultQualityOption: QualityOption
        get() = qualityOptions.first()
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
    val qualityOption: QualityOption? = null,
    val quantity: Int = 1,
    val userName: String,
    val isSold: Boolean = false,
    val amountPaid: Int = 0,
    val isPaidViaQr: Boolean = false,
    val isDirectCheckout: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


