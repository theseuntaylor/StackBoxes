package com.theseuntaylor.stackboxes

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {

    private val prefs = FakeSharedPreferences()

    @Test
    fun `sound and vibration are on by default`() {
        val settings = Settings(prefs)
        assertTrue(settings.sound)
        assertTrue(settings.vibration)
    }

    @Test
    fun `sound and vibration switch independently`() {
        val settings = Settings(prefs)
        settings.updateSound(false)
        assertFalse(settings.sound)
        assertTrue(settings.vibration)

        settings.updateVibration(false)
        settings.updateSound(true)
        assertTrue(settings.sound)
        assertFalse(settings.vibration)
    }

    @Test
    fun `choices survive a restart`() {
        Settings(prefs).apply {
            updateSound(false)
            updateVibration(false)
        }
        val reloaded = Settings(prefs)
        assertFalse(reloaded.sound)
        assertFalse(reloaded.vibration)

        reloaded.updateSound(true)
        assertTrue(Settings(prefs).sound)
    }
}
