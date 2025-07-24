package com.cgfay.image.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cgfay.image.widget.CropCoverView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ImageCropScreen(bitmap: Bitmap?) {
    val context = LocalContext.current
    val coverView = remember { CropCoverView(context) }
    var cropType by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            AndroidView(factory = { coverView }, modifier = Modifier.fillMaxSize())
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = cropType, modifier = Modifier.width(80.dp))
            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { cropType = "Horizontal" }) { Text("Horizontal") }
            Button(onClick = { cropType = "Ratio" }) { Text("Ratio") }
            Button(onClick = { cropType = "Rotate" }) { Text("Rotate") }
            Button(onClick = { cropType = "Flip" }) { Text("Flip") }
        }
    }
}
