package com.aslanovaslan.kotlinmessenger.recycleritem

import android.util.Log
import android.view.View
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activity.LatestMessageActivity
import com.aslanovaslan.kotlinmessenger.model.ChatMessage
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

class LatestMessageItem(private val chatMessage: ChatMessage) : Item() {
    var partnerUserItem:User?=null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {

            itemView.textViewLatestMessageText.text = chatMessage.text

        }

        val chatPartnerId =
            if (chatMessage.senderId == FirebaseAuth.getInstance().currentUser!!.uid) {
                chatMessage.receiverId
            } else {
                chatMessage.senderId
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

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}