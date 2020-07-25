package com.aslanovaslan.kotlinmessenger.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.model.ChatMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageReceiverItem
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageSenderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLog : AppCompatActivity() {
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)


        if (user != null) {
            supportActionBar!!.title = user.username
            supportActionBar!!.subtitle = (System.currentTimeMillis() / 1000).toString()
        } else {
            Log.d(TAG, "onCreate: $user")
        }
        fetchAllMessageFromFirebase()

        buttonChatLogSendMessage.apply {
            setOnClickListener {

                user?.let {
                    user.userId?.let {
                        adMessageFireBase(it)
                    }
                }

            }
        }
        recyclerViewChatLog.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(this@ChatLog)
            groupAdapter.notifyDataSetChanged()
        }
    }

    private fun fetchAllMessageFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA) ?: return
        val databaseRef = Firebase.database.getReference("messages").child(currentUserId).child(user.userId!!)

        databaseRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    when (currentUserId) {
                        chatMessage.senderId -> {
                            val user = LatestMessageActivity.CURRENT_USER
                            if (user != null)
                                groupAdapter.add(
                                    MessageReceiverItem(
                                        chatMessage.text,
                                        user
                                    )
                                )
                        }
                        else -> {
                            val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)
                            if (user != null)
                                groupAdapter.add(
                                    MessageSenderItem(
                                        chatMessage.text,
                                        user
                                    )
                                )
                        }
                    }
                } else return


            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                groupAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })
    }

    private fun adMessageFireBase(receiverId: String) {
        val messageText = editTextTextChatLogMessage.text.toString()
        if (messageText == "") return
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val senderRef = Firebase.database.getReference("messages").child(senderId).child(receiverId).push()
        val receiverRef = Firebase.database.getReference("messages").child(receiverId).child(senderId).push()
        val chatMessage = ChatMessage(
            senderRef.key!!,
            messageText,
            senderId,
            receiverId,
            System.currentTimeMillis() / 1000
        )
        senderRef.setValue(chatMessage).addOnSuccessListener {
            clearViewText()
            Log.d(TAG, "adMessageFireBase: ${senderRef.key}")
            recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount-1)
        }.addOnFailureListener {
            Log.d(TAG, "adMessageFireBase: ${it.message}")
        }
        receiverRef.setValue(chatMessage)
        val receiverLastMessageRef = Firebase.database.getReference("latest-messages").child(receiverId).child(senderId)
        val senderLastMessageRef = Firebase.database.getReference("latest-messages").child(senderId).child(receiverId)
        receiverLastMessageRef.setValue(chatMessage)
        senderLastMessageRef.setValue(chatMessage)

    }

    private fun clearViewText() {
        editTextTextChatLogMessage.text.clear()
    }



    companion object {
        private const val TAG = "ChatLog"
    }
}

