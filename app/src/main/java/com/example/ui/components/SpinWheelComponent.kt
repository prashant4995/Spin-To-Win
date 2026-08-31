package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.WheelSector
import com.example.ui.animation.WheelPhysicsEngine
import com.example.ui.animation.WheelSpinPhase
import com.example.ui.theme.ArtisticAmberDeep
import com.example.ui.theme.ArtisticAmberGlow
import com.example.ui.theme.ArtisticAmberGold
import com.example.ui.theme.ArtisticCream
import com.example.ui.theme.ArtisticMaroonBg
import com.example.ui.theme.ArtisticMaroonDark
import com.example.ui.theme.ArtisticMaroonSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.MaroonDeep
import com.example.ui.theme.MaroonRoyal
import com.example.ui.theme.TextGold

@Composable
fun LuckyWheel(
    selectedDish: Dish?,
    sectors: List<WheelSector>,
    currentRotationAngle: Float,
    isSpinning: Boolean,
    pointerDeflectionAngle: Float = 0f,
    phase: WheelSpinPhase = WheelSpinPhase.IDLE,
    angularVelocity: Float = 0f,
    landedSectorIndex: Int = 0,
    landingPulseAlpha: Float = 0f,
    onSpinClick: () -> Unit,
    onSpinComplete: (finalAngle: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Blinking lights around wheel rim
    val infiniteTransition = rememberInfiniteTransition(label = "rim_lights")
    val lightPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "light_pulse"
    )

    // Dynamic pointer needle angle directly from physics engine deflection
    val pointerJiggle = if (isSpinning) {
        pointerDeflectionAngle
    } else {
        0f
    }

    // Dynamic speed blur factor
    val speedNorm = (angularVelocity / 1500f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp)
            .graphicsLayer {
                // Realistic 3D Tilt perspective
                rotationX = 14f
                cameraDistance = 24f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Ambient Gold Radiant Aura & Deep 3D Table Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxRadius = minOf(cx, cy)

            // Deep 3D Cast Shadow (projected slightly down and elongated)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xBB000000),
                        Color(0x66000000),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy + 28f),
                    radius = maxRadius * 1.05f
                ),
                topLeft = Offset(cx - maxRadius * 1.02f, cy - maxRadius * 0.90f + 26f),
                size = Size(maxRadius * 2.04f, maxRadius * 1.95f)
            )

            // Radiant Festive Golden Aura (intensifies with velocity or landing pulse)
            val auraAlpha = 0.22f + (speedNorm * 0.25f) + (landingPulseAlpha * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ArtisticAmberGlow.copy(alpha = auraAlpha.coerceIn(0f, 0.7f)),
                        ArtisticAmberGold.copy(alpha = (auraAlpha * 0.45f).coerceIn(0f, 0.4f)),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = maxRadius * (1.12f + landingPulseAlpha * 0.08f)
                ),
                center = Offset(cx, cy),
                radius = maxRadius * (1.12f + landingPulseAlpha * 0.08f)
            )
        }

        // 2. Layered 3D Turntable Base (Extrusion Cylinder & Chamfer Bevels)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = minOf(cx, cy) * 0.96f

            // 3D Extrusion base layers (stepped downward to create vertical depth)
            val extrusionLayers = listOf(
                Pair(18f, Color(0xFF100101)),
                Pair(14f, Color(0xFF220303)),
                Pair(10f, Color(0xFF420808)),
                Pair(6f, Color(0xFF781808)),
                Pair(3f, ArtisticAmberDeep)
            )
            extrusionLayers.forEach { (offsetY, color) ->
                drawCircle(
                    color = color,
                    center = Offset(cx, cy + offsetY),
                    radius = radius
                )
            }

            // 3D Metallic Brass/Gold Outer Rim with Sweep Reflection
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ArtisticAmberGold,
                        Color(0xFFFFE082),
                        ArtisticAmberDeep,
                        ArtisticAmberGold,
                        Color(0xFFFFF8E1),
                        ArtisticAmberDeep,
                        ArtisticAmberGold
                    ),
                    center = Offset(cx, cy)
                ),
                center = Offset(cx, cy),
                radius = radius
            )

            // Outer Rim High-relief Bevel Groove (Dark Inset)
            drawCircle(
                color = ArtisticMaroonBg,
                center = Offset(cx, cy),
                radius = radius * 0.94f,
                style = Stroke(width = 7f)
            )

            // Inner Gold Ring Line
            drawCircle(
                color = ArtisticAmberGold,
                center = Offset(cx, cy),
                radius = radius * 0.92f,
                style = Stroke(width = 3.5f)
            )

            // 3D Carnival Rim Jewels / Rivet Studs (Spherical 3D shading)
            val numBulbs = 24
            val bulbOrbitRadius = radius * 0.958f
            for (i in 0 until numBulbs) {
                val angleRad = (i * (2 * Math.PI / numBulbs)).toFloat()
                val bx = cx + (Math.cos(angleRad.toDouble()) * bulbOrbitRadius).toFloat()
                val by = cy + (Math.sin(angleRad.toDouble()) * bulbOrbitRadius).toFloat()

                val isBulbActive = (i % 2 == 0 && lightPulse > 0.5f) || (i % 2 != 0 && lightPulse <= 0.5f) || (speedNorm > 0.6f)
                val bulbBaseColor = if (isBulbActive) ArtisticCream else ArtisticAmberDeep

                // 3D Stud Drop Shadow
                drawCircle(
                    color = Color(0x88000000),
                    center = Offset(bx + 1.2f, by + 1.5f),
                    radius = 4.5f
                )

                // Outer Active Glow
                if (isBulbActive) {
                    drawCircle(
                        color = ArtisticAmberGold.copy(alpha = 0.75f),
                        center = Offset(bx, by),
                        radius = 8.5f
                    )
                }

                // 3D Spherical Stud Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            bulbBaseColor,
                            ArtisticMaroonDark
                        ),
                        center = Offset(bx - 1f, by - 1f),
                        radius = 4.5f
                    ),
                    center = Offset(bx, by),
                    radius = 4f
                )

                // Specular Light Pinpoint
                drawCircle(
                    color = Color.White,
                    center = Offset(bx - 1.2f, by - 1.2f),
                    radius = 1.2f
                )
            }
        }

        // 3. Rotating 3D Concave Slices Disc
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
                .rotate(currentRotationAngle)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val wheelRadius = minOf(cx, cy)

            // Draw 4 Slices with radial gradient & 3D bevels
            sectors.forEachIndexed { index, sector ->
                val startAngle = sector.startAngleDeg
                val sweepAngle = sector.sweepAngleDeg
                val isTargetLandedSector = (phase == WheelSpinPhase.FINAL_LANDING || phase == WheelSpinPhase.LANDED) &&
                        index == landedSectorIndex

                // Sector Arc with 3D Center-to-Edge Shading
                val baseColor = sector.primaryColor
                val deepColor = sector.secondaryColor

                val gradientBrush = Brush.radialGradient(
                    colors = listOf(
                        if (isTargetLandedSector && sector.isWin) Color(0xFFFFD54F) else baseColor,
                        deepColor,
                        Color(0xFF1F0101) // Ambient occlusion near outer rim
                    ),
                    center = Offset(cx, cy),
                    radius = wheelRadius
                )

                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(cx - wheelRadius, cy - wheelRadius),
                    size = Size(wheelRadius * 2, wheelRadius * 2)
                )

                // Highlighting landed prize sector
                if (isTargetLandedSector) {
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ArtisticAmberGlow.copy(alpha = 0.45f + landingPulseAlpha * 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = wheelRadius
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(cx - wheelRadius, cy - wheelRadius),
                        size = Size(wheelRadius * 2, wheelRadius * 2)
                    )

                    // Radiant Golden Border around winning sector
                    drawArc(
                        color = Color(0xFFFFE082).copy(alpha = 0.7f + landingPulseAlpha * 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(cx - wheelRadius * 0.98f, cy - wheelRadius * 0.98f),
                        size = Size(wheelRadius * 1.96f, wheelRadius * 1.96f),
                        style = Stroke(width = 5f)
                    )
                }

                // 3D Golden Divider Spoke Lines (Double Stroke: shadow + highlight)
                val lineAngleRad = Math.toRadians(startAngle.toDouble())
                val cosA = Math.cos(lineAngleRad).toFloat()
                val sinA = Math.sin(lineAngleRad).toFloat()
                val lineEndX = cx + cosA * wheelRadius
                val lineEndY = cy + sinA * wheelRadius

                // Spoke Shadow
                drawLine(
                    color = Color(0x88000000),
                    start = Offset(cx + 1f, cy + 1f),
                    end = Offset(lineEndX + 1f, lineEndY + 1f),
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )
                // Spoke Gold Highlight
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(ArtisticAmberGold, Color(0xFFFFF9C4), ArtisticAmberDeep),
                        start = Offset(cx, cy),
                        end = Offset(lineEndX, lineEndY)
                    ),
                    start = Offset(cx, cy),
                    end = Offset(lineEndX, lineEndY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Draw 24 3D Metallic Boundary Pegs around wheel rim (15 degrees each)
            val numPins = WheelPhysicsEngine.NUM_PINS
            val pinOrbitRadius = wheelRadius * 0.93f
            for (p in 0 until numPins) {
                val pAngleDeg = p * WheelPhysicsEngine.PIN_SPACING_DEG
                val pAngleRad = Math.toRadians(pAngleDeg.toDouble())
                val pinX = cx + (Math.cos(pAngleRad) * pinOrbitRadius).toFloat()
                val pinY = cy + (Math.sin(pAngleRad) * pinOrbitRadius).toFloat()

                // Pin Shadow
                drawCircle(
                    color = Color(0x99000000),
                    center = Offset(pinX + 1.8f, pinY + 2.2f),
                    radius = 5.5f
                )
                // Pin 3D Metallic Head
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, ArtisticAmberGold, ArtisticAmberDeep),
                        center = Offset(pinX - 1.2f, pinY - 1.2f),
                        radius = 5.5f
                    ),
                    center = Offset(pinX, pinY),
                    radius = 4.8f
                )
            }

            // Draw Sector Text & Graphics inside Native Canvas
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                sectors.forEachIndexed { index, sector ->
                    val midAngleDeg = sector.startAngleDeg + sector.sweepAngleDeg / 2f
                    nativeCanvas.save()
                    nativeCanvas.rotate(midAngleDeg, cx, cy)

                    val isWin = sector.type == SectorType.WIN
                    val isTargetLanded = (phase == WheelSpinPhase.FINAL_LANDING || phase == WheelSpinPhase.LANDED) &&
                            index == landedSectorIndex

                    val titleTextSize = (wheelRadius * 0.10f).coerceIn(24f, 40f)
                    val titleColor = if (isWin || isTargetLanded) GoldLight.toArgb() else Color.White.toArgb()

                    val titlePaint = Paint().apply {
                        color = titleColor
                        textSize = titleTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val shadowPaint = Paint().apply {
                        color = Color(0xDD000000).toArgb()
                        textSize = titleTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val primaryLabel = sector.primaryLabel
                    val subLabel = sector.subLabel(selectedDish)
                    val emoji = sector.emoji

                    val emojiPaint = Paint().apply {
                        textSize = (wheelRadius * 0.12f).coerceIn(28f, 46f)
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val textRadius = cx + wheelRadius * 0.52f

                    // Draw Emoji
                    nativeCanvas.drawText(
                        emoji,
                        textRadius + wheelRadius * 0.18f,
                        cy + (emojiPaint.textSize * 0.35f),
                        emojiPaint
                    )

                    // Draw Primary Text with Clean Drop Shadow
                    val textX = textRadius - wheelRadius * 0.02f
                    val textY = cy - 12f

                    nativeCanvas.drawText(primaryLabel, textX + 2f, textY + 2f, shadowPaint)
                    nativeCanvas.drawText(primaryLabel, textX, textY, titlePaint)

                    // Draw Dish Name or Subtitle with Drop Shadow
                    val subTextSize = (wheelRadius * 0.082f).coerceIn(18f, 30f)
                    val subTitlePaint = Paint().apply {
                        color = Color(0xFFFFF9C4).toArgb()
                        textSize = subTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val subShadowPaint = Paint().apply {
                        color = Color(0xCC000000).toArgb()
                        textSize = subTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val subY = cy + subTextSize + 2f
                    nativeCanvas.drawText(subLabel, textX + 1.5f, subY + 1.5f, subShadowPaint)
                    nativeCanvas.drawText(subLabel, textX, subY, subTitlePaint)

                    nativeCanvas.restore()
                }
            }

            // 3D Inner Wheel Shadow Vignette (Gives deep bowl / turntable dish appearance)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0x66000000)
                    ),
                    center = Offset(cx, cy),
                    radius = wheelRadius
                ),
                center = Offset(cx, cy),
                radius = wheelRadius
            )

            // Inner Wheel Center Rim
            drawCircle(
                color = ArtisticAmberGold,
                center = Offset(cx, cy),
                radius = wheelRadius * 0.28f,
                style = Stroke(width = 4f)
            )
        }

        // 4. 3D Specular Light Sheen Overlay (Gloss reflection from top-left)
        Canvas(modifier = Modifier.fillMaxSize().padding(22.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = minOf(cx, cy)

            // Top-left glossy crescent shine
            val sheenPath = Path().apply {
                moveTo(cx - r * 0.7f, cy - r * 0.7f)
                cubicTo(
                    cx - r * 0.2f, cy - r * 0.95f,
                    cx + r * 0.2f, cy - r * 0.95f,
                    cx + r * 0.7f, cy - r * 0.7f
                )
                cubicTo(
                    cx + r * 0.3f, cy - r * 0.5f,
                    cx - r * 0.3f, cy - r * 0.5f,
                    cx - r * 0.7f, cy - r * 0.7f
                )
                close()
            }
            drawPath(
                path = sheenPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f + speedNorm * 0.12f),
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                )
            )
        }

        // 5. 3D Tiered Center Hub / Tactile Button (Gold & Maroon with 3D Depth)
        val centerHubSize = 92.dp
        val centerLabel = when (phase) {
            WheelSpinPhase.IDLE -> "SPIN"
            WheelSpinPhase.ACCELERATION -> "ACCEL..."
            WheelSpinPhase.DECELERATION -> "SPINNING"
            WheelSpinPhase.FINAL_LANDING -> "LANDING!"
            WheelSpinPhase.LANDED -> if (sectors.getOrNull(landedSectorIndex)?.isWin == true) "JACKPOT!" else "STOPPED"
        }

        Box(
            modifier = Modifier
                .size(centerHubSize)
                .shadow(24.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFE082),
                            ArtisticAmberGold,
                            ArtisticAmberDeep,
                            Color(0xFF4A2500)
                        )
                    )
                )
                .clickable(
                    enabled = !isSpinning,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onSpinClick()
                }
                .testTag("center_spin_hub"),
            contentAlignment = Alignment.Center
        ) {
            // Tier 2: Raised Inner Gold Bevel Ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ArtisticMaroonSurface,
                                ArtisticMaroonBg,
                                Color(0xFF180101)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerLabel,
                        color = ArtisticAmberGold,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isSpinning) 10.sp else 15.sp,
                        letterSpacing = 1.6.sp
                    )
                    // 3D Center Golden Pin
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White, ArtisticAmberGold, ArtisticAmberDeep)
                                )
                            )
                    )
                }
            }
        }

        // 6. 3D Suspended Ticker Needle (Cast 3D Drop Shadow & Metallic Pointer with Dynamic Physics Deflection)
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 44.dp, height = 50.dp)
                .rotate(pointerJiggle)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // 3D Cast Shadow (projected downwards onto slanted wheel)
            val shadowPath = Path().apply {
                moveTo(cx + 3f, h + 4f)
                lineTo(3f, 4f)
                lineTo(w + 3f, 4f)
                close()
            }
            drawPath(shadowPath, Color(0xAA000000))

            // 3D Metallic Amber Pointer Triangle
            val needlePath = Path().apply {
                moveTo(cx, h - 2f)
                lineTo(2f, 2f)
                lineTo(w - 2f, 2f)
                close()
            }

            drawPath(
                path = needlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF59D),
                        ArtisticAmberGold,
                        ArtisticAmberDeep
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Needle Edge Highlight Stroke
            drawPath(
                path = needlePath,
                color = ArtisticCream,
                style = Stroke(width = 2.2f)
            )

            // Pointer Tip Specular Highlight (intensifies during active peg strike)
            if (isSpinning) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = Offset(cx, h - 4f),
                    radius = 2.5f
                )
            }

            // Top Pivot Cap with 3D Shading
            drawCircle(
                color = Color(0x88000000),
                center = Offset(cx + 1f, 11f),
                radius = 6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, ArtisticAmberGold, ArtisticMaroonBg),
                    center = Offset(cx - 1.5f, 8.5f),
                    radius = 6f
                ),
                center = Offset(cx, 10f),
                radius = 5.5f
            )
        }
    }
}

