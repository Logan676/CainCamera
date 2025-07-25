package com.cgfay.camera.camera

import android.graphics.SurfaceTexture

/**
 * SurfaceTexture准备成功监听器
 */
fun interface OnSurfaceTextureListener {
    fun onSurfaceTexturePrepared(surfaceTexture: SurfaceTexture)
}
