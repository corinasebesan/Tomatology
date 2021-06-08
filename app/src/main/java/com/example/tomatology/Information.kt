package com.example.tomatology

import android.os.Parcel
import android.os.Parcelable

data class Information(val causes: String, val diseaseName: String, val prevention: String, val symptoms: String, val symptomsSummary: String, val treatment: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(causes)
        dest?.writeString(diseaseName)
        dest?.writeString(prevention)
        dest?.writeString(symptoms)
        dest?.writeString(symptomsSummary)
        dest?.writeString(treatment)
    }

    companion object CREATOR : Parcelable.Creator<Information> {
        override fun createFromParcel(parcel: Parcel): Information {
            return Information(parcel)
        }

        override fun newArray(size: Int): Array<Information?> {
            return arrayOfNulls(size)
        }
    }
}