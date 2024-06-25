package com.titan.titanvideotrimmingpoc.video.trim

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class VideoTrimmerViewModel: ViewModel() {
    sealed class CutState{
        object NotCutting:CutState()
        object StartCutting:CutState()
        class Cutting(val progress: Int):CutState()
        class FinishCutting(val uri: String):CutState()
    }
    var mCurrentStateFlow: MutableStateFlow<CutState> = MutableStateFlow(CutState.NotCutting)
    val mVideoTrimListener: VideoTrimListener = object : VideoTrimListener {
        override fun onStartTrim() {
            Log.e("TAG", "onStartTrim")
            if (VideoTrimmerUtil.videoTrimListener != null) {
                VideoTrimmerUtil.videoTrimListener.onStartTrim()
            }
            mCurrentStateFlow.value = CutState.StartCutting
        }

        override fun onFinishTrim(url: String?) {
            Log.e("TAG", "onFinishTrim$url")
            mCurrentStateFlow.value = CutState.FinishCutting(url!!)
        }

        override fun onProgress(progress: Int) {
            Log.e("TAG", "onProgress$progress")
            mCurrentStateFlow.value = CutState.Cutting(progress)
        }

        override fun onCancel() {
            if (VideoTrimmerUtil.videoTrimListener != null) {
                VideoTrimmerUtil.videoTrimListener.onCancel()
            }
        }

        override fun onError() {
            if (VideoTrimmerUtil.videoTrimListener != null) {
                VideoTrimmerUtil.videoTrimListener.onError()
            }
        }
    }
}