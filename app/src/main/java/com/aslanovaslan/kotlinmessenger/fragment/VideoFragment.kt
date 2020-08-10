package com.aslanovaslan.kotlinmessenger.fragment

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.internal.eventBus.EventBusData
import com.universalvideoview.UniversalMediaController
import com.universalvideoview.UniversalVideoView
import kotlinx.android.synthetic.main.fragment_video.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VideoFragment : Fragment(), View.OnClickListener {
    private val SEEK_POSITION_KEY = "SEEK_POSITION_KEY"
    private val VIDEO_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"

    private var videoPath: Uri? = null

    private lateinit var universalVideoView: UniversalVideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        universalVideoView = view.videoViewPlayer
        if (videoPath != null) {
            universalVideoView.setVideoURI(videoPath)
        }
        view.apply {
            buttonPlayVideoView.setOnClickListener {
                universalVideoView.start()
            }
            videoViewPlayer.setOnClickListener{
                buttonPlayVideoView.visibility=View.VISIBLE
            }
        }
        return view
    }


    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    internal fun getEventBusData(videoUrl: EventBusData.SharedVideoUrl) {
        videoPath = Uri.parse(videoUrl.videoPathUri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }


    companion object {
        private const val TAG = "VideoFragment"
    }

    override fun onClick(v: View?) {

    }
}