package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SongPlaybackState(
    val isPlaying: Boolean = false,
    val celebrantName: String = "",
    val wishedByMailName: String = "",
    val currentLyricLine: String = ""
)

class BirthdaySongPlayer(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var playJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null

    private val _state = MutableStateFlow(SongPlaybackState())
    val state: StateFlow<SongPlaybackState> = _state.asStateFlow()

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setPitch(1.12f)
                    tts?.setSpeechRate(0.92f)
                    isTtsReady = true
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun playHappyBirthdaySong(celebrantName: String, wishedByMailName: String) {
        stop()
        val cleanName = celebrantName.trim().ifBlank { "My Friend" }
        val cleanSender = wishedByMailName.trim().ifBlank { "Friend" }

        playJob = scope.launch(Dispatchers.Default) {
            _state.value = SongPlaybackState(
                isPlaying = true,
                celebrantName = cleanName,
                wishedByMailName = cleanSender,
                currentLyricLine = "🎵 Happy Birthday to you…"
            )

            // Speak/sing the personalized greeting via TextToSpeech if available
            if (isTtsReady) {
                val sungText = "Happy Birthday to you! Happy Birthday to you! " +
                    "Happy Birthday dear $cleanName! Happy Birthday to you! " +
                    "Best wishes from $cleanSender!"
                try {
                    tts?.speak(sungText, TextToSpeech.QUEUE_FLUSH, null, "bday_song_utterance")
                } catch (ignored: Exception) {
                }
            }

            // "Happy Birthday to You" 4-phrase musical notes: (Frequency Hz, Duration ms, Lyric Line)
            val phrases = listOf(
                "🎵 Happy Birthday to you…" to listOf(
                    392.0 to 320, 392.0 to 180, 440.0 to 480, 392.0 to 480, 523.25 to 480, 493.88 to 900
                ),
                "🎶 Happy Birthday to you…" to listOf(
                    392.0 to 320, 392.0 to 180, 440.0 to 480, 392.0 to 480, 587.33 to 480, 523.25 to 900
                ),
                "🎂 Happy Birthday Dear $cleanName!" to listOf(
                    392.0 to 320, 392.0 to 180, 783.99 to 480, 659.25 to 480, 523.25 to 480, 493.88 to 480, 440.0 to 850
                ),
                "🎉 Happy Birthday to you! (From $cleanSender)" to listOf(
                    698.46 to 320, 698.46 to 180, 659.25 to 480, 523.25 to 480, 587.33 to 480, 523.25 to 1100
                )
            )

            for ((lyric, notes) in phrases) {
                if (!isActive) break
                _state.value = _state.value.copy(currentLyricLine = lyric)
                for ((freq, durationMs) in notes) {
                    if (!isActive) break
                    playChimeNote(freq, durationMs)
                    delay(45L)
                }
            }

            if (isActive) {
                delay(400L)
                _state.value = SongPlaybackState(isPlaying = false)
            }
        }
    }

    private fun playChimeNote(freqHz: Double, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Warm music-box / bell harmonic synthesis with gentle exponential decay
            val envelope = exp(-3.2 * t / (durationMs / 1000.0)).coerceIn(0.0, 1.0)
            val attack = (i.toDouble() / (sampleRate * 0.012)).coerceAtMost(1.0)
            val fundamental = sin(2.0 * PI * freqHz * t)
            val harmonic2 = 0.35 * sin(2.0 * PI * (freqHz * 2.0) * t)
            val harmonic3 = 0.12 * sin(2.0 * PI * (freqHz * 3.0) * t)
            val wave = (fundamental + harmonic2 + harmonic3) / 1.47
            pcm[i] = (wave * envelope * attack * Short.MAX_VALUE * 0.55).toInt().toShort()
        }

        try {
            val bufferSize = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize.coerceAtLeast(AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            activeAudioTrack = track
            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong())
            track.stop()
            track.release()
        } catch (ignored: Exception) {
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        try {
            tts?.stop()
        } catch (ignored: Exception) {
        }
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (ignored: Exception) {
        }
        activeAudioTrack = null
        _state.value = SongPlaybackState(isPlaying = false)
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (ignored: Exception) {
        }
    }
}
