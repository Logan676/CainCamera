package com.cgfay.camera.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cgfay.cameralibrary.R

@Composable
fun PreviewSettingScreen(
    enableChangeFlash: Boolean = false,
    onFlashChanged: (Boolean) -> Unit,
    onTouchTakeChanged: (Boolean) -> Unit,
    onTimeLapseChanged: (Boolean) -> Unit,
    onOpenCameraSetting: () -> Unit,
    onLuminousCompensationChanged: (Boolean) -> Unit,
    onEdgeBlurChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var flashOn by remember { mutableStateOf(false) }
    var touchTake by remember { mutableStateOf(false) }
    var timeLapse by remember { mutableStateOf(false) }
    var luminous by remember { mutableStateOf(false) }
    var edgeBlur by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.popup_background))
            .padding(vertical = 20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SettingItem(
                selected = touchTake,
                iconSelected = R.drawable.ic_camera_setting_more_light,
                iconUnselected = R.drawable.ic_camera_setting_more_dark,
                text = stringResource(id = R.string.tv_touch_take),
                modifier = Modifier.weight(1f)
            ) {
                touchTake = !touchTake
                onTouchTakeChanged(touchTake)
            }
            SettingItem(
                selected = timeLapse,
                iconSelected = R.drawable.ic_camera_setting_more_light,
                iconUnselected = R.drawable.ic_camera_setting_more_dark,
                text = stringResource(id = R.string.tv_time_lapse),
                modifier = Modifier.weight(1f)
            ) {
                timeLapse = !timeLapse
                onTimeLapseChanged(timeLapse)
            }
            SettingItem(
                selected = flashOn,
                iconSelected = R.drawable.ic_camera_flash_on,
                iconUnselected = R.drawable.ic_camera_flash_off,
                text = stringResource(id = R.string.tv_flash),
                enabled = enableChangeFlash,
                modifier = Modifier.weight(1f)
            ) {
                if (enableChangeFlash) {
                    flashOn = !flashOn
                    onFlashChanged(flashOn)
                }
            }
            SettingItem(
                selected = false,
                iconSelected = R.drawable.ic_camera_setting,
                iconUnselected = R.drawable.ic_camera_setting,
                text = stringResource(id = R.string.tv_camera_setting),
                modifier = Modifier.weight(1f)
            ) {
                onOpenCameraSetting()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.tv_luminous_compensation),
                color = colorResource(id = R.color.popup_text_color),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = luminous,
                onCheckedChange = {
                    luminous = it
                    onLuminousCompensationChanged(it)
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.tv_edge_blur),
                color = colorResource(id = R.color.popup_text_color),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = edgeBlur,
                onCheckedChange = {
                    edgeBlur = it
                    onEdgeBlurChanged(it)
                }
            )
        }
    }
}

@Composable
private fun SettingItem(
    selected: Boolean,
    iconSelected: Int,
    iconUnselected: Int,
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .padding(horizontal = 4.dp)
            .clickable(enabled = enabled) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = if (selected) iconSelected else iconUnselected),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = text,
            color = if (selected) Color.White else colorResource(id = R.color.popup_text_normal)
        )
    }
}
