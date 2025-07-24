package com.cgfay.uitls.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.cgfay.uitls.dialog.DialogBuilder
import com.cgfay.uitls.dialog.DialogComponent
import com.cgfay.uitls.dialog.DialogType
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
        return DialogBuilder.from(requireActivity(), DialogType.TWO_BUTTON)
            .setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .setText(DialogComponent.TITLE, if (resId == -1) R.string.back_pressed_message else resId)
            .setDismissOnClick(DialogComponent.CANCEL_BUTTON, true)
            .setText(DialogComponent.CANCEL_BUTTON, "取消")
            .setDismissOnClick(DialogComponent.OK_BUTTON, true)
            .setText(DialogComponent.OK_BUTTON, "确定")
            .setOnClickListener(DialogComponent.OK_BUTTON) {
                parent?.activity?.finish()
            }
            .create()
    }

    companion object {
        const val MESSAGE = "message"
    }
}
