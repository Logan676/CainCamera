package com.cgfay.camera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import com.cgfay.camera.widget.BeautyList
import com.cgfay.camera.widget.FilterGrid
import com.cgfay.camera.widget.MakeupList
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cgfay.cameralibrary.R
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.MakeupHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import java.io.File

@Composable
fun PreviewEffectScreen(
    onCompareEffect: (Boolean) -> Unit,
    onFilterChange: (DynamicColor?) -> Unit,
    onMakeupChange: (DynamicMakeup?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tabIndex by remember { mutableStateOf(0) }
    var beautyIndex by remember { mutableStateOf(0) }
    var makeupIndex by remember { mutableStateOf(-1) }
    var filterIndex by remember { mutableStateOf(0) }
    val beautyNames = stringArrayResource(id = R.array.preview_beauty)
    val makeupNames = stringArrayResource(id = R.array.preview_makeup)
    val filterList = remember { FilterHelper.getFilterList() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC000000))
    ) {
        Box(Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_compare_normal),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onCompareEffect(true)
                                tryAwaitRelease()
                                onCompareEffect(false)
                            }
                        )
                    }
            )
        }
        if (tabIndex == 0) {
            Slider(
                value = 0.5f,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        when (tabIndex) {
            0 -> BeautyList(names = beautyNames, selected = beautyIndex) { beautyIndex = it }
            1 -> MakeupList(names = makeupNames, selected = makeupIndex) { index ->
                makeupIndex = index
                if (index == 0 && MakeupHelper.getMakeupList().size > 1) {
                    val folderPath = MakeupHelper.getMakeupDirectory(context) + File.separator +
                            MakeupHelper.getMakeupList()[1].unzipFolder
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
            2 -> FilterGrid(list = filterList, selected = filterIndex) { index ->
                filterIndex = index
                val data = filterList[index]
                if (data.name != "none") {
                    val folderPath = FilterHelper.getFilterDirectory(context) + File.separator + data.unzipFolder
                    val color = try {
                        ResourceJsonCodec.decodeFilterData(folderPath)
                    } catch (e: Exception) {
                        null
                    }
                    onFilterChange(color)
                } else {
                    onFilterChange(null)
                }
            }
        }
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }) {
                Text(text = stringResource(id = R.string.tab_preview_beauty), modifier = Modifier.padding(8.dp))
            }
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }) {
                Text(text = stringResource(id = R.string.tab_preview_makeup), modifier = Modifier.padding(8.dp))
            }
            Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }) {
                Text(text = stringResource(id = R.string.tab_preview_filter), modifier = Modifier.padding(8.dp))
            }
        }
    }
}
