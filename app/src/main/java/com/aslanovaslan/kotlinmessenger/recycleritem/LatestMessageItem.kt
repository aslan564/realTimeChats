package com.aslanovaslan.kotlinmessenger.recycleritem

import android.util.Log
import android.view.View
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.model.ChatTextMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.text.SimpleDateFormat

class LatestMessageItem(private val chatTextMessage: ChatTextMessage) : Item() {
    var partnerUserItem:User?=null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            itemView.textViewLatestMessageText.text = chatTextMessage.text
            setTimeText(viewHolder)
        }


        val chatPartnerId =
            if (chatTextMessage.senderId == FirebaseAuth.getInstance().currentUser!!.uid) {
                chatTextMessage.receiverId
            } else {
                chatTextMessage.senderId
            }
        val databaseRef = Firebase.database.getReference("users").child(chatPartnerId)
        databaseRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("Cancelled", "onCancelled: $error")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                partnerUserItem = snapshot.getValue(User::class.java) ?: return
                if (partnerUserItem != null) {
                    viewHolder.itemView.textViewLatestMessageUsername.text =
                        partnerUserItem!!.username
                    Picasso.get().load(partnerUserItem!!.profilePicturePath).placeholder(
                        R.drawable.ic_fire_emoji
                    )
                        .into(viewHolder.itemView.imageViewLatestMessage)
                    viewHolder.itemView.progressBarLatestMessage.visibility = View.GONE
                    Log.d(
                        "LatestMessageActivity",
                        "onDataChange: ${partnerUserItem.toString()} "
                    )
                }
            }
        })

    }
    private fun setTimeText(viewHolder: GroupieViewHolder) {
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.   itemView.textViewLatestMesageTime.text = dataFormat.format(chatTextMessage.time)
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}