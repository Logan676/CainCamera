package com.cgfay.uitls.utils

import android.util.Log

/**
 * Loads native libraries once and caches the result.
 */
object NativeLibraryLoader {
    private const val TAG = "NativeLibraryLoader"
    private val loadedLibraries = HashSet<String>()

    @JvmStatic
    fun loadLibraries(vararg libraries: String) {
        for (lib in libraries) {
            if (loadedLibraries.contains(lib)) {
                continue
            }
            try {
                System.loadLibrary(lib)
                loadedLibraries.add(lib)
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library $lib", e)
                throw RuntimeException("Failed to load native library: $lib", e)
            }
        }
    }
}
