package com.aslanovaslan.kotlinmessenger.recycleritem

import android.annotation.SuppressLint
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activity.chats.ChatLog
import com.aslanovaslan.kotlinmessenger.fragment.VideoFragment
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.internal.eventBus.EventBusData
import com.aslanovaslan.kotlinmessenger.model.ChatVideoMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.video_mesage_sender_row.view.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat

class MessageSenderVideoItem(
    private val chatVideoMessage: ChatVideoMessage,
    private val user: User,
    private val chatLogContext: ChatLog
) : Item() {
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val chatLogContainer = chatLogContext.findViewById<ConstraintLayout>(R.id.chatLogContainer)
        val baseFrameChatLog = chatLogContext.findViewById<FrameLayout>(R.id.baseFrameChatLog)
        val actionBarVideoItem=chatLogContext.supportActionBar /*R.menu.chat_log_menu*/
        viewHolder.apply {
            setTimeText(viewHolder)
            GlideApp.with(chatLogContext).load(
                StorageUtil.pathToReference(
                    chatVideoMessage.videoPath
                )
            )
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.video_view_sender_message)
            itemView.progressBarVideoPlayerSender.visibility = View.GONE
            itemView.play_video.isEnabled = true
            itemView.play_video.setOnClickListener {
                //itemView.video_view_sender_message.start()
                chatLogContainer.visibility = View.GONE
                baseFrameChatLog.visibility = View.VISIBLE
                actionBarVideoItem!!.hide()
                val transaction = chatLogContext.supportFragmentManager.beginTransaction()
                EventBus.getDefault().postSticky(EventBusData.SharedVideoUrl(chatVideoMessage.videoPathUri))
                transaction.replace(R.id.baseFrameChatLog, VideoFragment())
                transaction.addToBackStack("senderTransaction")
                transaction.commit()
            }
            GlideApp.with(chatLogContext).load(user.profilePicturePath?.let {
                StorageUtil.pathToReference(
                    it
                )
            })
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageVideoToChatLogSenderImage)
        }
    }

    private fun setTimeText(viewHolder: GroupieViewHolder) {
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.itemView.text_view_video_sender_message_time.text =
            dataFormat.format(chatVideoMessage.time)
    }

    override fun getLayout(): Int {
        return R.layout.video_mesage_sender_row
    }

}