package com.cgfay.uitls.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CombineVideoDialog(message: String, dimable: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = if (dimable) onDismiss else {},
        confirmButton = {},
        text = { DialogContent(message) }
    )
}

@Composable
private fun DialogContent(text: String) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(30.dp))
            Text(text = text, color = Color(0xFF999999), fontSize = 14.sp)
        }
    }
}
