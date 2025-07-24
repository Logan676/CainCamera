package com.cgfay.camera.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.cgfay.cameralibrary.R
import com.cgfay.camera.camera.CameraParam

class CameraSettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraSettingScreen(onSelectWatermark = {
                startActivity(Intent(this, WatermarkActivity::class.java))
            })
        }
    }
}

@Composable
fun CameraSettingScreen(onSelectWatermark: () -> Unit) {
    var drawFacePoints by remember { mutableStateOf(CameraParam.getInstance().drawFacePoints) }
    var showFps by remember { mutableStateOf(CameraParam.getInstance().showFps) }

    val verticalMargin = dimensionResource(id = R.dimen.setting_page_margin_top)
    val startMargin = dimensionResource(id = R.dimen.setting_page_margin_start)
    val endMargin = dimensionResource(id = R.dimen.setting_page_margin_end)
    val dividerHeight = dimensionResource(id = R.dimen.setting_split_height)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingRow(
            text = stringResource(id = R.string.select_watermark),
            startMargin = startMargin,
            endMargin = endMargin,
            verticalMargin = verticalMargin,
            onClick = onSelectWatermark
        )
        Divider(thickness = dividerHeight, color = Color(0xFF696969))

        SettingRow(
            text = if (drawFacePoints) stringResource(R.string.show_face_points) else stringResource(R.string.hide_face_points),
            startMargin = startMargin,
            endMargin = endMargin,
            verticalMargin = verticalMargin,
            onClick = {
                CameraParam.getInstance().drawFacePoints = !CameraParam.getInstance().drawFacePoints
                drawFacePoints = CameraParam.getInstance().drawFacePoints
            }
        )
        Divider(thickness = dividerHeight, color = Color(0xFF696969))

        SettingRow(
            text = if (showFps) stringResource(R.string.show_fps) else stringResource(R.string.hide_fps),
            startMargin = startMargin,
            endMargin = endMargin,
            verticalMargin = verticalMargin,
            onClick = {
                CameraParam.getInstance().showFps = !CameraParam.getInstance().showFps
                showFps = CameraParam.getInstance().showFps
            }
        )
        Divider(thickness = dividerHeight, color = Color(0xFF696969))
    }
}

@Composable
private fun SettingRow(
    text: String,
    startMargin: Dp,
    endMargin: Dp,
    verticalMargin: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = startMargin, end = endMargin, top = verticalMargin, bottom = verticalMargin),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.ic_settings_arrow_right),
            contentDescription = null
        )
    }
}
