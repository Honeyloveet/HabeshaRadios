package com.sampro.habesharadios.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class StationsModelParcelable(
    var name: String? = "",
    var uri: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(uri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StationsModelParcelable> {
        override fun createFromParcel(parcel: Parcel): StationsModelParcelable {
            return StationsModelParcelable(parcel)
        }

        override fun newArray(size: Int): Array<StationsModelParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
