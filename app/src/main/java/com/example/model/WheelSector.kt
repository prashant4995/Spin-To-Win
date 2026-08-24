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
    val labelText: (Dish?) -> String,
    val subText: String,
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
                    labelText = { dish -> "🎉 FREE ${dish?.title?.uppercase() ?: "DISH"}" },
                    subText = "Jackpot Prize!",
                    startAngleDeg = 0f,
                    primaryColor = MaroonRoyal,
                    secondaryColor = MaroonDeep
                ),
                WheelSector(
                    id = 1,
                    type = SectorType.TRY_AGAIN,
                    labelText = { _ -> "🔁 TRY AGAIN" },
                    subText = "Better Luck Next!",
                    startAngleDeg = 90f,
                    primaryColor = SaffronPrimary,
                    secondaryColor = SaffronDark
                ),
                WheelSector(
                    id = 2,
                    type = SectorType.WIN,
                    labelText = { dish -> "🎉 FREE ${dish?.title?.uppercase() ?: "DISH"}" },
                    subText = "Winner Winner!",
                    startAngleDeg = 180f,
                    primaryColor = MaroonRoyal,
                    secondaryColor = MaroonDeep
                ),
                WheelSector(
                    id = 3,
                    type = SectorType.TRY_AGAIN,
                    labelText = { _ -> "🔁 TRY AGAIN" },
                    subText = "Almost Had It!",
                    startAngleDeg = 270f,
                    primaryColor = SaffronPrimary,
                    secondaryColor = SaffronDark
                )
            )
        }
    }
}
