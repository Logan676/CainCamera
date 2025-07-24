package com.cgfay.camera.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cgfay.cameralibrary.R

@Composable
fun PreviewSettingScreen(
    modifier: Modifier = Modifier,
    enableChangeFlash: Boolean = false,
    initialFlash: Boolean = false,
    initialTouchTake: Boolean = false,
    initialTimeLapse: Boolean = false,
    initialLuminous: Boolean = false,
    initialEdgeBlur: Boolean = false,
    onFlashChanged: (Boolean) -> Unit = {},
    onTouchTakeChanged: (Boolean) -> Unit = {},
    onTimeLapseChanged: (Boolean) -> Unit = {},
    onOpenCameraSetting: () -> Unit = {},
    onLuminousCompensationChanged: (Boolean) -> Unit = {},
    onEdgeBlurChanged: (Boolean) -> Unit = {}
) {
    var flash by remember { mutableStateOf(initialFlash) }
    var touchTake by remember { mutableStateOf(initialTouchTake) }
    var timeLapse by remember { mutableStateOf(initialTimeLapse) }
    var luminous by remember { mutableStateOf(initialLuminous) }
    var edgeBlur by remember { mutableStateOf(initialEdgeBlur) }

    val unselectedColor = Color(0xFF333333)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xAA808080), RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconItem(
                selected = touchTake,
                onClick = {
                    touchTake = !touchTake
                    onTouchTakeChanged(touchTake)
                },
                text = stringResource(R.string.tv_touch_take),
                selectedIcon = R.drawable.ic_camera_setting_more_light,
                unselectedIcon = R.drawable.ic_camera_setting_more_dark
            )
            IconItem(
                selected = timeLapse,
                onClick = {
                    timeLapse = !timeLapse
                    onTimeLapseChanged(timeLapse)
                },
                text = stringResource(R.string.tv_time_lapse),
                selectedIcon = R.drawable.ic_camera_setting_more_light,
                unselectedIcon = R.drawable.ic_camera_setting_more_dark
            )
            IconItem(
                selected = flash,
                onClick = {
                    if (enableChangeFlash) {
                        flash = !flash
                        onFlashChanged(flash)
                    }
                },
                text = stringResource(R.string.tv_flash),
                selectedIcon = R.drawable.ic_camera_flash_on,
                unselectedIcon = R.drawable.ic_camera_flash_off,
                enabled = enableChangeFlash
            )
            IconItem(
                selected = false,
                onClick = onOpenCameraSetting,
                text = stringResource(R.string.tv_camera_setting),
                selectedIcon = R.drawable.ic_camera_setting,
                unselectedIcon = R.drawable.ic_camera_setting
            )
        }
        SettingSwitchRow(
            text = stringResource(R.string.tv_luminous_compensation),
            checked = luminous,
            onCheckedChange = {
                luminous = it
                onLuminousCompensationChanged(it)
            }
        )
        SettingSwitchRow(
            text = stringResource(R.string.tv_edge_blur),
            checked = edgeBlur,
            onCheckedChange = {
                edgeBlur = it
                onEdgeBlurChanged(it)
            }
        )
    }
}

@Composable
private fun IconItem(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    selectedIcon: Int,
    unselectedIcon: Int,
    enabled: Boolean = true
) {
    val color = if (selected) Color.White else Color(0xFF333333)
    Column(
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(if (selected) selectedIcon else unselectedIcon),
            contentDescription = text,
            modifier = Modifier
                .size(30.dp)
                .padding(5.dp)
        )
        Text(text = text, color = color)
    }
}

@Composable
private fun SettingSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, color = Color.White)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

