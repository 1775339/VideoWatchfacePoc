package com.titan.titanvideotrimmingpoc.video.trim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.coder.ffmpeg.call.CommonCallBack;
import com.coder.ffmpeg.call.IFFmpegCallBack;
import com.coder.ffmpeg.jni.FFmpegCommand;
import com.coder.ffmpeg.utils.FFmpegUtils;
import com.github.gzuliyujiang.imagepicker.SP;
import com.titan.titanvideotrimmingpoc.utils.SM;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;


/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoTrimmerUtil {

    private static final String TAG = VideoTrimmerUtil.class.getSimpleName();
    public static long MIN_SHOOT_DURATION = 1000L;// 最小剪辑时间1s
    public static long MAX_SHOOT_DURATION_SELECT = 5000L;// 最小剪辑时间1s
    public static int VIDEO_MAX_TIME = 10;// 5秒
    public static long MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L;//视频最多剪切多长时间

    public static int MAX_COUNT_RANGE = 10;  //seekBar的区域内一共有多少张图片
    public static int SCREEN_WIDTH_FULL = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static int RECYCLER_VIEW_PADDING = (int) DimensionUtils.applyDimension(35, DimensionUtils.Unit.DIP);
    public static int VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2;
    public static int THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VIDEO_MAX_TIME;
    public static int THUMB_HEIGHT = (int) DimensionUtils.applyDimension(50, DimensionUtils.Unit.DIP);

    // 添加一个全局的视频裁剪监听
    public static VideoTrimListener videoTrimListener = null;

    private static int startX;
    private static int startY;

    public static void setVideoTrimListener(VideoTrimListener videoTrimListener) {
        VideoTrimmerUtil.videoTrimListener = videoTrimListener;
    }

    public static void trim(Context context, String inputFile, String outputFile, long startMs, long endMs, final VideoTrimListener callback) {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        final String outputName = "trimmedVideo_" + timeStamp + ".mp4";
        outputFile = outputFile + "/" + outputName;

//    String start = convertSecondsToTime(startMs / 1000);
//    String duration = convertSecondsToTime((endMs - startMs) / 1000);
        String start = String.valueOf(startMs / 1000);
        String duration = String.valueOf(endMs / 1000);

        /** 裁剪视频ffmpeg指令说明：
         * ffmpeg -ss START -t DURATION -i INPUT -codec copy -avoid_negative_ts 1 OUTPUT
         -ss 开始时间，如： 00:00:20，表示从20秒开始；
         -t 时长，如： 00:00:10，表示截取10秒长的视频；
         -i 输入，后面是空格，紧跟着就是输入视频文件；
         -codec copy -avoid_negative_ts 1 表示所要使用的视频和音频的编码格式，这里指定为copy表示原样拷贝；
         INPUT，输入视频文件；
         OUTPUT，输出视频文件
         */
        //TODO: Here are some instructions
        //https://trac.ffmpeg.org/wiki/Seeking
        //https://superuser.com/questions/138331/using-ffmpeg-to-cut-up-video

        String targetPath = context.getCacheDir().toString() + File.separator + timeStamp + "target.gif";
        String targetMP4 = context.getCacheDir().toString() + File.separator + "target.mp4";
//    "ffmpeg -y -i %s -vf scale=466:-1 -vf crop=466:466 -q:v 20 -b:v 2M -r 20 -ss %d -t %d -f gif %s"

//    String[] commandFinal = command.split(" "); //以空格分割为字符串数组

        //开始时间
//    String  targetPng = context.getCacheDir().toString() +File.separator + "target.png";
//    FFmpegCommand.runCmd(FFmpegUtils.frame2Image(inputFile, targetPng,"00:00:0"+start));
//    String cmd = "ffmpeg -y -i %s -vf scale=466:-1 -vf crop=466:466 -q:v 20 -b:v 2M -r 20 -ss %d -t %d -f gif %s";
//    cmd = String.format(cmd, inputFile, start, duration, targetPath);
        cutVideo(context, inputFile, targetMP4, targetPath, start, duration, callback);

    }

    public static void shootVideoThumbInBackground(final Context context, final Uri videoUri, final int totalThumbsCount, final long startPosition,
                                                   final long endPosition, final SingleCallback<Bitmap, Integer> callback) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
            @Override
            public void execute() {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(context, videoUri);
                    // Retrieve media data use microsecond
                    long interval = 0;
                    if (totalThumbsCount > 1) {
                        interval = (endPosition - startPosition) / (totalThumbsCount - 1);
                    } else {//小于或者等于1秒的防止崩溃
                        interval = endPosition - startPosition;
                    }
                    for (long i = 0; i < totalThumbsCount; ++i) {
                        long frameTime = startPosition + interval * i;
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        if (bitmap == null) continue;
                        try {
                            bitmap = Bitmap.createScaledBitmap(bitmap, THUMB_WIDTH, THUMB_HEIGHT, false);
                        } catch (final Throwable t) {
                            t.printStackTrace();
                        }
                        callback.onSingleCallback(bitmap, (int) interval);
                    }
                    mediaMetadataRetriever.release();
                } catch (final Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }

    public static String getVideoFilePath(String url) {
        if (TextUtils.isEmpty(url) || url.length() < 5) return "";
        if (url.substring(0, 4).equalsIgnoreCase("http")) {

        } else {
            url = "file://" + url;
        }

        return url;
    }

    private static String convertSecondsToTime(long seconds) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (seconds <= 0) {
            return "00:00";
        } else {
            minute = (int) seconds / 60;
            if (minute < 60) {
                second = (int) seconds % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) return "99:59:59";
                minute = minute % 60;
                second = (int) (seconds - hour * 3600 - minute * 60);
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    public static String cutVideo(Context context, String inputFile, String outputFile, String finalOtuputFile, String startTime, String durationTime, final VideoTrimListener callback) {
        //开始时间
        startX = SP.spLoadInt(context, "startX");
        //结束时间
        startY = SP.spLoadInt(context, "startY");
        int finalyWidth = SM.spLoadInt(context, "finalyWidth");//宽 * 高
        int finalyHeight = SM.spLoadInt(context, "finalyHeight");//宽 * 高
        if (!SM.spLoadBoolean(context, "isFirst")) {
            //对比换算
            float r = SM.spLoadInt(context, "mViewHeight") / (float) SM.spLoadInt(context, "mLayoutHeight");
//    float rWidth = SP.spLoadInt(context,"mVideoWidth")  / (float)SP.spLoadInt(context,"mLayoutWidths") ;
            startX = (int) (SM.spLoadInt(context, "startX") / r);
            startY = (int) (SM.spLoadInt(context, "startY") / r);
            finalyWidth = (int) (SM.spLoadInt(context, "finalyWidth") / r);
            finalyHeight = (int) ((int) finalyWidth * 1.2);
        }


        Log.e("cutVideo", "startX" + startX + "startY" + startY);
        String cmd = "";
        durationTime = String.valueOf(Integer.parseInt(durationTime) - Integer.parseInt(startTime));//结束时间-开始时间（选择的时间）
        if (Integer.parseInt(durationTime) == 0 || Integer.parseInt(durationTime) < 0) {//最少1秒
            durationTime = "1";
        }
        if (startX > 0 || startY > 0) {
            cmd = "ffmpeg -i " + inputFile + " -vf crop=" + finalyWidth + ":" + finalyWidth + ":" + startX + ":" + startY + " -ss " + startTime + " -t " + durationTime + " -q:v 10 -b:v 1M " + outputFile;
        } else {
        }
        Log.e("cutVideo", "长宽" + finalyWidth + "--------" + finalyHeight);
        Log.e("cutVideo", cmd);
        String[] command = cmd.split(" ");//以空格分割为字符串数组


        try {

            String finalDurationTime = durationTime;
            for (String s : FFmpegUtils.cutVideo3(inputFile, Integer.parseInt(startTime), Integer.parseInt(durationTime), outputFile, finalyWidth, finalyHeight, startX, startY)) {
                Log.e("caixin",s);
            }

            FFmpegCommand.runCmd(FFmpegUtils.cutVideo3(inputFile, Integer.parseInt(startTime), Integer.parseInt(durationTime),outputFile,finalyWidth,finalyHeight, startX, startY), new CommonCallBack() {
                @Override
                public void onStart() {
                    Log.e("runCmd", "onStart");
                    callback.onStartTrim();
                }

                @Override
                public void onProgress(int progress, long pts) {
                    Log.e("runCmd", "onProgress"+progress);
                    callback.onProgress(progress);
                }

                @Override
                public void onCancel() {
                    Log.e("runCmd", "onCancel");

                }

                @SuppressLint("CheckResult")
                @Override
                public void onComplete() {
//          EventBus.getDefault().post(UPDATE_LOADING_CLOSE);
                    Observable.timer(500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(a -> {
                                dealGif(context, outputFile, finalOtuputFile, startTime, finalDurationTime, callback);
                            });
                    Log.e("runCmd", "onComplete");
                }

                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                    Log.e("runCmd", "" + errorCode);
                    Log.e("runCmd", errorMsg);
                    callback.onError();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String dealGif(Context context, String inputFile, String outputFile, String start, String duration, final VideoTrimListener callback) {

        String cmd = "";
        cmd = "ffmpeg -y"+ " -i " + inputFile+" -vf crop=466:466:"+startX+":"+startY+" -q:v 5 -b:v 1M -r 12" + " -f gif " + outputFile;
        if (false) {
            cmd = "ffmpeg -y"+ " -i " + inputFile+" -vf crop=480:480:"+startX+":"+startY+" -q:v 5 -b:v 1M -r 12"+" -f gif " + outputFile;
        }
        String[] command = cmd.split(" ");//以空格分割为字符串数组

        try {
            FFmpegCommand.runCmd(command, new IFFmpegCallBack() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(int progress, long pts) {
                    Log.e("runcmd","progress"+progress);
                }

                @Override
                public void onCancel() {
                    callback.onCancel();
                }

                @Override
                public void onComplete() {

                    try {
                        SM.spSaveBoolean(context, "isRunVideo", false);
                        callback.onFinishTrim(outputFile);
                    } catch (Exception e) {

                    }

                }

                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                    callback.onError();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
