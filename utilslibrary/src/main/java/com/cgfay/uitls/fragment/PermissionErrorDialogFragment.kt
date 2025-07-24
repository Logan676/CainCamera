package com.cgfay.uitls.fragment

import android.app.Activity
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
import com.cgfay.utilslibrary.R

/**
 * Dialog shown when required permissions are denied.
 */
class PermissionErrorDialogFragment : DialogFragment() {

    private var errorForceClose = false
    private var requestCode: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()
        requestCode = requireArguments().getInt(REQUEST_CODE)
        errorForceClose = requireArguments().getBoolean(ERROR_CLOSE)
        val message = requireArguments().getString(ARG_MESSAGE) ?: ""
        return ComposeView(requireContext()).apply {
            setContent {
                PermissionErrorDialog(
                    message = message,
                    onConfirm = {
                        if (errorForceClose) {
                            activity.finish()
                        }
                        dismiss()
                    },
                    onDismiss = { dismiss() }
                )
            }
        }
    }
    }

    companion object {
        private const val ARG_MESSAGE = "message"
        private const val REQUEST_CODE = "requestCode"
        private const val ERROR_CLOSE = "forceClose"

        @JvmStatic
        fun newInstance(message: String, requestCode: Int, errorForceClose: Boolean = true): PermissionErrorDialogFragment {
            val dialog = PermissionErrorDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            args.putInt(REQUEST_CODE, requestCode)
            args.putBoolean(ERROR_CLOSE, errorForceClose)
            dialog.arguments = args
            return dialog
        }
    }
}

@Composable
private fun PermissionErrorDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(android.R.string.cancel)) }
        }
    )
}
