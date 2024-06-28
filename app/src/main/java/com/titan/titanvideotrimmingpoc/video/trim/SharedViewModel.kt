package com.titan.titanvideotrimmingpoc.video.trim

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel:ViewModel() {
    val pathData:MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}