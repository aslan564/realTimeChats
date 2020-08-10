package com.aslanovaslan.kotlinmessenger.model

import java.util.*

class ChatVideoMessage(
    val videoPath: String,
    val videoPathUri: String,
    override val id: String,
    override val senderId: String,
    override val receiverId: String,
    override val time: Date,
    override val type: String=MessageType.VIDEO
):Message {
    constructor() : this("","", "", "", "", Date(0))

    override fun toString(): String {
        return "ChatVideoMessage(videoPath='$videoPath', videoPathUri='$videoPathUri', id='$id', senderId='$senderId', receiverId='$receiverId', time=$time, type='$type')"
    }


}