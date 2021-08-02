package com.kuolw.livebenchmark

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.kuolw.livebenchmark.databinding.ActivityMainBinding
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        // ViewPage
        val viewPageAdapter = ViewPageAdapter(this)
        binding.view.adapter = viewPageAdapter

        // TabLayout
        TabLayoutMediator(binding.tab, binding.view) { tab, position ->
            tab.text = listOf("List", "Info")[position]
        }.attach()

        model.source.observe(this, { item ->
            binding.video.setUrl(item.url)
            binding.video.start()
        })

        // every minute timer
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (binding.video.isPlaying) {
                        val mp: IjkMediaPlayer = binding.video.mMediaPlayer
                        model.bitRate.value = mp.bitRate
                        model.decodeFps.value = mp.videoDecodeFramesPerSecond
                        model.outputFps.value = mp.videoOutputFramesPerSecond
                    }
                }
            }
        }, 0, 1000)

        // prepared listener
        binding.video.setOnPreparedListener {
            val videoInfo = model.videoInfo.value
            videoInfo?.width = it.videoWidth
            videoInfo?.height = it.videoHeight
            videoInfo?.format = it.mediaInfo.mMeta.mFormat
            videoInfo?.videoDecoder = it.mediaInfo.mVideoDecoderImpl
            videoInfo?.audioDecoder = it.mediaInfo.mAudioDecoderImpl
            model.videoInfo.value = videoInfo
        }
    }

    override fun onPause() {
        super.onPause()

        binding.video.pause()
    }
}