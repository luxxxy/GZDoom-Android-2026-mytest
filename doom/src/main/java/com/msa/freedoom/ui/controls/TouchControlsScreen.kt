package com.msa.freedoom.ui.controls

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import com.beloko.touchcontrols.TouchSettings
import com.msa.freedoom.R

/**
 * Compose front-end for the on-screen touch-control settings. It reads and writes the
 * same TouchSettings preferences as the legacy settings screen, so engine behaviour
 * and preference keys remain unchanged.
 */
@Composable
fun TouchControlsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var alpha by remember { mutableIntStateOf(TouchSettings.getIntOption(context, "alpha", 50)) }
    var fwd by remember { mutableIntStateOf(TouchSettings.getIntOption(context, "fwdSens", 50)) }
    var strafe by remember { mutableIntStateOf(TouchSettings.getIntOption(context, "strafeSens", 50)) }
    var pitch by remember { mutableIntStateOf(TouchSettings.getIntOption(context, "pitchSens", 50)) }
    var yaw by remember { mutableIntStateOf(TouchSettings.getIntOption(context, "yawSens", 50)) }

    var mouseMode by remember { mutableStateOf(TouchSettings.getBoolOption(context, "mouse_mode", true)) }
    var invertLook by remember { mutableStateOf(TouchSettings.getBoolOption(context, "invert_look", false)) }
    var precision by remember { mutableStateOf(TouchSettings.getBoolOption(context, "precision_shoot", false)) }
    var showSticks by remember { mutableStateOf(TouchSettings.getBoolOption(context, "show_sticks", false)) }
    var weaponWheel by remember { mutableStateOf(TouchSettings.getBoolOption(context, "enable_ww", true)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ControlsHero(alpha = alpha, mouseMode = mouseMode)

        SectionHeading(
            eyebrow = "01  /  DISPLAY",
            title = "HUD appearance",
            subtitle = "Tune how visible your on-screen controls are.",
        )
        SliderPanel {
            PercentSlider(stringResource(R.string.touch_opacity), alpha) {
                alpha = it
                TouchSettings.setIntOption(context, "alpha", it)
            }
        }

        SectionHeading(
            eyebrow = "02  /  MOVEMENT & AIM",
            title = stringResource(R.string.touch_sensitivity_header),
            subtitle = "Set how quickly your view and movement respond.",
        )
        SliderPanel {
            SensSlider(stringResource(R.string.touch_forward_sens), fwd) {
                fwd = it
                TouchSettings.setIntOption(context, "fwdSens", it)
            }
            SliderDivider()
            SensSlider(stringResource(R.string.touch_strafe_sens), strafe) {
                strafe = it
                TouchSettings.setIntOption(context, "strafeSens", it)
            }
            SliderDivider()
            SensSlider(stringResource(R.string.touch_pitch_sens), pitch) {
                pitch = it
                TouchSettings.setIntOption(context, "pitchSens", it)
            }
            SliderDivider()
            SensSlider(stringResource(R.string.touch_yaw_sens), yaw) {
                yaw = it
                TouchSettings.setIntOption(context, "yawSens", it)
            }
        }

        SectionHeading(
            eyebrow = "03  /  INPUT",
            title = stringResource(R.string.touch_behaviour_header),
            subtitle = "Choose the touch features that suit your play style.",
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ToggleRow(stringResource(R.string.touch_mouse_mode), mouseMode) {
                    mouseMode = it
                    TouchSettings.setBoolOption(context, "mouse_mode", it)
                }
                ToggleDivider()
                ToggleRow(stringResource(R.string.touch_invert_look), invertLook) {
                    invertLook = it
                    TouchSettings.setBoolOption(context, "invert_look", it)
                }
                ToggleDivider()
                ToggleRow(stringResource(R.string.touch_precision_shoot), precision) {
                    precision = it
                    TouchSettings.setBoolOption(context, "precision_shoot", it)
                }
                ToggleDivider()
                ToggleRow(stringResource(R.string.touch_show_sticks), showSticks) {
                    showSticks = it
                    TouchSettings.setBoolOption(context, "show_sticks", it)
                }
                ToggleDivider()
                ToggleRow(stringResource(R.string.touch_weapon_wheel), weaponWheel) {
                    weaponWheel = it
                    TouchSettings.setBoolOption(context, "enable_ww", it)
                }
            }
        }

        OutlinedButton(
            onClick = {
                applyDefaults(context)
                alpha = 50
                fwd = 50
                strafe = 50
                pitch = 50
                yaw = 50
                mouseMode = true
                invertLook = false
                precision = false
                showSticks = false
                weaponWheel = true
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                stringResource(R.string.touch_reset_defaults),
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ControlsHero(alpha: Int, mouseMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    "UZDOOM  •  PLAYER SETTINGS",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                "Make it yours.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.8).sp,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                stringResource(R.string.touch_controls_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
            )
            HudPreview(alpha = alpha, mouseMode = mouseMode)
        }
    }
}

@Composable
private fun HudPreview(alpha: Int, mouseMode: Boolean) {
    val foreground = MaterialTheme.colorScheme.onSurface
    val previewAlpha = (0.25f + (alpha / 100f) * 0.75f).coerceIn(0.25f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "CONTROL LAYOUT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "$alpha% opacity",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Text(
                    "+",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    color = foreground.copy(alpha = 0.35f),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .size(58.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = previewAlpha * 0.22f),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = previewAlpha),
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("MOVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = foreground.copy(alpha = previewAlpha))
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PreviewButton("A", previewAlpha)
                    PreviewButton("B", previewAlpha)
                }
                Text(
                    if (mouseMode) "LOOK: TOUCH" else "LOOK: STICK",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewButton(label: String, alpha: Float) {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha * 0.22f),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
        }
    }
}

@Composable
private fun SectionHeading(eyebrow: String, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            eyebrow,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SliderPanel(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
private fun SliderDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    )
}

private fun applyDefaults(ctx: Context) {
    TouchSettings.setIntOption(ctx, "alpha", 50)
    TouchSettings.setIntOption(ctx, "fwdSens", 50)
    TouchSettings.setIntOption(ctx, "strafeSens", 50)
    TouchSettings.setIntOption(ctx, "pitchSens", 50)
    TouchSettings.setIntOption(ctx, "yawSens", 50)
    TouchSettings.setBoolOption(ctx, "mouse_mode", true)
    TouchSettings.setBoolOption(ctx, "invert_look", false)
    TouchSettings.setBoolOption(ctx, "precision_shoot", false)
    TouchSettings.setBoolOption(ctx, "show_sticks", false)
    TouchSettings.setBoolOption(ctx, "enable_ww", true)
}

/** 0–100 % opacity. */
@Composable
private fun PercentSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    LabeledSlider(label, "$value%", value.toFloat(), 0f..100f) { onChange(it.roundToInt()) }
}

/** Sensitivity 0–100 → engine multiplier; 50 = 1.0×. */
@Composable
private fun SensSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    val multiplier = String.format(java.util.Locale.US, "%.1f×", value / 50f)
    LabeledSlider(label, multiplier, value.toFloat(), 0f..100f) { onChange(it.roundToInt()) }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    valueText,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                )
            }
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("LOW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("HIGH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ToggleDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                if (checked) "ENABLED" else "DISABLED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                ),
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
