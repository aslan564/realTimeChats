package com.aslanovaslan.kotlinmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val userId: String?, val username: String?, val profilePicturePath: String?,val oneSignalIds:String?=null
                ,val userBio:String?="User bios") :
    Parcelable {
    constructor() : this("", "", "",null)

    override fun toString(): String {
        return "User(userId=$userId, username=$username, profilePicturePath=$profilePicturePath, oneSignalIds=$oneSignalIds, userBio=$userBio)"
    }


}