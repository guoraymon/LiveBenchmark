package com.kuolw.livebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kuolw.livebenchmark.databinding.SourceInfoBinding

class SourceInfoFragment : Fragment() {
    private var _binding: SourceInfoBinding? = null
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SourceInfoBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.videoInfo.observe(this, { videoInfo: VideoInfo ->
            _binding!!.width.text = videoInfo.width.toString()
            _binding!!.height.text = videoInfo.height.toString()
            _binding!!.format.text = videoInfo.format
            _binding!!.videoDecoder.text = videoInfo.videoDecoder
            _binding!!.audioDecoder.text = videoInfo.audioDecoder
        })

        model.bitRate.observe(this, { bitRate: Long ->
            _binding!!.bitRate.text = bitRate.toString()
        })
        model.decodeFps.observe(this, { decodeFps: Float ->
            _binding!!.decodeFps.text = decodeFps.toString()
        })
        model.outputFps.observe(this, { outputFps: Float ->
            _binding!!.outputFps.text = outputFps.toString()
        })
    }
}