package com.titan.titanvideotrimmingpoc.utils

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

fun View.touchAlphaEffect() {
    this.setOnTouchListener(object : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (!v.isEnabled) {
                return false
            }
            when (event.action) {
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    if (v is ImageView) {
                        if (v.drawable == null) {
                            v.background?.alpha = 255
                        } else {
                            v.drawable.alpha = 255
                        }
                    } else {
                        v.background?.alpha = 255
                    }
                }

                else -> {
                    if (v is ImageView) {
                        if (v.drawable == null) {
                            v.background?.alpha = (255 * 0.2).toInt()
                        } else {
                            v.drawable.alpha = (255 * 0.2).toInt()
                        }
                    } else {
                        v.background?.alpha = (255 * 0.2).toInt()
                    }
                }
            }
            return false
        }
    })
}