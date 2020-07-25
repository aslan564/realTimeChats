package com.aslanovaslan.kotlinmessenger.internal

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    @SuppressLint("SimpleDateFormat")
    fun convertDate(localDate: Long):String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        val date = Date(localDate)
        return simpleDateFormat.format(date)
    }
    @SuppressLint("SimpleDateFormat")
    fun convertFullDate(localDate: Long):String {
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT");
        val date = Date(localDate)
        return simpleDateFormat.format(date)
    }
}