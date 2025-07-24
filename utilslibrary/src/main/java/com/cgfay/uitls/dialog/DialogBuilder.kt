package com.cgfay.uitls.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cgfay.utilslibrary.R

/**
 * Simple dialog builder that renders content using Jetpack Compose instead of XML.
 */
class DialogBuilder private constructor(
    private val context: Context,
    @LayoutRes private val layout: Int
) {

    private val resBinder = HashMap<Int, ResBinder>()
    private var cancelable = true
    private var cancelOnTouchOutside = true

    fun setBackgroundColor(@IdRes id: Int, @ColorInt backgroundColor: Int): DialogBuilder {
        resBinder.getOrPut(id) { ResBinder(id) }.backgroundColor = backgroundColor
        return this
    }

    fun setDrawable(@IdRes id: Int, @DrawableRes drawable: Int): DialogBuilder {
        resBinder.getOrPut(id) { ResBinder(id) }.drawable = drawable
        return this
    }

    fun setText(@IdRes id: Int, text: String?): DialogBuilder {
        resBinder.getOrPut(id) { ResBinder(id) }.text = text
        return this
    }

    fun setDismissOnClick(@IdRes id: Int, dismissOnClick: Boolean): DialogBuilder {
        resBinder.getOrPut(id) { ResBinder(id) }.dismissOnClick = dismissOnClick
        return this
    }

    fun setOnClickListener(@IdRes id: Int, listener: View.OnClickListener): DialogBuilder {
        resBinder.getOrPut(id) { ResBinder(id) }.clickListener = listener
        return this
    }

    fun setCancelable(cancelable: Boolean): DialogBuilder {
        this.cancelable = cancelable
        return this
    }

    fun setCanceledOnTouchOutside(canceled: Boolean): DialogBuilder {
        cancelOnTouchOutside = canceled
        return this
    }

    fun create(): Dialog {
        val dialog = Dialog(context, R.style.CommonDialogStyle)
        val view = ComposeView(context)
        view.setContent {
            when (layout) {
                R.layout.dialog_one_button -> OneButtonDialog(dialog, resBinder)
                R.layout.dialog_two_button -> TwoButtonDialog(dialog, resBinder)
                else -> {}
            }
        }
        dialog.setContentView(view)
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelOnTouchOutside)
        return dialog
    }

    fun show(): Dialog {
        val dialog = create()
        dialog.show()
        return dialog
    }

    private data class ResBinder(
        @IdRes val id: Int,
        @ColorInt var backgroundColor: Int = 0,
        @DrawableRes var drawable: Int = 0,
        var text: String? = null,
        var dismissOnClick: Boolean = false,
        var clickListener: View.OnClickListener? = null
    )

    companion object {
        fun from(context: Context, @LayoutRes layout: Int): DialogBuilder = DialogBuilder(context, layout)
    }
}

@Composable
private fun OneButtonDialog(dialog: Dialog, binders: Map<Int, DialogBuilder.ResBinder>) {
    val title = binders[R.id.tv_dialog_title]?.text ?: ""
    val message = binders[R.id.tv_dialog_message]?.text
    val okBinder = binders[R.id.btn_dialog_ok]
    val closeBinder = binders[R.id.iv_dialog_close]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title)
            if (!message.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = message)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = {
                if (okBinder?.dismissOnClick == true) dialog.dismiss()
                okBinder?.clickListener?.onClick(null)
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = okBinder?.text ?: context.getString(R.string.btn_dialog_ok))
            }
        }
        if (closeBinder != null) {
            IconButton(
                onClick = {
                    if (closeBinder.dismissOnClick) dialog.dismiss()
                    closeBinder.clickListener?.onClick(null)
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            ) {
                Icon(painterResource(id = closeBinder.drawable.takeIf { it != 0 } ?: R.drawable.ic_close), contentDescription = null)
            }
        }
    }
}

@Composable
private fun TwoButtonDialog(dialog: Dialog, binders: Map<Int, DialogBuilder.ResBinder>) {
    val title = binders[R.id.tv_dialog_title]?.text ?: ""
    val message = binders[R.id.tv_dialog_message]?.text
    val okBinder = binders[R.id.btn_dialog_ok]
    val cancelBinder = binders[R.id.btn_dialog_cancel]
    val closeBinder = binders[R.id.iv_dialog_close]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title)
            if (!message.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = message)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(
                    onClick = {
                        if (cancelBinder?.dismissOnClick == true) dialog.dismiss()
                        cancelBinder?.clickListener?.onClick(null)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(text = cancelBinder?.text ?: context.getString(R.string.btn_dialog_cancel)) }
                Button(
                    onClick = {
                        if (okBinder?.dismissOnClick == true) dialog.dismiss()
                        okBinder?.clickListener?.onClick(null)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(text = okBinder?.text ?: context.getString(R.string.btn_dialog_ok)) }
            }
        }
        if (closeBinder != null) {
            IconButton(
                onClick = {
                    if (closeBinder.dismissOnClick) dialog.dismiss()
                    closeBinder.clickListener?.onClick(null)
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            ) {
                Icon(painterResource(id = closeBinder.drawable.takeIf { it != 0 } ?: R.drawable.ic_close), contentDescription = null)
            }
        }
    }
}
