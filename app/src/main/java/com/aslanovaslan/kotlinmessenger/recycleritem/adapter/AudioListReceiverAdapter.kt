package com.aslanovaslan.kotlinmessenger.recycleritem.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.model.ChatAudioMessage

class AudioListReceiverAdapter(val chatAudioMessage: ChatAudioMessage) :
    RecyclerView.Adapter<AudioListReceiverAdapter.MyReceiverViewHolder>() {
    private lateinit var audioListReceiver : ArrayList<ChatAudioMessage>

    class MyReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReceiverViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.audio_mesage_reciver_row, parent, false)
        return MyReceiverViewHolder(view)
    }

    override fun getItemCount(): Int {
        return audioListReceiver.size
    }

    override fun onBindViewHolder(holder: MyReceiverViewHolder, position: Int) {

    }
}