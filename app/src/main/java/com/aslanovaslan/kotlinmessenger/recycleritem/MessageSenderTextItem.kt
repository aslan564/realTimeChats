package com.aslanovaslan.kotlinmessenger.recycleritem

import android.annotation.SuppressLint
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.model.ChatImageMessage
import com.aslanovaslan.kotlinmessenger.model.ChatTextMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.text_mesage_sender_row.view.*
import java.text.SimpleDateFormat

class MessageSenderTextItem(private val chatMessage: ChatTextMessage, private val user: User) : Item() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            itemView.textViewChatLogToMessage.text = chatMessage.text
            Picasso.get()
                .load(user.profilePicturePath).placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageViewFromChatLog)
        }
    }

    override fun getLayout(): Int {
        return R.layout.text_mesage_sender_row
    }

}