package com.cgfay.picker.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.cgfay.picker.compose.MediaPreviewScreen
import com.cgfay.picker.MediaPicker
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.adapter.AlbumDataAdapter
import com.cgfay.picker.adapter.AlbumItemDecoration
import com.cgfay.picker.adapter.MediaDataPagerAdapter
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.AlbumDataScanner
import com.cgfay.picker.selector.OnMediaSelector
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.scan.R
import com.cgfay.uitls.utils.PermissionUtils

class MediaPickerFragment : AppCompatDialogFragment(), MediaDataFragment.OnSelectedChangeListener {

    companion object {
        const val TAG = "MediaPickerFragment"
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activity: FragmentActivity? = null
    private var pickerParam: MediaPickerParam = MediaPickerParam()

    private var mediaSelector: OnMediaSelector? = null

    // album list
    private var albumDataScanner: AlbumDataScanner? = null
    private lateinit var albumDataAdapter: AlbumDataAdapter
    private var albumRecyclerView: RecyclerView? = null

    private var tabLayout: com.google.android.material.tabs.TabLayout? = null
    private var viewPager: ViewPager? = null

    private var imageDataFragment: MediaDataFragment? = null
    private var videoDataFragment: MediaDataFragment? = null
    private val mediaDataFragments = mutableListOf<MediaDataFragment>()
    private var mediaDataPagerAdapter: MediaDataPagerAdapter? = null

    private val albumTitle = mutableStateOf("所有照片")
    private val selectText = mutableStateOf("")
    private val showAlbumList = mutableStateOf(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = if (context is FragmentActivity) context else activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PickerDialogStyle)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.getSerializable(MediaPicker.PICKER_PARAMS)?.let {
            pickerParam = it as MediaPickerParam
        }
        return ComposeView(requireContext()).apply {
            setContent { MediaPickerScreen() }
        }
    }

    @Composable
    private fun MediaPickerScreen() {
        BackHandler { animateCloseFragment() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            TopBar()
            AndroidView(factory = { ctx ->
                com.google.android.material.tabs.TabLayout(ctx).also { tabLayout = it }
            }, modifier = Modifier.fillMaxWidth(), update = {
                initTabView(it)
            })
            AndroidView(factory = { ctx ->
                ViewPager(ctx).also { viewPager = it }
            }, modifier = Modifier.weight(1f), update = {
                initMediaListView(it)
            })
            if (showAlbumList.value) {
                AndroidView(factory = { ctx ->
                    RecyclerView(ctx).also { albumRecyclerView = it }
                }, modifier = Modifier.weight(1f), update = {
                    initAlbumRecyclerView(it)
                })
            }
        }
    }

    @Composable
    private fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(id = R.drawable.ic_media_picker_close),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { animateCloseFragment() }
            )
            Spacer(modifier = Modifier.width(18.dp))
            Row(
                modifier = Modifier.clickable {
                    showAlbumList.value = !showAlbumList.value
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = albumTitle.value, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painterResource(id = R.drawable.ic_media_album_indicator),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (selectText.value.isNotEmpty()) {
                Text(
                    text = selectText.value,
                    color = Color.Red,
                    modifier = Modifier.clickable { onSelectClick() }
                )
            }
        }
    }

    private fun onSelectClick() {
        val index = viewPager?.currentItem ?: 0
        mediaSelector?.onMediaSelect(activity, mediaDataFragments[index].selectedMediaDataList)
        if (pickerParam.isAutoDismiss) {
            animateCloseFragment()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (PermissionUtils.permissionChecking(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            activity?.let { initAlbumController(it) }
        } else {
            PermissionUtils.requestStoragePermission(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activity?.let { initAlbumController(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        albumDataScanner?.resume()
    }

    override fun onPause() {
        super.onPause()
        albumDataScanner?.pause()
    }

    override fun onDestroy() {
        albumDataScanner?.destroy()
        albumDataScanner = null
        super.onDestroy()
    }

    private fun initAlbumController(context: Context) {
        if (albumDataScanner == null) {
            albumDataScanner = AlbumDataScanner(context, LoaderManager.getInstance(this), pickerParam)
            albumDataScanner?.setAlbumDataReceiver(object : AlbumDataScanner.AlbumDataReceiver {
                override fun onAlbumDataObserve(albumDataList: List<AlbumData>) {
                    albumDataAdapter.setAlbumDataList(albumDataList)
                }

                override fun onAlbumDataReset() {
                    albumDataAdapter.reset()
                }
            })
        }
    }

    private fun initAlbumRecyclerView(recyclerView: RecyclerView) {
        albumDataAdapter = AlbumDataAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(AlbumItemDecoration())
        recyclerView.adapter = albumDataAdapter
        albumDataAdapter.addOnAlbumSelectedListener { album ->
            albumTitle.value = album.displayName
            showAlbumList.value = false
            mediaDataFragments.forEach { it.loadAlbumMedia(album) }
        }
    }

    private fun initMediaListView(vp: ViewPager) {
        mediaDataFragments.clear()
        if (!pickerParam.showImageOnly()) {
            if (videoDataFragment == null) videoDataFragment = VideoDataFragment()
            videoDataFragment?.mediaPickerParam = pickerParam
            videoDataFragment?.addOnSelectedChangeListener(this)
            mediaDataFragments.add(videoDataFragment!!)
        }
        if (!pickerParam.showVideoOnly()) {
            if (imageDataFragment == null) imageDataFragment = ImageDataFragment()
            imageDataFragment?.mediaPickerParam = pickerParam
            imageDataFragment?.addOnSelectedChangeListener(this)
            mediaDataFragments.add(imageDataFragment!!)
        }
        mediaDataPagerAdapter = MediaDataPagerAdapter(childFragmentManager, mediaDataFragments)
        vp.adapter = mediaDataPagerAdapter
    }

    private fun initTabView(tab: com.google.android.material.tabs.TabLayout) {
        viewPager?.let { tab.setupWithViewPager(it) }
        tab.visibility = if (mediaDataFragments.size > 1) View.VISIBLE else View.GONE
        tab.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: com.google.android.material.tabs.TabLayout.Tab) {
                mediaDataFragments[t.position].checkSelectedButton()
            }
            override fun onTabUnselected(t: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(t: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    override fun onMediaDataPreview(mediaData: MediaData) {
        if (mediaData.isVideo) {
            if (videoDataFragment?.isMultiSelect == true) {
                parseVideoOrientation(mediaData)
                onPreviewMedia(mediaData)
            } else {
                mediaSelector?.let {
                    val list = arrayListOf<MediaData>()
                    parseVideoOrientation(mediaData)
                    list.add(mediaData)
                    it.onMediaSelect(activity, list)
                    if (pickerParam.isAutoDismiss) animateCloseFragment()
                }
            }
        } else {
            onPreviewMedia(mediaData)
        }
    }

    private fun parseVideoOrientation(mediaData: MediaData) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(MediaMetadataUtils.getPath(requireContext(), mediaData.contentUri))
        when (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)) {
            "90" -> mediaData.orientation = 90
            "180" -> mediaData.orientation = 180
            "270" -> mediaData.orientation = 270
            else -> mediaData.orientation = 0
        }
    }

    override fun onSelectedChange(text: String) {
        selectText.value = text
    }

    private fun onPreviewMedia(mediaData: MediaData) {
        val dialog = AppCompatDialog(requireContext(), R.style.PickerPreviewStyle)
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent { MediaPreviewScreen(mediaData) { dialog.dismiss() } }
        })
        dialog.show()
    }

    private fun animateCloseFragment() {
        mainHandler.post { closeFragment() }
    }

    private fun closeFragment() {
        parentFragment?.childFragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
            ?: activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
    }

    fun setOnMediaSelector(selector: OnMediaSelector) {
        mediaSelector = selector
    }
}
