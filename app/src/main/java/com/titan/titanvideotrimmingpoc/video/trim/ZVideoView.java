package com.titan.titanvideotrimmingpoc.video.trim;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.titan.titanvideotrimmingpoc.utils.SM;


/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/10/20 11:22 AM
 * version: 1.0
 * description:
 */
public class ZVideoView extends VideoView {
    private int mVideoWidth = 466;
    private int mVideoHeight = 466;
    private int videoRealW = 1;
    private int videoRealH = 1;

    public ZVideoView(Context context) {
        super(context);
    }

    public ZVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(uri.getPath());
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        try {
            videoRealH = SM.spLoadInt(getContext(), "mLayoutHeight");
            videoRealW = SM.spLoadInt(getContext(), "mLayoutWidth");
            //初始化
            SM.spSaveInt(getContext(), "startX", (int) (videoRealW / 2 - 233));
            SM.spSaveInt(getContext(), "startY", (int) (videoRealH / 2 - 233));
            SM.spSaveBoolean(getContext(), "isFirst", true);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (height > width) {
            //竖屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
                float r = videoRealW / (float) videoRealH;
                mVideoWidth = (int) (mVideoHeight * r);
            } else {

                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth = width;
                float r = videoRealH / (float) videoRealW;
                mVideoHeight = (int) (mVideoWidth * r);
            }
        } else {

        }
        SM.spSaveInt(getContext(), "mViewWidth", mVideoWidth);
        SM.spSaveInt(getContext(), "mViewHeight", mVideoHeight);
        if (videoRealH == videoRealW && videoRealH == 1) {
            //没能获取到视频真实的宽高，自适应就可以了，什么也不用做
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight);
        }

    }

}
