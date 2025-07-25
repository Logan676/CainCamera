package com.cgfay.filter.glfilter.utils

/**
 * Utility methods for texture coordinate rotation.
 */
object TextureRotationUtils {
    const val CoordsPerVertex = 2

    val CubeVertices = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    val TextureVertices = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )

    // x-axis flipped
    val TextureVertices_flipx = floatArrayOf(
        1f, 0f,
        0f, 0f,
        1f, 1f,
        0f, 1f
    )

    val TextureVertices_90 = floatArrayOf(
        1f, 0f,
        1f, 1f,
        0f, 0f,
        0f, 1f
    )

    val TextureVertices_180 = floatArrayOf(
        1f, 1f,
        0f, 1f,
        1f, 0f,
        0f, 0f
    )

    val TextureVertices_270 = floatArrayOf(
        0f, 1f,
        0f, 0f,
        1f, 1f,
        1f, 0f
    )

    /** Indices used with glDrawElements. */
    val Indices = shortArrayOf(
        0, 1, 2,
        2, 1, 3
    )

    @JvmStatic
    fun getRotation(rotation: Rotation, flipHorizontal: Boolean, flipVertical: Boolean): FloatArray {
        var rotatedTex = when (rotation) {
            Rotation.ROTATION_90 -> TextureVertices_90
            Rotation.ROTATION_180 -> TextureVertices_180
            Rotation.ROTATION_270 -> TextureVertices_270
            Rotation.NORMAL -> TextureVertices
        }
        if (flipHorizontal) {
            rotatedTex = floatArrayOf(
                flip(rotatedTex[0]), rotatedTex[1],
                flip(rotatedTex[2]), rotatedTex[3],
                flip(rotatedTex[4]), rotatedTex[5],
                flip(rotatedTex[6]), rotatedTex[7]
            )
        }
        if (flipVertical) {
            rotatedTex = floatArrayOf(
                rotatedTex[0], flip(rotatedTex[1]),
                rotatedTex[2], flip(rotatedTex[3]),
                rotatedTex[4], flip(rotatedTex[5]),
                rotatedTex[6], flip(rotatedTex[7])
            )
        }
        return rotatedTex
    }

    private fun flip(i: Float) = if (i == 0f) 1f else 0f
}
