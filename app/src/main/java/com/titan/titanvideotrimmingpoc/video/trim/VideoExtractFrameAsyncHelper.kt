package com.titan.titanvideotrimmingpoc.video.trim

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.blankj.utilcode.util.ImageUtils
import java.io.File
import java.io.IOException


class VideoExtractFrameAsyncHelper() {

    @Volatile
    private var stop = false

    fun getVideoThumbnails(
        videoPath: String?,
        outPutFileDirPath: String,
        startPosition: Long,
        endPosition: Long,
        thumbnailsCount: Int,
        callback:(VideoThumbImg) -> Unit
    ) {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(videoPath)
        val interval = (endPosition - startPosition) / (thumbnailsCount - 1)
        for (i in 0 until thumbnailsCount) {
            if (stop) {
                break
            }

            val time = startPosition + interval * i
            if (i == thumbnailsCount - 1) {
                if (interval > 1000) {
                    val path = extractFrame(metadataRetriever, endPosition - 800, outPutFileDirPath)
                    path?.let {callback(VideoThumbImg(it, endPosition - 800))}
                } else {
                    val path = extractFrame(metadataRetriever, endPosition, outPutFileDirPath)
                    path?.let {callback(VideoThumbImg(it, endPosition))}
                }
            } else {
                val path = extractFrame(metadataRetriever, time, outPutFileDirPath)
                path?.let {callback(VideoThumbImg(it, time))}
            }
        }

        try {
            metadataRetriever.release()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun extractFrame(
        retriever: MediaMetadataRetriever,
        timeMs: Long,
        outputDir: String
    ): String? {
        val bitmap = retriever.getFrameAtTime(
            timeMs * 1000,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
        if (bitmap != null) {
            val outputFile = File(outputDir, "${System.currentTimeMillis()}.jpg")
            ImageUtils.save(bitmap, outputFile, Bitmap.CompressFormat.JPEG, true)
            return outputFile.absolutePath
        }
        return null
    }

    fun stopExtract() {
        stop = true
    }

}