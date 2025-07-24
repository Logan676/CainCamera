package com.cgfay.video.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Types of video effects.
 */
enum class EffectMimeType(val displayName: String, val mimeType: Int) : Parcelable {
    FILTER("\u6ee4\u955c", 0),
    TRANSITION("\u8f6c\u573a", 1),
    MULTIFRAME("\u5206\u5c4f", 2),
    TIME("\u65f6\u95f4", 3);

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(displayName)
        dest.writeInt(mimeType)
    }

    companion object CREATOR : Parcelable.Creator<EffectMimeType> {
        override fun createFromParcel(source: Parcel): EffectMimeType =
            valueOf(source.readString()!!)

        override fun newArray(size: Int): Array<EffectMimeType?> = arrayOfNulls(size)
    }
}
