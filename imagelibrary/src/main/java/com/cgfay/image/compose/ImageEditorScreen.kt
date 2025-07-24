package com.cgfay.image.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cgfay.uitls.utils.BitmapUtils
import java.io.File

@Composable
fun ImageEditorScreen(imagePath: String?, deleteInputFile: Boolean, navController: NavController) {
    val bitmap = remember(imagePath) { imagePath?.let { BitmapUtils.getBitmapFromFile(File(it), 0, 0, true) } }
    var page by remember { mutableStateOf(1) }
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (page) {
                0 -> ImageCropScreen(bitmap)
                else -> ImageFilterScreen(bitmap, navController)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { page = 0 }) { Text("Crop") }
            Button(onClick = { page = 1 }) { Text("Filter") }
        }
    }
}
