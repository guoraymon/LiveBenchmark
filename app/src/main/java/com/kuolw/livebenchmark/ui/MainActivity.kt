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
import androidx.compose.ui.Alignment
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
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
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

    private lateinit var mIjkPlayer: IjkPlayer

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

            var currSource by remember { mutableStateOf(SourceEntity(id = 0, name = "", src = "")) }

            var loadStartAt = 0L // 开始加载时间
            var bufferStartAt = 0L // 开始缓冲时间
            var loadSuccess = false // 加载成功

            var loadTime by remember { mutableStateOf(0L) } //加载时长
            var bufferTime by remember { mutableStateOf(0L) } //缓冲时长
            var playTime by remember { mutableStateOf(0L) } //播放时长

            val initIjkPlayer = { ijkPlayer: IjkPlayer ->
                // 监听预备
                ijkPlayer.setOnPreparedListener { mp: IMediaPlayer ->
                    Log.d(TAG, "ijkPlayer onPreparedListener: $mp")

                    loadSuccess = true
                    loadTime = System.currentTimeMillis() - loadStartAt
                    // 记录加载时长
                    sourceViewModel.update(currSource.apply {
                        this.loadTime = loadTime
                        this.check = true
                    })

                    width = mp.videoWidth
                    height = mp.videoHeight
                    format = mp.mediaInfo.mMeta.mFormat
                    videoDecoder = mp.mediaInfo.mVideoDecoderImpl
                    audioDecoder = mp.mediaInfo.mAudioDecoderImpl
                }
                // 监听播放信息
                ijkPlayer.setOnInfoListener { mp, what, extra ->
                    Log.d(
                        TAG,
                        "ijkPlayer setOnInfoListener: $mp, $what, $extra"
                    )

                    when (what) {
                        // 开始缓冲
                        701 -> {
                            bufferStartAt = System.currentTimeMillis()
                        }
                        // 结束缓冲
                        702 -> {
                            bufferStartAt = 0
                            //记录缓冲时长
                            bufferTime = System.currentTimeMillis() - bufferStartAt // 缓冲时长
                            sourceViewModel.update(currSource.apply {
                                this.bufferTime = bufferTime
                            })
                        }
                    }

                    true
                }
                // 监听播放失败
                ijkPlayer.setOnErrorListener { _: IMediaPlayer, what, _ ->
                    sourceViewModel.update(currSource.apply {
                        this.score = 0F
                        this.check = true
                    })

                    Toast.makeText(applicationContext, "播放失败 $what", Toast.LENGTH_SHORT).show()
                    true
                }
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        bitRate = ijkPlayer.mMediaPlayer.bitRate
                        decodeFps = ijkPlayer.mMediaPlayer.videoDecodeFramesPerSecond
                        outputFps = ijkPlayer.mMediaPlayer.videoOutputFramesPerSecond

                        if (loadSuccess) {
                            playTime = System.currentTimeMillis() - loadStartAt //刷新播放时长
                            if (bufferStartAt > 0) {
                                bufferTime = System.currentTimeMillis() - bufferStartAt //刷新缓冲时长
                            }
                            // 评分
                            val loadScore = (5000 - loadTime) / 5000F
                            val playScore = (playTime - bufferTime) / playTime.toFloat()
                            val score = ((loadScore * 300F).roundToInt() + (playScore * 700F).roundToInt()) / 10.0F

                            sourceViewModel.update(currSource.apply {
                                this.playTime = playTime
                                this.bufferTime = bufferTime
                                this.score = score
                            })
                        }
                    }
                }, 0, 1000)
                mIjkPlayer = ijkPlayer
            }

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
                                PlayerView(initIjkPlayer)
                                PlayerInfo(
                                    width,
                                    height,
                                    format,
                                    videoDecoder,
                                    audioDecoder,
                                    bitRate,
                                    decodeFps,
                                    outputFps,
                                    loadTime,
                                    bufferTime,
                                    playTime,
                                )
                            }
                            SourceList(sourceViewModel) { source ->
                                currSource = source
                                loadSuccess = false
                                loadStartAt = System.currentTimeMillis()
                                bufferTime = 0
                                mIjkPlayer.setUrl(source.src)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mIjkPlayer.pause()
    }

    override fun onRestart() {
        super.onRestart()
        mIjkPlayer.start()
    }
}

@Composable
fun PlayerView(init: ((IjkPlayer) -> Unit)) {
    AndroidView(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .aspectRatio(4 / 3f),
        factory = { context ->
            IjkPlayer(context).apply(init)
        },
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
    loadTime: Long,
    bufferTime: Long,
    playTime: Long,
) {
    Column(
        Modifier.background(Color(0x88EEEEEE))
    ) {
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
        Column(
            Modifier.padding(top = 16.dp)
        ) {
            Row {
                Text("loadTime: $loadTime ms", color = Color.Red)
            }
            Row {
                Text("bufferTime: ${DecimalFormat("#0.0").format((bufferTime / 1000.0))} s", color = Color.Red)
            }
            Row {
                Text("playTime: ${DecimalFormat("#0").format((playTime / 1000.0))} s", color = Color.Red)
            }
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
                verticalAlignment = Alignment.CenterVertically,
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
                Text(if (source.check) source.score.toString() else "未测试", color = if (source.score >= 80) Color.Green else Color.Red)
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