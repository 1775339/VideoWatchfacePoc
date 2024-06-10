package com.titan.titanvideotrimmingpoc.widget

import android.content.Context
import android.content.res.Resources
import com.blankj.utilcode.util.Utils
import com.titan.titanvideotrimmingpoc.App

fun Int.dp(): Int {
    return dp2px(toFloat())
}

fun dp2px(dpValue: Float): Int {
    println("v.sagar context getApplicationContext()")
    return getApplicationContext()?.let { (dpValue * getApplicationContext().resources.displayMetrics.density + 0.5f).toInt() } ?: 0
}
fun getApplicationContext():Context= Utils.getApp().applicationContext

private val res: Resources
    get() = context.resources

private val context: Context
    get() = Utils.getApp().applicationContext