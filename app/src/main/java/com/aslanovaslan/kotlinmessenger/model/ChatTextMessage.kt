package com.aslanovaslan.kotlinmessenger.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import java.util.*

@Keep
class ChatTextMessage(
    val text: String,
    override val id: String,
    override val senderId: String,
    override val receiverId: String,
    override val time: Date,
    override val type: String = MessageType.TEXT
) : Message {
    constructor() : this("", "", "", "", Date(0))
}