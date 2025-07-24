package com.cgfay.camera.compose

import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.cameralibrary.R
import com.cgfay.camera.camera.CameraParam
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.MakeupHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.uitls.utils.BitmapUtils
import java.io.File

/**
 * Composable replacement for PreviewEffectFragment.
 */
@Composable
fun PreviewEffectScreen(
    onCompare: (Boolean) -> Unit,
    onFilterChange: (DynamicColor?) -> Unit,
    onMakeupChange: (DynamicMakeup?) -> Unit,
    initialFilterIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val beautyNames = context.resources.getStringArray(R.array.preview_beauty)
    val makeupNames = context.resources.getStringArray(R.array.preview_makeup)
    val filters = remember { FilterHelper.getFilterList() }
    var selectedTab by remember { mutableStateOf(0) }
    var beautyIndex by remember { mutableStateOf(0) }
    var sliderValue by remember { mutableStateOf((CameraParam.getInstance().beauty.beautyIntensity * 100).toInt()) }
    var filterIndex by remember { mutableStateOf(initialFilterIndex) }
    Column(modifier.background(Color(0xAA000000))) {
        // Compare button
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 20.dp, top = 8.dp)
                .size(40.dp)
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> onCompare(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onCompare(false)
                    }
                    true
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color.White)
        }

        // Slider for beauty values
        if (selectedTab == 0) {
            androidx.compose.material.Slider(
                value = sliderValue.toFloat(),
                onValueChange = {
                    sliderValue = it.toInt()
                    processBeautyParam(beautyIndex, sliderValue)
                },
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text(text = stringResource(R.string.tab_preview_beauty)) }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text(text = stringResource(R.string.tab_preview_makeup)) }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text(text = stringResource(R.string.tab_preview_filter)) }
        }

        when (selectedTab) {
            0 -> BeautyList(names = beautyNames.toList(), selected = beautyIndex, onSelected = { index ->
                beautyIndex = index
                setSeekBarBeautyParam(index) { sliderValue = it }
            })
            1 -> MakeupList(names = makeupNames.toList()) { index ->
                if (index == 0) {
                    val folderPath = MakeupHelper.getMakeupDirectory(context) + File.separator + MakeupHelper.getMakeupList()[1].unzipFolder
                    val makeup = try {
                        ResourceJsonCodec.decodeMakeupData(folderPath)
                    } catch (e: Exception) {
                        null
                    }
                    onMakeupChange(makeup)
                } else {
                    onMakeupChange(null)
                }
            }
            2 -> FilterList(filters = filters, selected = filterIndex, onSelected = { idx, data ->
                val color = if (data.name != "none") {
                    val folderPath = FilterHelper.getFilterDirectory(context) + File.separator + data.unzipFolder
                    try { ResourceJsonCodec.decodeFilterData(folderPath) } catch (e: Exception) { null }
                } else null
                onFilterChange(color)
                filterIndex = idx
            })
        }
    }
}

@Composable
private fun BeautyList(names: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(names) { index, name ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onSelected(index) }
                    .background(if (selected == index) Color.White else Color.Transparent)
                    .padding(8.dp)
            ) { Text(text = name, color = Color.White) }
        }
    }
}

@Composable
private fun MakeupList(names: List<String>, onSelected: (Int) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(names) { index, name ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onSelected(index) }
                    .background(Color(0x33000000))
                    .padding(8.dp)
            ) { Text(text = name, color = Color.White) }
        }
    }
}

@Composable
private fun FilterList(filters: List<ResourceData>, selected: Int, onSelected: (Int, ResourceData) -> Unit) {
    val context = LocalContext.current
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(filters) { index, data ->
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onSelected(index, data) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    factory = { ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                    modifier = Modifier.size(60.dp),
                    update = { imageView ->
                        val bitmap = if (data.thumbPath.startsWith("assets://")) {
                            BitmapUtils.getImageFromAssetsFile(context, data.thumbPath.removePrefix("assets://"))
                        } else {
                            BitmapUtils.getBitmapFromFile(data.thumbPath)
                        }
                        imageView.setImageBitmap(bitmap)
                        imageView.setBackgroundResource(if (selected == index) R.drawable.ic_camera_effect_selected else 0)
                    }
                )
                Text(text = data.name, color = Color.White)
            }
        }
    }
}

private fun setSeekBarBeautyParam(position: Int, update: (Int) -> Unit) {
    val beauty = CameraParam.getInstance().beauty
    val value = when (position) {
        0 -> (beauty.beautyIntensity * 100).toInt()
        1 -> (beauty.complexionIntensity * 100).toInt()
        2 -> (beauty.faceLift * 100).toInt()
        3 -> (beauty.faceShave * 100).toInt()
        4 -> (beauty.faceNarrow * 100).toInt()
        5 -> (((beauty.chinIntensity + 1f) * 50)).toInt()
        6 -> (beauty.nasolabialFoldsIntensity * 100).toInt()
        7 -> (((beauty.foreheadIntensity + 1f) * 50)).toInt()
        8 -> (beauty.eyeEnlargeIntensity * 100).toInt()
        9 -> (((beauty.eyeDistanceIntensity + 1f) * 50)).toInt()
        10 -> (((beauty.eyeCornerIntensity + 1f) * 50)).toInt()
        11 -> (beauty.eyeFurrowsIntensity * 100).toInt()
        12 -> (beauty.eyeBagsIntensity * 100).toInt()
        13 -> (beauty.eyeBrightIntensity * 100).toInt()
        14 -> (beauty.noseThinIntensity * 100).toInt()
        15 -> (beauty.alaeIntensity * 100).toInt()
        16 -> (beauty.proboscisIntensity * 100).toInt()
        17 -> (beauty.mouthEnlargeIntensity * 100).toInt()
        18 -> (beauty.teethBeautyIntensity * 100).toInt()
        else -> 0
    }
    update(value)
}

private fun processBeautyParam(position: Int, progress: Int) {
    val beauty = CameraParam.getInstance().beauty
    when (position) {
        0 -> beauty.beautyIntensity = progress / 100f
        1 -> beauty.complexionIntensity = progress / 100f
        2 -> beauty.faceLift = progress / 100f
        3 -> beauty.faceShave = progress / 100f
        4 -> beauty.faceNarrow = progress / 100f
        5 -> beauty.chinIntensity = (progress - 50f) / 50f
        6 -> beauty.nasolabialFoldsIntensity = progress / 100f
        7 -> beauty.foreheadIntensity = (progress - 50f) / 50f
        8 -> beauty.eyeEnlargeIntensity = progress / 100f
        9 -> beauty.eyeDistanceIntensity = (progress - 50f) / 50f
        10 -> beauty.eyeCornerIntensity = (progress - 50f) / 50f
        11 -> beauty.eyeFurrowsIntensity = progress / 100f
        12 -> beauty.eyeBagsIntensity = progress / 100f
        13 -> beauty.eyeBrightIntensity = progress / 100f
        14 -> beauty.noseThinIntensity = progress / 100f
        15 -> beauty.alaeIntensity = progress / 100f
        16 -> beauty.proboscisIntensity = progress / 100f
        17 -> beauty.mouthEnlargeIntensity = progress / 100f
        18 -> beauty.teethBeautyIntensity = progress / 100f
    }
}

