package com.aslanovaslan.kotlinmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val userId: String?, val username: String?, val profilePicturePath: String?):
    Parcelable {
    constructor() : this("", "", "")

    override fun toString(): String {
        return "User(userId=$userId, username=$username, profilePicturePath=$profilePicturePath)"
    }

}