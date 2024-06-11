package com.titan.titanvideotrimmingpoc

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController

import com.titan.titanvideotrimmingpoc.databinding.FragmentFirstBinding
import com.titan.titanvideotrimmingpoc.video.trim.ExtractTimmedFramesWorkThread
import com.titan.titanvideotrimmingpoc.video.trim.TrimVideoViewModel
import com.titan.titanvideotrimmingpoc.video.trim.VideoThumbImg
import com.titan.titanvideotrimmingpoc.video.trim.VideoThumbSpacingItemDecoration
import com.titan.titanvideotrimmingpoc.widget.dp
import java.io.File
import java.lang.ref.WeakReference


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@UnstableApi
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    var player: ExoPlayer? = null
    var videoPathUri: String? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var mExtractFrameWorkThread: ExtractTimmedFramesWorkThread? = null
    private val viewModel: TrimVideoViewModel by viewModels()
    private val thumbSpacingItemDecoration = VideoThumbSpacingItemDecoration(56.dp())
    private var trimmedVideoPath:String?=null

    companion object {

        private const val MAX_COUNT_RANGE = 10 //seekBar的区域内一共有多少张图片

        private val MARGIN: Int = 56.dp()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("sagar video poc", "onCreateView")
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("sagar video poc", "onViewCreated")
        initViews()
        binding.buttonFirst.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "video/*"
            startActivityForResult(intent, 2)
        }
        binding.buttonTrimVideo.setOnClickListener {
            videoPathUri?.let {
                val bundle = Bundle()
                bundle.putString("ORIGINAL_VIDEO_PATH", it)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
            } ?: Toast.makeText(
                requireContext(),
                "file path is null",
                Toast.LENGTH_SHORT
            ).show()

        }
        setFragmentResultListener("video") { requestKey, bundle ->
            if (requestKey == "video") {
                val path = bundle.getString("path", "unknown")
                trimmedVideoPath=path
                Log.i("sagar video poc", "videoPath trimmed $path")
                binding.playerView.visibility = View.VISIBLE
                preparePlayer(path)
//                viewModel.updateVideoFile(File(path))
            }
        }
    }

    private fun initViews() {
        binding.buttonVideoFrames.setOnClickListener {
            trimmedVideoPath?.let { path ->
                val maxSeconds = 5
                val max = maxSeconds * 1000L

                val duration = viewModel.start(path, max)

                val thumbnailsCount: Int = if (duration <= max) {
                    MAX_COUNT_RANGE
                } else {
                    (duration * 1.0f / max * MAX_COUNT_RANGE).toInt()
                }

                thumbSpacingItemDecoration.count = thumbnailsCount

                val outputDir = requireContext().externalCacheDir ?: requireContext().cacheDir
                val outputDirPath = outputDir.absolutePath +File.separator + "TimmedVideoImages/"
                path.let { path ->
                    mExtractFrameWorkThread = ExtractTimmedFramesWorkThread(
                        mUIHandler, path, outputDirPath, 0, duration, thumbnailsCount
                    )
                }

                mExtractFrameWorkThread?.start()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                2 -> {
                    val videoUri: Uri = data?.data!!
                    val videoPath = parsePath(videoUri)
                    videoPathUri = videoPath
                    videoPath?.let {
                        binding.playerView.visibility = View.VISIBLE
                        preparePlayer(it)
                    }
                    Log.d("TAG", "$videoPath is the path that you need...")
                }
            }
        }
//        Log.d("SelectedVideoPath", videoPath)
    }

    fun preparePlayer(videoFilePath: String) {
        player?.release()
        player = ExoPlayer.Builder(requireContext()).build()
//        val mediaItem = MediaItem.fromUri(uri!!)
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(videoFilePath)
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()
        binding.playerView.player = player
        player?.setMediaItem(mediaItem)
        player?.prepare()
        binding.playerView.hideController()
        player?.repeatMode = Player.REPEAT_MODE_ONE
        player?.play()
    }

    fun parsePath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = context?.contentResolver?.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    private val mUIHandler = MainTrimHandler(this)

    private class MainTrimHandler(activity: FirstFragment) : Handler() {

        private val mActivity: WeakReference<FirstFragment>

        init {
            mActivity = WeakReference<FirstFragment>(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity: FirstFragment? = mActivity.get()
            if (activity != null) {
                if (msg.what == ExtractTimmedFramesWorkThread.MSG_FRAME_SAVE_SUCCESS) {
                    val info = msg.obj as ArrayList<VideoThumbImg>
                    Log.i("sagar video poc","timmed frames $info")
//                    activity.videoEditAdapter.addItemVideoInfo(info)
                }
            }
        }
    }

    override fun onDestroyView() {
        Log.i("sagar video poc", "onDestroyView")

        super.onDestroyView()
//        _binding = nullm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("sagar video poc", "onCreate")

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        Log.i("sagar video poc", "onStart")

        super.onStart()
    }

    override fun onResume() {
        Log.i("sagar video poc", "onResume")

        super.onResume()
    }

    override fun onPause() {
        Log.i("sagar video poc", "onPause")
        player?.release()
        super.onPause()
    }

    override fun onStop() {
        Log.i("sagar video poc", "onStop")

        super.onStop()
    }

    override fun onDestroy() {
        Log.i("sagar video poc", "onDestroy")

        super.onDestroy()
    }

    override fun onDetach() {
        Log.i("sagar video poc", "onDetach")

        super.onDetach()
    }

}