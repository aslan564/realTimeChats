package com.aslanovaslan.kotlinmessenger.model

import java.util.*

class ChatAudioMessage(
    val audioPath: String,
    override val id: String,
    override val senderId: String,
    override val receiverId: String,
    override val time: Date,
    override val type: String=MessageType.AUDIO
):Message {
    constructor() : this("", "", "", "", Date(0))

    override fun toString(): String {
        return "ChatImageMessage(picturePath='$audioPath', id='$id', senderId='$senderId', receiverId='$receiverId', time=$time, type='$type')"
    }

}