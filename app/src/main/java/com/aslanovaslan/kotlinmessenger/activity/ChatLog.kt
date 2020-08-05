package com.aslanovaslan.kotlinmessenger.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.model.*
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageReceiverImageItem
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageReceiverTextItem
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageSenderImageItem
import com.aslanovaslan.kotlinmessenger.recycleritem.MessageSenderTextItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.onesignal.OneSignal
import com.onesignal.OneSignal.PostNotificationResponseHandler
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*


@Suppress("NAME_SHADOWING")
class ChatLog : AppCompatActivity(), TextWatcher {
    private val AA_SELECT_IMAGE = 2
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var receiverId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)
        fetchAllMessageFromFirebase()
        editTextTextChatLogMessage.addTextChangedListener(this)
        if (user != null) {
            supportActionBar!!.title = user.username
            supportActionBar!!.subtitle = null
            receiverId = user.userId.toString()
        } else {
            Log.d(TAG, "onCreate: $user")
        }
        buttonSendImage.apply {
            setOnClickListener {
                Log.d(TAG, "onCreate: click")
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpeg", "image/png", "image/jpg")
                    )
                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    AA_SELECT_IMAGE
                )

            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            var selectedImageBitmap: Bitmap
            if (selectedImagePath != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val sources = ImageDecoder.createSource(this.contentResolver, selectedImagePath)
                    selectedImageBitmap = ImageDecoder.decodeBitmap(sources)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val selectedImageBytes = outputStream.toByteArray()
                    addMessageDatabase(selectedImageBytes)
                } else {
                    selectedImageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val selectedImageBytes = outputStream.toByteArray()
                    addMessageDatabase(selectedImageBytes)
                }
            }
        }
    }

    private fun addMessageDatabase(selectedImageBytes: ByteArray) {
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val senderRef =
            Firebase.database.getReference("messages").child(senderId).child(receiverId).push()
        val receiverRef =
            Firebase.database.getReference("messages").child(receiverId).child(senderId).push()
        StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath->
            val messageToSend = ChatImageMessage(
                imagePath,
                senderRef.key!!,
                senderId,
                receiverId,
                Calendar.getInstance().time
            )
            senderRef.setValue(messageToSend).addOnSuccessListener {
                clearViewText()
                Log.d(TAG, "adMessageFireBase: ${senderRef.key}")
                recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)

                fetchedSignalIds(receiverId, "goruntu mesajiniz var")


            }.addOnFailureListener {
                Log.d(TAG, "adMessageFireBase: ${it.message}")
            }
            receiverRef.setValue(messageToSend)
            val receiverLastMessageRef =
                Firebase.database.getReference("latest-messages").child(receiverId).child(senderId)
            val senderLastMessageRef =
                Firebase.database.getReference("latest-messages").child(senderId).child(receiverId)
            receiverLastMessageRef.setValue(messageToSend)
            senderLastMessageRef.setValue(messageToSend)

        }
    }

    private fun sendMessageNotification(receiverId: String, message: String) {
        try {
            OneSignal.postNotification(JSONObject("{'contents': {'en':'$message'}, 'include_player_ids': ['$receiverId']}"),
                object : PostNotificationResponseHandler {
                    override fun onSuccess(response: JSONObject) {
                        Log.i(
                            "OneSignalExample",
                            "postNotification Success: $response"
                        )
                    }

                    override fun onFailure(response: JSONObject) {
                        Log.e(
                            "OneSignalExample",
                            "postNotification Failure: $response"
                        )
                    }
                })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun fetchAllMessageFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA) ?: return
        val databaseRef =
            Firebase.database.getReference("messages").child(currentUserId).child(user.userId!!)

        databaseRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageType = snapshot.child("type").value.toString()
                val senderId = snapshot.child("senderId").value.toString()
                if (snapshot.value != null) {
                        //val chatTextMessage = snapshot.getValue(ChatTextMessage::class.java)
                        // if (chatTextMessage != null) {
                        when (currentUserId) {
                            senderId -> {
                                messageSender(snapshot, messageType)
                            }
                            else -> {
                                messageReciver(snapshot, messageType)
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

    private fun messageReciver(
        message: DataSnapshot,
        messageType: String
    ) {
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)
        if (user != null) {
            if (messageType == MessageType.TEXT) {
               val chatMessage =message.getValue(ChatTextMessage::class.java)
                groupAdapter.add(MessageSenderTextItem(chatMessage!!, user))
                recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)
            } else if (messageType == MessageType.IMAGE) {
                val chatMessage =message.getValue(ChatImageMessage::class.java)
                groupAdapter.add(MessageSenderImageItem(chatMessage!!, user,this))
                recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)
            }
        }
    }

    private fun messageSender(
        message: DataSnapshot,
        messageType: String
    ) {
        val user = LatestMessageActivity.CURRENT_USER
        if (user != null) {
            if (messageType == MessageType.TEXT) {
                val chatMessage = message.getValue(ChatTextMessage::class.java)
                groupAdapter.add(MessageReceiverTextItem(chatMessage!!, user))
                recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)
            } else if (messageType == MessageType.IMAGE) {
                val chatMessage = message.getValue(ChatImageMessage::class.java)
                groupAdapter.add(MessageReceiverImageItem(chatMessage!!, user,this))
                recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)
            }
        }
    }


    private fun adMessageFireBase(receiverId: String) {
        val messageText = editTextTextChatLogMessage.text.toString().trim()
        if (messageText == "") return
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val senderRef =
            Firebase.database.getReference("messages").child(senderId).child(receiverId).push()
        val receiverRef =
            Firebase.database.getReference("messages").child(receiverId).child(senderId).push()
        val chatMessage = ChatTextMessage(
            messageText,
            senderRef.key!!,
            senderId,
            receiverId,
            Calendar.getInstance().time
        )
        senderRef.setValue(chatMessage).addOnSuccessListener {
            clearViewText()
            Log.d(TAG, "adMessageFireBase: ${senderRef.key}")
            recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)

            fetchedSignalIds(receiverId, chatMessage.text)


        }.addOnFailureListener {
            Log.d(TAG, "adMessageFireBase: ${it.message}")
        }
        receiverRef.setValue(chatMessage)
        val receiverLastMessageRef =
            Firebase.database.getReference("latest-messages").child(receiverId).child(senderId)
        val senderLastMessageRef =
            Firebase.database.getReference("latest-messages").child(senderId).child(receiverId)
        receiverLastMessageRef.setValue(chatMessage)
        senderLastMessageRef.setValue(chatMessage)

    }

    private fun fetchedSignalIds(receiverId: String, message: String) {
        val databaseRef = Firebase.database.reference
        databaseRef.child("users").child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userModel = snapshot.getValue(User::class.java)
                    if (!userModel!!.oneSignalIds.isNullOrEmpty()) {
                        userModel.oneSignalIds?.let { osi -> sendMessageNotification(osi, message) }
                    }
                }

            })
    }

    private fun clearViewText() {
        editTextTextChatLogMessage.text.clear()
    }


    companion object {
        private const val TAG = "ChatLog"
    }

    private fun addMessageImageFirestorToFirebase(
        imageBytes: ByteArray,
        onSuccess: (imagePath: String) -> Unit
    ) {


        val fbsr = FirebaseStorage.getInstance()
            .getReference("/image/${UUID.nameUUIDFromBytes(imageBytes)}")
        fbsr.putBytes(imageBytes).addOnSuccessListener {
            fbsr.downloadUrl.addOnSuccessListener { fbsrUri ->
                onSuccess(fbsr.path)
            }
        }.addOnCanceledListener {
            Log.d(TAG, "addPictureStorage: cancel")
        }.addOnFailureListener {
            Log.d(TAG, "addPictureStorage: ${it.message}")
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s != null) {
            if (s.isEmpty()) {
                buttonChatLogSendMessage.visibility = View.GONE
                buttonChatLogSendVoice.visibility = View.VISIBLE
            } else {
                buttonChatLogSendMessage.visibility = View.VISIBLE
                buttonChatLogSendVoice.visibility = View.GONE
            }
        }

    }
}

