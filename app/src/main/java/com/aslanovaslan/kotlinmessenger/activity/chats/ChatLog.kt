package com.aslanovaslan.kotlinmessenger.activity.chats

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activityhelper.showSnackbar
import com.aslanovaslan.kotlinmessenger.internal.StateRecord
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.internal.internet.CheckNetworkState
import com.aslanovaslan.kotlinmessenger.model.*
import com.aslanovaslan.kotlinmessenger.recycleritem.*
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


@Suppress("DEPRECATION")
class ChatLog : AppCompatActivity(), TextWatcher, CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val AA_SELECT_IMAGE = 2
    private val AA_RECORD_AUDIO = 18
    private val AA_RECORD_VIDEO = 101
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var receiverId: String
    private val permissionRequest = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var isPlayingState = StateRecord.STOP
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBarHandler: Handler
    private lateinit var videoUri: Uri

    private var isRecording = false
    private lateinit var updateSeekBar: Runnable
    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        job = Job()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)
        if (!CheckNetworkState(this).isOnline()) {
            showSnackbar(chatLogBaseContainer, "Internet yoxdu ")
            return
        }
        fetchAllMessageFromFirebase()

        editTextTextChatLogMessage.addTextChangedListener(this)
        if (user != null) {
            supportActionBar!!.title = user.username
            supportActionBar!!.subtitle = null
            receiverId = user.userId.toString()
        } else {
            Log.d(TAG, "onCreate: $user")
        }
        buttonChatLogSendVoice.apply {
            setOnClickListener {
                if (isRecording) {
                    //stop recording
                    stopRecord()
                    buttonChatLogSendVoice.setBackgroundResource(R.drawable.ic_baseline_mic_24)
                    isRecording = false

                } else {
                    //start recording
                    if (checkPermissionsAudio()) {
                        startRecord()
                        buttonChatLogSendVoice.setBackgroundResource(R.drawable.ic_record_voice_over_24)
                        isRecording = true
                    }
                }
            }
        }
        buttonSendImage.apply {
            setOnClickListener {
                if (isRecording) {
                    stopRecord()
                }
                isRecording = false
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
                        addTextMessageDatabase(it)
                    }
                }

            }
        }
        recyclerViewChatLog.apply {
            adapter = groupAdapter
            setItemViewCacheSize(300)
            layoutManager = LinearLayoutManager(this@ChatLog)
            groupAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_log_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.take_chat_log_video -> {
                val videoViewIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

                if (videoViewIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(
                        Intent.createChooser(videoViewIntent, "Take a video"),
                        AA_RECORD_VIDEO
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermissionsAudio(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                permissionRequest[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(this, permissionRequest, AA_RECORD_AUDIO)
            false
        }
    }

    private fun startRecord() {
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val recorderPath = this.getExternalFilesDir("/")!!.absolutePath
        val dateNow = Date()
        record_timer_chat_log.apply {
            visibility = View.VISIBLE
            base = SystemClock.elapsedRealtime()
            start()
        }
        editTextTextChatLogMessage.visibility = View.INVISIBLE
        val recordFile = "Recording${dateFormat.format(dateNow)}.3gp"
        fileName = "$recorderPath/$recordFile"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
            start()
        }
    }

    private fun stopRecord() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        uploadAudioStorage()
        record_timer_chat_log.apply {
            visibility = View.GONE
            stop()
        }
        editTextTextChatLogMessage.visibility = View.VISIBLE
    }

    private fun uploadAudioStorage() {
        if (!fileName.isNullOrEmpty()) {
            val uri = Uri.fromFile(File(fileName!!))

            StorageUtil.uploadMessageAudio(uri) { audioPath ->
                val senderId = FirebaseAuth.getInstance().currentUser!!.uid
                val senderRef =
                    Firebase.database.getReference("messages").child(senderId).child(receiverId)
                        .push()
                val receiverRef =
                    Firebase.database.getReference("messages").child(receiverId).child(senderId)
                        .push()
                val messageToSend = ChatAudioMessage(
                    audioPath,
                    senderRef.key!!,
                    senderId,
                    receiverId,
                    Calendar.getInstance().time
                )
                senderRef.setValue(messageToSend).addOnSuccessListener {
                    clearViewText()
                    Log.d(TAG, "adMessageFireBase: ${senderRef.key}")
                    recyclerScrollPosition()
                    fetchedSignalIds(receiverId, "goruntu mesajiniz var")


                }.addOnFailureListener {
                    Log.d(TAG, "adMessageFireBase: ${it.message}")
                }
                receiverRef.setValue(messageToSend)
                val receiverLastMessageRef =
                    Firebase.database.getReference("latest-messages").child(receiverId)
                        .child(senderId)
                val senderLastMessageRef =
                    Firebase.database.getReference("latest-messages").child(senderId)
                        .child(receiverId)
                receiverLastMessageRef.setValue(messageToSend)
                senderLastMessageRef.setValue(messageToSend)
            }
        }
    }

    private fun uploadVideoStorage(uriFromGallery: Uri) {
        val ref =
            FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("messages/video/${UUID.randomUUID()}")
        ref.putFile(uriFromGallery).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                //addUserToDatabase(uri)
                uploadVideoPathDatabase(ref.path, uri)
            }.addOnFailureListener {
                Log.d(TAG, "addPictureStorage: ${it.message}")
            }
        }.addOnFailureListener {
            Log.d(TAG, "addPictureStorage: ${it.message}")
        }
    }

    private fun uploadVideoPathDatabase(videoPath: String, videoPathUri: Uri) {
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val senderRef =
            Firebase.database.getReference("messages").child(senderId).child(receiverId)
                .push()
        val receiverRef =
            Firebase.database.getReference("messages").child(receiverId).child(senderId)
                .push()
        val messageToSend = ChatVideoMessage(
            videoPath,
            videoPathUri.toString(),
            senderRef.key!!,
            senderId,
            receiverId,
            Calendar.getInstance().time
        )
        senderRef.setValue(messageToSend).addOnSuccessListener {
            clearViewText()
            Log.d(TAG, "adMessageFireBase: ${senderRef.key}")
            recyclerScrollPosition()
            fetchedSignalIds(receiverId, "video mesajiniz var")
        }.addOnFailureListener {
            Log.d(TAG, "adMessageFireBase: ${it.message}")
        }
        receiverRef.setValue(messageToSend)
        val receiverLastMessageRef =
            Firebase.database.getReference("latest-messages").child(receiverId)
                .child(senderId)
        val senderLastMessageRef =
            Firebase.database.getReference("latest-messages").child(senderId)
                .child(receiverId)
        receiverLastMessageRef.setValue(messageToSend)
        senderLastMessageRef.setValue(messageToSend)
    }

    @SuppressLint("SdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBitmap: Bitmap
            if (selectedImagePath != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val sources = ImageDecoder.createSource(this.contentResolver, selectedImagePath)
                    selectedImageBitmap = ImageDecoder.decodeBitmap(sources)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val selectedImageBytes = outputStream.toByteArray()
                    addImageMessageDatabase(selectedImageBytes)
                } else {
                    selectedImageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val selectedImageBytes = outputStream.toByteArray()
                    addImageMessageDatabase(selectedImageBytes)
                }
            }
        } else if (requestCode == AA_RECORD_VIDEO && resultCode == Activity.RESULT_OK && data != null) {
            videoUri = data.data!!


          uploadVideoStorage(data.data!!)
        }
    }



    private fun addImageMessageDatabase(selectedImageBytes: ByteArray) {
        val senderId = FirebaseAuth.getInstance().currentUser!!.uid
        val senderRef =
            Firebase.database.getReference("messages").child(senderId).child(receiverId).push()
        val receiverRef =
            Firebase.database.getReference("messages").child(receiverId).child(senderId).push()
        StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
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
                recyclerScrollPosition()

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
                            messageReceiver(snapshot, messageType)
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

    private fun messageReceiver(
        message: DataSnapshot,
        messageType: String
    ) {
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_DATA)
        if (user != null) {
            when (messageType) {
                MessageType.TEXT -> {
                    val chatMessage = message.getValue(ChatTextMessage::class.java)
                    groupAdapter.add(MessageSenderTextItem(chatMessage!!, user))
                    recyclerScrollPosition()
                }
                MessageType.IMAGE -> {
                    val chatMessage = message.getValue(ChatImageMessage::class.java)
                    groupAdapter.add(MessageSenderImageItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
                MessageType.AUDIO -> {
                    val chatMessage = message.getValue(ChatAudioMessage::class.java)
                    groupAdapter.add(MessageReceiverAudioItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
                MessageType.VIDEO -> {
                    val chatMessage = message.getValue(ChatVideoMessage::class.java)
                    groupAdapter.add(MessageReceiverVideoItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
            }
        }
    }

    private fun recyclerScrollPosition() {
        recyclerViewChatLog.scrollToPosition(groupAdapter.itemCount - 1)
    }

    private fun messageSender(
        message: DataSnapshot,
        messageType: String
    ) {
        val user = LatestMessageActivity.CURRENT_USER
        if (user != null) {
            when (messageType) {
                MessageType.TEXT -> {
                    val chatMessage = message.getValue(ChatTextMessage::class.java)
                    groupAdapter.add(MessageReceiverTextItem(chatMessage!!, user))
                    recyclerScrollPosition()
                }
                MessageType.IMAGE -> {
                    val chatMessage = message.getValue(ChatImageMessage::class.java)
                    groupAdapter.add(MessageReceiverImageItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
                MessageType.AUDIO -> {
                    val chatMessage = message.getValue(ChatAudioMessage::class.java)
                    groupAdapter.add(MessageSenderAudioItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
                MessageType.VIDEO -> {
                    val chatMessage = message.getValue(ChatVideoMessage::class.java)
                    groupAdapter.add(MessageSenderVideoItem(chatMessage!!, user, this))
                    recyclerScrollPosition()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DETSROY_ACTIV = true
        job.cancel()
    }

    override fun onStart() {
        super.onStart()
//supportActionBar!!.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        chatLogContainer.visibility = View.VISIBLE
        baseFrameChatLog.visibility = View.GONE
        stopRecord()
        supportActionBar!!.show()
        DETSROY_ACTIV = true
    }

    private fun addTextMessageDatabase(receiverId: String) {
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
        var DETSROY_ACTIV = false
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

