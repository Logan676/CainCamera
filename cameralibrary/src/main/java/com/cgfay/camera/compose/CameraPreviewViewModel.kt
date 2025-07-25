package com.cgfay.camera.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import com.cgfay.camera.presenter.CameraPreviewView
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup

/**
 * ViewModel used by [CameraActivity] and related composables. It also acts as
 * the [CameraPreviewView] implementation so the presenter can dispatch
 * callbacks without requiring a Fragment instance.
 */
class CameraPreviewViewModel : ViewModel(), CameraPreviewView {

    var showResourcePanel by mutableStateOf(false)
        private set

    var showEffectPanel by mutableStateOf(false)
        private set

    var showSettingPanel by mutableStateOf(false)
        private set

    fun toggleResourcePanel() {
        showResourcePanel = !showResourcePanel
    }

    fun hideResourcePanel() {
        showResourcePanel = false
    }

    fun toggleEffectPanel() {
        showEffectPanel = !showEffectPanel
    }

    fun hideEffectPanel() {
        showEffectPanel = false
    }

    fun toggleSettingPanel() {
        showSettingPanel = !showSettingPanel
    }

    fun hideSettingPanel() {
        showSettingPanel = false
    }

    fun onBackPressed(): Boolean {
        return when {
            showEffectPanel -> {
                hideEffectPanel()
                true
            }
            showResourcePanel -> {
                hideResourcePanel()
                true
            }
            showSettingPanel -> {
                hideSettingPanel()
                true
            }
            else -> false
        }
    }

    fun cancelRecordIfNeeded() {}

    fun onCompareEffect(compare: Boolean) {}

    fun onFilterChange(color: DynamicColor?) {}

    fun onMakeupChange(makeup: DynamicMakeup?) {}
}

