package com.cgfay.caincamera

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue
import com.cgfay.caincamera.ui.MusicPlayerContent

class MusicPlayerComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playPauseButtonCallsCallback() {
        var clicked = false
        val playingState = mutableStateOf(false)
        composeTestRule.setContent {
            MusicPlayerContent(
                path = "test",
                isPlaying = playingState.value,
                progress = 0f,
                speedProgress = 50f,
                onPlayPause = {
                    clicked = true
                    playingState.value = !playingState.value
                },
                onProgressChange = {},
                onSpeedChange = {}
            )
        }
        composeTestRule.onNodeWithTag("play_pause_button").performClick()
        composeTestRule.runOnIdle { assertTrue(clicked) }
    }
}
