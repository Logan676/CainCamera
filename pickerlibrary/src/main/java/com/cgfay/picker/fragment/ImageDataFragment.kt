package com.cgfay.picker.fragment

import android.view.View
import androidx.loader.app.LoaderManager
import com.cgfay.picker.scanner.ImageDataScanner
import com.cgfay.scan.R

/**
 * 图片选择列表
 */
class ImageDataFragment : MediaDataFragment() {

    override fun initView(rootView: View) {
        super.initView(rootView)
        mMultiSelect = true
        mMediaDataAdapter?.setShowCheckbox(true)
    }

    override fun initDataProvider() {
        if (mDataScanner == null) {
            mDataScanner = ImageDataScanner(mContext, LoaderManager.getInstance(this), this)
            mDataScanner?.setUserVisible(userVisibleHint)
            mMediaDataAdapter?.setShowCheckbox(true)
        }
    }

    override fun getContentLayout(): Int = R.layout.fragment_image_list

    override fun getMediaType(): Int = TypeImage

    override val title: String
        get() = "图片"
}
