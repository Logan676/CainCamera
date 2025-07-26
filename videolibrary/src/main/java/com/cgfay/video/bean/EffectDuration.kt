package com.cgfay.video.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable

/**
 * Duration for a selected effect.
 */
@Immutable
data class EffectDuration(
    var effectType: EffectType,
    var start: Long,
    var end: Long,
    var color: Int = 0
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        effectType = parcel.readParcelable(EffectType::class.java.classLoader) ?: EffectType(EffectMimeType.FILTER, "", 0, ""),
        start = parcel.readLong(),
        end = parcel.readLong(),
        color = parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(effectType, flags)
        dest.writeLong(start)
        dest.writeLong(end)
        dest.writeInt(color)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EffectDuration> {
        override fun createFromParcel(source: Parcel): EffectDuration = EffectDuration(source)
        override fun newArray(size: Int): Array<EffectDuration?> = arrayOfNulls(size)
    }
}
