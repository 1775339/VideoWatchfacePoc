package com.titan.titanvideotrimmingpoc.video.trim

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.titan.titanvideotrimmingpoc.R

class TrimVideoAdapter(
    context: Context
) : RecyclerView.Adapter<TrimVideoAdapter.VideoHolder>() {

    private val inflater = LayoutInflater.from(context)

    private val data: MutableList<VideoThumbImg> = ArrayList()
    private var itemWidth = 0

    fun setItemWidth(itemWidth: Int) {
        this.itemWidth = itemWidth
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(inflater.inflate(R.layout.demo_video_thumb_item, parent, false))
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.img.loadImage(data[position].path)
    }

    fun ImageView.loadImage(url: String, placeholder: Int = 0) {
        displayImage(this, url, placeholder)
    }

    fun displayImage(iv: ImageView, url: String?, defaultImage: Int) {
        url?.let {
            Glide.with(iv).load(url).placeholder(defaultImage).error(defaultImage).into(iv)
        } ?: run {
            iv.setImageResource(defaultImage)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class VideoHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var img: ImageView

        init {
            img = itemView.findViewById(R.id.thumb)
            val layoutParams = img.layoutParams
            layoutParams.width = itemWidth
            img.layoutParams = layoutParams
        }
    }

    fun addItemVideoInfo(info: VideoThumbImg) {
        data.add(info)
        notifyItemInserted(data.size)
    }

}