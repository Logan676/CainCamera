package com.cgfay.uitls.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cgfay.uitls.dialog.DialogBuilder
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent: Fragment? = parentFragment
        val resId = arguments?.getInt(MESSAGE, -1) ?: -1
        return DialogBuilder.from(requireActivity(), R.layout.dialog_two_button)
            .setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .setText(R.id.tv_dialog_title, if (resId == -1) R.string.back_pressed_message else resId)
            .setDismissOnClick(R.id.btn_dialog_cancel, true)
            .setText(R.id.btn_dialog_cancel, "取消")
            .setDismissOnClick(R.id.btn_dialog_ok, true)
            .setText(R.id.btn_dialog_ok, "确定")
            .setOnClickListener(R.id.btn_dialog_ok) {
                parent?.activity?.finish()
            }
            .create()
    }

    companion object {
        const val MESSAGE = "message"
    }
}
