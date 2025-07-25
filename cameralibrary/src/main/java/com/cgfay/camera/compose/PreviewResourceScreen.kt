package com.cgfay.camera.compose

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
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
            Image(
                painter = painterResource(id = R.drawable.ic_preview_none),
                contentDescription = null,
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
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(70.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (selected) colorResource(id = R.color.mediumaquamarine) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (resource.thumbPath.startsWith("assets://")) {
            val bitmap = remember(resource.thumbPath) {
                BitmapUtils.getImageFromAssetsFile(
                    context,
                    resource.thumbPath.removePrefix("assets://")
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
                model = Uri.parse(resource.thumbPath),
                placeholder = R.drawable.ic_camera_thumbnail_placeholder,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
