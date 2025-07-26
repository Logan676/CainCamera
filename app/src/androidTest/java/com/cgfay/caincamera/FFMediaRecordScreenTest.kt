package com.cgfay.caincamera

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.cgfay.caincamera.activity.FFMediaRecordActivity
import org.junit.Rule
import org.junit.Test

class FFMediaRecordScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<FFMediaRecordActivity>()

    @Test
    fun recordButtonHidesSwitchButton() {
        composeTestRule.onNodeWithText("Switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("record_button")
            .performTouchInput { down(center); up() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Switch").assertDoesNotExist()
    }
}
