package com.theseuntaylor.stackboxes

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The launcher icons a player can pick. Each is an activity-alias of [MainActivity] in the
 * manifest; exactly one is enabled, and that is the icon the launcher shows.
 */
enum class AppIcon(
    val label: String,
    val alias: String,
    @DrawableRes val foreground: Int,
    @ColorRes val background: Int,
    /** Whether the manifest enables this alias, i.e. it is the icon on a fresh install. */
    val default: Boolean,
) {
    Arcade("Arcade", ".ArcadeIcon", R.drawable.ic_launcher_arcade_foreground, R.color.ic_launcher_arcade_background, true),
    Sequence("Sequence", ".SequenceIcon", R.drawable.ic_launcher_sequence_foreground, R.color.ic_launcher_sequence_background, false),
}

/**
 * The player's icon choice. [select] only records it; [apply] switches the launcher icon, and is
 * called when the player leaves the app, because some launchers close an app whose icon changes.
 */
class AppIconChooser(private val context: Context) {
    private val packageManager = context.packageManager

    var selected by mutableStateOf(current())
        private set

    fun select(icon: AppIcon) {
        selected = icon
    }

    fun apply() {
        val icon = selected
        if (icon == current()) return
        // Enable the new alias before disabling the old one, so there is always an icon.
        setEnabled(icon, true)
        AppIcon.entries.filter { it != icon }.forEach { setEnabled(it, false) }
    }

    /** The icon the launcher shows now. */
    private fun current(): AppIcon = AppIcon.entries.firstOrNull { isEnabled(it) } ?: AppIcon.Arcade

    private fun isEnabled(icon: AppIcon): Boolean =
        when (packageManager.getComponentEnabledSetting(component(icon))) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> icon.default
            else -> false
        }

    private fun setEnabled(icon: AppIcon, enabled: Boolean) {
        packageManager.setComponentEnabledSetting(
            component(icon),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun component(icon: AppIcon) = ComponentName(context, context.packageName + icon.alias)
}
