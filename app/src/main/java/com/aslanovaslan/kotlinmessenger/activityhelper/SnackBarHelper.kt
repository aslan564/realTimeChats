package com.aslanovaslan.kotlinmessenger.activityhelper

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.showSnackbar(view: View,titleMessage:String)=
    Snackbar.make(view, titleMessage, Snackbar.LENGTH_LONG).show()
fun Context.showToast(context: Context,titleMessage:String)=
    Toast.makeText(context, titleMessage, Toast.LENGTH_LONG).show()