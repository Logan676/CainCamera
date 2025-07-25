package com.cgfay.camera.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cgfay.cameralibrary.R

enum class RecordSpeed(val type: Int, val speed: Float) {
    SPEED_L0(-2, 1f / 3f),
    SPEED_L1(-1, 1f / 2f),
    SPEED_L2(0, 1f),
    SPEED_L3(1, 2f),
    SPEED_L4(2, 3f)
}

@Composable
fun RecordSpeedLevelBar(
    currentSpeed: RecordSpeed = RecordSpeed.SPEED_L2,
    touchEnable: Boolean = true,
    onSpeedChanged: (RecordSpeed) -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = stringArrayResource(id = R.array.record_speed_texts)
    var selected by remember { mutableStateOf(currentSpeed) }
    Row(modifier.background(Color(0xFF2A2A2A))) {
        RecordSpeed.values().forEachIndexed { index, speed ->
            val text = texts.getOrElse(index) { speed.name }
            val selectedColor = if (speed == selected) Color.Black else Color.White.copy(alpha = 0.5f)
            val bgColor = if (speed == selected) Color.White else Color.Transparent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bgColor)
                    .clickable(touchEnable) {
                        if (speed != selected) {
                            selected = speed
                            onSpeedChanged(speed)
                        }
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, color = selectedColor, fontSize = 15.sp)
            }
        }
    }
}
