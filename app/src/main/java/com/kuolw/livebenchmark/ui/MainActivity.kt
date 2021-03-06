package com.kuolw.livebenchmark.ui

import android.net.Uri
import android.os.Bundle
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
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.kuolw.ijkplayer.IjkPlayer
import com.kuolw.livebenchmark.MainApplication
import com.kuolw.livebenchmark.db.entity.SourceEntity
import com.kuolw.livebenchmark.ui.theme.AppTheme
import com.kuolw.livebenchmark.viewmodel.PlayViewModel
import com.kuolw.livebenchmark.viewmodel.PlayViewModelFactory
import com.kuolw.livebenchmark.viewmodel.SourceViewModel
import com.kuolw.livebenchmark.viewmodel.SourceViewModelFactory
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.text.DecimalFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val sourceViewModel: SourceViewModel by viewModels {
        SourceViewModelFactory((application as MainApplication).repository)
    }

    private val playViewModel: PlayViewModel by viewModels {
        PlayViewModelFactory()
    }

    /**
     * 数据导入
     */
    private val import = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            return@registerForActivityResult
        }
        contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) {
                return@registerForActivityResult
            }
            csvReader().open(inputStream) {
                readAllAsSequence().forEach { row: List<String> ->
                    sourceViewModel.insert(
                        SourceEntity(
                            name = row[0],
                            src = row[1]
                        )
                    )
                }
            }
        }
    }

    /**
     * 数据导出
     */
    private val export = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
        if (uri === null) {
            return@registerForActivityResult
        }
        contentResolver.openOutputStream(uri).use { outputStream ->
            if (outputStream == null) {
                return@registerForActivityResult
            }
            csvWriter().open(outputStream) {
                writeRow(
                    "name",
                    "src",
                    "width",
                    "height",
                    "format",
                    "videoDecoder",
                    "audioDecoder",
                    "loadTime",
                    "bufferTime",
                    "playTime",
                    "score"
                )
                sourceViewModel.sources.forEach {
                    writeRow(
                        it.name,
                        it.src,
                        it.width,
                        it.height,
                        it.format,
                        it.videoDecoder,
                        it.audioDecoder,
                        it.loadTime,
                        it.bufferTime,
                        it.playTime,
                        it.score
                    )
                }
            }
        }
    }

    private lateinit var mIjkPlayer: IjkPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var loadStartAt = 0L // 开始加载时间
            var bufferStartAt = 0L // 开始缓冲时间
            var loadSuccess = false // 加载成功

            val onClick = { source: SourceEntity ->
                playViewModel.currSource.value = source
                playViewModel.reset()

                loadStartAt = System.currentTimeMillis()
                bufferStartAt = 0
                loadSuccess = false

                mIjkPlayer.setUrl(source.src)
            }

            val playNext = {
                val sources = sourceViewModel.sources
                val index = sources.indexOf(playViewModel.currSource.value)
                val nextSource = sources.elementAtOrElse(index + 1, defaultValue = {
                    sources.firstOrNull()
                })
                if (nextSource != null) {
                    onClick(nextSource)
                }
            }

            val initIjkPlayer = { ijkPlayer: IjkPlayer ->
                // 监听预备
                ijkPlayer.setOnPreparedListener { mp: IMediaPlayer ->
                    loadSuccess = true
                    val loadTime = System.currentTimeMillis() - loadStartAt
                    playViewModel.loadTime.value = loadTime
                    playViewModel.getCurrSource()?.let {
                        sourceViewModel.update(it.apply {
                            this.check = true
                            this.loadTime = loadTime
                            this.width = mp.videoWidth
                            this.height = mp.videoHeight
                            this.format = mp.mediaInfo.mMeta.mFormat
                            this.videoDecoder = mp.mediaInfo.mVideoDecoderImpl
                            this.audioDecoder = mp.mediaInfo.mAudioDecoderImpl
                        })
                    }
                }
                // 监听播放信息
                ijkPlayer.setOnInfoListener { _, what, _ ->
                    when (what) {
                        // 开始缓冲
                        701 -> {
                            bufferStartAt = System.currentTimeMillis()
                        }
                        // 结束缓冲
                        702 -> {
                            val currBufferTime = System.currentTimeMillis() - bufferStartAt

                            // 记录缓冲时长
                            playViewModel.currBufferTime.value = currBufferTime // 当前缓冲时长
                            playViewModel.bufferTime.value += currBufferTime // 缓冲时长

                            // 缓存结束
                            bufferStartAt = 0
                            playViewModel.currBufferTime.value = 0

                            playViewModel.getCurrSource()?.let {
                                sourceViewModel.update(it.apply {
                                    this.bufferTime += currBufferTime
                                })
                            }
                        }
                    }
                    true
                }
                // 监听播放失败
                ijkPlayer.setOnErrorListener { _: IMediaPlayer, what, _ ->
                    playViewModel.getCurrSource()?.let {
                        sourceViewModel.update(it.apply {
                            this.check = true
                            this.score = 0F
                        })
                        playNext() // 播放下一个源
                    }

                    Toast.makeText(applicationContext, "播放失败 $what", Toast.LENGTH_SHORT).show()
                    true
                }
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        if (loadSuccess) {
                            val playTime = System.currentTimeMillis() - loadStartAt
                            playViewModel.playTime.value = playTime // 刷新播放时长
                            if (bufferStartAt > 0) {
                                playViewModel.currBufferTime.value = System.currentTimeMillis() - bufferStartAt //刷新缓冲时长
                            }

                            playViewModel.getCurrSource()?.let {
                                sourceViewModel.update(it.apply {
                                    this.playTime = playTime
                                    this.bufferTime = playViewModel.getSumBufferTime()
                                    this.score = playViewModel.getScore()
                                })
                            }

                            if (playTime >= 60 * 1000) {
                                playNext() // 播放下一个源
                            }
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
                            title = { Text(text = "Delete All") },
                            text = { Text("Delete All? ") },
                            onDismissRequest = { deleteALLDialog.value = false },
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
                            TopBar(
                                onImport = {
                                    import.launch(arrayOf("text/*"))
                                },
                                onExport = {
                                    export.launch("export.csv")
                                },
                                onDeleteAll = {
                                    deleteALLDialog.value = true
                                }
                            )
                        }
                    ) {
                        Column {
                            Box {
                                PlayerView(initIjkPlayer)
                                PlayerInfo(playViewModel)
                            }
                            SourceList(sourceViewModel, playViewModel, onClick)
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
fun TopBar(
    onImport: () -> Unit,
    onExport: () -> Unit,
    onDeleteAll: () -> Unit,
) {
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
                        expanded = false
                        onImport()
                    }) {
                        Text("Import")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onExport()
                    }) {
                        Text("Export")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onDeleteAll()
                    }) {
                        Text("Delete All")
                    }
                }
            }
        }
    )
}

@Composable
fun PlayerView(init: ((IjkPlayer) -> Unit)) {
    AndroidView(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .aspectRatio(16 / 9f),
        factory = { context ->
            IjkPlayer(context).apply(init)
        },
    )
}

@Composable
fun PlayerInfo(playViewModel: PlayViewModel) {
    val currSource = playViewModel.getCurrSource()
    val width = (currSource?.width ?: 0).toString()
    val height = (currSource?.height ?: 0).toString()
    val format = currSource?.format ?: ""
    val videoDecoder = currSource?.videoDecoder ?: ""
    val audioDecoder = currSource?.audioDecoder ?: ""
    val decoder = if (videoDecoder != "") "$videoDecoder,$audioDecoder" else ""

    Column(
        Modifier.background(Color(0x88EEEEEE))
    ) {
        Row {
            Text("Width: ", color = Color.Red)
            Text(width, color = Color.Red)
        }
        Row {
            Text("Height: ", color = Color.Red)
            Text(height, color = Color.Red)
        }
        Row {
            Text("Format: ", color = Color.Red)
            Text(format, color = Color.Red)
        }
        Row {
            Text("Decoder: ", color = Color.Red)
            Text(decoder, color = Color.Red)
        }
        Column(
            Modifier.padding(top = 16.dp)
        ) {
            Row {
                Text("loadTime: ${playViewModel.loadTime.value} ms", color = Color.Red)
            }
            Row {
                Text("bufferTime: ${DecimalFormat("#0.0").format((playViewModel.getSumBufferTime() / 1000.0))} s", color = Color.Red)
            }
            Row {
                Text("playTime: ${DecimalFormat("#0").format((playViewModel.playTime.value / 1000.0))} s", color = Color.Red)
            }
        }
    }
}

@Composable
fun SourceList(
    sourceViewModel: SourceViewModel,
    playViewModel: PlayViewModel,
    onClick: (SourceEntity) -> Unit
) {
    val sources = sourceViewModel.sources.toList()
    var expandedId: Int? by remember { mutableStateOf(null) }

    LazyColumn {
        itemsIndexed(sources) { index, source ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = {
                        onClick(source)
                    })
                    .background(if (playViewModel.currSource.value == source) Color.LightGray else Color.White)
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
                        DropdownMenuItem(onClick = {}) {
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