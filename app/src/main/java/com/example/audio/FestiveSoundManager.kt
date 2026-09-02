package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Interface defining sound effects for wheel rotation, celebratory prize wins, and UI feedback.
 */
interface SoundEffectManager {
    val isMuted: StateFlow<Boolean>
    fun toggleMute(): Boolean
    fun setMuted(muted: Boolean)

    /**
     * Plays the continuous decelerating wheel rotation audio track.
     */
    fun playSpinSound()
    fun stopSpinSound()

    /**
     * Plays a crisp clicking sound during wheel rotation when pegs cross the pointer needle.
     */
    fun playWheelClick(velocityFactor: Float = 1.0f)

    /**
     * Plays an individual mechanical ratchet tick with custom pitch and volume.
     */
    fun playRatchetTick(pitch: Float = 1.0f, volume: Float = 0.5f)

    /**
     * Plays a celebratory fanfare sound when a prize is won.
     */
    fun playCelebrationSound()

    /**
     * Plays golden celebratory win chimes.
     */
    fun playWinChime()

    /**
     * Plays reward claim fanfare.
     */
    fun playClaimChime()

    /**
     * Plays encouraging try again tone.
     */
    fun playTryAgainSound()

    /**
     * Plays tactile UI click sound.
     */
    fun playClickSound()

    /**
     * Announces winner with TTS.
     */
    fun announceWinner(userName: String, prizeName: String = "")
    fun speakText(text: String)
    fun stopAnnouncement()
    fun release()
}

/**
 * High-performance SoundPool audio manager tailored for the Ganesh Utsav Lucky Spin app.
 * Provides synthesized festive sound effects:
 * - Dynamic clicking sound during wheel rotation
 * - Glorious celebratory fanfare and golden chimes when a prize is won
 * - Decelerating wheel spinning whirl and ratchet ticks
 * - Festive reward claim fanfare
 * - Encouraging try-again tones
 * - Tactile UI interaction clicks
 * - TextToSpeech announcer for declaring winner names out loud
 */
class FestiveSoundManager private constructor(private val context: Context) : SoundEffectManager, TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var soundPool: SoundPool? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    private var spinSoundId: Int = 0
    private var wheelClickSoundId: Int = 0
    private var celebrationSoundId: Int = 0
    private var winChimeSoundId: Int = 0
    private var claimChimeSoundId: Int = 0
    private var tryAgainSoundId: Int = 0
    private var clickSoundId: Int = 0

    private val loadedSoundIds = mutableSetOf<Int>()
    private var activeSpinStreamId: Int = 0

    init {
        initializeSoundPool()
    }

    private fun initializeTts() {
        if (textToSpeech != null) return
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Throwable) {
            Log.w("FestiveSoundManager", "TTS initialization bypassed or not supported", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            try {
                // Try Indian English first for regional Indian names, fallback to default/US
                val localeIn = Locale("en", "IN")
                val result = textToSpeech?.setLanguage(localeIn)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.language = Locale.getDefault()
                }
                textToSpeech?.setSpeechRate(0.95f)
                textToSpeech?.setPitch(1.05f)
            } catch (e: Exception) {
                Log.w("FestiveSoundManager", "Error configuring TTS locale", e)
            }
        }
    }

    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0) {
                            synchronized(loadedSoundIds) {
                                loadedSoundIds.add(sampleId)
                            }
                        }
                    }
                }

            scope.launch {
                generateAndLoadSounds()
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error initializing SoundPool", e)
        }
    }

    private fun generateAndLoadSounds() {
        try {
            val cacheDir = context.cacheDir

            // 1. Wheel Spin Sound Track (4.6s decelerating)
            val spinFile = File(cacheDir, "festive_spin_4600ms.wav")
            if (!spinFile.exists() || spinFile.length() < 1000) {
                val spinBytes = synthesizeSpinSound()
                FileOutputStream(spinFile).use { it.write(spinBytes) }
            }
            soundPool?.let { spinSoundId = it.load(spinFile.absolutePath, 1) }

            // 2. Wheel Dynamic Peg Click (Crisp physical collision during rotation)
            val wheelClickFile = File(cacheDir, "wheel_peg_click.wav")
            if (!wheelClickFile.exists() || wheelClickFile.length() < 500) {
                val clickBytes = synthesizeWheelPegClick()
                FileOutputStream(wheelClickFile).use { it.write(clickBytes) }
            }
            soundPool?.let { wheelClickSoundId = it.load(wheelClickFile.absolutePath, 1) }

            // 3. Grand Celebration Sound (Prize Won Fanfare + Shimmering Bells)
            val celebFile = File(cacheDir, "festive_celebration_win.wav")
            if (!celebFile.exists() || celebFile.length() < 1000) {
                val celebBytes = synthesizeCelebrationSound()
                FileOutputStream(celebFile).use { it.write(celebBytes) }
            }
            soundPool?.let { celebrationSoundId = it.load(celebFile.absolutePath, 1) }

            // 4. Win Celebratory Chime
            val winFile = File(cacheDir, "festive_win_chime.wav")
            if (!winFile.exists() || winFile.length() < 1000) {
                val winBytes = synthesizeWinChime()
                FileOutputStream(winFile).use { it.write(winBytes) }
            }
            soundPool?.let { winChimeSoundId = it.load(winFile.absolutePath, 1) }

            // 5. Reward Claim Fanfare
            val claimFile = File(cacheDir, "festive_claim_fanfare.wav")
            if (!claimFile.exists() || claimFile.length() < 1000) {
                val claimBytes = synthesizeClaimFanfare()
                FileOutputStream(claimFile).use { it.write(claimBytes) }
            }
            soundPool?.let { claimChimeSoundId = it.load(claimFile.absolutePath, 1) }

            // 6. Try Again Tone
            val tryAgainFile = File(cacheDir, "festive_try_again.wav")
            if (!tryAgainFile.exists() || tryAgainFile.length() < 1000) {
                val tryAgainBytes = synthesizeTryAgainTone()
                FileOutputStream(tryAgainFile).use { it.write(tryAgainBytes) }
            }
            soundPool?.let { tryAgainSoundId = it.load(tryAgainFile.absolutePath, 1) }

            // 7. Tactile UI Click
            val clickFile = File(cacheDir, "festive_click.wav")
            if (!clickFile.exists() || clickFile.length() < 1000) {
                val clickBytes = synthesizeClickSound()
                FileOutputStream(clickFile).use { it.write(clickBytes) }
            }
            soundPool?.let { clickSoundId = it.load(clickFile.absolutePath, 1) }

        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error generating audio cache", e)
        }
    }

    override fun toggleMute(): Boolean {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        if (newMuted) {
            stopSpinSound()
        }
        return newMuted
    }

    override fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stopSpinSound()
        }
    }

    private fun isSoundLoaded(soundId: Int): Boolean {
        if (soundId == 0) return false
        return synchronized(loadedSoundIds) {
            loadedSoundIds.contains(soundId)
        }
    }

    /**
     * Plays the festive wheel spinning sound (4.6-second decelerating ratchet ticking + whirl).
     */
    override fun playSpinSound() {
        if (_isMuted.value) return
        stopSpinSound()
        try {
            val sp = soundPool ?: return
            if (isSoundLoaded(spinSoundId)) {
                activeSpinStreamId = sp.play(spinSoundId, 0.95f, 0.95f, 2, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing spin sound", e)
        }
    }

    /**
     * Stops the active wheel spinning sound immediately.
     */
    override fun stopSpinSound() {
        try {
            if (activeSpinStreamId != 0) {
                soundPool?.stop(activeSpinStreamId)
                activeSpinStreamId = 0
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error stopping spin sound", e)
        }
    }

    /**
     * Plays a crisp clicking sound during the wheel rotation as each peg hits the flapper.
     */
    override fun playWheelClick(velocityFactor: Float) {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            val soundId = if (isSoundLoaded(wheelClickSoundId)) wheelClickSoundId else if (isSoundLoaded(clickSoundId)) clickSoundId else 0
            if (soundId != 0) {
                val pitch = (0.90f + velocityFactor * 0.25f).coerceIn(0.7f, 1.6f)
                val volume = (0.55f + velocityFactor * 0.25f).coerceIn(0.3f, 0.95f)
                sp.play(soundId, volume, volume, 1, 0, pitch)
            }
        } catch (e: Exception) {
            // Non-critical frame tick
        }
    }

    /**
     * Plays an individual dynamic ratchet peg tick with variable pitch and volume.
     */
    override fun playRatchetTick(pitch: Float, volume: Float) {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            val soundId = if (isSoundLoaded(wheelClickSoundId)) wheelClickSoundId else if (isSoundLoaded(clickSoundId)) clickSoundId else 0
            if (soundId != 0) {
                val clampedPitch = pitch.coerceIn(0.6f, 1.8f)
                val clampedVol = volume.coerceIn(0.1f, 0.9f)
                sp.play(soundId, clampedVol, clampedVol, 1, 0, clampedPitch)
            }
        } catch (e: Exception) {
            // Non-critical sound tick
        }
    }

    /**
     * Plays a grand celebratory sound effect when a prize is won.
     */
    override fun playCelebrationSound() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            val soundId = if (isSoundLoaded(celebrationSoundId)) celebrationSoundId else if (isSoundLoaded(winChimeSoundId)) winChimeSoundId else 0
            if (soundId != 0) {
                sp.play(soundId, 1.0f, 1.0f, 3, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing celebration sound", e)
        }
    }

    /**
     * Plays the celebratory golden win chime with shimmering temple bell harmonics.
     */
    override fun playWinChime() {
        playCelebrationSound()
    }

    /**
     * Plays the triumphant reward claim celebration chime.
     */
    override fun playClaimChime() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (isSoundLoaded(claimChimeSoundId)) {
                sp.play(claimChimeSoundId, 1.0f, 1.0f, 3, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing claim chime", e)
        }
    }

    /**
     * Plays a friendly, encouraging try-again melodic chime.
     */
    override fun playTryAgainSound() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (isSoundLoaded(tryAgainSoundId)) {
                sp.play(tryAgainSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing try again tone", e)
        }
    }

    /**
     * Plays a crisp tactile click for card and button interactions.
     */
    override fun playClickSound() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (isSoundLoaded(clickSoundId)) {
                sp.play(clickSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing click sound", e)
        }
    }

    /**
     * Announces the winner's name and prize out loud using Text-To-Speech with celebratory enthusiasm.
     */
    override fun announceWinner(userName: String, prizeName: String) {
        if (_isMuted.value) return
        val cleanName = userName.trim()
        val announcement = if (cleanName.isNotBlank() && !cleanName.equals("Valued Guest", ignoreCase = true) && !cleanName.equals("Guest", ignoreCase = true)) {
            if (prizeName.isNotBlank()) {
                "Congratulations $cleanName! You have won free $prizeName! Ganpati Bappa Morya!"
            } else {
                "Congratulations $cleanName! You are our lucky festive winner! Ganpati Bappa Morya!"
            }
        } else {
            if (prizeName.isNotBlank()) {
                "Congratulations! You have won free $prizeName! Ganpati Bappa Morya!"
            } else {
                "Congratulations! You are our lucky festive winner! Ganpati Bappa Morya!"
            }
        }

        speakText(announcement)
    }

    /**
     * Speaks arbitrary announcement text out loud.
     */
    override fun speakText(text: String) {
        if (_isMuted.value) return
        try {
            if (textToSpeech == null) {
                initializeTts()
            }
            if (isTtsReady && textToSpeech != null) {
                textToSpeech?.stop()
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LUCKY_SPIN_ANNOUNCEMENT_${System.currentTimeMillis()}")
            }
        } catch (e: Throwable) {
            Log.w("FestiveSoundManager", "Error in TextToSpeech announcement", e)
        }
    }

    /**
     * Stops any ongoing TTS announcement.
     */
    override fun stopAnnouncement() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error stopping TTS", e)
        }
    }

    override fun release() {
        try {
            stopSpinSound()
            stopAnnouncement()
            textToSpeech?.shutdown()
            textToSpeech = null
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error releasing SoundPool/TTS", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FestiveSoundManager? = null

        fun getInstance(context: Context): FestiveSoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FestiveSoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ==========================================
    // PROCEDURAL AUDIO SYNTHESIS ENGINES
    // ==========================================

    /**
     * Synthesizes a crisp, acoustic wheel peg click sound (25ms duration).
     */
    private fun synthesizeWheelPegClick(sampleRate: Int = 44100): ByteArray {
        val durationSec = 0.025f // 25ms crisp peg collision
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Sharp initial impulse with steep decay
            val decay = exp(-t * 350.0)
            val wave = (0.75 * sin(twoPi * 2100.0 * t) + 0.25 * sin(twoPi * 920.0 * t)) * decay
            pcm[i] = (wave * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return pcmToWav(pcm, sampleRate)
    }

    /**
     * Synthesizes a grand celebratory sound with major triad fanfare and golden chime resonance.
     */
    private fun synthesizeCelebrationSound(sampleRate: Int = 44100): ByteArray {
        val durationSec = 3.0f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        // Festive celebration fanfare chord progression + shimmering bell arpeggio
        data class FanfareLayer(val startSec: Double, val freq: Double, val duration: Double, val amp: Double, val decayRate: Double)

        val notes = listOf(
            // Opening quick brass fanfare motif: G5 -> C6 -> E6 -> G6
            FanfareLayer(0.00, 783.99, 0.12, 0.65, 8.0),   // G5
            FanfareLayer(0.10, 1046.50, 0.14, 0.70, 7.0),  // C6
            FanfareLayer(0.22, 1318.51, 0.16, 0.80, 6.0),  // E6
            FanfareLayer(0.35, 1567.98, 0.22, 0.90, 4.5),  // G6

            // Grand triumphant victory chord (C Major triumphant sustained chord)
            FanfareLayer(0.55, 523.25, 2.4, 0.75, 1.8),   // C5
            FanfareLayer(0.55, 659.25, 2.4, 0.80, 1.8),   // E5
            FanfareLayer(0.55, 783.99, 2.4, 0.85, 1.8),   // G5
            FanfareLayer(0.55, 1046.50, 2.4, 0.90, 1.6),  // C6
            FanfareLayer(0.55, 1318.51, 2.4, 0.85, 1.6),  // E6
            FanfareLayer(0.55, 1567.98, 2.4, 0.95, 1.4),  // G6
            FanfareLayer(0.55, 2093.00, 2.4, 0.55, 2.2),  // C7 Sparkle

            // Shimmering golden bell chime overtones
            FanfareLayer(0.90, 2637.02, 1.8, 0.35, 2.5),  // E7 Bell
            FanfareLayer(1.15, 3135.96, 1.5, 0.30, 3.0)   // G7 Bell
        )

        for (note in notes) {
            val startIdx = (note.startSec * sampleRate).toInt()
            val noteSamples = (note.duration * sampleRate).toInt()

            for (i in 0 until noteSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val attack = (t / 0.012).coerceIn(0.0, 1.0)
                    val decay = exp(-t * note.decayRate)
                    val shimmer = 1.0 + 0.06 * sin(twoPi * 7.0 * t)

                    val wave = (
                        0.65 * sin(twoPi * note.freq * t) +
                        0.25 * sin(twoPi * (note.freq * 2.0) * t) +
                        0.10 * sin(twoPi * (note.freq * 3.0) * t)
                    ) * attack * decay * shimmer * note.amp

                    val sampleVal = (wave * 12500).toInt()
                    pcm[idx] = (pcm[idx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeSpinSound(sampleRate: Int = 44100, durationSec: Float = 4.6f): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)

        // 1. Calculate realistic decelerating ratchet click timestamps
        val clickTimes = mutableListOf<Float>()
        var currentT = 0.05f
        val totalDuration = 4.5f

        while (currentT < totalDuration) {
            clickTimes.add(currentT)
            // Progress ratio 0.0 -> 1.0
            val progress = (currentT / totalDuration).coerceIn(0f, 1f)
            // Deceleration interval: 22ms initially up to 280ms at the end (slow clicks)
            val interval = 0.022f + 0.28f * (progress * progress * progress)
            currentT += interval
        }

        // Add a final distinct stopping peg click
        clickTimes.add(4.52f)

        // 2. Synthesize each click into the buffer
        val twoPi = 2.0 * PI
        for (clickT in clickTimes) {
            val startIdx = (clickT * sampleRate).toInt()
            val clickDuration = 0.018f // 18ms crisp click
            val clickSamples = (clickDuration * sampleRate).toInt()

            for (i in 0 until clickSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val decay = exp(-t * 360.0)
                    val wave = (0.72 * sin(twoPi * 1850.0 * t) + 0.28 * sin(twoPi * 850.0 * t)) * decay
                    val sampleVal = (wave * 22000).toInt()
                    pcm[idx] = (pcm[idx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        // 3. Add soft festive spinning whirl background
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            if (t < totalDuration) {
                val progress = (t / totalDuration).toFloat()
                val whirlVolume = (1.0 - progress.toDouble()).coerceIn(0.0, 1.0) * 0.22
                // Gliding frequency from 340Hz down to 80Hz
                val freq = 340.0 - 260.0 * progress
                val whirl = sin(twoPi * freq * t) * whirlVolume
                val sampleVal = (whirl * 11000).toInt()
                pcm[i] = (pcm[i] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeWinChime(sampleRate: Int = 44100): ByteArray {
        return synthesizeCelebrationSound(sampleRate)
    }

    private fun synthesizeClaimFanfare(sampleRate: Int = 44100): ByteArray {
        val durationSec = 2.2f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        data class FanfareNote(val startSec: Double, val freq: Double, val duration: Double, val amp: Double)

        val fanfare = listOf(
            FanfareNote(0.00, 783.99, 0.10, 0.6),  // G5
            FanfareNote(0.12, 783.99, 0.10, 0.6),  // G5
            FanfareNote(0.24, 1046.50, 0.20, 0.8), // C6
            // Sustained glorious celebratory chord
            FanfareNote(0.42, 1046.50, 1.60, 0.7), // C6
            FanfareNote(0.42, 1318.51, 1.60, 0.7), // E6
            FanfareNote(0.42, 1567.98, 1.60, 0.8), // G6
            FanfareNote(0.42, 2093.00, 1.60, 0.4)  // C7 sparkle
        )

        for (note in fanfare) {
            val startIdx = (note.startSec * sampleRate).toInt()
            val noteSamples = (note.duration * sampleRate).toInt()

            for (i in 0 until noteSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val attack = (t / 0.015).coerceIn(0.0, 1.0)
                    val decay = exp(-t * 2.2)
                    val wave = (
                        0.7 * sin(twoPi * note.freq * t) +
                        0.3 * sin(twoPi * note.freq * 2.0 * t)
                    ) * attack * decay * note.amp

                    val sampleVal = (wave * 14000).toInt()
                    pcm[idx] = (pcm[idx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeTryAgainTone(sampleRate: Int = 44100): ByteArray {
        val durationSec = 1.1f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        val notes = listOf(
            Pair(0.00, 659.25), // E5
            Pair(0.25, 523.25)  // C5
        )

        for ((startSec, freq) in notes) {
            val startIdx = (startSec * sampleRate).toInt()
            val noteSamples = ((durationSec - startSec) * sampleRate).toInt()

            for (i in 0 until noteSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val decay = exp(-t * 5.0)
                    val wave = (0.8 * sin(twoPi * freq * t) + 0.2 * sin(twoPi * freq * 3.0 * t)) * decay * 0.7
                    val sampleVal = (wave * 15000).toInt()
                    pcm[idx] = (pcm[idx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeClickSound(sampleRate: Int = 44100): ByteArray {
        val durationSec = 0.04f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 220.0)
            val wave = (0.7 * sin(twoPi * 1200.0 * t) + 0.3 * sin(twoPi * 650.0 * t)) * decay * 0.8
            pcm[i] = (wave * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return pcmToWav(pcm, sampleRate)
    }

    /**
     * Converts a 16-bit PCM ShortArray to standard RIFF/WAV format byte array.
     */
    private fun pcmToWav(pcm: ShortArray, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val dataSize = pcm.size * 2
        val totalSize = 36 + dataSize

        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk descriptor
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalSize)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // "fmt " sub-chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // SubChunk1Size (16 for PCM)
        buffer.putShort(1) // AudioFormat (1 for PCM)
        buffer.putShort(numChannels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * numChannels * (bitsPerSample / 8)) // ByteRate
        buffer.putShort((numChannels * (bitsPerSample / 8)).toShort()) // BlockAlign
        buffer.putShort(bitsPerSample.toShort())

        // "data" sub-chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(dataSize)

        // PCM Sample payload
        for (sample in pcm) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }
}

val LocalSoundEffectManager = staticCompositionLocalOf<SoundEffectManager?> { null }
val LocalFestiveSoundManager = LocalSoundEffectManager


