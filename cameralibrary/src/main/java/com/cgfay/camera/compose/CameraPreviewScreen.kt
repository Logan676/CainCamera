package com.cgfay.camera.compose

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cgfay.cameralibrary.R
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.camera.loader.impl.CameraMediaLoader
import com.cgfay.filter.glfilter.resource.ResourceHelper
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.uitls.utils.BitmapUtils
import com.cgfay.camera.compose.PreviewEffectScreen

@Composable
fun CameraPreviewScreen(viewModel: CameraPreviewViewModel = viewModel()) {
    BackHandler { viewModel.onBackPressed() }
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.toggleResourcePanel() },
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        ) {
            Text(text = "Resources")
        }

        Button(
            onClick = { viewModel.toggleEffectPanel() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Text(text = "Effects")
        }

        AnimatedVisibility(
            visible = viewModel.showResourcePanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewResourcePanel()
        }

        AnimatedVisibility(
            visible = viewModel.showEffectPanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewEffectScreen(
                onCompareEffect = { viewModel.onCompareEffect(it) },
                onFilterChange = { color -> viewModel.onFilterChange(color) },
                onMakeupChange = { makeup -> viewModel.onMakeupChange(makeup) }
            )
        }
    }
}

@Composable
private fun PreviewResourcePanel() {
    val resources = remember { ResourceHelper.getResourceList() }
    var selectedIndex by remember { mutableStateOf(-1) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFFE5E5E5)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                factory = { ctx ->
                    android.widget.ImageView(ctx).apply { setImageResource(R.drawable.ic_preview_none) }
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(40.dp)
                    .clickable {
                        selectedIndex = -1
                    }
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF4F4F4))
        ) {
            itemsIndexed(resources) { index, item ->
                ResourceItem(
                    resource = item,
                    selected = index == selectedIndex,
                    onClick = {
                        selectedIndex = index
                    }
                )
            }
        }
    }
}

@Composable
private fun ResourceItem(resource: ResourceData, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val loader = remember { CameraMediaLoader() }
    AndroidView(
        modifier = Modifier
            .padding(2.dp)
            .size(70.dp)
            .clickable { onClick() },
        factory = { ctx ->
            android.widget.FrameLayout(ctx).apply {
                setPadding(2, 2, 2, 2)
                addView(android.widget.ImageView(ctx).apply {
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        update = { layout ->
            val imageView = layout.getChildAt(0) as android.widget.ImageView
            if (resource.thumbPath.startsWith("assets://")) {
                imageView.setImageBitmap(
                    BitmapUtils.getImageFromAssetsFile(
                        context,
                        resource.thumbPath.removePrefix("assets://")
                    )
                )
            } else {
                loader.loadThumbnail(
                    context,
                    imageView,
                    Uri.parse(resource.thumbPath),
                    R.drawable.ic_camera_thumbnail_placeholder,
                    0
                )
            }
            layout.setBackgroundResource(
                if (selected) R.drawable.ic_camera_effect_selected else 0
            )
        }
    )
}
