package com.aslanovaslan.kotlinmessenger.model

data class ChatMessage(
    val id: String,
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long
) {
    constructor() : this("", "", "", "", -1)
}