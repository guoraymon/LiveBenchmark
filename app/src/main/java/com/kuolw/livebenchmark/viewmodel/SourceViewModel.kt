package com.kuolw.livebenchmark.viewmodel

import androidx.lifecycle.ViewModel
import com.kuolw.livebenchmark.model.Source

class SourceViewModel : ViewModel() {
    val sources = listOf(
        Source(
            "厦门卫视",
            "http://223.110.246.73/ott.js.chinamobile.com/PLTV/4/224/3221226996/index.m3u8"
        ),
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
        ),
    )
}