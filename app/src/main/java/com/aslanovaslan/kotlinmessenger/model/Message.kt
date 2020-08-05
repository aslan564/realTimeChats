package com.aslanovaslan.kotlinmessenger.model

import android.media.Image
import java.util.*

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val AUDIO = "AUDIO"
    const val VIDEO = "VIDEO"
}

interface Message {
    val id: String
    val senderId: String
    val receiverId: String
    val time: Date
    val type:String
}