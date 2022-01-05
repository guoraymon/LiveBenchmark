package com.kuolw.livebenchmark

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kuolw.ijkplayer.IjkPlayer
import com.kuolw.livebenchmark.ui.theme.LiveBenchmarkTheme
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveBenchmarkTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
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
                    var url by remember { mutableStateOf("") }
                    var width by remember { mutableStateOf(0) }
                    var height by remember { mutableStateOf(0) }
                    var format by remember { mutableStateOf("") }
                    var videoDecoder by remember { mutableStateOf("") }
                    var audioDecoder by remember { mutableStateOf("") }
                    var bitRate by remember { mutableStateOf(0L) }
                    var decodeFps by remember { mutableStateOf(0F) }
                    var outputFps by remember { mutableStateOf(0F) }
                    Column {
                        Box {
                            PlayerView(
                                url,
                                onPreparedListener = {
                                    width = it.videoWidth
                                    height = it.videoHeight
                                    format = it.mediaInfo.mMeta.mFormat
                                    videoDecoder = it.mediaInfo.mVideoDecoderImpl
                                    audioDecoder = it.mediaInfo.mAudioDecoderImpl
                                },
                                onPlayerListener = {
                                    bitRate = it.bitRate
                                    decodeFps = it.videoDecodeFramesPerSecond
                                    outputFps = it.videoOutputFramesPerSecond
                                }
                            )
                            PlayerInfo(
                                width,
                                height,
                                format,
                                videoDecoder,
                                audioDecoder,
                                bitRate,
                                decodeFps,
                                outputFps
                            )
                        }
                        SourceList(sources) { source ->
                            url = source.url
                        }
                    }
                }
            }
        }
    }
}

class Source(val name: String, val url: String)

@Composable
fun PlayerView(
    url: String,
    onPreparedListener: (IMediaPlayer) -> Unit,
    onPlayerListener: (IjkMediaPlayer) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4 / 3f),
        factory = { context ->
            IjkPlayer(context).apply {
                this.setOnPreparedListener {
                    Log.d(TAG, "测试")
                    onPreparedListener(it)
                }

                val view = this
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        if (view.isPlaying) {
                            onPlayerListener(view.mMediaPlayer)
                        }
                    }
                }, 0, 1000)
            }
        },
        update = { view ->
            view.setUrl(url)
            view.start()
        }
    )
}

@Composable
fun PlayerInfo(
    width: Int,
    height: Int,
    format: String,
    videoDecoder: String,
    audioDecoder: String,
    bitRate: Long,
    decodeFps: Float,
    outputFps: Float,
) {
    Column {
        Row {
            Text("Width: ", color = Color.Red)
            Text(width.toString(), color = Color.Red)
        }
        Row {
            Text("Height: ", color = Color.Red)
            Text(height.toString(), color = Color.Red)
        }
        Row {
            Text("Format: ", color = Color.Red)
            Text(format, color = Color.Red)
        }
        Row {
            Text("Decoder: ", color = Color.Red)
            Text(
                videoDecoder + (if (isEmpty(videoDecoder)) "" else ",") + audioDecoder,
                color = Color.Red
            )
        }
        Row {
            Text("BitRate: ", color = Color.Red)
            Text(bitRate.toString(), color = Color.Red)
        }
        Row {
            Text("Fps: ", color = Color.Red)
            Text("%.1f".format(decodeFps) + "," + "%.1f".format(outputFps), color = Color.Red)
        }
    }
}

@Composable
fun SourceList(sources: List<Source>, onClick: (Source) -> Unit) {
    LazyColumn {
        items(sources) { source ->
            Column(
                Modifier
                    .clickable(onClick = { onClick(source) })
                    .padding(4.dp)
            ) {
                Text(source.name)
                Text(
                    source.url,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}