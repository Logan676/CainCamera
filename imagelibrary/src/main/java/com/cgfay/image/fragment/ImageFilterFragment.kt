package com.cgfay.image.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.widget.GLImageSurfaceView
import com.cgfay.image.activity.ImagePreviewActivity
import com.cgfay.uitls.utils.BitmapUtils
import java.io.File
import java.nio.ByteBuffer

class ImageFilterFragment : Fragment() {

    private var mActivity: Activity? = null
    private val mMainHandler = Handler(Looper.getMainLooper())
    private var mBitmap: Bitmap? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { ImageFilterScreen() }
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
    }

    private fun getImagePath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = "$directoryPath/${Environment.DIRECTORY_PICTURES}/CainCamera_${System.currentTimeMillis()}.jpeg"
        val file = File(path)
        if (!file.parentFile!!.exists()) {
            file.parentFile!!.mkdirs()
        }
        return path
    }

    @Composable
    private fun ImageFilterScreen() {
        val context = LocalContext.current
        val glView = remember {
            GLImageSurfaceView(context).apply {
                setCaptureCallback(object : GLImageSurfaceView.CaptureCallback {
                    override fun onCapture(buffer: ByteBuffer?, width: Int, height: Int) {
                        if (buffer == null) return
                        mMainHandler.post {
                            val filePath = getImagePath(context)
                            BitmapUtils.saveBitmap(filePath, buffer, width, height)
                            val intent = Intent(context, ImagePreviewActivity::class.java)
                            intent.putExtra(ImagePreviewActivity.PATH, filePath)
                            startActivity(intent)
                        }
                    }
                })
            }
        }
        LaunchedEffect(mBitmap) {
            mBitmap?.let { glView.setBitmap(it) }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { glView }, modifier = Modifier.weight(1f))
            FilterList(glView)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { glView.getCaptureFrame() }) { Text(text = "Save") }
            }
        }
    }

    @Composable
    private fun FilterList(glView: GLImageSurfaceView) {
        val context = LocalContext.current
        val filters = remember { FilterHelper.getFilterList() }
        var selected by remember { mutableStateOf(0) }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            itemsIndexed(filters) { index, data ->
                FilterItem(
                    data = data,
                    selected = selected == index,
                    onClick = {
                        selected = index
                        glView.setFilter(data)
                    }
                )
            }
        }
    }

    @Composable
    private fun FilterItem(data: ResourceData, selected: Boolean, onClick: () -> Unit) {
        val context = LocalContext.current
        val bitmap = remember(data.thumbPath) {
            if (data.thumbPath.startsWith("assets://")) {
                BitmapUtils.getImageFromAssetsFile(context, data.thumbPath.removePrefix("assets://"))
            } else {
                BitmapUtils.getBitmapFromFile(File(data.thumbPath), 0, 0, true)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(4.dp)
                .clickable(onClick = onClick)
        ) {
            bitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(60.dp)) }
            Text(text = data.name, color = if (selected) Color.Blue else Color.White)
        }
    }
}
