package com.cgfay.camera.fragment

import android.content.Context
import android.content.Intent
import com.cgfay.image.activity.ImageEditActivity
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.selector.OnMediaSelector
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.video.activity.MultiVideoActivity
import com.cgfay.video.activity.VideoCutActivity

/**
 * Handles selected media items and launches the appropriate compose-based screen.
 */
class NormalMediaSelector : OnMediaSelector {
    override fun onMediaSelect(context: Context, mediaDataList: List<MediaData>) {
        if (mediaDataList.isEmpty()) return

        val pathList = ArrayList<String>(mediaDataList.size)
        var hasVideo = false
        for (media in mediaDataList) {
            hasVideo = hasVideo || media.isVideo
            pathList.add(MediaMetadataUtils.getPath(context, media.contentUri))
        }

        if (hasVideo) {
            if (pathList.size > 1) {
                context.startActivity(Intent(context, MultiVideoActivity::class.java).apply {
                    putStringArrayListExtra(MultiVideoActivity.PATH, pathList)
                })
            } else {
                context.startActivity(Intent(context, VideoCutActivity::class.java).apply {
                    putExtra(VideoCutActivity.PATH, pathList[0])
                })
            }
        } else {
            context.startActivity(Intent(context, ImageEditActivity::class.java).apply {
                putExtra(ImageEditActivity.IMAGE_PATH, pathList[0])
            })
        }
    }
}
