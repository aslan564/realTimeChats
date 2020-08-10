package com.aslanovaslan.kotlinmessenger.internal

import android.net.Uri
import com.aslanovaslan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.util.*


object StorageUtil {

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val databaseInstance: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    private val currentUserDatabase: FirebaseDatabase
        get() = databaseInstance.reference.database

    fun uploadProfilePhoto(
        imageBytes: ByteArray,
        onSuccess: (imagePath: String) -> Unit
    ) {
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun uploadMessageImage(
        imageBytes: ByteArray,
        onSuccess: (imagePath: String) -> Unit
    ) {
        val ref = currentUserRef.child("messages/image/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun uploadMessageAudio(
        recordAudio: Uri,
        onSuccess: (audioPath: String) -> Unit
    ) {
        val ref = currentUserRef.child("messages/audio/${UUID.randomUUID()}")
        ref.putFile(recordAudio).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }
    fun uploadMessageVideo(recordVideo: Uri, onSuccess: (videoPath: String) -> Unit) {
        val ref= currentUserRef.child("messages/video/${UUID.randomUUID()}")
        ref.putFile(recordVideo).addOnSuccessListener {
             onSuccess(ref.path)
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDatabase.getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
        .addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    onComplete(snapshot.getValue(User::class.java)!!)
                }
            }

        })

    }


    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val databaseRef = Firebase.database.reference
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["username"] = name
        if (bio.isNotBlank()) userFieldMap["userBio"] = bio
        if (profilePicturePath != null) {
            userFieldMap["profilePicturePath"] = profilePicturePath
        }
        databaseRef.child("users").child(userId).updateChildren(userFieldMap)
    }
}