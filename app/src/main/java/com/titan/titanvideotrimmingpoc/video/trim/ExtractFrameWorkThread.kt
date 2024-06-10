package com.titan.titanvideotrimmingpoc.video.trim

import android.os.Handler

class ExtractFrameWorkThread(
    private val handler: Handler,
    private val videoPath: String,
    private val outPutDirPath: String,
    private val startPosition: Long,
    private val endPosition: Long,
    private val thumbnailsCount: Int
) : Thread() {

    private val helper = VideoExtractFrameAsyncHelper()

    override fun run() {
        helper.getVideoThumbnails(
            videoPath,
            outPutDirPath,
            startPosition,
            endPosition,
            thumbnailsCount
        ) {
            sendImageMessage(it)
        }
    }

    fun stopExtract() {
        helper.stopExtract()
        handler.removeMessages(MSG_SAVE_SUCCESS)
    }

    private fun sendImageMessage(img: VideoThumbImg) {
        handler.obtainMessage(MSG_SAVE_SUCCESS).apply {
            this.obj = img
            handler.sendMessage(this)
        }
    }

    companion object {
        const val MSG_SAVE_SUCCESS = 1000
    }

}