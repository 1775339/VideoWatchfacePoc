package com.titan.titanvideotrimmingpoc.video.trim

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.LinkedList
import javax.inject.Inject

/**
 * @author Jakob Liu
 */
class TrimVideoViewModel @Inject constructor(
) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _event = MutableLiveData<Event<Int>>()
    val event: LiveData<Event<Int>> = _event

    private lateinit var mExtractVideoInfoUtil: ExtractVideoInfoUtil

    private var videoPath: String? = null

    var startMillis: Long = 0
    var stopMillis: Long = 0

    override fun onCleared() {
        super.onCleared()
        mExtractVideoInfoUtil.release()
    }

    fun start(videoPath: String, max: Long): Long {
        this.videoPath = videoPath

        mExtractVideoInfoUtil = ExtractVideoInfoUtil(videoPath)
        val duration = mExtractVideoInfoUtil.duration
        startMillis = 0
        stopMillis = if (duration > max) max else duration

        return duration
    }

    fun cropVideo(context: Context, callback: (videoPath: String, videoThumb: String) -> Unit) {
        viewModelScope.launch {
            _dataLoading.value = true
            var f:File? = null
            var firstSample:File? = null
            withContext(Dispatchers.IO) {
                val movie = MovieCreator.build(videoPath)
                val tracks = movie.tracks
                movie.tracks = LinkedList()

                tracks.find { it.handler == "vide" }?.let { track ->
                    val startSec = startMillis * 1.0 / 1000
                    val endSec = stopMillis * 1.0 / 1000
                    movie.addTrack(cropTrack(track, startSec, endSec))

                    f = saveNewVideo(context,movie)
                    //获取视频第一帧图片
                    mExtractVideoInfoUtil = ExtractVideoInfoUtil(f!!.absolutePath)
                    val bitmap = mExtractVideoInfoUtil.extractFrame()
                    firstSample = File(context.externalCacheDir, "thumb_${Date().time}.jpg")
                    ImageUtils.save(bitmap, firstSample, Bitmap.CompressFormat.JPEG)
                    if (bitmap != null && !bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                } ?: run {
//                    Timber.w("未找到vide轨道")
                }
            }
            _dataLoading.value = false

            if (f != null && firstSample!= null) {
                callback(f!!.absolutePath, firstSample!!.absolutePath)
            }
        }
    }

    private fun cropTrack(track: Track, startSec: Double, endSec: Double): CroppedTrack {
//        Timber.d("裁剪视频，开始：$startSec, 结束：$endSec")
        var currentTime = 0.0
        var lastTime = -1.0
        var startSample: Int = -1
        var endSample: Int = -1
        for ((currentSample, i) in track.sampleDurations.indices.withIndex()) {
            val delta = track.sampleDurations[i]
            if (currentTime > lastTime && currentTime <= startSec) {
                startSample = currentSample
            }
            if (currentTime > lastTime && currentTime <= endSec) {
                endSample = currentSample
            }
            //计算出某一帧的时长 = 采样时长 / 时间长度
            lastTime = currentTime
            //这里就是帧数（采样）加一
            currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
        }
        //在这里，裁剪是根据关键帧进行裁剪的，而不是指定的开始时间和结束时间
//        Timber.d("裁剪视频，开始帧：$startSample, 结束帧：$endSample")
        return CroppedTrack(track, startSample.toLong(), endSample.toLong())
    }

    private fun saveNewVideo(context: Context,movie: Movie): File {
        val dstFile = File(context.externalCacheDir, "video_trimmed_${Date().time}.mp4")
        val out = DefaultMp4Builder().build(movie)
        FileOutputStream(dstFile).use {
            out.writeContainer(it.channel)
        }
        return dstFile
    }

}