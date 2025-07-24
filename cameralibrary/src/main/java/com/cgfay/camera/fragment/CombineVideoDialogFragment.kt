package com.cgfay.camera.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.NonNull
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf

/**
 * Dialog fragment showing progress while combining videos using Jetpack Compose.
 */
class CombineVideoDialogFragment : DialogFragment() {

    private var message by mutableStateOf("")

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent: Fragment? = parentFragment
        message = requireArguments().getString(ARG_MESSAGE) ?: ""
        val dimable = requireArguments().getBoolean(KEY_DIMABLE)

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                var text by remember { mutableStateOf(message) }
                // keep reference so setProgressMessage can update
                messageState = { text = it }
                DialogContent(text)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(composeView)
            .create()
        dialog.setCancelable(dimable)
        dialog.setCanceledOnTouchOutside(dimable)
        dialog.setOnKeyListener { _: DialogInterface?, keyCode: Int, _: KeyEvent? ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        return dialog
    }

    private var messageState: ((String) -> Unit)? = null

    /** Update progress message shown in the dialog. */
    fun setProgressMessage(msg: String) {
        messageState?.invoke(msg) ?: run { message = msg }
    }

    companion object {
        private const val ARG_MESSAGE = "message"
        private const val KEY_DIMABLE = "dimable"

        @JvmStatic
        fun newInstance(message: String, dimable: Boolean = false) =
            CombineVideoDialogFragment().apply {
                arguments = bundleOf(ARG_MESSAGE to message, KEY_DIMABLE to dimable)
            }
    }
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
