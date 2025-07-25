package com.cgfay.camera.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cgfay.cameralibrary.R

/**
 * Jetpack Compose version of the old CameraPreviewTopbar view. All callbacks are
 * delivered through lambda parameters.
 */
object PanelType {
    const val PanelMusic = 0
    const val PanelSpeedBar = 1
    const val PanelFilter = 2
    const val PanelSetting = 3
}

@Composable
fun CameraPreviewTopbar(
    modifier: Modifier = Modifier,
    musicName: String? = null,
    speedBarOpen: Boolean = false,
    visible: Boolean = true,
    onCameraClose: () -> Unit = {},
    onCameraSwitch: () -> Unit = {},
    onShowPanel: (Int) -> Unit = {},
) {
    if (!visible) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCameraClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera_preview_close),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { onShowPanel(PanelType.PanelMusic) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera_music),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = musicName ?: stringResource(id = R.string.tv_select_music))
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onCameraSwitch) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_switch_camera_light),
                        contentDescription = null
                    )
                }
                Text(text = "\u7ffb\u8f6c")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onShowPanel(PanelType.PanelSpeedBar) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_setting_more_light),
                        contentDescription = null
                    )
                }
                Text(text = if (speedBarOpen) "\u901f\u5ea6\u5f00" else "\u901f\u5ea6\u5173")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onShowPanel(PanelType.PanelFilter) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_effect_light),
                        contentDescription = null
                    )
                }
                Text(text = "\u6ee4\u955c")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onShowPanel(PanelType.PanelSetting) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_setting_more_light),
                        contentDescription = null
                    )
                }
                Text(text = "\u66f4\u591a")
            }
        }
    }
}
