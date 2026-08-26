package com.example.ui.animation

import androidx.compose.ui.graphics.Color
import com.example.model.Dish
import com.example.model.SectorType
import com.example.model.WheelSector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * High-fidelity rotational physics state of the Lucky Wheel.
 */
enum class WheelSpinPhase {
    IDLE,
    ACCELERATION,
    DECELERATION,
    FINAL_LANDING,
    LANDED
}

/**
 * Real-time physics parameters and telemetry for the spinning wheel.
 */
data class WheelPhysicsState(
    val currentAngle: Float = 0f,
    val angularVelocityDegPerSec: Float = 0f,
    val normalizedVelocity: Float = 0f,
    val phase: WheelSpinPhase = WheelSpinPhase.IDLE,
    val pointerDeflectionAngle: Float = 0f,
    val isPegCrossing: Boolean = false,
    val progress: Float = 0f,
    val landedSectorIndex: Int = 0,
    val landedSector: WheelSector? = null,
    val isLandedPrizeWin: Boolean = false,
    val landingPulseAlpha: Float = 0f
)

/**
 * Physics calculation engine for wheel rotation, peg-flapper collision dynamics,
 * progressive friction drag, and damped harmonic landing settling.
 */
object WheelPhysicsEngine {

    const val DEFAULT_SPIN_DURATION_MS = 4600L
    const val NUM_PINS = 24
    const val PIN_SPACING_DEG = 360f / NUM_PINS // 15 degrees per pin

    // Normalized phase breakpoints
    const val PHASE_ACCEL_END = 0.18f
    const val PHASE_DECEL_END = 0.88f

    /**
     * Computes the normalized progression [0.0..1.0] along the physics trajectory.
     * Incorporates:
     * 1. Acceleration: Power-law torque buildup with smooth inertia onset.
     * 2. Deceleration: Aerodynamic and bearing friction decay.
     * 3. Final Landing: Damped harmonic spring settlement into the prize sector pocket.
     */
    fun calculatePhysicsProgress(p: Float): Float {
        val clamped = p.coerceIn(0f, 1f)
        if (clamped <= 0f) return 0f
        if (clamped >= 1f) return 1f

        return when {
            // 1. Acceleration Phase (0.0 -> 0.18): Progressive torque acceleration
            clamped < PHASE_ACCEL_END -> {
                val ratio = clamped / PHASE_ACCEL_END
                // Smooth progressive acceleration curve
                val s = ratio.toDouble().pow(2.4).toFloat()
                0.32f * s
            }

            // 2. Friction Deceleration Phase (0.18 -> 0.88): Power-law decay
            clamped < PHASE_DECEL_END -> {
                val ratio = (clamped - PHASE_ACCEL_END) / (PHASE_DECEL_END - PHASE_ACCEL_END)
                // Viscous friction formula: 1 - (1 - ratio)^k
                val frictionDecay = (1.0 - (1.0 - ratio.toDouble()).pow(3.0)).toFloat()
                0.32f + 0.63f * frictionDecay
            }

            // 3. Final Landing Settle Phase (0.88 -> 1.0): Spring-damper settlement
            else -> {
                val tau = (clamped - PHASE_DECEL_END) / (1.0f - PHASE_DECEL_END)
                // Damped harmonic oscillation settling smoothly into target angle:
                // Overdamped/critically damped settle with subtle mechanical bounce
                val decay = exp(-4.2 * tau.toDouble()).toFloat()
                val oscillation = cos(1.5 * PI * tau.toDouble()).toFloat()
                val bounce = 1.0f - (decay * oscillation)
                0.95f + 0.05f * bounce
            }
        }
    }

    /**
     * Calculates instantaneous angular velocity in deg/sec based on current progress.
     */
    fun calculateAngularVelocity(
        progress: Float,
        totalDeltaDeg: Float,
        durationMs: Long = DEFAULT_SPIN_DURATION_MS
    ): Float {
        if (progress <= 0f || progress >= 1f) return 0f
        val dt = 0.005f
        val p1 = (progress - dt).coerceIn(0f, 1f)
        val p2 = (progress + dt).coerceIn(0f, 1f)
        val prog1 = calculatePhysicsProgress(p1)
        val prog2 = calculatePhysicsProgress(p2)
        val deltaProg = if (p2 > p1) (prog2 - prog1) / (p2 - p1) else 0f
        val totalSec = durationMs / 1000f
        return (totalDeltaDeg / totalSec) * deltaProg
    }

    /**
     * Computes the physical deflection of the pointer needle (flapper)
     * as the wheel pegs pass under the pointer needle located at 270°.
     */
    fun calculatePointerDeflection(
        wheelAngle: Float,
        velocityDegPerSec: Float
    ): Pair<Float, Boolean> {
        val speedFactor = (velocityDegPerSec / 1200f).coerceIn(0f, 1.2f)
        var maxDeflection = 0f
        var isPegCrossing = false

        // Top pointer needle position
        val pointerAngle = 270f

        // Check proximity to any of the 24 pins
        for (i in 0 until NUM_PINS) {
            val pinBaseAngle = i * PIN_SPACING_DEG
            val currentPinAngle = (wheelAngle + pinBaseAngle) % 360f
            val diff = (currentPinAngle - pointerAngle + 540f) % 360f - 180f

            // Pin engagement window: -3° to +7° relative to pointer
            if (diff in -3.5f..7.5f) {
                // Pin pushes pointer in direction of rotation
                val pushProgress = if (diff < 2f) {
                    (diff + 3.5f) / 5.5f
                } else {
                    1f - (diff - 2f) / 5.5f
                }

                val deflection = pushProgress * (18f * speedFactor + 3.5f)
                if (deflection > maxDeflection) {
                    maxDeflection = deflection
                }

                // Crossing trigger point (exact release point)
                if (diff in 1.8f..3.2f) {
                    isPegCrossing = true
                }
            }
        }

        // Add high-speed flutter vibration when spinning fast
        val flutter = if (speedFactor > 0.35f) {
            (sin((wheelAngle * 1.8).toDouble()).toFloat() * 3.5f * speedFactor)
        } else {
            0f
        }

        val totalDeflection = (maxDeflection + flutter).coerceIn(-6f, 26f)
        return Pair(totalDeflection, isPegCrossing)
    }

    /**
     * Identifies which sector is currently centered under the 270° pointer.
     */
    fun calculateLandedSectorIndex(currentAngle: Float, sectorCount: Int = 4): Int {
        val normalizedAngle = (270f - (currentAngle % 360f) + 360f) % 360f
        val sectorArc = 360f / sectorCount
        val index = (normalizedAngle / sectorArc).toInt() % sectorCount
        return index.coerceIn(0, sectorCount - 1)
    }
}
