package com.cgfay.camera.camera

/**
 * Types for calculating camera preview and capture sizes.
 */
enum class CalculateType {
    /** The minimum supported size. */
    Min,
    /** The maximum supported size. */
    Max,
    /** Slightly larger than the expected size. */
    Larger,
    /** Slightly smaller than the expected size. */
    Lower
}
