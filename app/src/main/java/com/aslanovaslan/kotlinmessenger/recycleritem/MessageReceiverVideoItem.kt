package com.aslanovaslan.kotlinmessenger.recycleritem

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activity.chats.ChatLog
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.model.ChatVideoMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.video_mesage_receiver_row.view.*
import kotlinx.android.synthetic.main.video_mesage_sender_row.view.*
import kotlinx.android.synthetic.main.video_mesage_sender_row.view.play_video
import java.text.SimpleDateFormat

class MessageReceiverVideoItem(
    private val chatVideoMessage: ChatVideoMessage,
    private val user: User,
    private val chatLogContext: ChatLog
) : Item() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            setTimeText(viewHolder)
            GlideApp.with(chatLogContext)
                .load(StorageUtil.pathToReference(chatVideoMessage.videoPath))
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.video_view_receiver_message)

            itemView.progressBarVideoPlayer.visibility = View.GONE
            itemView.play_video_receiver.isEnabled = true
            itemView.play_video_receiver.apply {
                setOnClickListener {
                }
            }
            GlideApp.with(chatLogContext).load(user.profilePicturePath?.let {
                StorageUtil.pathToReference(
                    it
                )
            })
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageVideoToChatLogReceiverImage)
        }
    }

    private fun setTimeText(viewHolder: GroupieViewHolder) {
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.itemView.text_view_video_receiver_message_time.text =
            dataFormat.format(chatVideoMessage.time)
    }

    override fun getLayout(): Int {
        return R.layout.video_mesage_receiver_row
    }

}