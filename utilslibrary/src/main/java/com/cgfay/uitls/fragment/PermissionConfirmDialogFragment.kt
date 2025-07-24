package com.cgfay.uitls.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cgfay.uitls.utils.PermissionUtils

/**
 * Runtime permission request dialog implemented in Kotlin.
 */
class PermissionConfirmDialogFragment : DialogFragment() {

    private var errorForceClose = false
    private var requestCode: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent: Fragment = parentFragment ?: this
        requestCode = requireArguments().getInt(REQUEST_CODE)
        errorForceClose = requireArguments().getBoolean(ERROR_CLOSE)
        return AlertDialog.Builder(requireActivity())
            .setMessage(requireArguments().getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                when (requestCode) {
                    PermissionUtils.REQUEST_CAMERA_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.CAMERA), PermissionUtils.REQUEST_CAMERA_PERMISSION)
                    PermissionUtils.REQUEST_STORAGE_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    PermissionUtils.REQUEST_SOUND_PERMISSION -> parent.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PermissionUtils.REQUEST_SOUND_PERMISSION)
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                if (errorForceClose) {
                    parent.activity?.finish()
                }
            }
            .create()
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
