package com.aslanovaslan.kotlinmessenger.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
@Keep
class ChatMessage(
    val id: String,
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long
) {
    constructor() : this("", "", "", "", -1)
}