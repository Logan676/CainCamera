package com.badlogic.gdx

import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun helloTextIsDisplayed() {
        composeTestRule.setContent {
            Text("Hello Compose")
        }
        composeTestRule.onNodeWithText("Hello Compose").assertIsDisplayed()
    }
}
