package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import kotlin.random.Random

/**
 * Sacred Ganesha Idol Icon graphic.
 */
@Composable
fun GaneshaIdolIcon(
    modifier: Modifier = Modifier,
    contentDescription: String = "Lord Ganesha Idol"
) {
    Image(
        painter = painterResource(id = R.drawable.ic_ganesha_idol),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

/**
 * Festive Diya (Indian Oil Lamp) with animated glowing flame.
 */
@Composable
fun DiyaLamp(
    modifier: Modifier = Modifier,
    sizePx: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_flicker"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.65f

        // Flame Outer Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x99FFA000), Color.Transparent),
                center = Offset(cx, cy - h * 0.35f),
                radius = w * 0.5f * flameFlicker
            ),
            center = Offset(cx, cy - h * 0.35f),
            radius = w * 0.5f * flameFlicker
        )

        // Diya Clay/Brass Bowl
        val bowlPath = Path().apply {
            moveTo(cx - w * 0.40f, cy - h * 0.05f)
            cubicTo(
                cx - w * 0.35f, cy + h * 0.30f,
                cx + w * 0.35f, cy + h * 0.30f,
                cx + w * 0.40f, cy - h * 0.05f
            )
            // Lip of diya
            cubicTo(
                cx + w * 0.15f, cy - h * 0.12f,
                cx - w * 0.15f, cy - h * 0.12f,
                cx - w * 0.40f, cy - h * 0.05f
            )
            close()
        }

        drawPath(
            path = bowlPath,
            brush = Brush.linearGradient(
                colors = listOf(GoldLight, GoldAccent, GoldDark),
                start = Offset(cx - w * 0.4f, cy),
                end = Offset(cx + w * 0.4f, cy + h * 0.3f)
            )
        )

        // Flame Tear-Drop
        val flameHeight = h * 0.45f * flameFlicker
        val flameTopY = cy - h * 0.10f - flameHeight
        val flameBaseY = cy - h * 0.05f

        val flamePath = Path().apply {
            moveTo(cx, flameTopY)
            cubicTo(
                cx + w * 0.18f, cy - h * 0.20f,
                cx + w * 0.14f, flameBaseY,
                cx, flameBaseY
            )
            cubicTo(
                cx - w * 0.14f, flameBaseY,
                cx - w * 0.18f, cy - h * 0.20f,
                cx, flameTopY
            )
            close()
        }

        // Outer Flame (Warm Saffron / Orange)
        drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6D00), Color(0xFFD50000)),
                startY = flameTopY,
                endY = flameBaseY
            )
        )

        // Inner Flame Core (Brilliant White/Yellow)
        val coreHeight = flameHeight * 0.55f
        val coreTopY = flameBaseY - coreHeight
        val corePath = Path().apply {
            moveTo(cx, coreTopY)
            cubicTo(
                cx + w * 0.08f, flameBaseY - coreHeight * 0.4f,
                cx + w * 0.06f, flameBaseY,
                cx, flameBaseY
            )
            cubicTo(
                cx - w * 0.06f, flameBaseY,
                cx - w * 0.08f, flameBaseY - coreHeight * 0.4f,
                cx, coreTopY
            )
            close()
        }

        drawPath(
            path = corePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFFFFF59D)),
                startY = coreTopY,
                endY = flameBaseY
            )
        )
    }
}

/**
 * Decorative Marigold Flower Garland Banner Border.
 */
@Composable
fun MarigoldGarland(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val numFlowers = (w / 36f).toInt().coerceAtLeast(8)
        val step = w / numFlowers

        for (i in 0..numFlowers) {
            val cx = i * step
            val cy = h * 0.5f + (Math.sin(i * 0.7).toFloat() * (h * 0.2f))
            val isOrange = i % 2 == 0
            val flowerColor1 = if (isOrange) SaffronPrimary else GoldAccent
            val flowerColor2 = if (isOrange) SaffronDark else GoldDark

            // Marigold Petal Clusters
            for (p in 0 until 8) {
                val angle = p * (Math.PI / 4).toFloat()
                val px = cx + (Math.cos(angle.toDouble()) * 8f).toFloat()
                val py = cy + (Math.sin(angle.toDouble()) * 8f).toFloat()
                drawCircle(
                    color = flowerColor1,
                    center = Offset(px, py),
                    radius = 7.5f
                )
            }
            // Flower Center
            drawCircle(
                color = flowerColor2,
                center = Offset(cx, cy),
                radius = 6f
            )
            // Golden bead
            drawCircle(
                color = Color(0xFFFFF9C4),
                center = Offset(cx, cy),
                radius = 2.5f
            )
        }
    }
}

@Composable
fun ConfettiOverlay(
    trigger: Any? = Unit,
    modifier: Modifier = Modifier
) {
    ParticleConfetti(
        trigger = trigger,
        modifier = modifier.fillMaxSize(),
        particleCount = 160,
        isCenterBurst = true,
        isDualCannons = true,
        isRainCascade = true
    )
}

