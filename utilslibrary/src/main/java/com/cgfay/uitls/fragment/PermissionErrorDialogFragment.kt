package com.cgfay.uitls.fragment

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.cgfay.uitls.dialog.DialogBuilder
import com.cgfay.uitls.dialog.DialogComponent
import com.cgfay.uitls.dialog.DialogType
import com.cgfay.utilslibrary.R

/**
 * Dialog shown when required permissions are denied.
 */
class PermissionErrorDialogFragment : DialogFragment() {

    private var errorForceClose = false
    private var requestCode: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        requestCode = requireArguments().getInt(REQUEST_CODE)
        errorForceClose = requireArguments().getBoolean(ERROR_CLOSE)
        return DialogBuilder.from(activity, DialogType.TWO_BUTTON)
            .setText(DialogComponent.TITLE, requireArguments().getString(ARG_MESSAGE))
            .setText(DialogComponent.CANCEL_BUTTON, "取消")
            .setDismissOnClick(DialogComponent.CANCEL_BUTTON, true)
            .setText(DialogComponent.OK_BUTTON, "确定")
            .setOnClickListener(DialogComponent.OK_BUTTON) {
                if (errorForceClose) {
                    activity.finish()
                }
            }
            .create()
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
