package com.theseuntaylor.stackboxes

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val KEY_SOUND = "sound"
private const val KEY_VIBRATION = "vibration"
private const val KEY_SLOWER_PLAYBACK = "slowerPlayback"
private const val KEY_REDUCE_MOTION = "reduceMotion"
private const val KEY_SHOW_NUMBERS = "showNumbers"
private const val KEY_APPEARANCE = "appearance"

/** Light or dark: with the system, or always one of them. */
enum class Appearance(val label: String) { System("System"), Light("Light"), Dark("Dark") }

/**
 * Player settings, kept on the device: sound and vibration (on by default), and the
 * accessibility options slower playback and reduce motion (off) and box numbers (on), and the
 * appearance (with the system by default).
 */
class Settings(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("settings", Context.MODE_PRIVATE))

    var sound by mutableStateOf(prefs.getBoolean(KEY_SOUND, true))
        private set
    var vibration by mutableStateOf(prefs.getBoolean(KEY_VIBRATION, true))
        private set
    var slowerPlayback by mutableStateOf(prefs.getBoolean(KEY_SLOWER_PLAYBACK, false))
        private set
    var reduceMotion by mutableStateOf(prefs.getBoolean(KEY_REDUCE_MOTION, false))
        private set
    var showNumbers by mutableStateOf(prefs.getBoolean(KEY_SHOW_NUMBERS, true))
        private set
    var appearance by mutableStateOf(
        Appearance.entries.firstOrNull { it.name == prefs.getString(KEY_APPEARANCE, null) } ?: Appearance.System
    )
        private set

    fun updateSound(on: Boolean) {
        sound = on
        prefs.edit().putBoolean(KEY_SOUND, on).apply()
    }

    fun updateVibration(on: Boolean) {
        vibration = on
        prefs.edit().putBoolean(KEY_VIBRATION, on).apply()
    }

    fun updateSlowerPlayback(on: Boolean) {
        slowerPlayback = on
        prefs.edit().putBoolean(KEY_SLOWER_PLAYBACK, on).apply()
    }

    fun updateReduceMotion(on: Boolean) {
        reduceMotion = on
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, on).apply()
    }

    fun updateShowNumbers(on: Boolean) {
        showNumbers = on
        prefs.edit().putBoolean(KEY_SHOW_NUMBERS, on).apply()
    }

    fun updateAppearance(value: Appearance) {
        appearance = value
        prefs.edit().putString(KEY_APPEARANCE, value.name).apply()
    }
}
