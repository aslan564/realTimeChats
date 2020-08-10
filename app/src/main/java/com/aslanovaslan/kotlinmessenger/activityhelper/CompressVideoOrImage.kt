package com.aslanovaslan.kotlinmessenger.activityhelper

import android.content.Context
import android.net.Uri
import android.util.Log
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class CompressVideoOrImage(val context: Context) {
    suspend fun compressImage(imageContentUri: Uri, onSuccess: (newFilePath: String) -> Unit) {
        val destinationDirectory =
            File(context.getExternalFilesDir("/")!!.absolutePath + "/DCIM/Clone/Images")
        val data = withContext(Dispatchers.IO) {

          SiliCompressor.with(context)
                .compress(imageContentUri.toString(), destinationDirectory, true)
        }
        onSuccess(data)
    }

    fun compressVideo(videoContentUri: Uri, onSuccess: (newFilePath: String) -> Unit) {

        val destinationDirectory =
            File(context.getExternalFilesDir("/")!!.absolutePath + "/DCIM/Clone/Images")
        Log.d("123456789", "onActivityResult: $destinationDirectory")
        val filePath =
            SiliCompressor.with(context)
                .compressVideo(videoContentUri, destinationDirectory.toString())

        Log.d("123456789", "onActivityResult: $filePath")
        onSuccess(filePath)
    }

}