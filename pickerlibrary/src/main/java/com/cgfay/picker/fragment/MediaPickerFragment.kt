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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
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

import com.cgfay.picker.MediaPicker
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.adapter.AlbumDataAdapter
import com.cgfay.picker.adapter.AlbumItemDecoration
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.AlbumDataScanner
import com.cgfay.picker.compose.ImageDataScreen
import com.cgfay.picker.compose.VideoDataScreen
import com.cgfay.picker.selector.OnMediaSelector
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.scan.R
import com.cgfay.uitls.utils.PermissionUtils

class MediaPickerFragment : AppCompatDialogFragment() {

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

    private var selectedTab by mutableStateOf(0)
    private val currentAlbum = mutableStateOf<AlbumData?>(null)

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
        val tabs = remember {
            mutableListOf<String>().apply {
                if (!pickerParam.showImageOnly()) add("视频")
                if (!pickerParam.showVideoOnly()) add("图片")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            TopBar()
            if (tabs.size > 1) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                            Text(text = title)
                        }
                    }
                }
            }
            val loader = LoaderManager.getInstance(this@MediaPickerFragment)
            Box(modifier = Modifier.weight(1f)) {
                if (tabs.getOrNull(selectedTab) == "视频") {
                    VideoDataScreen(loader, currentAlbum.value, pickerParam.spanCount) {
                        onMediaDataPreview(it)
                    }
                } else {
                    ImageDataScreen(loader, currentAlbum.value, pickerParam.spanCount) {
                        onMediaDataPreview(it)
                    }
                }
            }
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
        mediaSelector?.onMediaSelect(activity ?: return, emptyList())
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
            currentAlbum.value = album
        }
    }

    private fun onMediaDataPreview(mediaData: MediaData) {
        if (mediaData.isVideo) {
            parseVideoOrientation(mediaData)
        }
        onPreviewMedia(mediaData)
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


    private fun onPreviewMedia(mediaData: MediaData) {
        val fragment = MediaPreviewFragment.newInstance(mediaData)
        childFragmentManager.beginTransaction()
            .add(fragment, FRAGMENT_TAG)
            .commitNowAllowingStateLoss()
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
