package com.titan.titanvideotrimmingpoc.video.trim;

public interface VideoTrimListener {
    void onStartTrim();
    void onFinishTrim(String url);
    void onCancel();
    void onError();

    default void onProgress(int progress){}
}
