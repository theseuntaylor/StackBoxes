package com.theseuntaylor.stackboxes

import android.app.Activity
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.sp
import com.theseuntaylor.stackboxes.ui.theme.activeColor

/** The gear on the game screen that opens [SettingsScreen]. */
@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(painterResource(R.drawable.ic_settings), contentDescription = "Settings")
    }
}

/** Sound, vibration, accessibility and the app icon. The Back arrow or system Back returns to the game. */
@Composable
fun SettingsScreen(
    settings: Settings,
    appIcon: AppIconChooser,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val activity = LocalActivity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
            }
            Text(
                "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        }

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
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppIcon.entries.forEach { icon ->
                IconOption(icon, selected = icon == appIcon.selected, onSelect = { appIcon.select(icon) })
            }
        }
        Text(
            "The new icon shows once you leave the app.",
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
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
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.primary,
        modifier = Modifier
            .padding(start = 8.dp, top = 24.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingSwitch(label: String, detail: String? = null, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = on, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 16.sp)
            if (detail != null) {
                Text(detail, fontSize = 14.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
            }
        }
        Switch(checked = on, onCheckedChange = null)
    }
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
                    color = if (selected) activeColor else MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
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
        Text(icon.label, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
