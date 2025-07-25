package com.cgfay.camera.adapter

import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.cameralibrary.R
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.camera.loader.impl.CameraMediaLoader
import com.cgfay.uitls.utils.BitmapUtils

@Composable
fun BeautyList(names: Array<String>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(names) { index, item ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onSelect(index) }
                    .background(
                        if (index == selected) Color.White.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                androidx.compose.material.Text(text = item, color = Color.White)
            }
        }
    }
}

@Composable
fun MakeupList(names: Array<String>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(names) { index, item ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onSelect(index) }
                    .background(
                        if (index == selected) Color.White.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                androidx.compose.material.Text(text = item, color = Color.White)
            }
        }
    }
}

@Composable
fun FilterGrid(list: List<ResourceData>, selected: Int, onSelect: (Int) -> Unit) {
    val context = LocalContext.current
    val loader = remember { CameraMediaLoader() }
    LazyVerticalGrid(columns = GridCells.Fixed(5)) {
        gridItemsIndexed(list) { index, item ->
            AndroidView(
                modifier = Modifier
                    .padding(4.dp)
                    .size(70.dp)
                    .clickable { onSelect(index) },
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
                    if (item.thumbPath.startsWith("assets://")) {
                        imageView.setImageBitmap(
                            BitmapUtils.getImageFromAssetsFile(
                                context,
                                item.thumbPath.removePrefix("assets://")
                            )
                        )
                    } else {
                        loader.loadThumbnail(
                            context,
                            imageView,
                            Uri.parse(item.thumbPath),
                            R.drawable.ic_camera_thumbnail_placeholder,
                            0
                        )
                    }
                    layout.setBackgroundResource(
                        if (index == selected) R.drawable.ic_camera_effect_selected else 0
                    )
                }
            )
        }
    }
}

