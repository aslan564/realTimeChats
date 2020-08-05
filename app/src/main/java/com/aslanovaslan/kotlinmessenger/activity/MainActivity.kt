package com.aslanovaslan.kotlinmessenger.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.ProgressFragment
import com.aslanovaslan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

const val AA_SELECTED_IMAGE = 2

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job
    private lateinit var selectedImageBytes: ByteArray
    private lateinit var progressBar: ProgressFragment
    private  var selectedImagePath: Uri?=null
    private var pictureJustChanged: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        job = Job()


        register_button_register.setOnClickListener {
            createProgressBar()
            if (createUserFireBase()) return@setOnClickListener
        }
        already_have_an_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        profile_image_register.setOnClickListener {
            setPictureToProfile()
        }

    }

    private fun createProgressBar() {
        progressBar = ProgressFragment()
        progressBar.show(supportFragmentManager, "mainDialog")
        progressBar.isCancelable = false
    }
    private fun cancelProgressBar() {
        progressBar.dismiss()
    }
    private fun createUserFireBase(): Boolean {
        val email = editTextTextEmailAddressRegister.text.toString()
        val password = editTextTextPasswordRegister.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/pass ", Toast.LENGTH_SHORT).show()
            cancelProgressBar()
            return true
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                addPictureStorage(selectedImagePath)
                Log.d(TAG, "onCreate: user created: ${it.result?.user?.uid}")
            }.addOnFailureListener {
                Toast.makeText(this, "User not created ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onCreate: user not created")
                cancelProgressBar()
            }
        return false
    }

    private fun setPictureToProfile() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Image"),
            AA_SELECTED_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_SELECTED_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImagePath = data.data!!
            val selectedImagePathBitmap = MediaStore.Images.Media
                .getBitmap(this.contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImagePathBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            GlideApp.with(this)
                .load(selectedImageBytes)
                .placeholder(R.drawable.ic_fire_emoji)
                .into(profile_image_register)
            /* if (selectedImagePath != null) {

                 // imageCompress(selectedImagePath.path!!,this@MainActivity)
                 //resizeImageToUploadStorage(selectedImagePathBitmap.toString())
             }*/
            pictureJustChanged = true
        }
    }


    private fun resizeImageToUploadStorage(selectedImagePath: String) {
        Log.d(TAG, "addPictureStorage: $selectedImagePath")
        val destinationFilePath =
            File(Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Chatskotlin/Images")
        val imageBitmap = SiliCompressor.with(this).getCompressBitmap(selectedImagePath)
        Log.d(TAG, "resizeImageToUploadStorage: $imageBitmap")
        // addPictureStorage(compressPath)
    }

    private fun addPictureStorage(uri: Uri?) {
        if (uri != null) {

        val uuid = UUID.randomUUID()
        val ref = FirebaseStorage.getInstance().getReference("/image/$uuid")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    addUserToFirestormsDatabase(uri)
                    Log.d(TAG, "addPictureStorage: ${uri.path}")
                }.addOnFailureListener {
                    Log.d(TAG, "addPictureStorage: ${it.message}")
                }
                Log.d(TAG, "addPictureStorage: ${ref.downloadUrl}")
            }.addOnFailureListener {
                Log.d(TAG, "addPictureStorage: ${it.message}")
            }.addOnProgressListener {
                progressBar.dialogProgress.text =
                    (100 * it.bytesTransferred - it.totalByteCount).toString()
            }
        }else {
            addUserToFirestormsDatabase(null)
        }
        Log.d(TAG, "addPictureStorage: $uri")
    }

    private fun addUserToFirestormsDatabase(uri: Uri?) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d(TAG, "addUserToFirestormsDatabase: $userId")
        val user = User(
            userId,
            editTextTextPersonNameRegister.text.toString(),
            uri.toString()
        )
        val databaseRef = Firebase.database.reference
        databaseRef.child("users").child(userId).setValue(user).addOnSuccessListener {
            val intent = Intent(this, LatestMessageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK))
            startActivity(intent)
            finish()
            cancelProgressBar()
        }.addOnFailureListener {
            cancelProgressBar()
            Log.d(TAG, "addUserToFirestormsDatabase: ${it.message}")
            Toast.makeText(this, "User Not created", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().currentUser!!.delete()
            /* if (uri != null) {
                 FirebaseStorage.getInstance().getReference(uri).delete()
             }*/
        }

    }


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
