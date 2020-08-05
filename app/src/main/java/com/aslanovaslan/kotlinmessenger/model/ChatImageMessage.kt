package com.aslanovaslan.kotlinmessenger.model

import java.util.*

class ChatImageMessage(
    val picturePath: String,
    override val id: String,
    override val senderId: String,
    override val receiverId: String,
    override val time: Date,
    override val type: String=MessageType.IMAGE
):Message {
    constructor() : this("", "", "", "", Date(0))

    override fun toString(): String {
        return "ChatImageMessage(picturePath='$picturePath', id='$id', senderId='$senderId', receiverId='$receiverId', time=$time, type='$type')"
    }

}