package com.cgfay.uitls.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
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
    private val type: DialogType
) {

    private val resBinder = HashMap<DialogComponent, ResBinder>()
    private var cancelable = true
    private var cancelOnTouchOutside = true

    fun setBackgroundColor(component: DialogComponent, @ColorInt backgroundColor: Int): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.backgroundColor = backgroundColor
        return this
    }

    fun setDrawable(component: DialogComponent, @DrawableRes drawable: Int): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.drawable = drawable
        return this
    }

    fun setText(component: DialogComponent, text: String?): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.text = text
        return this
    }

    fun setText(component: DialogComponent, @androidx.annotation.StringRes textRes: Int): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.text = textRes
        return this
    }

    fun setDismissOnClick(component: DialogComponent, dismissOnClick: Boolean): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.dismissOnClick = dismissOnClick
        return this
    }

    fun setOnClickListener(component: DialogComponent, listener: View.OnClickListener): DialogBuilder {
        resBinder.getOrPut(component) { ResBinder(component) }.clickListener = listener
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
            when (type) {
                DialogType.ONE_BUTTON -> OneButtonDialog(dialog, resBinder, context)
                DialogType.TWO_BUTTON -> TwoButtonDialog(dialog, resBinder, context)
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
        val component: DialogComponent,
        @ColorInt var backgroundColor: Int = 0,
        @DrawableRes var drawable: Int = 0,
        var text: Any? = null,
        var dismissOnClick: Boolean = false,
        var clickListener: View.OnClickListener? = null
    ) {
        fun getText(context: Context): String? = when (val t = text) {
            is Int -> context.getString(t)
            is String -> t
            else -> null
        }
    }

    companion object {
        fun from(context: Context, type: DialogType): DialogBuilder = DialogBuilder(context, type)
    }
}

@Composable
private fun OneButtonDialog(
    dialog: Dialog,
    binders: Map<DialogComponent, DialogBuilder.ResBinder>,
    context: Context
) {
    val title = binders[DialogComponent.TITLE]?.getText(context) ?: ""
    val message = binders[DialogComponent.MESSAGE]?.getText(context)
    val okBinder = binders[DialogComponent.OK_BUTTON]
    val closeBinder = binders[DialogComponent.CLOSE_BUTTON]

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
                Text(text = okBinder?.getText(context) ?: context.getString(R.string.btn_dialog_ok))
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
private fun TwoButtonDialog(
    dialog: Dialog,
    binders: Map<DialogComponent, DialogBuilder.ResBinder>,
    context: Context
) {
    val title = binders[DialogComponent.TITLE]?.getText(context) ?: ""
    val message = binders[DialogComponent.MESSAGE]?.getText(context)
    val okBinder = binders[DialogComponent.OK_BUTTON]
    val cancelBinder = binders[DialogComponent.CANCEL_BUTTON]
    val closeBinder = binders[DialogComponent.CLOSE_BUTTON]

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
                ) { Text(text = cancelBinder?.getText(context) ?: context.getString(R.string.btn_dialog_cancel)) }
                Button(
                    onClick = {
                        if (okBinder?.dismissOnClick == true) dialog.dismiss()
                        okBinder?.clickListener?.onClick(null)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(text = okBinder?.getText(context) ?: context.getString(R.string.btn_dialog_ok)) }
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
