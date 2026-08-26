package com.example

import com.example.ui.animation.WheelPhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WheelPhysicsEngineTest {

    @Test
    fun physicsProgress_startsAtZeroAndEndsAtOne() {
        val start = WheelPhysicsEngine.calculatePhysicsProgress(0f)
        val end = WheelPhysicsEngine.calculatePhysicsProgress(1f)
        assertEquals(0f, start, 0.001f)
        assertEquals(1f, end, 0.001f)
    }

    @Test
    fun physicsProgress_monotonicIncrease() {
        var prev = 0f
        for (i in 1..100) {
            val progress = WheelPhysicsEngine.calculatePhysicsProgress(i / 100f)
            assertTrue("Physics progress should be monotonically increasing or settling near 1", progress >= prev - 0.05f)
            prev = progress
        }
    }

    @Test
    fun angularVelocity_peaksDuringAccelerationAndDecays() {
        val totalDelta = 2160f // 6 rotations
        val durationMs = 4600L

        val vStart = WheelPhysicsEngine.calculateAngularVelocity(0f, totalDelta, durationMs)
        val vPeak = WheelPhysicsEngine.calculateAngularVelocity(0.18f, totalDelta, durationMs)
        val vLate = WheelPhysicsEngine.calculateAngularVelocity(0.75f, totalDelta, durationMs)
        val vEnd = WheelPhysicsEngine.calculateAngularVelocity(1f, totalDelta, durationMs)

        assertEquals(0f, vStart, 0.1f)
        assertTrue("Peak velocity should be positive and high", vPeak > 800f)
        assertTrue("Late velocity should decay below peak", vLate < vPeak)
        assertEquals(0f, vEnd, 0.1f)
    }

    @Test
    fun landedSectorIndex_calculatesAccurately() {
        // Pointer is at 270 deg
        // If angle is 0 deg: normalizedAngle = (270 - 0) % 360 = 270 deg. 270 / 90 = 3
        assertEquals(3, WheelPhysicsEngine.calculateLandedSectorIndex(0f, 4))
        // If angle is 270 deg: normalizedAngle = (270 - 270) % 360 = 0 deg. 0 / 90 = 0
        assertEquals(0, WheelPhysicsEngine.calculateLandedSectorIndex(270f, 4))
        // If angle is 180 deg: normalizedAngle = (270 - 180) % 360 = 90 deg. 90 / 90 = 1
        assertEquals(1, WheelPhysicsEngine.calculateLandedSectorIndex(180f, 4))
    }

    @Test
    fun pointerDeflection_withinBoundedRange() {
        val (deflection, _) = WheelPhysicsEngine.calculatePointerDeflection(123.45f, 900f)
        assertTrue("Deflection should be within reasonable bounds", deflection in -25f..25f)
    }
}
