package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.Dish
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight

@Composable
fun DishIllustration(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A0012),
                        Color(0xFF200007)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (dish) {
            Dish.KHANDVI -> KhandviArt(modifier = Modifier.fillMaxSize())
            Dish.MODAK -> ModakArt(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun KhandviArt(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "steam_khandvi")
    val steamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steam_anim"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Warm radial glow behind plate
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FFC107), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.45f
            ),
            center = Offset(cx, cy),
            radius = w * 0.45f
        )

        // Traditional Brass / Gold Thali base
        val plateRadius = minOf(w, h) * 0.40f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldLight, GoldAccent, GoldDark),
                center = Offset(cx - plateRadius * 0.2f, cy - plateRadius * 0.2f),
                radius = plateRadius
            ),
            center = Offset(cx, cy + h * 0.04f),
            radius = plateRadius
        )
        // Plate inner rim
        drawCircle(
            color = Color(0xFF8A5A00),
            center = Offset(cx, cy + h * 0.04f),
            radius = plateRadius * 0.92f,
            style = Stroke(width = 4f)
        )
        // Banana leaf backdrop on plate
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                start = Offset(cx - plateRadius * 0.8f, cy),
                end = Offset(cx + plateRadius * 0.8f, cy)
            ),
            topLeft = Offset(cx - plateRadius * 0.78f, cy - plateRadius * 0.65f + h * 0.04f),
            size = Size(plateRadius * 1.56f, plateRadius * 1.45f)
        )

        // 3 Silky Spiced Khandvi Cylindrical Rolls stacked neatly
        val rollWidth = plateRadius * 0.62f
        val rollHeight = plateRadius * 0.28f

        // Khandvi Roll 1 (Back Left)
        drawKhandviRoll(
            centerX = cx - plateRadius * 0.25f,
            centerY = cy - plateRadius * 0.15f + h * 0.04f,
            width = rollWidth,
            height = rollHeight,
            rotationDeg = -10f
        )

        // Khandvi Roll 2 (Back Right)
        drawKhandviRoll(
            centerX = cx + plateRadius * 0.22f,
            centerY = cy - plateRadius * 0.10f + h * 0.04f,
            width = rollWidth,
            height = rollHeight,
            rotationDeg = 12f
        )

        // Khandvi Roll 3 (Front Center Hero Roll)
        drawKhandviRoll(
            centerX = cx - plateRadius * 0.02f,
            centerY = cy + plateRadius * 0.18f + h * 0.04f,
            width = rollWidth * 1.10f,
            height = rollHeight * 1.10f,
            rotationDeg = 0f,
            isHero = true
        )

        // Fresh Green Mint/Coriander Chutney Bowl
        val bowlCx = cx + plateRadius * 0.48f
        val bowlCy = cy - plateRadius * 0.32f + h * 0.04f
        val bowlR = plateRadius * 0.22f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldAccent, GoldDark),
                center = Offset(bowlCx, bowlCy),
                radius = bowlR
            ),
            center = Offset(bowlCx, bowlCy),
            radius = bowlR
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)),
                center = Offset(bowlCx, bowlCy),
                radius = bowlR * 0.78f
            ),
            center = Offset(bowlCx, bowlCy),
            radius = bowlR * 0.78f
        )

        // Delicate rising steam lines
        val steamColor = Color(0x66FFF8E1)
        val steamPath1 = Path().apply {
            moveTo(cx - 20f, cy - plateRadius * 0.35f - steamOffset)
            cubicTo(
                cx - 35f, cy - plateRadius * 0.55f - steamOffset,
                cx - 10f, cy - plateRadius * 0.75f - steamOffset,
                cx - 25f, cy - plateRadius * 0.95f - steamOffset
            )
        }
        val steamPath2 = Path().apply {
            moveTo(cx + 15f, cy - plateRadius * 0.40f - steamOffset * 0.8f)
            cubicTo(
                cx + 30f, cy - plateRadius * 0.60f - steamOffset * 0.8f,
                cx + 5f, cy - plateRadius * 0.80f - steamOffset * 0.8f,
                cx + 20f, cy - plateRadius * 1.0f - steamOffset * 0.8f
            )
        }
        drawPath(steamPath1, steamColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
        drawPath(steamPath2, steamColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawKhandviRoll(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    rotationDeg: Float,
    isHero: Boolean = false
) {
    val khandviYellowBright = if (isHero) Color(0xFFFFD54F) else Color(0xFFFFCA28)
    val khandviYellowRich = if (isHero) Color(0xFFFFB300) else Color(0xFFFFA000)
    val khandviYellowDeep = if (isHero) Color(0xFFFF8F00) else Color(0xFFE65100)

    val halfW = width / 2f
    val halfH = height / 2f

    // Soft drop shadow under the roll
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(centerX - halfW + 3f, centerY - halfH + 6f),
        size = Size(width, height),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Silky smooth Khandvi rolled cylindrical body
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(khandviYellowBright, khandviYellowRich, khandviYellowDeep),
            start = Offset(centerX - halfW, centerY - halfH),
            end = Offset(centerX - halfW, centerY + halfH)
        ),
        topLeft = Offset(centerX - halfW, centerY - halfH),
        size = Size(width, height),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Glistening silky top highlight
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0x99FFF9C4), Color.Transparent),
            start = Offset(centerX, centerY - halfH + 2f),
            end = Offset(centerX, centerY - halfH + 8f)
        ),
        topLeft = Offset(centerX - halfW + 4f, centerY - halfH + 2f),
        size = Size(width - 8f, 6f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Characteristic spiral roll layers at edge
    val spiralColor = Color(0x55E65100)
    drawOval(
        color = spiralColor,
        topLeft = Offset(centerX - halfW + 2f, centerY - halfH + 2f),
        size = Size(10f, height - 4f),
        style = Stroke(width = 1.5f)
    )
    drawOval(
        color = spiralColor,
        topLeft = Offset(centerX - halfW + 5f, centerY - halfH + 5f),
        size = Size(6f, height - 10f),
        style = Stroke(width = 1.2f)
    )

    // Black Mustard Seeds Tadka (Rai)
    val mustardColor = Color(0xFF212121)
    val mustardOffsets = listOf(
        Offset(-halfW * 0.4f, -halfH * 0.2f),
        Offset(-halfW * 0.1f, halfH * 0.3f),
        Offset(halfW * 0.3f, -halfH * 0.3f),
        Offset(halfW * 0.15f, halfH * 0.1f),
        Offset(-halfW * 0.6f, halfH * 0.1f),
        Offset(halfW * 0.5f, -halfH * 0.1f)
    )
    for (m in mustardOffsets) {
        drawCircle(
            color = mustardColor,
            center = Offset(centerX + m.x, centerY + m.y),
            radius = 2.5f
        )
    }

    // Golden / White Sesame Seeds (Til)
    val sesameColor = Color(0xFFFFFDE7)
    val sesameOffsets = listOf(
        Offset(-halfW * 0.3f, -halfH * 0.4f),
        Offset(halfW * 0.05f, -halfH * 0.2f),
        Offset(halfW * 0.35f, halfH * 0.25f),
        Offset(-halfW * 0.15f, halfH * 0.4f),
        Offset(halfW * 0.45f, -halfH * 0.35f)
    )
    for (s in sesameOffsets) {
        drawOval(
            color = sesameColor,
            topLeft = Offset(centerX + s.x - 2.5f, centerY + s.y - 1.5f),
            size = Size(5.5f, 3f)
        )
    }

    // Grated Fresh White Coconut Shreds
    val coconutColor = Color(0xEEFFFFFF)
    val coconutShreds = listOf(
        Offset(-halfW * 0.5f, 0f),
        Offset(-halfW * 0.2f, -halfH * 0.1f),
        Offset(halfW * 0.2f, -halfH * 0.2f),
        Offset(halfW * 0.4f, halfH * 0.2f),
        Offset(0f, halfH * 0.25f)
    )
    for (c in coconutShreds) {
        drawRoundRect(
            color = coconutColor,
            topLeft = Offset(centerX + c.x, centerY + c.y),
            size = Size(7f, 2.2f),
            cornerRadius = CornerRadius(1f, 1f)
        )
    }

    // Fresh Green Coriander Garnish (Cilantro flecks)
    val corianderColor = Color(0xFF388E3C)
    val corianderOffsets = listOf(
        Offset(-halfW * 0.35f, halfH * 0.15f),
        Offset(halfW * 0.1f, -halfH * 0.35f),
        Offset(halfW * 0.25f, halfH * 0.05f)
    )
    for (cor in corianderOffsets) {
        drawCircle(
            color = corianderColor,
            center = Offset(centerX + cor.x, centerY + cor.y),
            radius = 3.2f
        )
    }
}

@Composable
fun ModakArt(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val kesarGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_anim"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Warm festive halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FFB300), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.48f
            ),
            center = Offset(cx, cy),
            radius = w * 0.48f
        )

        // Sacred Puja Brass Thali / Golden Plate
        val plateRadius = minOf(w, h) * 0.40f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldLight, GoldAccent, Color(0xFF8D5B00)),
                center = Offset(cx - plateRadius * 0.2f, cy - plateRadius * 0.2f),
                radius = plateRadius
            ),
            center = Offset(cx, cy + h * 0.05f),
            radius = plateRadius
        )

        // Plate Decorative Floral / Dot Rim
        for (i in 0 until 18) {
            val angle = i * (2 * Math.PI / 18).toFloat()
            val dotR = plateRadius * 0.94f
            val dotX = cx + (Math.cos(angle.toDouble()) * dotR).toFloat()
            val dotY = (cy + h * 0.05f) + (Math.sin(angle.toDouble()) * dotR).toFloat()
            drawCircle(
                color = Color(0xFFFFF176),
                center = Offset(dotX, dotY),
                radius = 3.5f
            )
        }

        // Inner Red Velvet / Festive Center Base
        drawCircle(
            color = Color(0xFF5C0012),
            center = Offset(cx, cy + h * 0.05f),
            radius = plateRadius * 0.84f
        )

        // Steamed Ukadiche Modak Hero Piece
        val modakWidth = plateRadius * 0.95f
        val modakHeight = plateRadius * 1.10f
        val modakBottomY = cy + plateRadius * 0.50f
        val modakTopY = modakBottomY - modakHeight

        // Shadow under modak
        drawOval(
            color = Color(0x77000000),
            topLeft = Offset(cx - modakWidth * 0.48f, modakBottomY - 14f),
            size = Size(modakWidth * 0.96f, 28f)
        )

        // Modak Outer Curvaceous Shape Path
        val modakPath = Path().apply {
            moveTo(cx, modakTopY) // Tip of modak
            // Right fluted curve
            cubicTo(
                cx + modakWidth * 0.15f, modakTopY + modakHeight * 0.28f,
                cx + modakWidth * 0.56f, modakTopY + modakHeight * 0.65f,
                cx + modakWidth * 0.42f, modakBottomY
            )
            // Rounded bottom
            cubicTo(
                cx + modakWidth * 0.20f, modakBottomY + 12f,
                cx - modakWidth * 0.20f, modakBottomY + 12f,
                cx - modakWidth * 0.42f, modakBottomY
            )
            // Left fluted curve
            cubicTo(
                cx - modakWidth * 0.56f, modakTopY + modakHeight * 0.65f,
                cx - modakWidth * 0.15f, modakTopY + modakHeight * 0.28f,
                cx, modakTopY
            )
            close()
        }

        // Steamed Rice Flour Body (Luscious pearlescent off-white)
        drawPath(
            path = modakPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFFDE7),
                    Color(0xFFF0EBE1),
                    Color(0xFFDED4C5)
                ),
                start = Offset(cx - modakWidth * 0.3f, modakTopY),
                end = Offset(cx + modakWidth * 0.4f, modakBottomY)
            )
        )

        // Fluting Petal Pleats (Characteristic 21-fold Modak pleats)
        val pleatColor = Color(0x33BCAAA4)
        val pleatHighlight = Color(0x66FFFFFF)

        val pleatsX = listOf(-0.32f, -0.20f, -0.09f, 0f, 0.09f, 0.20f, 0.32f)
        for (px in pleatsX) {
            val pleatPath = Path().apply {
                moveTo(cx, modakTopY + 4f)
                cubicTo(
                    cx + modakWidth * px * 0.4f, modakTopY + modakHeight * 0.45f,
                    cx + modakWidth * px * 0.9f, modakTopY + modakHeight * 0.85f,
                    cx + modakWidth * px * 0.95f, modakBottomY - 4f
                )
            }
            drawPath(pleatPath, pleatColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            drawPath(pleatPath, pleatHighlight, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
        }

        // Modak Crown / Tip with Golden Saffron Strand (Kesar) & Pure Ghee drop
        val kesarPath = Path().apply {
            moveTo(cx - 2f, modakTopY - 4f)
            cubicTo(
                cx - 10f, modakTopY + 12f,
                cx + 4f, modakTopY + 22f,
                cx - 1f, modakTopY + 38f
            )
        }
        // Glowing Kesar saffron strand
        drawPath(
            path = kesarPath,
            color = Color(0xFFFF6D00).copy(alpha = kesarGlow),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Second saffron thread
        val kesarPath2 = Path().apply {
            moveTo(cx + 1f, modakTopY + 2f)
            cubicTo(
                cx + 8f, modakTopY + 16f,
                cx + 12f, modakTopY + 28f,
                cx + 6f, modakTopY + 42f
            )
        }
        drawPath(
            path = kesarPath2,
            color = Color(0xFFFF9100).copy(alpha = kesarGlow),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Drop of pure golden Cow Ghee (Tupachi dhar) on tip
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F)),
                center = Offset(cx, modakTopY + 8f),
                radius = 7f
            ),
            center = Offset(cx, modakTopY + 8f),
            radius = 7f
        )

        // Grated fresh coconut flakes & cardamom speckles on side
        val flakeOffsets = listOf(
            Offset(-modakWidth * 0.25f, modakHeight * 0.65f),
            Offset(modakWidth * 0.28f, modakHeight * 0.70f),
            Offset(-modakWidth * 0.15f, modakHeight * 0.85f),
            Offset(modakWidth * 0.12f, modakHeight * 0.80f)
        )
        for (f in flakeOffsets) {
            drawOval(
                color = Color(0xEEFFFFFF),
                topLeft = Offset(cx + f.x, modakTopY + f.y),
                size = Size(6f, 3f)
            )
        }
    }
}
