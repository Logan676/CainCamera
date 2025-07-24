package com.cgfay.widget

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.AbstractComposeView
import com.cgfay.widget.compose.CameraTabRow
import com.cgfay.widget.compose.TabItem

/**
 * Bridging view that exposes a traditional View API backed by Compose [CameraTabRow].
 */
class CameraTabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    /** Tab representation mirroring the previous Java API. */
    inner class Tab internal constructor(var title: CharSequence) {
        var position: Int = -1
            internal set

        fun setText(resId: Int): Tab {
            title = context.getString(resId)
            return this
        }

        fun setText(text: CharSequence): Tab {
            title = text
            return this
        }
    }

    /** Listener for tab selection events. */
    interface OnTabSelectedListener {
        fun onTabSelected(tab: Tab)
        fun onTabUnselected(tab: Tab)
        fun onTabReselected(tab: Tab)
    }

    private val tabs = mutableStateListOf<Tab>()
    private val selectedIndex = mutableStateOf(0)
    private val listeners = mutableListOf<OnTabSelectedListener>()

    fun newTab(): Tab = Tab("")

    @JvmOverloads
    fun addTab(tab: Tab, selected: Boolean = false) {
        tab.position = tabs.size
        tabs.add(tab)
        if (selected) {
            selectTab(tab)
        }
    }

    fun addOnTabSelectedListener(listener: OnTabSelectedListener) {
        listeners.add(listener)
    }

    fun setIndicateCenter(center: Boolean) {
        // no-op in Compose implementation
    }

    fun setScrollAutoSelected(auto: Boolean) {
        // no-op in Compose implementation
    }

    private fun selectTab(tab: Tab) {
        val previous = selectedIndex.value
        val newIndex = tab.position
        if (previous == newIndex) {
            listeners.forEach { it.onTabReselected(tab) }
            return
        }
        listeners.forEach { it.onTabUnselected(tabs[previous]) }
        selectedIndex.value = newIndex
        listeners.forEach { it.onTabSelected(tab) }
    }

    override fun Content() {
        CameraTabRow(
            tabs = tabs.map { TabItem(it.title.toString()) },
            selectedTabIndex = selectedIndex,
            onTabSelected = { index -> selectTab(tabs[index]) }
        )
    }
}
