package com.cgfay.uitls.fragment

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.cgfay.uitls.dialog.DialogBuilder
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
        return DialogBuilder.from(activity, R.layout.dialog_two_button)
            .setText(R.id.tv_dialog_title, requireArguments().getString(ARG_MESSAGE))
            .setText(R.id.btn_dialog_cancel, "取消")
            .setDismissOnClick(R.id.btn_dialog_cancel, true)
            .setText(R.id.btn_dialog_ok, "确定")
            .setOnClickListener(R.id.btn_dialog_ok) {
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
