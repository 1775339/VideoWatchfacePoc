package com.titan.titanvideotrimmingpoc.video.trim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.titan.titanvideotrimmingpoc.R
import com.titan.titanvideotrimmingpoc.utils.SM
import com.titan.titanvideotrimmingpoc.utils.touchAlphaEffect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ServiceLoader

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
class VideoTrimmerActivity : AppCompatActivity() {
    /**
     * 解决字体大小问题
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(getConfigurationContext(newBase))
    }

    private fun getConfigurationContext(context: Context?): Context? {
        return context?.let {
            val configuration = context.resources.configuration
            configuration.fontScale = 1f
            context.createConfigurationContext(configuration)
        }
    }

    companion object {
        private const val TAG = "jason"
        private const val VIDEO_PATH_KEY = "video-file-path"
        private const val COMPRESSED_VIDEO_FILE_NAME = "compress.mp4"
        const val VIDEO_TRIM_REQUEST_CODE = 0x001

        //  private ActivityVideoTrimBinding mBinding;
        const val UPDATE_LOADING_CLOSE = "UPDATE_LOADING_CLOSE"
        const val UPDATE_LOADING_SHOW = "UPDATE_LOADING_SHOW"
        var VIDEC_REQUEST_CODE = 0

        @JvmStatic
        fun call(from: Context, videoPath: String?, requestCode: Int) {
            if (!TextUtils.isEmpty(videoPath)) {
                VIDEC_REQUEST_CODE = requestCode
                val bundle = Bundle()
                bundle.putString(VIDEO_PATH_KEY, videoPath)
                val intent = Intent(from, VideoTrimmerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtras(bundle)
                if (from is Activity) {
                    from.startActivityForResult(intent, requestCode)
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    from.startActivity(intent)
                }
            }
        }
    }

    private lateinit var videoTrimmerView: VideoTrimmerView
    private val mVideoTrimmerViewModel by lazy {
        ViewModelProvider(this, defaultViewModelProviderFactory)[VideoTrimmerViewModel::class.java]
    }
    private val mCurrentStateFlow by lazy { mVideoTrimmerViewModel.mCurrentStateFlow.asStateFlow() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trim)
        if (findUiEx()) {
            videoTrimmerActivityUiEx!!.onActivityCreate(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val back = findViewById<ImageView>(R.id.back)
        val action1 = findViewById<ImageView>(R.id.action1)
        videoTrimmerView = findViewById(R.id.trimmer_view)

//        EventBus.getDefault().register(this);
        //初始化466*466
        SM.spSaveInt(this, "finalyWidth", 466)
        SM.spSaveInt(this, "finalyHeight", 466)
        //初始化没有运行
        SM.spSaveBoolean(this, "isRunVideo", false)
        back.touchAlphaEffect()
        action1.touchAlphaEffect()
        back.setOnClickListener {
            if (!videoTrimmerView.mSeeked) {
                finish()
            } else {
                if (findUiEx()) {
                    videoTrimmerActivityUiEx!!.onShowConfirmExitDialog(this@VideoTrimmerActivity)
                } else {
                    finish()
                }
            }
        }
        action1.setOnClickListener {
            //防止重复生成
            if (mCurrentStateFlow.value !is VideoTrimmerViewModel.CutState.NotCutting && mCurrentStateFlow.value !is VideoTrimmerViewModel.CutState.FinishCutting){
                return@setOnClickListener
            }
            videoTrimmerView.onSaveClicked()
        }

        try {
            val bd = intent.extras
            var path: String? = ""
            if (bd != null) path = bd.getString(VIDEO_PATH_KEY)
            videoTrimmerView.setOnTrimVideoListener(mVideoTrimmerViewModel.mVideoTrimListener)
            videoTrimmerView.initVideoByURI(Uri.parse(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lifecycleScope.launch {
            mCurrentStateFlow.flowWithLifecycle(lifecycle).collectLatest {
                when (it) {
                    is VideoTrimmerViewModel.CutState.StartCutting -> {
                        if (findUiEx()) {
                            dialog = videoTrimmerActivityUiEx!!.onShowProgressDialog(this@VideoTrimmerActivity, 0)
                        }
                    }
                    is VideoTrimmerViewModel.CutState.Cutting -> {
                       if (findUiEx()){
                           if (dialog == null) {
                               dialog = videoTrimmerActivityUiEx!!.onShowProgressDialog(
                                   this@VideoTrimmerActivity,
                                   it.progress
                               )
                           } else {
                               videoTrimmerActivityUiEx!!.onProgressChange(
                                   this@VideoTrimmerActivity,
                                   dialog!!,
                                   it.progress
                               )
                           }
                       }
                    }
                    is VideoTrimmerViewModel.CutState.FinishCutting -> {
                        if (findUiEx()) {
                            lifecycleScope.launch {
                                dialog?.let {
                                    videoTrimmerActivityUiEx!!.onDismissProgressDialog(this@VideoTrimmerActivity, it)
                                }
                                videoTrimmerActivityUiEx!!.onShowPreviewDialog(
                                    this@VideoTrimmerActivity,
                                    it.uri,
                                    { dialog, _ ->
                                        dialog.dismiss()
                                        mVideoTrimmerViewModel.mCurrentStateFlow.value =
                                            VideoTrimmerViewModel.CutState.NotCutting
                                    }) { _, _ -> setResult(it.uri) }
                            }
                        } else {
                            setResult(it.uri)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!videoTrimmerView.mSeeked) {
            super.onBackPressed()
        } else {
            if (findUiEx()) {
                videoTrimmerActivityUiEx!!.onShowConfirmExitDialog(this@VideoTrimmerActivity)
            } else {
                super.onBackPressed()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        videoTrimmerView.onVideoPause()
        videoTrimmerView.setRestoreState(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoTrimmerView.onDestroy()
    }



    private fun setResult(`in`: String) {
        val intent = Intent()
        val bundle1 = Bundle()
        bundle1.putString("path", `in`)
        intent.putExtras(bundle1)
        setResult(RESULT_OK, intent)
        finish() //关闭BActivity文件并回调数据
        if (VideoTrimmerUtil.videoTrimListener != null) {
            VideoTrimmerUtil.videoTrimListener.onFinishTrim(`in`)
        }
    }


    private var videoTrimmerActivityUiEx: VideoTrimmerActivityUiEx<Any>? = null
    private var dialog: Any? = null
    private fun findUiEx(): Boolean {
        if (videoTrimmerActivityUiEx == null) {
            val iterator = ServiceLoader.load(
                VideoTrimmerActivityUiEx::class.java, classLoader
            )
                .iterator()
            if (iterator.hasNext()) {
                videoTrimmerActivityUiEx = iterator.next() as VideoTrimmerActivityUiEx<Any>?
            }
        }
        return videoTrimmerActivityUiEx != null
    }

}
