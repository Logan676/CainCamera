package com.cgfay.video.compose

import androidx.lifecycle.ViewModel
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.video.bean.EffectMimeType
import com.cgfay.video.bean.EffectType
import com.cgfay.video.fragment.EffectFilterHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoEditViewModel : ViewModel() {

    private val _videoPath = MutableStateFlow("")
    val videoPath: StateFlow<String> get() = _videoPath

    private val _category = MutableStateFlow(EffectMimeType.FILTER)
    val category: StateFlow<EffectMimeType> get() = _category

    private val _effectList = MutableStateFlow<List<EffectType>>(emptyList())
    val effectList: StateFlow<List<EffectType>> get() = _effectList

    private val _selectedIndex = MutableStateFlow(-1)
    val selectedIndex: StateFlow<Int> get() = _selectedIndex

    private val _selectedEffect = MutableStateFlow<String?>(null)
    val selectedEffect: StateFlow<String?> get() = _selectedEffect

    init {
        // initialize with filter effects
        _effectList.value = EffectFilterHelper.getInstance().getEffectFilterData()
    }

    fun setVideoPath(path: String) {
        _videoPath.value = path
    }

    fun selectCategory(category: EffectMimeType) {
        _category.value = category
        _selectedIndex.value = -1
        _effectList.value = when (category) {
            EffectMimeType.FILTER -> EffectFilterHelper.getInstance().getEffectFilterData()
            EffectMimeType.TRANSITION -> EffectFilterHelper.getInstance().getEffectTransitionData()
            EffectMimeType.MULTIFRAME -> EffectFilterHelper.getInstance().getEffectMultiData()
            EffectMimeType.TIME -> emptyList()
        }
    }

    fun selectEffect(index: Int) {
        _selectedIndex.value = index
        val effect = _effectList.value.getOrNull(index)
        _selectedEffect.value = effect?.name
    }
}
