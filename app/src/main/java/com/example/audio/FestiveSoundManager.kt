package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.PlaybackParams
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
     * Plays the golden win chime effect with harmonic resonance.
     */
    fun playWinChime()

    /**
     * Plays the reward claim celebration chime.
     */
    fun playClaimChime()

    /**
     * Plays an encouraging try-again melodic tone.
     */
    fun playTryAgainSound()

    /**
     * Plays a tactile click for button presses and card taps.
     */
    fun playClickSound()

    /**
     * Announces winner with TTS.
     */
    fun announceWinner(userName: String, prizeName: String)

    /**
     * Speaks arbitrary announcement text.
     */
    fun speakText(text: String)

    /**
     * Stops any ongoing TTS announcement.
     */
    fun stopAnnouncement()

    /**
     * Releases audio and synthesizer resources.
     */
    fun release()
}

/**
 * High-performance, direct in-memory AudioTrack manager tailored for the Ganesh Utsav Lucky Spin app.
 * Generates raw PCM audio in memory and plays it directly via AudioTrack static buffers,
 * bypassing file I/O and media decoders completely for zero-latency, artifact-free audio.
 */
class FestiveSoundManager private constructor(private val context: Context) : SoundEffectManager, TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    @Volatile private var spinTrack: AudioTrack? = null
    private val pegClickTracks = ArrayList<AudioTrack>()
    @Volatile private var pegClickIndex = 0

    private val clickTracks = ArrayList<AudioTrack>()
    @Volatile private var clickIndex = 0

    @Volatile private var celebrationTrack: AudioTrack? = null
    @Volatile private var claimTrack: AudioTrack? = null
    @Volatile private var tryAgainTrack: AudioTrack? = null

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    init {
        initializeAudioTracks()
    }

    private fun initializeAudioTracks() {
        scope.launch {
            try {
                // 1. Wheel Spin Track (4.6s decelerating ratchet + whirl)
                val spinPcm = synthesizeSpinSound()
                spinTrack = buildStaticAudioTrack(spinPcm)

                // 2. Wheel Peg Click (pool of 4 tracks for rapid overlapping ticks)
                val pegPcm = synthesizeWheelPegClick()
                repeat(4) {
                    buildStaticAudioTrack(pegPcm)?.let { pegClickTracks.add(it) }
                }

                // 3. Tactile UI Click (pool of 2 tracks)
                val clickPcm = synthesizeClickSound()
                repeat(2) {
                    buildStaticAudioTrack(clickPcm)?.let { clickTracks.add(it) }
                }

                // 4. Grand Celebration Win Fanfare
                val celebPcm = synthesizeCelebrationSound()
                celebrationTrack = buildStaticAudioTrack(celebPcm)

                // 5. Reward Claim Fanfare
                val claimPcm = synthesizeClaimFanfare()
                claimTrack = buildStaticAudioTrack(claimPcm)

                // 6. Encouraging Try Again Tone
                val tryAgainPcm = synthesizeTryAgainTone()
                tryAgainTrack = buildStaticAudioTrack(tryAgainPcm)
            } catch (e: Throwable) {
                Log.w("FestiveSoundManager", "Error initializing AudioTracks", e)
            }
        }
    }

    private fun buildStaticAudioTrack(pcm: ShortArray, sampleRate: Int = 44100): AudioTrack? {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            val written = track.write(pcm, 0, pcm.size)
            if (written > 0) {
                track
            } else {
                track.release()
                null
            }
        } catch (e: Throwable) {
            Log.w("FestiveSoundManager", "Error building AudioTrack", e)
            null
        }
    }

    private fun playStatic(track: AudioTrack?, volume: Float = 1.0f, pitch: Float = 1.0f) {
        if (track == null || _isMuted.value) return
        try {
            if (track.state != AudioTrack.STATE_INITIALIZED) return
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.reloadStaticData()
            track.setVolume(volume.coerceIn(0.0f, 1.0f))
            if (pitch != 1.0f) {
                track.playbackParams = PlaybackParams().apply {
                    this.pitch = pitch.coerceIn(0.5f, 2.0f)
                    this.speed = 1.0f
                }
            }
            track.play()
        } catch (e: Throwable) {
            // Non-critical playback frame
        }
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

    /**
     * Plays the festive wheel spinning sound (4.6-second decelerating ratchet ticking + whirl).
     */
    override fun playSpinSound() {
        if (_isMuted.value) return
        stopSpinSound()
        playStatic(spinTrack, volume = 0.95f)
    }

    /**
     * Stops the active wheel spinning sound immediately.
     */
    override fun stopSpinSound() {
        try {
            spinTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED && it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
            }
        } catch (e: Throwable) {
            Log.w("FestiveSoundManager", "Error stopping spin sound", e)
        }
    }

    /**
     * Plays a crisp clicking sound during the wheel rotation as each peg hits the flapper.
     */
    override fun playWheelClick(velocityFactor: Float) {
        if (_isMuted.value) return
        val pitch = (0.90f + velocityFactor * 0.25f).coerceIn(0.7f, 1.6f)
        val volume = (0.55f + velocityFactor * 0.25f).coerceIn(0.3f, 0.95f)
        val tracks = pegClickTracks
        if (tracks.isNotEmpty()) {
            val idx = Math.floorMod(pegClickIndex++, tracks.size)
            playStatic(tracks.getOrNull(idx), volume = volume, pitch = pitch)
        }
    }

    /**
     * Plays an individual dynamic ratchet peg tick with variable pitch and volume.
     */
    override fun playRatchetTick(pitch: Float, volume: Float) {
        if (_isMuted.value) return
        val clampedPitch = pitch.coerceIn(0.6f, 1.8f)
        val clampedVol = volume.coerceIn(0.1f, 0.9f)
        val tracks = pegClickTracks
        if (tracks.isNotEmpty()) {
            val idx = Math.floorMod(pegClickIndex++, tracks.size)
            playStatic(tracks.getOrNull(idx), volume = clampedVol, pitch = clampedPitch)
        }
    }

    /**
     * Plays a grand celebratory sound effect when a prize is won.
     */
    override fun playCelebrationSound() {
        if (_isMuted.value) return
        playStatic(celebrationTrack, volume = 1.0f)
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
        playStatic(claimTrack, volume = 1.0f)
    }

    /**
     * Plays a friendly, encouraging try-again melodic chime.
     */
    override fun playTryAgainSound() {
        if (_isMuted.value) return
        playStatic(tryAgainTrack, volume = 0.85f)
    }

    /**
     * Plays a crisp tactile click for card and button interactions.
     */
    override fun playClickSound() {
        if (_isMuted.value) return
        val tracks = clickTracks
        if (tracks.isNotEmpty()) {
            val idx = Math.floorMod(clickIndex++, tracks.size)
            playStatic(tracks.getOrNull(idx), volume = 0.65f)
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
            spinTrack?.let {
                it.stop()
                it.release()
            }
            spinTrack = null

            pegClickTracks.forEach {
                try { it.stop(); it.release() } catch (_: Throwable) {}
            }
            pegClickTracks.clear()

            clickTracks.forEach {
                try { it.stop(); it.release() } catch (_: Throwable) {}
            }
            clickTracks.clear()

            celebrationTrack?.let {
                it.stop()
                it.release()
            }
            celebrationTrack = null

            claimTrack?.let {
                it.stop()
                it.release()
            }
            claimTrack = null

            tryAgainTrack?.let {
                it.stop()
                it.release()
            }
            tryAgainTrack = null

            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isTtsReady = false
        } catch (e: Exception) {
            Log.w("FestiveSoundManager", "Error releasing AudioTracks/TTS", e)
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
    // PROCEDURAL AUDIO SYNTHESIS ENGINES (PCM)
    // ==========================================

    /**
     * Synthesizes a crisp, acoustic wheel peg click sound (25ms duration).
     */
    private fun synthesizeWheelPegClick(sampleRate: Int = 44100): ShortArray {
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

        return pcm
    }

    /**
     * Synthesizes a grand celebratory sound with major triad fanfare and golden chime resonance.
     */
    private fun synthesizeCelebrationSound(sampleRate: Int = 44100): ShortArray {
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

        return pcm
    }

    private fun synthesizeSpinSound(sampleRate: Int = 44100, durationSec: Float = 4.6f): ShortArray {
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

        return pcm
    }

    private fun synthesizeWinChime(sampleRate: Int = 44100): ShortArray {
        return synthesizeCelebrationSound(sampleRate)
    }

    private fun synthesizeClaimFanfare(sampleRate: Int = 44100): ShortArray {
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

        return pcm
    }

    private fun synthesizeTryAgainTone(sampleRate: Int = 44100): ShortArray {
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

        return pcm
    }

    private fun synthesizeClickSound(sampleRate: Int = 44100): ShortArray {
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

        return pcm
    }
}

val LocalSoundEffectManager = staticCompositionLocalOf<SoundEffectManager?> { null }
val LocalFestiveSoundManager = LocalSoundEffectManager
