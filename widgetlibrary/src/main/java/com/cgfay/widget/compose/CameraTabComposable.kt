package com.cgfay.widget.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Jetpack Compose implementation of [com.cgfay.widget.CameraTabView].
 *
 * @param tabs Items to display.
 * @param selectedTabIndex Currently selected tab index. If omitted, internal state is used.
 * @param modifier Modifier applied to the TabRow.
 * @param indicatorColor Color of the selected tab indicator.
 * @param onTabSelected Callback invoked when a tab is selected.
 */
@Composable
fun CameraTabRow(
    tabs: List<TabItem>,
    selectedTabIndex: MutableState<Int> = remember { mutableStateOf(0) },
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.Unspecified,
    onTabSelected: (index: Int) -> Unit = {}
) {
    TabRow(
        selectedTabIndex = selectedTabIndex.value,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        contentColor = indicatorColor
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedTabIndex.value,
                onClick = {
                    selectedTabIndex.value = index
                    onTabSelected(index)
                },
                text = { Text(text = tab.title) },
                icon = tab.icon?.let { { Icon(it, contentDescription = tab.title) } }
            )
        }
    }
}
