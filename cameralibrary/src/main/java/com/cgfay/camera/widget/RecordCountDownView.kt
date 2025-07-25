package com.cgfay.camera.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import kotlinx.coroutines.delay

class RecordCountDownState {
    var isCounting by mutableStateOf(false)
        private set
    var current by mutableStateOf(0)
        private set

    fun start(count: Int) {
        current = count
        isCounting = true
    }

    fun cancel() {
        isCounting = false
        current = 0
    }
}

@Composable
fun rememberRecordCountDownState() = remember { RecordCountDownState() }

@Composable
fun RecordCountDownView(
    state: RecordCountDownState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 48.sp, textAlign = TextAlign.Center),
    onCountDownEnd: () -> Unit = {},
    onCountDownCancel: () -> Unit = {}
) {
    if (state.isCounting && state.current == 0) {
        onCountDownEnd()
    }
    AnimatedVisibility(
        visible = state.isCounting && state.current > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            Text(text = state.current.toString(), style = textStyle)
        }
    }
    LaunchedEffect(state.isCounting) {
        if (state.isCounting) {
            val count = state.current
            while (state.isCounting && state.current > 0) {
                delay(1000)
                state.current--
            }
            if (count > 0 && state.current == 0 && state.isCounting) {
                state.isCounting = false
                onCountDownEnd()
            } else if (!state.isCounting) {
                onCountDownCancel()
            }
        }
    }
}
