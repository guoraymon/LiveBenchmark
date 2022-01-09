package com.kuolw.livebenchmark.ui

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kuolw.ijkplayer.IjkPlayer
import com.kuolw.livebenchmark.MainApplication
import com.kuolw.livebenchmark.db.entity.SourceEntity
import com.kuolw.livebenchmark.ui.theme.AppTheme
import com.kuolw.livebenchmark.viewmodel.SourceViewModel
import com.kuolw.livebenchmark.viewmodel.SourceViewModelFactory
import net.bjoernpetersen.m3u.M3uParser
import net.bjoernpetersen.m3u.model.M3uEntry
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.InputStreamReader
import java.util.*

class MainActivity : ComponentActivity() {
    private val sourceViewModel: SourceViewModel by viewModels {
        SourceViewModelFactory((application as MainApplication).repository)
    }

    private val importActivityResult =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri !== null) {
                Thread {
                    contentResolver.openInputStream(uri).use { inputStream ->
                        val m3uReader: InputStreamReader = inputStream!!.reader()
                        val m3uEntries: List<M3uEntry> = M3uParser.parse(m3uReader)

                        for (m3uEntry in m3uEntries) {
                            (application as MainApplication).database.sourceDao().insert(
                                SourceEntity(
                                    id = 0,
                                    name = m3uEntry.title!!,
                                    src = m3uEntry.location.toString()
                                )
                            )
                            Log.d(TAG, "insert success!")
                        }
                    }
                }.start()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var url by remember { mutableStateOf("") }
            var width by remember { mutableStateOf(0) }
            var height by remember { mutableStateOf(0) }
            var format by remember { mutableStateOf("") }
            var videoDecoder by remember { mutableStateOf("") }
            var audioDecoder by remember { mutableStateOf("") }
            var bitRate by remember { mutableStateOf(0L) }
            var decodeFps by remember { mutableStateOf(0F) }
            var outputFps by remember { mutableStateOf(0F) }

            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("LiveBenchmark") },
                                actions = {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(onClick = {
                                                importActivityResult.launch(arrayOf("audio/x-mpegurl"))
                                                expanded = false
                                            }) {
                                                Text("Import")
                                            }
                                            DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                                                Text("Export")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    ) {
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
                            SourceList(sourceViewModel) { source ->
                                url = source.src
                            }
                        }
                    }
                }
            }
        }
    }
}

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
fun SourceList(sourceViewModel: SourceViewModel, onClick: (SourceEntity) -> Unit) {
    var clickId: Int? by remember { mutableStateOf(null) }
    var expandedId: Int? by remember { mutableStateOf(null) }

    val sources = sourceViewModel.sources.collectAsState(arrayListOf())
    LazyColumn {
        itemsIndexed(sources.value) { index, source ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .clickable(onClick = {
                        clickId = index
                        onClick(source)
                    })
                    .background(if (clickId == index) Color.LightGray else Color.White)
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                ) {
                    Text(source.name)
                    Text(
                        source.src,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box {
                    IconButton(onClick = { expandedId = index }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = expandedId == index,
                        onDismissRequest = { expandedId = null }
                    ) {
                        DropdownMenuItem(onClick = { }) {
                            Text("Edit")
                        }
                        DropdownMenuItem(onClick = { }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}