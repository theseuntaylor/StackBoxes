package com.theseuntaylor.stackboxes

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

private const val TICK_MS = 15L
private const val BUZZ_MS = 250L

/** A light tick for a correct tap and a short buzz for a mistake; silent without a vibrator. */
class Haptics(context: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }

    fun tick() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), TICK_MS)
        else -> vibrate(null, TICK_MS)
    }

    fun buzz() = vibrate(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createOneShot(BUZZ_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        } else null,
        BUZZ_MS
    )

    // effect is null below API 26, where only a plain duration is supported.
    private fun vibrate(effect: VibrationEffect?, fallbackMs: Long) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        if (effect != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(fallbackMs)
        }
    }
}
