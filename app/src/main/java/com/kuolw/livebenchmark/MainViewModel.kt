package com.kuolw.livebenchmark

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val source: MutableLiveData<Source> = MutableLiveData<Source>()
    val sources: MutableLiveData<MutableList<Source>> by lazy {
        MutableLiveData<MutableList<Source>>().also {
            it.value = loadSources()
        }
    }

    var videoInfo: MutableLiveData<VideoInfo> = MutableLiveData(VideoInfo())
    var bitRate: MutableLiveData<Long> = MutableLiveData(0L)
    var decodeFps: MutableLiveData<Float> = MutableLiveData(0F)
    var outputFps: MutableLiveData<Float> = MutableLiveData(0F)

    private fun loadSources(): MutableList<Source> {
        return mutableListOf(
            Source(
                "CCTV1",
                "http://39.135.53.199/ott.fj.chinamobile.com/PLTV/88888888/224/3221225829/index.m3u8"
            ),
            Source(
                "CCTV2",
                "http://39.135.53.199/ott.fj.chinamobile.com/PLTV/88888888/224/3221225923/index.m3u8"
            ),
            Source(
                "CCTV4",
                "http://39.135.53.199/ott.fj.chinamobile.com/PLTV/88888888/224/3221226968/index.m3u8"
            ),
            Source(
                "厦门卫视",
                "http://39.135.53.194/ott.fj.chinamobile.com/PLTV/88888888/224/3221226781/index.m3u8"
            ),
            Source(
                "漳州一套",
                "http://31182.hlsplay.aodianyun.com/lms_31182/tv_channel_175.m3u8"
            )
        )
    }
}