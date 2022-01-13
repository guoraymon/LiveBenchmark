package com.kuolw.ijkplayer

import android.view.SurfaceView
import android.view.View
import kotlin.math.min

class MeasureHelper {
    enum class AspectRatio {
        AR_ASPECT_FIT_PARENT,
        AR_ASPECT_FILL_PARENT,
        AR_ASPECT_WRAP_CONTENT,
        AR_MATCH_PARENT,
        AR_16_9_FIT_PARENT,
        AR_4_3_FIT_PARENT
    }

    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mVideoSarNum = 0
    private var mVideoSarDen = 0

    private var mMeasuredWidth = 0
    private var mMeasuredHeight = 0

    private val mCurrentAspectRatio: Int = AspectRatio.AR_ASPECT_FIT_PARENT.ordinal

    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        mVideoWidth = videoWidth
        mVideoHeight = videoHeight
    }

    fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int) {
        mVideoSarNum = videoSarNum
        mVideoSarDen = videoSarDen
    }

    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = SurfaceView.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = SurfaceView.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mCurrentAspectRatio == AspectRatio.AR_MATCH_PARENT.ordinal) {
            width = widthMeasureSpec
            height = heightMeasureSpec
        } else if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
                val specAspectRatio = widthSpecSize.toFloat() / heightSpecSize.toFloat()
                var displayAspectRatio: Float
                when (mCurrentAspectRatio) {
                    AspectRatio.AR_16_9_FIT_PARENT.ordinal -> {
                        displayAspectRatio = 16.0f / 9.0f
                    }
                    AspectRatio.AR_4_3_FIT_PARENT.ordinal -> {
                        displayAspectRatio = 4.0f / 3.0f
                    }
                    AspectRatio.AR_ASPECT_FIT_PARENT.ordinal, AspectRatio.AR_ASPECT_FILL_PARENT.ordinal, AspectRatio.AR_ASPECT_WRAP_CONTENT.ordinal -> {
                        displayAspectRatio = mVideoWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) displayAspectRatio =
                            displayAspectRatio * mVideoSarNum / mVideoSarDen
                    }
                    else -> {
                        displayAspectRatio = mVideoWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) displayAspectRatio =
                            displayAspectRatio * mVideoSarNum / mVideoSarDen
                    }
                }
                val shouldBeWider = displayAspectRatio > specAspectRatio
                when (mCurrentAspectRatio) {
                    AspectRatio.AR_ASPECT_FIT_PARENT.ordinal, AspectRatio.AR_16_9_FIT_PARENT.ordinal, AspectRatio.AR_4_3_FIT_PARENT.ordinal -> if (shouldBeWider) {
                        // too wide, fix width
                        width = widthSpecSize
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        // too high, fix height
                        height = heightSpecSize
                        width = (height * displayAspectRatio).toInt()
                    }
                    AspectRatio.AR_ASPECT_FILL_PARENT.ordinal -> if (shouldBeWider) {
                        // not high enough, fix height
                        height = heightSpecSize
                        width = (height * displayAspectRatio).toInt()
                    } else {
                        // not wide enough, fix width
                        width = widthSpecSize
                        height = (width / displayAspectRatio).toInt()
                    }
                    AspectRatio.AR_ASPECT_WRAP_CONTENT.ordinal -> if (shouldBeWider) {
                        // too wide, fix width
                        width = min(mVideoWidth, widthSpecSize)
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        // too high, fix height
                        height = min(mVideoHeight, heightSpecSize)
                        width = (height * displayAspectRatio).toInt()
                    }
                    else -> if (shouldBeWider) {
                        width = min(mVideoWidth, widthSpecSize)
                        height = (width / displayAspectRatio).toInt()
                    } else {
                        height = min(mVideoHeight, heightSpecSize)
                        width = (height * displayAspectRatio).toInt()
                    }
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        }

        mMeasuredWidth = width
        mMeasuredHeight = height
    }

    fun getMeasuredWidth(): Int {
        return mMeasuredWidth
    }

    fun getMeasuredHeight(): Int {
        return mMeasuredHeight
    }
}