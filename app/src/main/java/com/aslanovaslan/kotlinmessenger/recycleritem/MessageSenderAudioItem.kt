package com.aslanovaslan.kotlinmessenger.recycleritem

import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.activity.chats.ChatLog
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StateRecord
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.model.ChatAudioMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.audio_mesage_reciver_row.*
import kotlinx.android.synthetic.main.audio_mesage_sender_row.*
import kotlinx.android.synthetic.main.audio_mesage_sender_row.view.*
import kotlinx.android.synthetic.main.text_mesage_sender_row.view.*
import java.io.IOException
import java.text.SimpleDateFormat

class MessageSenderAudioItem(
    private val chatAudioMessage: ChatAudioMessage,
    private val user: User,
    val context: ChatLog
) : Item() {

    private var isPlayingState = StateRecord.STOP
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var seekBarHandler: Handler
    private var isFirstTime = true
    private lateinit var updateSeekBar: Runnable

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        setTimeText(viewHolder)
        viewHolder.apply {
            GlideApp.with(viewHolder.itemView.context).load(user.profilePicturePath?.let {
                StorageUtil.pathToReference(
                    it
                )
            })
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageViewToChatLogSenderAudio)
        }
        StorageUtil.pathToReference(chatAudioMessage.audioPath).downloadUrl.addOnSuccessListener { downoladUri ->
            fetchedRecord(downoladUri, viewHolder)
        }
        if (ChatLog.DETSROY_ACTIV) {
            if (mediaPlayer!=null) {
                stopPlayingRecord(mediaPlayer!!)
            }
        }
    }


    private fun fetchedRecord(
        uri: Uri?,
        viewHolder: GroupieViewHolder
    ) {
        if (uri != null) {
            viewHolder.progress_bar_play_image_sender.visibility = View.GONE
            viewHolder.imageViewPlayAudioSender.setOnClickListener {
                if (isPlayingState == StateRecord.START
                ) {
                    stopPlayingRecord(mediaPlayer!!)
                } else if (isPlayingState == StateRecord.STOP && isFirstTime
                ) {
                    startPlayingRecord(uri, viewHolder)
                    viewHolder.imageViewPlayAudioSender.setBackgroundResource(R.drawable.ic_pause)
                } else if (isPlayingState == StateRecord.RESUME && mediaPlayer!!.isPlaying && !isFirstTime) {
                    pauseAudio(viewHolder)
                } else {
                    resumeAudio(viewHolder)
                }
                isFirstTime = false
            }
            viewHolder.seekbar_audio_message_sender.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    pauseAudio(viewHolder)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (seekBar != null) {
                        val progress = seekBar.progress
                        mediaPlayer!!.seekTo(progress)
                        resumeAudio(viewHolder)
                    }

                }

            })
        }
    }

    private fun pauseAudio(viewHolder: GroupieViewHolder) {
        viewHolder.imageViewPlayAudioSender.setBackgroundResource(R.drawable.ic_play_audio)
        mediaPlayer!!.pause()
        isPlayingState = StateRecord.PAUSE
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun resumeAudio(viewHolder: GroupieViewHolder) {
        if (mediaPlayer == null) return
        viewHolder.imageViewPlayAudioSender.setBackgroundResource(R.drawable.ic_pause)
        mediaPlayer!!.start()
        isPlayingState = StateRecord.RESUME
        seekBarHandler = Handler()
        updateRunnable(viewHolder)
        seekBarHandler.postDelayed(updateSeekBar, 500)
    }

    fun stopPlayingRecord(mediaPlayer: MediaPlayer) {
        mediaPlayer.stop()
        isPlayingState = StateRecord.STOP
        seekBarHandler.removeCallbacks(updateSeekBar)
    }

    private fun startPlayingRecord(
        uri: Uri,
        viewHolder: GroupieViewHolder
    ) {
        isPlayingState = StateRecord.RESUME
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        viewHolder.seekbar_audio_message_sender.max = mediaPlayer!!.duration
        seekBarHandler = Handler()
        updateRunnable(viewHolder)
        seekBarHandler.postDelayed(updateSeekBar, 0)

    }

    private fun updateRunnable(viewHolder: GroupieViewHolder) {
        updateSeekBar = object : Runnable {
            override fun run() {
                viewHolder.seekbar_audio_message_sender.progress = mediaPlayer!!.currentPosition
                seekBarHandler.postDelayed(this, 500)
            }
        }
    }

    private fun setTimeText(viewHolder: GroupieViewHolder) {
        val dataFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.itemView.text_view_sender_audio_message_time.text =
            dataFormat.format(chatAudioMessage.time)
    }

    override fun getLayout(): Int {
        return R.layout.audio_mesage_sender_row
    }
}