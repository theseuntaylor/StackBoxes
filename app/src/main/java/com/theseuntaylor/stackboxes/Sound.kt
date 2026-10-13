package com.theseuntaylor.stackboxes

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.min

private const val TAG = "StackBoxes"
private const val SAMPLE_RATE = 44_100
private const val VOLUME = 0.15
private const val BOX_TONE_MS = 180
private const val BUZZ_HZ = 110.0
private const val BUZZ_MS = 300
private const val FADE_MS = 8

/**
 * Retro square-wave beeps, generated in code: one tone per box and a low buzz for mistakes.
 * Plays as game audio, so it follows the media volume. Call [close] when done.
 */
class SoundPlayer : AutoCloseable {
    private val handler = Handler(Looper.getMainLooper())
    private val samples = mutableMapOf<Double, ShortArray>()
    private val playing = mutableSetOf<AudioTrack>()

    fun box(index: Int, boxCount: Int) = play(boxToneHz(index, boxCount), BOX_TONE_MS)

    fun buzz() = play(BUZZ_HZ, BUZZ_MS)

    private fun play(hz: Double, ms: Int) {
        val pcm = samples.getOrPut(hz) { squareWave(hz, ms) }
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
                .build()
        } catch (e: Exception) {
            // Out of audio tracks or no audio device: stay silent rather than crash the game.
            Log.w(TAG, "tone not played", e)
            return
        }
        track.write(pcm, 0, pcm.size)
        track.play()
        Log.d(TAG, "tone hz=${"%.2f".format(hz)}")
        playing += track
        handler.postDelayed({ release(track) }, ms + 50L)
    }

    private fun release(track: AudioTrack) {
        if (playing.remove(track)) track.release()
    }

    override fun close() {
        handler.removeCallbacksAndMessages(null)
        playing.toList().forEach(::release)
    }
}

/** A square wave with short fades at both ends so it starts and stops without a click. */
private fun squareWave(hz: Double, ms: Int): ShortArray {
    val count = SAMPLE_RATE * ms / 1000
    val fade = SAMPLE_RATE * FADE_MS / 1000
    val period = SAMPLE_RATE / hz
    val peak = Short.MAX_VALUE * VOLUME
    return ShortArray(count) { i ->
        val envelope = min(1.0, min(i, count - 1 - i).toDouble() / fade)
        val high = (i % period) < period / 2
        ((if (high) peak else -peak) * envelope).toInt().toShort()
    }
}
