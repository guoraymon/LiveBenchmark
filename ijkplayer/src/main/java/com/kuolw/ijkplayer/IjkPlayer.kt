package com.kuolw.ijkplayer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.MediaController
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class IjkPlayer : FrameLayout, MediaController.MediaPlayerControl {
    private var mSurfaceView: SurfaceView = SurfaceView(context)
    var mMediaPlayer: IjkMediaPlayer = IjkMediaPlayer()

    private lateinit var mOnPreparedListener: IMediaPlayer.OnPreparedListener

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        mSurfaceView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
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
    }

    fun setUrl(url: String) {
        if (mMediaPlayer.dataSource != null) {
            mMediaPlayer.reset()
            mMediaPlayer.setDisplay(mSurfaceView.holder)
        }

        mMediaPlayer.dataSource = url
        mMediaPlayer.prepareAsync()

        mMediaPlayer.setOnPreparedListener(mOnPreparedListener)
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: IMediaPlayer.OnPreparedListener) {
        mOnPreparedListener = l
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
}