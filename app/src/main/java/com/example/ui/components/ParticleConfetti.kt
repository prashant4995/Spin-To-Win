package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shape types for diverse, festive celebration confetti particles.
 */
enum class ConfettiShape {
    RECTANGLE,
    RIBBON,
    CIRCLE,
    STAR,
    DIAMOND,
    SPARKLE,
    COIN,
    PETAL
}

/**
 * Individual physics particle for Canvas confetti animation.
 */
private class PhysicsParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val gravity: Float,
    val drag: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    var flipAngle: Float,
    val flipSpeed: Float,
    val shape: ConfettiShape,
    val color: Color,
    val width: Float,
    val height: Float,
    var alpha: Float = 1f,
    val wobbleFreq: Float,
    val wobbleAmp: Float,
    var wobblePhase: Float,
    var ageMs: Long = 0L,
    val maxAgeMs: Long,
    val delayMs: Long = 0L
) {
    val isStarted: Boolean get() = ageMs >= delayMs
    val isAlive: Boolean get() = (ageMs - delayMs) < maxAgeMs && alpha > 0.01f

    fun update(dtSec: Float, dtMs: Long) {
        ageMs += dtMs
        if (!isStarted) return

        // Aerodynamic drag
        vx *= (1f - (1f - drag) * dtSec * 60f).coerceIn(0.80f, 1f)
        vy *= (1f - (1f - drag) * dtSec * 60f).coerceIn(0.80f, 1f)

        // Gravity
        vy += gravity * dtSec

        // Horizontal sinusoidal wobble for fluttering festive paper & petal physics
        wobblePhase += wobbleFreq * dtSec * 2f * PI.toFloat()
        val wobbleOffset = sin(wobblePhase) * wobbleAmp * dtSec * 60f

        x += (vx + wobbleOffset) * dtSec
        y += vy * dtSec

        // 2D Rotation & 3D Flip (tumbling paper effect)
        rotation = (rotation + rotationSpeed * dtSec) % 360f
        flipAngle = (flipAngle + flipSpeed * dtSec) % (2f * PI.toFloat())

        // Smooth fade out in the last 30% of lifetime
        val activeAge = ageMs - delayMs
        val fadeStart = maxAgeMs * 0.70f
        if (activeAge > fadeStart) {
            val progress = (activeAge - fadeStart) / (maxAgeMs - fadeStart).toFloat()
            alpha = (1f - progress).coerceIn(0f, 1f)
        }
    }
}

/**
 * High-performance, celebratory confetti animation rendered via Compose Canvas.
 * Generates an exuberant multi-wave celebration featuring:
 * - High-velocity dual-angled cannons from bottom corners
 * - Radial golden starburst from wheel center/pointer
 * - Fluttering golden marigold petals and shimmering ribbons raining down
 *
 * @param trigger Trigger key (e.g. timestamp or win boolean). Re-triggers when changed.
 * @param particleCount Total number of emitted confetti particles.
 * @param isCenterBurst If true, emits an explosive radial burst from the wheel center.
 * @param isDualCannons If true, fires criss-crossing angled bursts from bottom-left & bottom-right corners.
 * @param isRainCascade If true, cascades sparkling festive stars, marigold petals and ribbons from above.
 * @param onAnimationFinished Optional callback invoked when all particles have faded.
 */
@Composable
fun ParticleConfetti(
    trigger: Any? = Unit,
    modifier: Modifier = Modifier,
    particleCount: Int = 180,
    isCenterBurst: Boolean = true,
    isDualCannons: Boolean = true,
    isRainCascade: Boolean = true,
    onAnimationFinished: (() -> Unit)? = null
) {
    var particles by remember(trigger) { mutableStateOf<List<PhysicsParticle>>(emptyList()) }
    var isInitialized by remember(trigger) { mutableStateOf(false) }
    var frameClock by remember(trigger) { mutableLongStateOf(0L) }

    // Pre-allocated Shape Paths
    val starPath = remember { Path() }
    val diamondPath = remember { Path() }
    val petalPath = remember { Path() }

    LaunchedEffect(trigger) {
        if (trigger == null || trigger == false) {
            particles = emptyList()
            isInitialized = false
            return@LaunchedEffect
        }

        var lastFrameTime = 0L
        while (true) {
            withFrameMillis { frameTime ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTime
                    frameClock = frameTime
                    return@withFrameMillis
                }

                val dtMs = (frameTime - lastFrameTime).coerceIn(1L, 50L)
                val dtSec = dtMs / 1000f
                lastFrameTime = frameTime
                frameClock = frameTime

                if (particles.isNotEmpty()) {
                    var anyAlive = false
                    for (i in particles.indices) {
                        val p = particles[i]
                        p.update(dtSec, dtMs)
                        if (p.isAlive) {
                            anyAlive = true
                        }
                    }

                    if (!anyAlive && isInitialized) {
                        particles = emptyList()
                        onAnimationFinished?.invoke()
                    }
                }
            }
        }
    }

    val festiveColors = remember {
        listOf(
            GoldAccent,
            GoldLight,
            GoldDark,
            SaffronPrimary,
            SaffronDark,
            MaroonRoyal,
            Color(0xFFFFD700), // Pure Gold
            Color(0xFFFFC107), // Amber
            Color(0xFFFF9100), // Bright Saffron
            Color(0xFFFF1744), // Vermilion Red
            Color(0xFFFF4081), // Festive Pink
            Color(0xFF00E676), // Emerald Green
            Color(0xFF1DE9B6), // Mint Sparkle
            Color(0xFF00E5FF), // Cyan Sparkle
            Color(0xFFD500F9), // Royal Magenta
            Color(0xFF7C4DFF), // Deep Violet
            Color(0xFFFFF9C4), // Golden Shimmer
            Color(0xFFFFFFFF)  // Brilliant White
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Reading frameClock ensures Compose redraws every frame seamlessly
        val tick = frameClock
        val w = size.width
        val h = size.height

        // Initialize particles on first draw frame with measured canvas dimensions
        if (!isInitialized && trigger != null && trigger != false && w > 0 && h > 0) {
            val list = ArrayList<PhysicsParticle>(particleCount + 60)

            // 1. Dual Bottom Corner Cannons (Fires high velocity criss-crossing arcs across the screen)
            if (isDualCannons) {
                val cannonCount = (particleCount * 0.45f).toInt()
                for (i in 0 until cannonCount) {
                    val fromLeft = i % 2 == 0
                    val originX = if (fromLeft) w * 0.04f else w * 0.96f
                    val originY = h * 0.94f

                    // Left cannon shoots 50°-75° (up-right), Right shoots 105°-130° (up-left)
                    val baseAngleDeg = if (fromLeft) -62f else -118f
                    val angleSpread = Random.nextFloat() * 32f - 16f
                    val rad = (baseAngleDeg + angleSpread) * (PI.toFloat() / 180f)

                    val speed = (h * 0.95f + Random.nextFloat() * (h * 0.75f))
                    val vx = cos(rad) * speed
                    val vy = sin(rad) * speed

                    val pShape = when (Random.nextInt(7)) {
                        0 -> ConfettiShape.STAR
                        1 -> ConfettiShape.SPARKLE
                        2 -> ConfettiShape.RIBBON
                        3 -> ConfettiShape.PETAL
                        4 -> ConfettiShape.DIAMOND
                        5 -> ConfettiShape.COIN
                        else -> ConfettiShape.RECTANGLE
                    }

                    val baseSize = (14f + Random.nextFloat() * 18f)
                    val delay = (i % 6) * 35L

                    list.add(
                        PhysicsParticle(
                            x = originX,
                            y = originY,
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.88f,
                            drag = 0.985f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 600f - 300f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 16f - 8f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = when (pShape) {
                                ConfettiShape.RECTANGLE -> baseSize * (1.8f + Random.nextFloat() * 0.8f)
                                ConfettiShape.RIBBON -> baseSize * 3.2f
                                else -> baseSize
                            },
                            wobbleFreq = 1.6f + Random.nextFloat() * 3.4f,
                            wobbleAmp = 28f + Random.nextFloat() * 45f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (4000L + Random.nextLong(1800L)),
                            delayMs = delay
                        )
                    )
                }
            }

            // 2. Center Radial Burst (Wheel Center Explosion)
            if (isCenterBurst) {
                val centerCount = (particleCount * 0.35f).toInt()
                val cx = w * 0.5f
                val cy = h * 0.40f

                for (i in 0 until centerCount) {
                    val angle = Random.nextFloat() * (2f * PI.toFloat())
                    val speed = (w * 0.40f + Random.nextFloat() * (w * 0.85f))
                    val vx = cos(angle) * speed
                    val vy = sin(angle) * speed - (h * 0.28f) // Upward bias

                    val pShape = when (Random.nextInt(6)) {
                        0 -> ConfettiShape.STAR
                        1 -> ConfettiShape.COIN
                        2 -> ConfettiShape.PETAL
                        3 -> ConfettiShape.SPARKLE
                        4 -> ConfettiShape.DIAMOND
                        else -> ConfettiShape.RECTANGLE
                    }

                    val baseSize = (13f + Random.nextFloat() * 16f)
                    val delay = (i % 4) * 25L

                    list.add(
                        PhysicsParticle(
                            x = cx + (Random.nextFloat() * 30f - 15f),
                            y = cy + (Random.nextFloat() * 30f - 15f),
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.78f,
                            drag = 0.980f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 640f - 320f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 18f - 9f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = if (pShape == ConfettiShape.RECTANGLE) baseSize * 2.2f else baseSize,
                            wobbleFreq = 2f + Random.nextFloat() * 4f,
                            wobbleAmp = 22f + Random.nextFloat() * 40f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (3600L + Random.nextLong(1600L)),
                            delayMs = delay
                        )
                    )
                }
            }

            // 3. Sky Cascade Rainfall & Fluttering Petals
            if (isRainCascade) {
                val rainCount = (particleCount * 0.30f).toInt()
                for (i in 0 until rainCount) {
                    val startX = Random.nextFloat() * w
                    val startY = -Random.nextFloat() * (h * 0.40f)
                    val vx = Random.nextFloat() * 90f - 45f
                    val vy = (h * 0.18f + Random.nextFloat() * (h * 0.38f))

                    val pShape = when (Random.nextInt(5)) {
                        0 -> ConfettiShape.PETAL
                        1 -> ConfettiShape.STAR
                        2 -> ConfettiShape.SPARKLE
                        3 -> ConfettiShape.RIBBON
                        else -> ConfettiShape.RECTANGLE
                    }
                    val baseSize = (11f + Random.nextFloat() * 15f)
                    val delay = 200L + Random.nextLong(800L) // Staggered rain

                    list.add(
                        PhysicsParticle(
                            x = startX,
                            y = startY,
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.48f,
                            drag = 0.992f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 400f - 200f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 12f - 6f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = when (pShape) {
                                ConfettiShape.RECTANGLE -> baseSize * 2.4f
                                ConfettiShape.RIBBON -> baseSize * 3.5f
                                else -> baseSize
                            },
                            wobbleFreq = 1.2f + Random.nextFloat() * 2.8f,
                            wobbleAmp = 35f + Random.nextFloat() * 55f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (4600L + Random.nextLong(2000L)),
                            delayMs = delay
                        )
                    )
                }
            }

            particles = list
            isInitialized = true
        }

        // Draw all currently alive particles
        for (i in particles.indices) {
            val p = particles[i]
            if (!p.isStarted || !p.isAlive) continue

            // 3D paper / ribbon flipping scale factor: cos(flipAngle)
            val scaleX = cos(p.flipAngle).let { if (abs(it) < 0.04f) 0.04f else it }
            val center = Offset(p.x, p.y)

            rotate(degrees = p.rotation, pivot = center) {
                scale(scaleX = scaleX, scaleY = 1f, pivot = center) {
                    val pColor = p.color.copy(alpha = p.color.alpha * p.alpha)

                    when (p.shape) {
                        ConfettiShape.RECTANGLE, ConfettiShape.RIBBON -> {
                            drawRect(
                                color = pColor,
                                topLeft = Offset(p.x - p.width / 2f, p.y - p.height / 2f),
                                size = Size(p.width, p.height)
                            )
                        }

                        ConfettiShape.CIRCLE, ConfettiShape.COIN -> {
                            drawCircle(
                                color = pColor,
                                radius = p.width / 2f,
                                center = center
                            )
                            if (p.shape == ConfettiShape.COIN) {
                                drawCircle(
                                    color = Color(0xFFFFF9C4).copy(alpha = p.alpha * 0.8f),
                                    radius = p.width * 0.35f,
                                    center = center
                                )
                            }
                        }

                        ConfettiShape.STAR -> {
                            drawStar(
                                path = starPath,
                                center = center,
                                outerRadius = p.width * 0.70f,
                                innerRadius = p.width * 0.30f,
                                color = pColor
                            )
                        }

                        ConfettiShape.DIAMOND -> {
                            drawDiamond(
                                path = diamondPath,
                                center = center,
                                width = p.width,
                                height = p.height * 1.35f,
                                color = pColor
                            )
                        }

                        ConfettiShape.PETAL -> {
                            drawPetal(
                                path = petalPath,
                                center = center,
                                width = p.width,
                                height = p.height * 1.6f,
                                color = pColor
                            )
                        }

                        ConfettiShape.SPARKLE -> {
                            drawSparkle(
                                center = center,
                                radius = p.width * 0.75f,
                                color = pColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a festive Marigold petal onto Canvas.
 */
private fun DrawScope.drawPetal(
    path: Path,
    center: Offset,
    width: Float,
    height: Float,
    color: Color
) {
    path.reset()
    val topY = center.y - height / 2f
    val bottomY = center.y + height / 2f
    val halfW = width / 2f

    path.moveTo(center.x, topY)
    path.cubicTo(
        center.x + halfW * 1.2f, center.y - height * 0.15f,
        center.x + halfW * 0.8f, bottomY - height * 0.2f,
        center.x, bottomY
    )
    path.cubicTo(
        center.x - halfW * 0.8f, bottomY - height * 0.2f,
        center.x - halfW * 1.2f, center.y - height * 0.15f,
        center.x, topY
    )
    path.close()
    drawPath(path = path, color = color)
}

/**
 * Draws a crisp 5-pointed star onto Canvas.
 */
private fun DrawScope.drawStar(
    path: Path,
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    path.reset()
    val numPoints = 5
    val angleStep = (PI / numPoints).toFloat()
    var angle = -PI.toFloat() / 2f

    for (i in 0 until (numPoints * 2)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += angleStep
    }
    path.close()
    drawPath(path = path, color = color)
}

/**
 * Draws a diamond particle.
 */
private fun DrawScope.drawDiamond(
    path: Path,
    center: Offset,
    width: Float,
    height: Float,
    color: Color
) {
    path.reset()
    path.moveTo(center.x, center.y - height / 2f)
    path.lineTo(center.x + width / 2f, center.y)
    path.lineTo(center.x, center.y + height / 2f)
    path.lineTo(center.x - width / 2f, center.y)
    path.close()
    drawPath(path = path, color = color)
}

/**
 * Draws a glowing 8-axis star sparkle particle.
 */
private fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    // Primary vertical/horizontal rays
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = radius * 0.30f
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = radius * 0.30f
    )
    // Diagonal glints
    val diag = radius * 0.58f
    drawLine(
        color = color.copy(alpha = color.alpha * 0.75f),
        start = Offset(center.x - diag, center.y - diag),
        end = Offset(center.x + diag, center.y + diag),
        strokeWidth = radius * 0.20f
    )
    drawLine(
        color = color.copy(alpha = color.alpha * 0.75f),
        start = Offset(center.x - diag, center.y + diag),
        end = Offset(center.x + diag, center.y - diag),
        strokeWidth = radius * 0.20f
    )
    // Central core
    drawCircle(
        color = Color.White.copy(alpha = color.alpha),
        center = center,
        radius = radius * 0.28f
    )
}
