package com.kuolw.livebenchmark.ui

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.util.Log
import android.widget.Toast
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
import com.kuolw.livebenchmark.viewmodel.AppViewModel
import com.kuolw.livebenchmark.viewmodel.AppViewModelFactory
import com.kuolw.livebenchmark.viewmodel.SourceViewModel
import com.kuolw.livebenchmark.viewmodel.SourceViewModelFactory
import net.bjoernpetersen.m3u.M3uParser
import net.bjoernpetersen.m3u.model.M3uEntry
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.InputStreamReader
import java.util.*

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels {
        AppViewModelFactory()
    }
    private val sourceViewModel: SourceViewModel by viewModels {
        SourceViewModelFactory((application as MainApplication).repository)
    }

    private val importActivityResult =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri !== null) {
                contentResolver.openInputStream(uri).use { inputStream ->
                    val m3uReader: InputStreamReader = inputStream!!.reader()
                    val m3uEntries: List<M3uEntry> = M3uParser.parse(m3uReader)
                    for (m3uEntry in m3uEntries) {
                        sourceViewModel.insert(
                            SourceEntity(
                                id = 0,
                                name = m3uEntry.title!!,
                                src = m3uEntry.location.toString()
                            )
                        )
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                    // delete all dialog
                    val deleteALLDialog = remember { mutableStateOf(false) }
                    if (deleteALLDialog.value) {
                        AlertDialog(
                            title = {
                                Text(text = "Delete All")
                            },
                            text = {
                                Text(
                                    "Delete All? "
                                )
                            },
                            onDismissRequest = {
                                deleteALLDialog.value = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        sourceViewModel.deleteAll()
                                        deleteALLDialog.value = false
                                    }
                                ) {
                                    Text("Confirm")
                                }
                            },
                        )
                    }

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
                                            DropdownMenuItem(onClick = {
                                                expanded = false
                                            }) {
                                                Text("Export")
                                            }
                                            DropdownMenuItem(onClick = {
                                                deleteALLDialog.value = true
                                                expanded = false
                                            }) {
                                                Text("Delete All")
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
                                    appViewModel.isPlay.value,
                                    appViewModel.url.value,
                                    onPingListener = {
                                        bitRate = it.mMediaPlayer.bitRate
                                        decodeFps = it.mMediaPlayer.videoDecodeFramesPerSecond
                                        outputFps = it.mMediaPlayer.videoOutputFramesPerSecond
                                    },
                                    onPreparedListener = {
                                        width = it.videoWidth
                                        height = it.videoHeight
                                        format = it.mediaInfo.mMeta.mFormat
                                        videoDecoder = it.mediaInfo.mVideoDecoderImpl
                                        audioDecoder = it.mediaInfo.mAudioDecoderImpl
                                    },
                                    onInfoListener = { mp: IMediaPlayer, what, extra ->
                                        Log.d(TAG, "onCreate: $mp, $what, $extra")
                                        true
                                    }
                                ) { mp: IMediaPlayer, what, extra ->
                                    Log.d(TAG, "onCreate: $mp, $what, $extra")
                                    Toast.makeText(
                                        applicationContext,
                                        "播放失败 $what",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    true
                                }
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
                                appViewModel.isPlay.value = true
                                appViewModel.url.value = source.src
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        appViewModel.isPlaySave.value = appViewModel.isPlay.value
        appViewModel.isPlay.value = false
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        appViewModel.isPlay.value = appViewModel.isPlaySave.value
    }
}

@Composable
fun PlayerView(
    isPlay: Boolean,
    url: String,
    onPingListener: ((IjkPlayer) -> Unit)? = null,
    onPreparedListener: IMediaPlayer.OnPreparedListener? = null,
    onInfoListener: IMediaPlayer.OnInfoListener? = null,
    onErrorListener: IMediaPlayer.OnErrorListener? = null,
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4 / 3f),
        factory = { context ->
            IjkPlayer(context).apply {
                val ijkPlayer = this

                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        if (onPingListener != null) {
                            onPingListener(ijkPlayer)
                        }
                    }
                }, 0, 1000)

                if (onPreparedListener != null) {
                    this.setOnPreparedListener(onPreparedListener)
                }
                if (onInfoListener != null) {
                    this.setOnInfoListener(onInfoListener)
                }
                if (onErrorListener != null) {
                    this.setOnErrorListener(onErrorListener)
                }
            }
        },
        update = { ijkPlayer: IjkPlayer ->
            if (isPlay) {
                ijkPlayer.setUrl(url)
                ijkPlayer.start()
            } else {
                ijkPlayer.pause()
            }
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
                        DropdownMenuItem(onClick = {
                            expandedId = null
                            sourceViewModel.delete(source)
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}