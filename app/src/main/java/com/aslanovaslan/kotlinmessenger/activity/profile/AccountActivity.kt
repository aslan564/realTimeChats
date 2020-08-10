package com.aslanovaslan.kotlinmessenger.activity.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activity.register.MainActivity
import com.aslanovaslan.kotlinmessenger.activityhelper.showSnackbar
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.internal.internet.CheckNetworkState
import com.aslanovaslan.kotlinmessenger.model.User
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.activity_new_message.*
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.*

@Suppress("DEPRECATION")
class AccountActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {
    private val AA_ACCOUNT_IMAGE = 2
    private var selectedImageBytes: ByteArray? = null
    private var pictureJustChanged: Boolean = false
    private lateinit var userdata: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        supportActionBar!!.title = "Profile"
        button_update_account.setOnClickListener(this)
        image_view_for_account.setOnClickListener(this)
        editTextTextPersonNameAccountBios.addTextChangedListener(this)
        editTextTextPersonNameAccount.addTextChangedListener(this)

        if (!CheckNetworkState(this).isOnline()) {
            showSnackbar(accountContainer, "Internet yoxdu ")
            return
        }

    }

    private fun fetchedUserInfoFromDatabase() {
        StorageUtil.getCurrentUser { user ->
            userdata = user
            uploadImageProfile(user)
            editTextTextPersonNameAccountBios.hint = user.userBio
            editTextTextPersonNameAccount.hint = user.username
        }
    }

    private fun uploadImageProfile(user: User) {
        GlideApp.with(this).load(user.profilePicturePath?.let { StorageUtil.pathToReference(it) })
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.ic_fire_emoji).into(image_view_for_account)
        progressBarAccount.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        if (CheckNetworkState(this).isOnline()) {
            if (!pictureJustChanged) {
                fetchedUserInfoFromDatabase()
            } else return
        } else {
            Snackbar.make(accountContainer, "Internet yoxdu ", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_update_account -> {
                progressBarAccountUpdate.visibility = View.VISIBLE
                if (pictureJustChanged) {
                    selectedImageBytes?.let { addProfileImageDatabase(it) }
                } else {
                    addProfileImageDatabase(null)
                }

            }
            R.id.image_view_for_account -> {
                getProfileUpdated()
            }
        }
    }

    private fun getProfileUpdated() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AA_ACCOUNT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AA_ACCOUNT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data!!
            val selectedImageBitmap: Bitmap
            if (Build.VERSION.SDK_INT >= 28) {
                val sources = ImageDecoder.createSource(this.contentResolver, selectedImagePath)
                selectedImageBitmap = ImageDecoder.decodeBitmap(sources)
                val outputStream = ByteArrayOutputStream()
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                selectedImageBytes = outputStream.toByteArray()
                pictureJustChanged = true
            } else {
                selectedImageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
                val outputStream = ByteArrayOutputStream()
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                selectedImageBytes = outputStream.toByteArray()
                pictureJustChanged = true
            }
            GlideApp.with(this)
                .load(selectedImageBytes)
                .placeholder(R.drawable.ic_fire_emoji)
                .into(image_view_for_account)
            button_update_account.isEnabled = true
            progressBarAccount.visibility = View.GONE

        }
    }

    private fun addProfileImageDatabase(selectedImageBytes: ByteArray?) {
        val username = editTextTextPersonNameAccount.text.toString().trim()
        val userBio = editTextTextPersonNameAccountBios.text.toString().trim()
        if (selectedImageBytes != null) {
            StorageUtil.uploadProfilePhoto(selectedImageBytes) { uri ->
                StorageUtil.updateCurrentUser(username, userBio, uri)
                checkUiState()
            }

        } else {
            StorageUtil.updateCurrentUser(username, userBio, null)
            checkUiState()
        }
    }

    private fun checkUiState() {
        button_update_account.isEnabled = false
        progressBarAccountUpdate.visibility = View.GONE
        Snackbar.make(accountContainer, "Profil Yenilendi", Snackbar.LENGTH_LONG).show()
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!s.isNullOrEmpty()) {
            if (s.length > 6) {
                button_update_account.isEnabled = true
            }
        }
    }

    companion object {
        const val TAG = "AccountActivity"
    }
}