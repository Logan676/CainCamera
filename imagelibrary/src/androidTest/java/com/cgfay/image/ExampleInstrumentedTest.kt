package com.cgfay.image

import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertTextEquals
import org.junit.Rule
import org.junit.Test
import androidx.compose.runtime.Composable

class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun AdditionText(a: Int, b: Int) {
        Text(text = "${'$'}{a + b}", modifier = Modifier.testTag("addition"))
    }

    @Test
    fun addition_isCorrect() {
        composeTestRule.setContent { AdditionText(2, 2) }
        composeTestRule.onNodeWithTag("addition").assertTextEquals("4")
    }
}
