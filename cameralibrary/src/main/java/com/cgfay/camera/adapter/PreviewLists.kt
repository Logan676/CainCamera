package com.cgfay.camera.adapter

import android.net.Uri
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import com.cgfay.cameralibrary.R
import com.cgfay.camera.loader.impl.CameraMediaLoader
import com.cgfay.filter.glfilter.resource.bean.ResourceData
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
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(70.dp)
                    .clickable { onSelect(index) }
                    .border(
                        width = 2.dp,
                        color = if (index == selected) colorResource(id = R.color.mediumaquamarine) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbPath.startsWith("assets://")) {
                    val bitmap = remember(item.thumbPath) {
                        BitmapUtils.getImageFromAssetsFile(
                            context,
                            item.thumbPath.removePrefix("assets://")
                        )
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    loader.Thumbnail(
                        model = Uri.parse(item.thumbPath),
                        placeholder = R.drawable.ic_camera_thumbnail_placeholder,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

