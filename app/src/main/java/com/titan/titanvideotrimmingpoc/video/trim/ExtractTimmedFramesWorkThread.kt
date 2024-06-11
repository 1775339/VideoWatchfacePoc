package com.titan.titanvideotrimmingpoc.video.trim

import android.os.Handler


class ExtractTimmedFramesWorkThread(
    private val handler: Handler,
    private val videoPath: String,
    private val outPutDirPath: String,
    private val startPosition: Long,
    private val endPosition: Long,
    private val thumbnailsCount: Int
) : Thread() {

    private val helper = VideoExtractFrameAsyncHelper()

    override fun run() {
        val framesList = helper.getVideoThumbnailsOfTimmedVideo(
            videoPath,
            outPutDirPath,
            startPosition,
            endPosition,
            thumbnailsCount
        )
//        ) {
//            sendImageMessage(it)
//        }
        sendImageMessage(framesList)
    }

    fun stopExtract() {
        helper.stopExtract()
        handler.removeMessages(MSG_FRAME_SAVE_SUCCESS)
    }

    private fun sendImageMessage(list: ArrayList<VideoThumbImg>) {
        handler.obtainMessage(MSG_FRAME_SAVE_SUCCESS).apply {
            this.obj = list
            handler.sendMessage(this)
        }
    }

    companion object {
        const val MSG_FRAME_SAVE_SUCCESS = 1001
    }

}