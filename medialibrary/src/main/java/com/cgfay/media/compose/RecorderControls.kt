package com.cgfay.media.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecorderControls(onStart: () -> Unit, onStop: () -> Unit) {
    Row {
        Button(onClick = onStart) { Text("Start") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onStop) { Text("Stop") }
    }
}
