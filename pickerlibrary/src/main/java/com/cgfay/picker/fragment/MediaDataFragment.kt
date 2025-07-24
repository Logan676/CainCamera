package com.cgfay.picker.fragment

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.adapter.MediaDataAdapter
import com.cgfay.picker.adapter.MediaItemDecoration
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.IMediaDataReceiver
import com.cgfay.picker.scanner.MediaDataScanner
import com.cgfay.picker.viewmodel.MediaDataViewModel
import com.cgfay.scan.R
import com.cgfay.uitls.utils.PermissionUtils
import io.reactivex.disposables.Disposable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.Locale

/**
 * 媒体列表页
 */
abstract class MediaDataFragment : Fragment(), IMediaDataReceiver,
    MediaDataAdapter.OnMediaDataChangeListener {

    companion object {
        const val TAG = "MediaDataFragment"
        const val TypeImage = 1
        const val TypeVideo = 2
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = [TypeImage, TypeVideo])
    @Retention(RetentionPolicy.SOURCE)
    annotation class MimeType

    protected var mContext: Context? = null
    protected val mMainHandler = Handler(Looper.getMainLooper())

    private lateinit var mLayoutBlank: View

    protected lateinit var mMediaDataListView: RecyclerView
    protected lateinit var mMediaDataAdapter: MediaDataAdapter

    protected var mLoadingMore = false

    protected var mDataScanner: MediaDataScanner? = null

    protected var mUpdateDisposable: Disposable? = null

    protected val viewModel: MediaDataViewModel by viewModels()

    protected var mSelectedChangeListener: OnSelectedChangeListener? = null

    protected var mMultiSelect = true

    protected var mMediaPickerParam: MediaPickerParam = MediaPickerParam()

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mDataScanner?.setUserVisible(isVisibleToUser)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        mContext = null
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(getContentLayout(), container, false)
        initView(rootView)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (PermissionUtils.permissionChecking(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            initDataProvider()
        }
    }

    override fun onResume() {
        super.onResume()
        mDataScanner?.resume()
        Log.d(TAG, "onResume: ${getMimeType(getMediaType())}")
    }

    override fun onPause() {
        super.onPause()
        mDataScanner?.pause()
        Log.d(TAG, "onPause: ${getMimeType(getMediaType())}")
    }

    override fun onDestroy() {
        mDataScanner?.destroy()
        mDataScanner = null
        mUpdateDisposable?.dispose()
        mUpdateDisposable = null
        super.onDestroy()
    }

    /** 初始化控件 */
    protected open fun initView(rootView: View) {
        initBlankView(rootView)
        mMediaDataListView = rootView.findViewById(R.id.rv_media_thumb_list)
        mMediaDataListView.addItemDecoration(
            MediaItemDecoration(
                mMediaPickerParam.spanCount,
                mMediaPickerParam.spaceSize,
                mMediaPickerParam.isHasEdge
            )
        )
        mMediaDataListView.layoutManager = GridLayoutManager(rootView.context,
            mMediaPickerParam.spanCount)
        (mMediaDataListView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        mMediaDataAdapter = MediaDataAdapter()
        mMediaDataAdapter.addOnMediaDataChangeListener(this)
        mMediaDataListView.adapter = mMediaDataAdapter
        setItemImageSize()

        mMediaDataListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mDataScanner?.isPageScan == true && dy > 0 && !mLoadingMore &&
                    isSlideToBottomLine(mMediaDataListView, MediaDataScanner.PAGE_SIZE / 2)) {
                    mLoadingMore = true
                    mMediaDataListView.post {
                        val scanner = mDataScanner ?: return@post
                        val mediaData = scanner.cacheMediaData
                        if (mediaData.isNotEmpty()) {
                            mMediaDataAdapter.appendNewMediaData(mediaData)
                        }
                        mLoadingMore = false
                    }
                }
            }
        })
    }

    /** 初始化空白控件 */
    private fun initBlankView(rootView: View) {
        mLayoutBlank = rootView.findViewById(R.id.layout_blank)
        val blankView = rootView.findViewById<TextView>(R.id.tv_blank_view)
        val emptyTips = rootView.resources.getString(R.string.media_empty)
        blankView.text = String.format(emptyTips, title)
    }

    /** 判断是否快滚动到底部 */
    protected fun isSlideToBottomLine(recyclerView: RecyclerView, offsetLine: Int): Boolean {
        val manager = recyclerView.layoutManager
        if (manager is LinearLayoutManager) {
            val itemCount = manager.itemCount
            val lastPosition = manager.findLastCompletelyVisibleItemPosition()
            if (lastPosition != itemCount && lastPosition >= itemCount - offsetLine) {
                return true
            }
        }
        return false
    }

    /** 指定加载图片的大小 */
    private fun setItemImageSize() {
        val divSize = (resources.getDimension(com.cgfay.utilslibrary.R.dimen.dp4) *
                (mMediaPickerParam.spanCount + 1)).toInt()
        val imageSize = resources.displayMetrics.widthPixels - divSize
        val resize = imageSize / mMediaPickerParam.spanCount
        mMediaDataAdapter.thumbnailResize = resize
    }

    var mediaPickerParam: MediaPickerParam
        get() = mMediaPickerParam
        set(pickerParam) {
            mMediaPickerParam = pickerParam
        }

    /** 获取媒体数据 */
    val mediaDataList: List<MediaData>
        get() = mMediaDataAdapter.mediaDataList

    fun reset() {
        viewModel.clear()
    }

    /** 获取选中媒体数据 */
    val selectedMediaDataList: List<MediaData>
        get() = viewModel.selectedMedia.value

    /** 是否多选 */
    val isMultiSelect: Boolean
        get() = mMultiSelect

    /** 初始化数据提供者 */
    protected abstract fun initDataProvider()

    /** 获取布局 */
    @LayoutRes
    protected abstract fun getContentLayout(): Int

    /** 获取媒体类型 */
    @MimeType
    protected abstract fun getMediaType(): Int

    /** 获取页面标题 */
    abstract val title: String

    /** 获取mimeType类型字符串 */
    protected fun getMimeType(@MimeType mimeType: Int): String {
        return when (mimeType) {
            TypeImage -> "Image"
            TypeVideo -> "Video"
            else -> "unknown"
        }
    }

    /** 加载某个相册的媒体数据 */
    fun loadAlbumMedia(album: AlbumData) {
        mDataScanner?.loadAlbumMedia(album)
    }

    /** 刷新数据提供者 */
    fun refreshDataProvider() {
        if (mDataScanner == null) {
            mMainHandler.post {
                initDataProvider()
                mDataScanner?.resume()
            }
        }
    }

    /** 刷新媒体数据 */
    fun refreshMediaData() {
        if (mMediaDataAdapter.itemCount == 0) {
            mMediaDataAdapter.notifyDataSetChanged()
        } else {
            refreshVisibleItemChange()
        }
    }

    /** 刷新可见的列表 */
    private fun refreshVisibleItemChange() {
        val manager = mMediaDataListView.layoutManager
        if (manager is GridLayoutManager) {
            val start = manager.findFirstVisibleItemPosition()
            val end = manager.findLastVisibleItemPosition()
            mMediaDataAdapter.notifyItemRangeChanged(start, end - start + 1)
        }
    }

    /** 媒体数据加载完成 */
    override fun onMediaDataObserve(mediaDataList: List<MediaData>) {
        mMediaDataAdapter.setMediaData(mediaDataList)
        mMediaDataListView.post { mMediaDataAdapter.notifyDataSetChanged() }
        checkBlankView()
    }

    override fun getSelectedIndex(mediaData: MediaData): Int {
        return viewModel.getSelectedIndex(mediaData)
    }

    override fun onMediaPreview(mediaData: MediaData) {
        mSelectedChangeListener?.onMediaDataPreview(mediaData)
    }

    override fun onMediaSelectedChange(mediaData: MediaData) {
        if (viewModel.getSelectedIndex(mediaData) >= 0) {
            viewModel.removeSelectedMedia(mediaData)
        } else {
            viewModel.addSelectedMedia(mediaData)
        }
        checkSelectedButton()
        refreshMediaData()
    }

    fun checkSelectedButton() {
        val selectSize = viewModel.selectedMedia.value.size
        mSelectedChangeListener?.let {
            var text = ""
            if (selectSize > 0) {
                text = String.format(Locale.getDefault(), "確定(%d)", selectSize)
            }
            if (getMediaType() == TypeImage && selectSize > 1) {
                text = String.format(Locale.getDefault(), "照片电影(%d)", selectSize)
            }
            it.onSelectedChange(text)
        }
    }

    /** 检查是否显示空白页 */
    private fun checkBlankView() {
        if (mediaDataList.isNotEmpty()) {
            mMediaDataListView.visibility = View.VISIBLE
            mLayoutBlank.visibility = View.GONE
        } else {
            mMediaDataListView.visibility = View.GONE
            mLayoutBlank.visibility = View.VISIBLE
        }
    }

    interface OnSelectedChangeListener {
        fun onMediaDataPreview(mediaData: MediaData)
        fun onSelectedChange(text: String)
    }

    fun addOnSelectedChangeListener(listener: OnSelectedChangeListener?) {
        mSelectedChangeListener = listener
    }
}

