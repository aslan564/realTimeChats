package com.aslanovaslan.kotlinmessenger.recycleritem

import android.annotation.SuppressLint
import android.content.Context
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.model.ChatImageMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.image_mesage_sender_row.view.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.text_mesage_reciver_row.view.*
import kotlinx.android.synthetic.main.text_mesage_reciver_row.view.imageViewToChatLog
import java.text.SimpleDateFormat

class MessageReceiverImageItem(
    private val chatImageMessage: ChatImageMessage,
    private val user: User,
    val context: Context
) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            setTimeText(viewHolder)
            GlideApp.with(context).load(StorageUtil.pathToReference(chatImageMessage.picturePath))
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.image_view_sender_message_image)
            Picasso.get()
                .load(user.profilePicturePath).placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageViewToChatLogSenderImage)
            //
        }
    }

    private fun setTimeText(viewHolder: GroupieViewHolder) {
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.itemView.text_view_sender_message_time.text =
            dataFormat.format(chatImageMessage.time)
    }

    override fun getLayout(): Int {
        return R.layout.image_mesage_sender_row
    }

}