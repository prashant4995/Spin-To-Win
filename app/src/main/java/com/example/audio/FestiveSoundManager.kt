package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.util.Log
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
import kotlin.math.pow
import kotlin.math.sin

/**
 * High-performance SoundPool audio manager tailored for the Ganesh Utsav Lucky Spin app.
 * Provides synthesized festive sound effects:
 * - Dynamic decelerating wheel spinning with realistic ratchet clicks and whirl
 * - Glorious celebratory win chimes with rich harmonic bell overtones
 * - Festive reward claim fanfare
 * - Encouraging try-again tones
 * - Tactile UI interaction clicks
 * - TextToSpeech announcer for declaring winner names out loud!
 */
class FestiveSoundManager private constructor(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var soundPool: SoundPool? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    private var spinSoundId: Int = 0
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
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0) {
                            loadedSoundIds.add(sampleId)
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

            // 1. Wheel Spin Sound
            val spinFile = File(cacheDir, "festive_spin_4200ms.wav")
            if (!spinFile.exists() || spinFile.length() < 1000) {
                val spinBytes = synthesizeSpinSound()
                FileOutputStream(spinFile).use { it.write(spinBytes) }
            }
            soundPool?.let { spinSoundId = it.load(spinFile.absolutePath, 1) }

            // 2. Win Celebratory Chime
            val winFile = File(cacheDir, "festive_win_chime.wav")
            if (!winFile.exists() || winFile.length() < 1000) {
                val winBytes = synthesizeWinChime()
                FileOutputStream(winFile).use { it.write(winBytes) }
            }
            soundPool?.let { winChimeSoundId = it.load(winFile.absolutePath, 1) }

            // 3. Reward Claim Fanfare
            val claimFile = File(cacheDir, "festive_claim_fanfare.wav")
            if (!claimFile.exists() || claimFile.length() < 1000) {
                val claimBytes = synthesizeClaimFanfare()
                FileOutputStream(claimFile).use { it.write(claimBytes) }
            }
            soundPool?.let { claimChimeSoundId = it.load(claimFile.absolutePath, 1) }

            // 4. Try Again Tone
            val tryAgainFile = File(cacheDir, "festive_try_again.wav")
            if (!tryAgainFile.exists() || tryAgainFile.length() < 1000) {
                val tryAgainBytes = synthesizeTryAgainTone()
                FileOutputStream(tryAgainFile).use { it.write(tryAgainBytes) }
            }
            soundPool?.let { tryAgainSoundId = it.load(tryAgainFile.absolutePath, 1) }

            // 5. Tactile UI Click
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

    fun toggleMute(): Boolean {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        if (newMuted) {
            stopSpinSound()
        }
        return newMuted
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stopSpinSound()
        }
    }

    /**
     * Plays the festive wheel spinning sound (4.2-second decelerating ratchet ticking + whirl).
     */
    fun playSpinSound() {
        if (_isMuted.value) return
        stopSpinSound()
        try {
            val sp = soundPool ?: return
            if (spinSoundId != 0) {
                activeSpinStreamId = sp.play(spinSoundId, 0.95f, 0.95f, 2, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing spin sound", e)
        }
    }

    /**
     * Stops the active wheel spinning sound immediately.
     */
    fun stopSpinSound() {
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
     * Plays the celebratory golden win chime with shimmering temple bell harmonics.
     */
    fun playWinChime() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (winChimeSoundId != 0) {
                sp.play(winChimeSoundId, 1.0f, 1.0f, 3, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing win chime", e)
        }
    }

    /**
     * Plays the triumphant reward claim celebration chime.
     */
    fun playClaimChime() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (claimChimeSoundId != 0) {
                sp.play(claimChimeSoundId, 1.0f, 1.0f, 3, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing claim chime", e)
        }
    }

    /**
     * Plays a friendly, encouraging try-again melodic chime.
     */
    fun playTryAgainSound() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (tryAgainSoundId != 0) {
                sp.play(tryAgainSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing try again tone", e)
        }
    }

    /**
     * Plays a crisp tactile click for card and button interactions.
     */
    fun playClickSound() {
        if (_isMuted.value) return
        try {
            val sp = soundPool ?: return
            if (clickSoundId != 0) {
                sp.play(clickSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error playing click sound", e)
        }
    }

    /**
     * Plays an individual dynamic ratchet peg tick with variable pitch and volume.
     */
    fun playRatchetTick(pitch: Float = 1.0f, volume: Float = 0.5f) {
        if (_isMuted.value || activeSpinStreamId != 0) return
        try {
            val sp = soundPool ?: return
            if (clickSoundId != 0) {
                val clampedPitch = pitch.coerceIn(0.6f, 1.8f)
                val clampedVol = volume.coerceIn(0.1f, 0.9f)
                sp.play(clickSoundId, clampedVol, clampedVol, 1, 0, clampedPitch)
            }
        } catch (e: Exception) {
            // Non-critical sound tick
        }
    }

    /**
     * Announces the winner's name and prize out loud using Text-To-Speech with celebratory enthusiasm.
     */
    fun announceWinner(userName: String, prizeName: String = "") {
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
    fun speakText(text: String) {
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
    fun stopAnnouncement() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error stopping TTS", e)
        }
    }

    fun release() {
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

    private fun synthesizeSpinSound(sampleRate: Int = 44100, durationSec: Float = 4.3f): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)

        // 1. Calculate realistic decelerating ratchet click timestamps
        val clickTimes = mutableListOf<Float>()
        var currentT = 0.05f
        val totalDuration = 4.2f

        while (currentT < totalDuration) {
            clickTimes.add(currentT)
            // Progress ratio 0.0 -> 1.0
            val progress = (currentT / totalDuration).coerceIn(0f, 1f)
            // Deceleration interval: 25ms initially (40 clicks/sec) up to 260ms at the end (slow clicks)
            // Cubic easing creates natural wheel momentum loss
            val interval = 0.025f + 0.26f * (progress * progress * progress)
            currentT += interval
        }

        // Add a final distinct stopping peg click
        clickTimes.add(4.20f)

        // 2. Synthesize each click into the buffer
        val twoPi = 2.0 * PI
        for (clickT in clickTimes) {
            val startIdx = (clickT * sampleRate).toInt()
            val clickDuration = 0.016f // 16ms crisp click
            val clickSamples = (clickDuration * sampleRate).toInt()

            for (i in 0 until clickSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    // Metallic / wooden peg impulse: dual resonant frequencies (1750Hz & 820Hz)
                    val decay = exp(-t * 380.0)
                    val wave = (0.7 * sin(twoPi * 1750.0 * t) + 0.3 * sin(twoPi * 820.0 * t)) * decay
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
                val whirlVolume = (1.0 - progress.toDouble()).coerceIn(0.0, 1.0) * 0.25
                // Gliding frequency from 320Hz down to 90Hz
                val freq = 320.0 - 230.0 * progress
                val whirl = sin(twoPi * freq * t) * whirlVolume
                val sampleVal = (whirl * 12000).toInt()
                pcm[i] = (pcm[i] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeWinChime(sampleRate: Int = 44100): ByteArray {
        val durationSec = 2.6f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        // Celestial major chord arpeggio with celebratory temple bell harmonics
        // Notes: C5 (523.25), E5 (659.25), G5 (783.99), C6 (1046.50), E6 (1318.51), G6 (1567.98)
        data class BellNote(val startSec: Double, val freq: Double, val amp: Double, val sustain: Double)

        val notes = listOf(
            BellNote(0.00, 523.25, 0.65, 3.2),
            BellNote(0.14, 659.25, 0.70, 3.2),
            BellNote(0.28, 783.99, 0.75, 3.2),
            BellNote(0.42, 1046.50, 0.85, 3.0),
            BellNote(0.56, 1318.51, 0.90, 2.8),
            BellNote(0.70, 1567.98, 1.00, 1.8), // Grand high celebration chime
            BellNote(0.70, 2093.00, 0.50, 1.6)  // Sparkle octave harmonic
        )

        for (note in notes) {
            val startIdx = (note.startSec * sampleRate).toInt()
            val noteDuration = durationSec - note.startSec
            val noteSamples = (noteDuration * sampleRate).toInt()

            for (i in 0 until noteSamples) {
                val idx = startIdx + i
                if (idx in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val decay = exp(-t * note.sustain)
                    // Bell tone: fundamental + harmonic overtones (2.76x and 5.4x)
                    val shimmer = 1.0 + 0.08 * sin(twoPi * 6.5 * t) // gentle tremolo
                    val wave = (
                        0.60 * sin(twoPi * note.freq * t) +
                        0.25 * sin(twoPi * (note.freq * 2.76) * t) +
                        0.15 * sin(twoPi * (note.freq * 5.40) * t)
                    ) * decay * shimmer * note.amp

                    val sampleVal = (wave * 15000).toInt()
                    pcm[idx] = (pcm[idx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        return pcmToWav(pcm, sampleRate)
    }

    private fun synthesizeClaimFanfare(sampleRate: Int = 44100): ByteArray {
        val durationSec = 2.0f
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples)
        val twoPi = 2.0 * PI

        data class FanfareNote(val startSec: Double, val freq: Double, val duration: Double, val amp: Double)

        val fanfare = listOf(
            FanfareNote(0.00, 783.99, 0.10, 0.6),  // G5
            FanfareNote(0.12, 783.99, 0.10, 0.6),  // G5
            FanfareNote(0.24, 1046.50, 0.20, 0.8), // C6
            // Sustained glorious celebratory chord
            FanfareNote(0.42, 1046.50, 1.50, 0.7), // C6
            FanfareNote(0.42, 1318.51, 1.50, 0.7), // E6
            FanfareNote(0.42, 1567.98, 1.50, 0.8), // G6
            FanfareNote(0.42, 2093.00, 1.50, 0.4)  // C7 sparkle
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

        // Gentle marimba two-note descending tone (E5 -> C5)
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
        val durationSec = 0.04f // 40ms
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

val LocalFestiveSoundManager = androidx.compose.runtime.staticCompositionLocalOf<FestiveSoundManager?> { null }

