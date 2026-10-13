package com.theseuntaylor.stackboxes

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val KEY_SOUND = "sound"
private const val KEY_VIBRATION = "vibration"

/** Sound and vibration switches, on by default and kept on the device. */
class Settings(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences("settings", Context.MODE_PRIVATE))

    var sound by mutableStateOf(prefs.getBoolean(KEY_SOUND, true))
        private set
    var vibration by mutableStateOf(prefs.getBoolean(KEY_VIBRATION, true))
        private set

    fun updateSound(on: Boolean) {
        sound = on
        prefs.edit().putBoolean(KEY_SOUND, on).apply()
    }

    fun updateVibration(on: Boolean) {
        vibration = on
        prefs.edit().putBoolean(KEY_VIBRATION, on).apply()
    }
}
