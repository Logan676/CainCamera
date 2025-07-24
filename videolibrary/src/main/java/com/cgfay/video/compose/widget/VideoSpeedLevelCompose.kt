package com.cgfay.video.compose.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import com.cgfay.video.R
import com.cgfay.video.bean.VideoSpeed

/**
 * Compose version of [com.cgfay.video.widget.VideoSpeedLevelBar].
 */
@Composable
fun VideoSpeedLevelBar(
    selectedPosition: MutableState<Int> = remember { mutableStateOf(1) },
    modifier: Modifier = Modifier,
    touchEnable: Boolean = true,
    onSpeedChanged: (VideoSpeed) -> Unit = {}
) {
    val labels = stringArrayResource(id = R.array.video_speed_texts)
    val background = colorResource(id = R.color.video_speed_background)
    Row(modifier = modifier.background(background).fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val selected = selectedPosition.value == index
            val bg = if (selected) colorResource(id = R.color.white) else Color.Transparent
            val textColor = if (selected) Color(0x80000000) else Color(0x80FFFFFF)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable(enabled = touchEnable) {
                        selectedPosition.value = index
                        onSpeedChanged(
                            when (index) {
                                0 -> VideoSpeed.SPEED_L1
                                1 -> VideoSpeed.SPEED_L2
                                2 -> VideoSpeed.SPEED_L3
                                else -> VideoSpeed.SPEED_L2
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, color = textColor, style = MaterialTheme.typography.body1)
            }
        }
    }
}
