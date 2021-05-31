package com.example.tomatology

import android.os.Parcel
import android.os.Parcelable

data class Prediction(val label: String?, val idLabel: Int, val percentage: Float): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readFloat()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(label)
        p0?.writeInt(idLabel)
        p0?.writeFloat(percentage)
    }

    companion object CREATOR : Parcelable.Creator<Prediction> {
        override fun createFromParcel(parcel: Parcel): Prediction {
            return Prediction(parcel)
        }

        override fun newArray(size: Int): Array<Prediction?> {
            return arrayOfNulls(size)
        }
    }
}