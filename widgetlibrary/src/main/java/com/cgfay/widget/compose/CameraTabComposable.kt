package com.cgfay.widget.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Simple composable replacement for [com.cgfay.widget.CameraTabView].
 *
 * @param tabs Labels for each tab.
 * @param modifier Modifier applied to the TabRow.
 * @param onTabSelected Callback when a tab is selected.
 */
@Composable
fun CameraTabRow(
    tabs: List<String>,
    modifier: Modifier = Modifier,
    onTabSelected: (index: Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    TabRow(selectedTabIndex = selectedTab, modifier = modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedTab,
                onClick = {
                    selectedTab = index
                    onTabSelected(index)
                },
                text = { Text(text = title) }
            )
        }
    }
}
