package com.cgfay.picker.fragment

import android.view.View
import android.widget.TextView
import androidx.loader.app.LoaderManager
import com.cgfay.picker.scanner.VideoDataScanner
import com.cgfay.scan.R

/**
 * 视频选择列表
 */
class VideoDataFragment : MediaDataFragment() {

    private var mMultiSelectView: TextView? = null

    override fun initView(rootView: View) {
        super.initView(rootView)
        mMultiSelect = false
        mMultiSelectView = rootView.findViewById(R.id.tv_multi_video)
        mMultiSelectView?.setOnClickListener { processMultiSelectView() }
        if (!mMultiSelect) {
            mMultiSelectView?.setText(R.string.video_multi_picker)
        } else {
            mMultiSelectView?.setText(R.string.video_single_picker)
        }
    }

    private fun processMultiSelectView() {
        mMultiSelect = !mMultiSelect
        if (!mMultiSelect) {
            mMultiSelectView?.setText(R.string.video_multi_picker)
            mMediaDataAdapter?.let {
                it.setShowCheckbox(false)
                it.notifyDataSetChanged()
            }
            viewModel.clear()
            mSelectedChangeListener?.onSelectedChange("")
        } else {
            mMultiSelectView?.setText(R.string.video_single_picker)
            mMediaDataAdapter?.let {
                it.setShowCheckbox(true)
                it.notifyDataSetChanged()
            }
        }
    }

    override fun getContentLayout(): Int = R.layout.fragment_video_list

    override fun initDataProvider() {
        if (mDataScanner == null) {
            mDataScanner = VideoDataScanner(mContext, LoaderManager.getInstance(this), this)
            mDataScanner?.setUserVisible(userVisibleHint)
            mMediaDataAdapter?.setShowCheckbox(mMultiSelect)
        }
    }

    override fun getMediaType(): Int = TypeVideo

    override val title: String
        get() = "视频"
}
