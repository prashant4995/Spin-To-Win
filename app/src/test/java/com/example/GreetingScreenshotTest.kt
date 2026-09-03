package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.Dish
import com.example.ui.screens.FoodSelectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelTablet, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun food_selection_tablet_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FoodSelectionScreen(
                    userName = "Ananya",
                    nameError = null,
                    dishQuantities = mapOf(Dish.MODAK to 1, Dish.KHANDVI to 1, Dish.COMBO_PLATE to 0),
                    totalQuantity = 2,
                    totalOrderAmount = 70,
                    canProceed = true,
                    onNameChanged = {},
                    onDishQuantityChanged = { _, _ -> },
                    onIncrementDishQuantity = {},
                    onDecrementDishQuantity = {},
                    onProceedClicked = {},
                    onOpenHistory = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
