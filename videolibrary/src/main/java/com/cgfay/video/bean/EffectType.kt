package com.cgfay.video.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable

/**
 * Information of a video effect.
 */
@Immutable
data class EffectType(
    val mimeType: EffectMimeType,
    val name: String,
    val id: Int,
    val thumb: String
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        mimeType = parcel.readParcelable(EffectMimeType::class.java.classLoader) ?: EffectMimeType.FILTER,
        name = parcel.readString() ?: "",
        id = parcel.readInt(),
        thumb = parcel.readString() ?: ""
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(mimeType, flags)
        dest.writeString(name)
        dest.writeInt(id)
        dest.writeString(thumb)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EffectType> {
        override fun createFromParcel(source: Parcel): EffectType = EffectType(source)
        override fun newArray(size: Int): Array<EffectType?> = arrayOfNulls(size)
    }
}
