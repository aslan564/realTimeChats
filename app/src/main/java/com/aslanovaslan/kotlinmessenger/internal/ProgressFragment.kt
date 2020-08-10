package com.aslanovaslan.kotlinmessenger.internal

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.aslanovaslan.kotlinmessenger.R

class ProgressFragment : DialogFragment() {

    lateinit var dialogProgress: TextView
    private lateinit var progressBar: ProgressBar

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_progres, container, false)
        dialogProgress = view.findViewById(R.id.textViewProgresBar)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.indeterminateDrawable.colorFilter.apply {
            ContextCompat.getColor(activity!!, android.R.color.black)
            PorterDuff.Mode.SRC_IN
        }
        return view
    }
}
