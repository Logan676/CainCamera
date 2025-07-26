package com.cgfay.video.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable

/**
 * Types of video effects.
 */
@Immutable
enum class EffectMimeType(val displayName: String, val mimeType: Int) : Parcelable {
    FILTER("\u6ee4\u955c", 0),
    TRANSITION("\u8f6c\u573a", 1),
    MULTIFRAME("\u5206\u5c4f", 2),
    TIME("\u65f6\u95f4", 3);

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EffectMimeType> {
        override fun createFromParcel(source: Parcel): EffectMimeType =
            values()[source.readInt()]

        override fun newArray(size: Int): Array<EffectMimeType?> = arrayOfNulls(size)
    }
}
