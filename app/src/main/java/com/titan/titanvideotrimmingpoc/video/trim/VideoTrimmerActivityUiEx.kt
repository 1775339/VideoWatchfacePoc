package com.titan.titanvideotrimmingpoc.video.trim;

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity

interface VideoTrimmerActivityUiEx<T> {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onShowPreviewDialog(
        activity: AppCompatActivity,
        uri: String,
        onCancelListener: DialogInterface.OnClickListener,
        onConfirmListener: DialogInterface.OnClickListener
    )

    fun onShowProgressDialog(activity: AppCompatActivity,progress: Int):T
    fun onProgressChange(activity: AppCompatActivity,appCompatDialog:T,progress:Int)
    fun onDismissProgressDialog(activity: AppCompatActivity,appCompatDialog:T)
    fun onShowConfirmExitDialog(activity: AppCompatActivity)
}