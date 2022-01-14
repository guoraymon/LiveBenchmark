package com.kuolw.ijkplayer

import android.content.ContentValues.TAG
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.widget.FrameLayout
import android.widget.MediaController
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class IjkPlayer : FrameLayout, MediaController.MediaPlayerControl {
    private var mSurfaceView: SurfaceRenderView = SurfaceRenderView(context)
    var mMediaPlayer: IjkMediaPlayer = IjkMediaPlayer()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        mSurfaceView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        mSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mMediaPlayer.setDisplay(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

            override fun surfaceDestroyed(p0: SurfaceHolder) {}
        })
        this.addView(mSurfaceView)

        // 视频大小变更监听
        mMediaPlayer.setOnVideoSizeChangedListener { _, width, height, sarNum, sarDen ->
            if (width != 0 && height != 0) {
                mSurfaceView.setVideoSize(width, height)
                mSurfaceView.setVideoSampleAspectRatio(sarNum, sarDen)
            }
        }
    }

    fun setUrl(url: String) {
        if (mMediaPlayer.dataSource != null) {
            if (mMediaPlayer.dataSource == url) {
                return
            }
            mMediaPlayer.reset()
            mMediaPlayer.setDisplay(mSurfaceView.holder)
        }

        mMediaPlayer.dataSource = url
        mMediaPlayer.prepareAsync()
    }

    override fun start() {
        mMediaPlayer.start()
    }

    override fun pause() {
        mMediaPlayer.pause()
    }

    override fun getDuration(): Int {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Int {
        TODO("Not yet implemented")
    }

    override fun seekTo(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer.isPlaying
    }

    override fun getBufferPercentage(): Int {
        TODO("Not yet implemented")
    }

    override fun canPause(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canSeekBackward(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canSeekForward(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAudioSessionId(): Int {
        TODO("Not yet implemented")
    }

    fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener) {
        mMediaPlayer.setOnPreparedListener(listener)
    }

    fun setOnInfoListener(listener: IMediaPlayer.OnInfoListener) {
        mMediaPlayer.setOnInfoListener(listener)
    }

    fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener) {
        mMediaPlayer.setOnErrorListener(listener)
    }
}