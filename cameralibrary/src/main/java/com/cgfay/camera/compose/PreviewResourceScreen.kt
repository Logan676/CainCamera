package com.cgfay.camera.compose

import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.cameralibrary.R
import com.cgfay.camera.loader.impl.CameraMediaLoader
import com.cgfay.filter.glfilter.resource.ResourceHelper
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.resource.bean.ResourceType
import com.cgfay.uitls.utils.BitmapUtils

/**
 * Sticker resource screen implemented with Jetpack Compose.
 */
@Composable
fun PreviewResourceScreen(
    onResourceChange: (ResourceData) -> Unit,
    modifier: Modifier = Modifier
) {
    val noneResource = remember {
        ResourceData(
            "none",
            "assets://resource/none.zip",
            ResourceType.NONE,
            "none",
            "assets://thumbs/resource/none.png"
        )
    }
    val resources = remember { ResourceHelper.getResourceList() }
    var selectedIndex by remember { mutableStateOf(-1) }
    Column(
        modifier = modifier
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
                    ImageView(ctx).apply { setImageResource(R.drawable.ic_preview_none) }
                },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(40.dp)
                    .clickable {
                        selectedIndex = -1
                        onResourceChange(noneResource)
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
                        onResourceChange(item)
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
            FrameLayout(ctx).apply {
                setPadding(2, 2, 2, 2)
                addView(ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        update = { layout ->
            val imageView = layout.getChildAt(0) as ImageView
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
