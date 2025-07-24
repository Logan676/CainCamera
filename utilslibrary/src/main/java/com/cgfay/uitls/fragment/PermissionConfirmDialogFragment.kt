package com.cgfay.uitls.fragment

import android.Manifest
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
import androidx.fragment.app.Fragment
import com.cgfay.uitls.utils.PermissionUtils
import com.cgfay.utilslibrary.R

/**
 * Runtime permission request dialog implemented in Kotlin.
 */
class PermissionConfirmDialogFragment : DialogFragment() {

    private var errorForceClose = false
    private var requestCode: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent: Fragment = parentFragment ?: this
        requestCode = requireArguments().getInt(REQUEST_CODE)
        errorForceClose = requireArguments().getBoolean(ERROR_CLOSE)
        val message = requireArguments().getString(ARG_MESSAGE) ?: ""
        return ComposeView(requireContext()).apply {
            setContent {
                PermissionConfirmDialog(
                    message = message,
                    onConfirm = {
                        when (requestCode) {
                            PermissionUtils.REQUEST_CAMERA_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.CAMERA), PermissionUtils.REQUEST_CAMERA_PERMISSION)
                            PermissionUtils.REQUEST_STORAGE_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                            PermissionUtils.REQUEST_SOUND_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        }
                        dismiss()
                    },
                    onDismiss = {
                        if (errorForceClose) parent.activity?.finish()
                        dismiss()
                    }
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
        fun newInstance(message: String, requestCode: Int, errorForceClose: Boolean = false): PermissionConfirmDialogFragment {
            val dialog = PermissionConfirmDialogFragment()
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
private fun PermissionConfirmDialog(
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
