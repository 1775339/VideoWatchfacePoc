package com.titan.titanvideotrimmingpoc.video.trim

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class VideoThumbSpacingItemDecoration(
    private val mSpaceWidth: Int,
    var count:Int = 0
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        //第一个与最后一个添加空白间距
        if (position == 0) {
            outRect.left = mSpaceWidth
            outRect.right = 0
        } else if (count > 10 && position == count - 1) {
            outRect.left = 0
            outRect.right = mSpaceWidth
        } else {
            outRect.left = 0
            outRect.right = 0
        }
    }
}