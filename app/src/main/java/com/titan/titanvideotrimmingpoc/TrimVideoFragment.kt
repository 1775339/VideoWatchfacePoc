package com.titan.titanvideotrimmingpoc

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.titan.titanvideotrimmingpoc.databinding.FragmentSecondBinding
import com.titan.titanvideotrimmingpoc.video.trim.ExtractFrameWorkThread
import com.titan.titanvideotrimmingpoc.video.trim.ExtractTimmedFramesWorkThread
import com.titan.titanvideotrimmingpoc.video.trim.TrimVideoAdapter
import com.titan.titanvideotrimmingpoc.video.trim.TrimVideoViewModel
import com.titan.titanvideotrimmingpoc.video.trim.VideoThumbImg
import com.titan.titanvideotrimmingpoc.video.trim.VideoThumbSpacingItemDecoration
import com.titan.titanvideotrimmingpoc.widget.RangeSeekBar
import com.titan.titanvideotrimmingpoc.widget.dp
import java.io.File
import java.lang.ref.WeakReference
import java.util.Date

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TrimVideoFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private lateinit var videoEditAdapter: TrimVideoAdapter


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val viewBinding get() = _binding!!
    var videoPath: String? = null
    private var seeking = false
    private var scrolling = false
    private var videoStarted = false
    private val viewModel: TrimVideoViewModel by viewModels()

    companion object {

        private const val MAX_COUNT_RANGE = 10 //seekBar的区域内一共有多少张图片

        private val MARGIN: Int = 56.dp()
    }

    private val thumbSpacingItemDecoration = VideoThumbSpacingItemDecoration(56.dp())
    private var mExtractFrameWorkThread: ExtractFrameWorkThread? = null
    private val maxSeconds = 5
    private var loadingDialog: LoadingDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return viewBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }*/
        init()//get video path
        initView()
        initData()
    }

    private fun initData() {
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            if (it) showLoadingDialog() else hideLoadingDialog()
        }

        val max = maxSeconds * 1000L
        videoPath?.let { path ->
            val duration = viewModel.start(path, max)
            initSeekBar(duration)

            val thumbnailsCount: Int = if (duration <= max) {
                MAX_COUNT_RANGE
            } else {
                (duration * 1.0f / max * MAX_COUNT_RANGE).toInt()
            }

            thumbSpacingItemDecoration.count = thumbnailsCount

            val outputDir = requireContext().externalCacheDir ?: requireContext().cacheDir
            val outputDirPath = outputDir.absolutePath
            videoPath?.let { path ->
                mExtractFrameWorkThread = ExtractFrameWorkThread(
                    mUIHandler, path, outputDirPath, 0, duration, thumbnailsCount
                )
            }

            mExtractFrameWorkThread?.start()
        }


    }

    protected fun showLoadingDialog() {
        if (loadingDialog == null) {
            initLoadingDialog()
        }
        loadingDialog?.showDialog()
    }

    private fun initView() {
        initVideoView()

        viewBinding.rvVideoThumb.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        videoEditAdapter = TrimVideoAdapter(requireContext())
        viewBinding.rvVideoThumb.adapter = videoEditAdapter
        viewBinding.rvVideoThumb.addOnScrollListener(mOnScrollListener)
        viewBinding.rvVideoThumb.addItemDecoration(thumbSpacingItemDecoration)
        viewBinding.rvVideoThumb.doOnPreDraw {
            videoEditAdapter.setItemWidth((viewBinding.seekBar.width - 16.dp()) / 10)
        }
//        viewBinding.rvVideoThumb.doOnPreDraw {
//            videoEditAdapter.run { setItemWidth(viewBinding.seekBar.width.minus(16.dp()) / 10) }
//        }

        viewBinding.seekBar.setOnRangeChangedListener(mOnRangeChangedListener)
        viewBinding.cropDone.setOnClickListener {
            val context = context
            context?.let {
                viewModel.cropVideo(it) { videoPath, _ ->
                    Log.i("sagar video poc", "videoPath cropped $videoPath")
                    findNavController().navigateUp()
                    setFragmentResult("video", Bundle().apply {
                        putString("path", videoPath)
                    })
                }
            }

        }

    }

    private fun initSeekBar(duration: Long) {
        val max = maxSeconds * 1000L
        val maxValue = if (duration > max) max else duration
        viewBinding.seekBar.setMinValue(0)
        viewBinding.seekBar.setMaxValue(maxValue)
        viewBinding.seekBar.selectedMinValue = 0L
        viewBinding.seekBar.selectedMaxValue = maxValue
        //设置最小裁剪时间
        viewBinding.seekBar.setMinLimit(1000)
        viewBinding.seekBar.isNotifyWhileDragging = true

        viewBinding.videoShootTip.text =
            String.format("裁剪 %d s", viewBinding.seekBar.selectedMaxValue / 1000)
    }

    private fun initLoadingDialog() {
        val dialog = LoadingDialog(requireContext())
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        loadingDialog = dialog
    }


    protected fun hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog?.hideDialog()
        }
    }

    private val mOnRangeChangedListener: RangeSeekBar.OnRangeChangedListener =
        RangeSeekBar.OnRangeChangedListener { _, minValue, maxValue, offset, action, pressedThumb ->
//            Timber.w("min=$minValue, max=$maxValue, offset=$offset")
            viewModel.startMillis = offset + minValue
            viewModel.stopMillis = offset + maxValue
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    videoPause()
                    scrolling = true
                }

                MotionEvent.ACTION_MOVE -> {
                    scrolling = true
                    val pos =
                        if (pressedThumb == null || pressedThumb == RangeSeekBar.Thumb.MIN) minValue else maxValue
                    videoSeekTo((offset + pos).toInt())
                }

                MotionEvent.ACTION_UP -> {
                    scrolling = false
                    videoSeekTo((offset + minValue).toInt())
                }

                else -> {}
            }

            viewBinding.videoShootTip.text =
                String.format("裁剪 %d s", (maxValue - minValue) / 1000)
        }

    private fun videoSeekTo(msec: Int) {
        if (!seeking) {
            seeking = true
            viewBinding.videoView.seekTo(msec)
        }
    }

    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {

            private var lastX = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false
                    videoStart()
                } else {
                    scrolling = true
                    videoPause()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                lastX += dx

                val offset = viewBinding.seekBar.setScrollOffset(lastX.toFloat())
                viewModel.startMillis = offset + viewBinding.seekBar.selectedMinValue
                viewModel.stopMillis = offset + viewBinding.seekBar.selectedMaxValue

                videoSeekTo(viewModel.startMillis.toInt())
            }
        }

    private fun initVideoView() {
        videoPath?.let { path ->
            viewBinding.videoView.setVideoPath(path)
            viewBinding.videoView.setOnPreparedListener { mp ->
                val lp: ViewGroup.LayoutParams = viewBinding.videoView.layoutParams
                val videoWidth: Int = mp.videoWidth
                val videoHeight: Int = mp.videoHeight
                val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
                val screenWidth: Int = viewBinding.layoutSurfaceView.width
                val screenHeight: Int = viewBinding.layoutSurfaceView.height
                val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
                if (videoProportion > screenProportion) {
                    lp.width = screenWidth
                    lp.height = (screenWidth.toFloat() / videoProportion).toInt()
                } else {
                    lp.width = (videoProportion * screenHeight.toFloat()).toInt()
                    lp.height = screenHeight
                }
                viewBinding.videoView.layoutParams = lp
                mp.setOnSeekCompleteListener {
                    seeking = false

                    if (!scrolling) {
                        videoStart()
                    }
                }
            }

            videoStart()
        }

    }

    private fun videoStart() {
        if (!videoStarted) {
            videoStarted = true

            viewBinding.videoView.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
            mUIHandler.postDelayed(pauseRunnable, viewModel.stopMillis - viewModel.startMillis)
        }
    }

    override fun onPause() {
        super.onPause()
        videoPause()
    }

    override fun onResume() {
        super.onResume()
        videoStart()
    }

    private val mUIHandler = MainHandler(this)

    private class MainHandler(activity: TrimVideoFragment) : Handler() {

        private val mActivity: WeakReference<TrimVideoFragment>

        init {
            mActivity = WeakReference<TrimVideoFragment>(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity: TrimVideoFragment? = mActivity.get()
            if (activity != null) {
                if (msg.what == ExtractFrameWorkThread.MSG_SAVE_SUCCESS) {
                    val info = msg.obj as VideoThumbImg
                    activity.videoEditAdapter.addItemVideoInfo(info)
                }
            }
        }
    }

    private val pauseRunnable: Runnable = Runnable {
        videoPause()
    }

    private fun videoPause() {
        if (videoStarted) {
            viewBinding.videoView.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }

            mUIHandler.removeCallbacks(pauseRunnable)
            videoStarted = false
        }
    }


    private fun init() {
        arguments?.let { bundle ->
            if (bundle.containsKey("ORIGINAL_VIDEO_PATH")) {
                videoPath = bundle.getString("ORIGINAL_VIDEO_PATH")
                Toast.makeText(
                    requireContext(), "file path is $videoPath", Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }

//    fun Number.dp(): Int = dp2px(toFloat())
//private fun Int.dp():Int= dp2px(toFloat())

}



