package com.cgfay.uitls.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cgfay.utilslibrary.R

/**
 * Dialog shown when leaving the preview page.
 */
class BackPressedDialogFragment : DialogFragment() {

    private var hostActivity: Activity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hostActivity = if (context is Activity) context else activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent: Fragment? = parentFragment
        val messageRes = arguments?.getInt(MESSAGE) ?: R.string.back_pressed_message
        return ComposeView(requireContext()).apply {
            setContent {
                BackPressedDialog(
                    message = stringResource(id = messageRes),
                    onConfirm = {
                        parent?.activity?.finish()
                        dismiss()
                    },
                    onDismiss = { dismiss() }
                )
            }
        }
    }

    companion object {
        const val MESSAGE = "message"
    }
}

@Composable
private fun BackPressedDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = "确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "取消") }
        }
    )
}
