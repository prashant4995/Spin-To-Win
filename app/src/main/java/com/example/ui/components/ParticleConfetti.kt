package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
 * Shape types for diverse, festive confetti particles.
 */
enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    STAR,
    DIAMOND,
    SPARKLE,
    COIN
}

/**
 * Individual physics particle for Canvas animation.
 */
private data class PhysicsParticle(
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
    val maxAgeMs: Long
) {
    val isAlive: Boolean get() = ageMs < maxAgeMs && alpha > 0.01f

    fun update(dtSec: Float, dtMs: Long) {
        ageMs += dtMs

        // Aerodynamic drag
        vx *= (1f - (1f - drag) * dtSec * 60f).coerceIn(0.85f, 1f)
        vy *= (1f - (1f - drag) * dtSec * 60f).coerceIn(0.85f, 1f)

        // Gravity
        vy += gravity * dtSec

        // Horizontal sinusoidal wobble for fluttering paper physics
        wobblePhase += wobbleFreq * dtSec * 2f * PI.toFloat()
        val wobbleOffset = sin(wobblePhase) * wobbleAmp * dtSec * 60f

        x += (vx + wobbleOffset) * dtSec
        y += vy * dtSec

        // 2D Rotation & 3D Flip (tumbling paper effect)
        rotation = (rotation + rotationSpeed * dtSec) % 360f
        flipAngle = (flipAngle + flipSpeed * dtSec) % (2f * PI.toFloat())

        // Fade out in the last 25% of lifetime
        val fadeStart = maxAgeMs * 0.70f
        if (ageMs > fadeStart) {
            val progress = (ageMs - fadeStart) / (maxAgeMs - fadeStart).toFloat()
            alpha = (1f - progress).coerceIn(0f, 1f)
        }
    }
}

/**
 * High-performance, physics-driven particle confetti animation rendered via Compose Canvas.
 * Automatically triggers celebratory dual-cannons, center explosions, and shimmering rainfall.
 *
 * @param trigger Trigger key (e.g. win state, timestamp, or boolean). Re-triggers when changed to true or non-null.
 * @param particleCount Total number of emitted confetti particles.
 * @param isCenterBurst If true, adds an explosive radial burst from the screen/wheel center.
 * @param isDualCannons If true, fires high-velocity angled bursts from bottom-left & bottom-right corners.
 * @param isRainCascade If true, rains sparkling festive stars and ribbons from top.
 * @param onAnimationFinished Optional callback invoked when all particles have faded.
 */
@Composable
fun ParticleConfetti(
    trigger: Any? = Unit,
    modifier: Modifier = Modifier,
    particleCount: Int = 140,
    isCenterBurst: Boolean = true,
    isDualCannons: Boolean = true,
    isRainCascade: Boolean = true,
    onAnimationFinished: (() -> Unit)? = null
) {
    var particles by remember(trigger) { mutableStateOf<List<PhysicsParticle>>(emptyList()) }
    var isInitialized by remember(trigger) { mutableStateOf(false) }

    // Pre-allocated Star Path cache
    val starPath = remember { Path() }
    val diamondPath = remember { Path() }

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
                    return@withFrameMillis
                }

                val dtMs = (frameTime - lastFrameTime).coerceIn(1L, 64L)
                val dtSec = dtMs / 1000f
                lastFrameTime = frameTime

                if (particles.isNotEmpty()) {
                    particles.forEach { p ->
                        p.update(dtSec, dtMs)
                    }
                    // Filter out dead particles
                    val alive = particles.filter { it.isAlive }
                    if (alive.isEmpty() && isInitialized) {
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
            Color(0xFFFFD54F), // Amber Gold
            Color(0xFF00E676), // Emerald Green
            Color(0xFF00C853), // Deep Emerald
            Color(0xFFFF1744), // Ruby Red
            Color(0xFFFF4081), // Festive Pink
            Color(0xFF00E5FF), // Cyan Sparkle
            Color(0xFF7C4DFF), // Royal Purple
            Color(0xFFFFF59D)  // Golden Shimmer
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Initialize particles on first layout frame if not already done
        if (!isInitialized && trigger != null && trigger != false && w > 0 && h > 0) {
            val list = mutableListOf<PhysicsParticle>()

            // 1. Dual Bottom Corner Cannons (high velocity upward blasts)
            if (isDualCannons) {
                val cannonCount = (particleCount * 0.40f).toInt()
                for (i in 0 until cannonCount) {
                    val fromLeft = i % 2 == 0
                    val originX = if (fromLeft) w * 0.05f else w * 0.95f
                    val originY = h * 0.92f

                    // Angle: Left cannon shoots 45°-75° (up-right), Right shoots 105°-135° (up-left)
                    val baseAngleDeg = if (fromLeft) -60f else -120f
                    val angleSpread = Random.nextFloat() * 34f - 17f
                    val rad = (baseAngleDeg + angleSpread) * (PI.toFloat() / 180f)

                    val speed = (h * 0.85f + Random.nextFloat() * (h * 0.65f))
                    val vx = cos(rad) * speed
                    val vy = sin(rad) * speed

                    val pShape = when (Random.nextInt(5)) {
                        0 -> ConfettiShape.STAR
                        1 -> ConfettiShape.SPARKLE
                        2 -> ConfettiShape.CIRCLE
                        3 -> ConfettiShape.DIAMOND
                        else -> ConfettiShape.RECTANGLE
                    }

                    val baseSize = (14f + Random.nextFloat() * 18f)
                    list.add(
                        PhysicsParticle(
                            x = originX,
                            y = originY,
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.82f,
                            drag = 0.982f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 540f - 270f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 14f - 7f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = if (pShape == ConfettiShape.RECTANGLE) baseSize * (1.6f + Random.nextFloat() * 0.8f) else baseSize,
                            wobbleFreq = 1.5f + Random.nextFloat() * 3.5f,
                            wobbleAmp = 25f + Random.nextFloat() * 45f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (3800L + Random.nextLong(1800L))
                        )
                    )
                }
            }

            // 2. Center Radial Burst (Wheel Center Explosion)
            if (isCenterBurst) {
                val centerCount = (particleCount * 0.35f).toInt()
                val cx = w * 0.5f
                val cy = h * 0.42f // Centered on wheel hub

                for (i in 0 until centerCount) {
                    val angle = Random.nextFloat() * (2f * PI.toFloat())
                    val speed = (w * 0.35f + Random.nextFloat() * (w * 0.75f))
                    val vx = cos(angle) * speed
                    val vy = sin(angle) * speed - (h * 0.25f) // slight upward bias

                    val pShape = when (Random.nextInt(6)) {
                        0 -> ConfettiShape.STAR
                        1 -> ConfettiShape.COIN
                        2 -> ConfettiShape.DIAMOND
                        3 -> ConfettiShape.SPARKLE
                        4 -> ConfettiShape.CIRCLE
                        else -> ConfettiShape.RECTANGLE
                    }

                    val baseSize = (12f + Random.nextFloat() * 16f)
                    list.add(
                        PhysicsParticle(
                            x = cx + (Random.nextFloat() * 40f - 20f),
                            y = cy + (Random.nextFloat() * 40f - 20f),
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.75f,
                            drag = 0.978f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 600f - 300f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 16f - 8f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = if (pShape == ConfettiShape.RECTANGLE) baseSize * 2f else baseSize,
                            wobbleFreq = 2f + Random.nextFloat() * 4f,
                            wobbleAmp = 20f + Random.nextFloat() * 40f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (3400L + Random.nextLong(1600L))
                        )
                    )
                }
            }

            // 3. Sky Cascade Rainfall (Golden shimmer from above)
            if (isRainCascade) {
                val rainCount = (particleCount * 0.25f).toInt()
                for (i in 0 until rainCount) {
                    val startX = Random.nextFloat() * w
                    val startY = -Random.nextFloat() * (h * 0.35f)
                    val vx = Random.nextFloat() * 80f - 40f
                    val vy = (h * 0.15f + Random.nextFloat() * (h * 0.35f))

                    val pShape = if (Random.nextBoolean()) ConfettiShape.STAR else ConfettiShape.RECTANGLE
                    val baseSize = (10f + Random.nextFloat() * 14f)

                    list.add(
                        PhysicsParticle(
                            x = startX,
                            y = startY,
                            vx = vx,
                            vy = vy,
                            gravity = h * 0.45f,
                            drag = 0.99f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 360f - 180f,
                            flipAngle = Random.nextFloat() * (2f * PI.toFloat()),
                            flipSpeed = Random.nextFloat() * 10f - 5f,
                            shape = pShape,
                            color = festiveColors.random(),
                            width = baseSize,
                            height = if (pShape == ConfettiShape.RECTANGLE) baseSize * 2.2f else baseSize,
                            wobbleFreq = 1f + Random.nextFloat() * 2f,
                            wobbleAmp = 30f + Random.nextFloat() * 50f,
                            wobblePhase = Random.nextFloat() * (2f * PI.toFloat()),
                            maxAgeMs = (4200L + Random.nextLong(1800L))
                        )
                    )
                }
            }

            particles = list
            isInitialized = true
        }

        // Draw all living particles
        for (p in particles) {
            if (!p.isAlive) continue

            // 3D paper flipping scale factor: cos(flipAngle)
            val scaleX = cos(p.flipAngle).let { if (abs(it) < 0.05f) 0.05f else it }
            val center = Offset(p.x, p.y)

            rotate(degrees = p.rotation, pivot = center) {
                scale(scaleX = scaleX, scaleY = 1f, pivot = center) {
                    val pColor = p.color.copy(alpha = p.color.alpha * p.alpha)

                    when (p.shape) {
                        ConfettiShape.RECTANGLE -> {
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
                                // Inner coin groove
                                drawCircle(
                                    color = Color(0xFFFFF9C4).copy(alpha = p.alpha * 0.7f),
                                    radius = p.width * 0.35f,
                                    center = center
                                )
                            }
                        }

                        ConfettiShape.STAR -> {
                            drawStar(
                                path = starPath,
                                center = center,
                                outerRadius = p.width * 0.65f,
                                innerRadius = p.width * 0.28f,
                                color = pColor
                            )
                        }

                        ConfettiShape.DIAMOND -> {
                            drawDiamond(
                                path = diamondPath,
                                center = center,
                                width = p.width,
                                height = p.height * 1.3f,
                                color = pColor
                            )
                        }

                        ConfettiShape.SPARKLE -> {
                            drawSparkle(
                                center = center,
                                radius = p.width * 0.7f,
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
 * Helper to draw a crisp 5-pointed star onto Canvas.
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
 * Helper to draw a diamond/rhombus particle.
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
 * Helper to draw an 8-axis brilliant sparkle particle.
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
        strokeWidth = radius * 0.28f
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = radius * 0.28f
    )
    // Diagonal subtle glints
    val diag = radius * 0.55f
    drawLine(
        color = color.copy(alpha = color.alpha * 0.75f),
        start = Offset(center.x - diag, center.y - diag),
        end = Offset(center.x + diag, center.y + diag),
        strokeWidth = radius * 0.18f
    )
    drawLine(
        color = color.copy(alpha = color.alpha * 0.75f),
        start = Offset(center.x - diag, center.y + diag),
        end = Offset(center.x + diag, center.y - diag),
        strokeWidth = radius * 0.18f
    )
    // Glowing central core
    drawCircle(
        color = Color.White.copy(alpha = color.alpha),
        center = center,
        radius = radius * 0.25f
    )
}
