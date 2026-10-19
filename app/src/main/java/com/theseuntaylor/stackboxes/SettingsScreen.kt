package com.theseuntaylor.stackboxes

import android.app.Activity
import android.app.UiModeManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The gear on the game screen that opens [SettingsScreen]. */
@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(painterResource(R.drawable.ic_settings), contentDescription = "Settings")
    }
}

/** Sound, vibration, accessibility and the app icon. The Back arrow or system Back returns to the game. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    appIcon: AppIconChooser,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val activity = LocalActivity.current

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", modifier = Modifier.semantics { heading() }) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(bottom = 16.dp)
        ) {

            SectionHeading("Appearance")
            AppearancePicker(
                selected = settings.appearance,
                onSelect = { value ->
                    settings.updateAppearance(value)
                    activity?.useAppearance(value)
                }
            )

            SectionHeading("Sound and vibration")
            SettingSwitch("Sound", on = settings.sound, onChange = settings::updateSound)
            SettingSwitch("Vibration", on = settings.vibration, onChange = settings::updateVibration)

            SectionHeading("Accessibility")
            SettingSwitch(
                "Slower playback", "Boxes stay lit 50% longer",
                on = settings.slowerPlayback, onChange = settings::updateSlowerPlayback
            )
            SettingSwitch(
                "Reduce motion", "A still launch screen, from Android 13",
                on = settings.reduceMotion,
                onChange = { on ->
                    settings.updateReduceMotion(on)
                    activity?.useSplashScreen(reduceMotion = on)
                }
            )
            SettingSwitch("Show box numbers", on = settings.showNumbers, onChange = settings::updateShowNumbers)

            SectionHeading("App icon")
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIcon.entries.forEach { icon ->
                    IconOption(icon, selected = icon == appIcon.selected, onSelect = { appIcon.select(icon) })
                }
            }
            Text(
                "The new icon shows once you leave the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Tells Android 12+ which appearance the app uses, so its window and launch screen match too.
 * (Older versions follow the system there; the app's own colours still follow the choice.)
 */
fun Activity.useAppearance(appearance: Appearance) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    getSystemService(UiModeManager::class.java).setApplicationNightMode(
        when (appearance) {
            Appearance.System -> UiModeManager.MODE_NIGHT_AUTO
            Appearance.Light -> UiModeManager.MODE_NIGHT_NO
            Appearance.Dark -> UiModeManager.MODE_NIGHT_YES
        }
    )
}

@Composable
private fun AppearancePicker(selected: Appearance, onSelect: (Appearance) -> Unit) {
    val options = Appearance.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { i, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size)
            ) {
                Text(option.label)
            }
        }
    }
}

/** Picks the launch screen for the next launches: still when reducing motion. Android 13+ only. */
fun Activity.useSplashScreen(reduceMotion: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    splashScreen.setSplashScreenTheme(
        if (reduceMotion) R.style.Theme_StackBoxes_Starting_Still else R.style.Theme_StackBoxes_Starting
    )
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingSwitch(label: String, detail: String? = null, on: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = detail?.let { { Text(it) } },
        trailingContent = { Switch(checked = on, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = on, role = Role.Switch, onValueChange = onChange)
    )
}

@Composable
private fun IconOption(icon: AppIcon, selected: Boolean, onSelect: () -> Unit) {
    // Selection is shown by the ring and the bold label, not just color.
    Column(
        modifier = Modifier
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 3.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .padding(5.dp)
        ) {
            // The adaptive icon's visible middle (72 of 108 units) on its background.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(icon.background))
            ) {
                Image(
                    painter = painterResource(icon.foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.5f)
                )
            }
        }
        Text(
            icon.label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
