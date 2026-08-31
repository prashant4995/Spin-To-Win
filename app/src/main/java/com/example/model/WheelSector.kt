package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MaroonDeep
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary

enum class SectorType {
    WIN,
    TRY_AGAIN
}

data class WheelSector(
    val id: Int,
    val type: SectorType,
    val primaryLabel: String,
    val subLabel: (Dish?) -> String,
    val emoji: String,
    val startAngleDeg: Float,
    val sweepAngleDeg: Float = 90f,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    val isWin: Boolean get() = type == SectorType.WIN

    companion object {
        fun createDefaultSectors(): List<WheelSector> {
            return listOf(
                WheelSector(
                    id = 0,
                    type = SectorType.WIN,
                    primaryLabel = "FREE",
                    subLabel = { dish -> dish?.title?.uppercase() ?: "DISH" },
                    emoji = "🎉",
                    startAngleDeg = 0f,
                    primaryColor = MaroonRoyal,
                    secondaryColor = MaroonDeep
                ),
                WheelSector(
                    id = 1,
                    type = SectorType.TRY_AGAIN,
                    primaryLabel = "BETTER",
                    subLabel = { "LUCK NEXT TIME" },
                    emoji = "🌟",
                    startAngleDeg = 90f,
                    primaryColor = SaffronPrimary,
                    secondaryColor = SaffronDark
                ),
                WheelSector(
                    id = 2,
                    type = SectorType.TRY_AGAIN,
                    primaryLabel = "PRASAD",
                    subLabel = { "WISHES" },
                    emoji = "🪙",
                    startAngleDeg = 180f,
                    primaryColor = MaroonRoyal,
                    secondaryColor = MaroonDeep
                ),
                WheelSector(
                    id = 3,
                    type = SectorType.TRY_AGAIN,
                    primaryLabel = "TRY",
                    subLabel = { "AGAIN" },
                    emoji = "✨",
                    startAngleDeg = 270f,
                    primaryColor = SaffronPrimary,
                    secondaryColor = SaffronDark
                )
            )
        }
    }
}
