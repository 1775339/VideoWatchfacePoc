package com.titan.titanvideotrimmingpoc.video.trim

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import java.io.File
import java.io.IOException

class ExtractVideoInfoUtil(path: String) {

    private var retriever: MediaMetadataRetriever

    init {
        if (TextUtils.isEmpty(path)) {
            throw RuntimeException("path must be not null !")
        }
        val file = File(path)
        if (!file.exists()) {
            throw RuntimeException("path file   not exists !")
        }
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
    }

    val videoWidth: Int
        get() {
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            return w?.toIntOrNull() ?: 0
        }

    val videoHeight: Int
        get() {
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            return h?.toIntOrNull() ?: 0
        }

    fun extractFrame(): Bitmap? {
        return retriever.frameAtTime
    }

    fun extractFrame(timeMs: Long): Bitmap? {
        //第一个参数是传入时间，只能是us(微秒)
        //OPTION_CLOSEST ,在给定的时间，检索最近一个帧,这个帧不一定是关键帧。
        //OPTION_CLOSEST_SYNC   在给定的时间，检索最近一个同步与数据源相关联的的帧（关键帧）
        //OPTION_NEXT_SYNC 在给定时间之后检索一个同步与数据源相关联的关键帧。
        //OPTION_PREVIOUS_SYNC  顾名思义，同上
        //Bitmap bitmap = mMetadataRetriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        var bitmap: Bitmap? = null
        if (timeMs < duration) {
            bitmap = retriever.getFrameAtTime(
                timeMs * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
        }
        return bitmap
    }

    val duration: Long
        get() {
            val l = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return l?.toLongOrNull() ?: 0L
        }

    val rotation: Int
        get() {
            val d = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            return d?.toIntOrNull() ?: 0
        }

    fun release() {
        try {
            retriever.release()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}