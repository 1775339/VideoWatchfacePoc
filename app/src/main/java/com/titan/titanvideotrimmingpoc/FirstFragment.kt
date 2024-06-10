 package com.titan.titanvideotrimmingpoc
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController

import com.titan.titanvideotrimmingpoc.databinding.FragmentFirstBinding
import java.io.File


 /**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@UnstableApi
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    var player: ExoPlayer? = null
    var videoPathUri:String?=null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("sagar video poc","onCreateView")
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("sagar video poc","onViewCreated")
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
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment,bundle)
            } ?: Toast.makeText(
                requireContext(),
                "file path is null",
                Toast.LENGTH_SHORT
            ).show()

        }
        setFragmentResultListener("video") { requestKey, bundle ->
            if (requestKey == "video") {
                val path = bundle.getString("path", "unknown")
                Log.i("sagar video poc","videoPath trimmed $path")
                binding.playerView.visibility=View.VISIBLE
                preparePlayer(path)
//                viewModel.updateVideoFile(File(path))
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
                    videoPathUri=videoPath
                    videoPath?.let {
                        binding.playerView.visibility=View.VISIBLE
                        preparePlayer(it)
                    }
                    Log.d("TAG", "$videoPath is the path that you need...")
                }
            }
        }
//        Log.d("SelectedVideoPath", videoPath)
    }
    fun preparePlayer(videoFilePath: String){
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
    override fun onDestroyView() {
        Log.i("sagar video poc","onDestroyView")

        super.onDestroyView()
        _binding = null
    }

     override fun onCreate(savedInstanceState: Bundle?) {
         Log.i("sagar video poc","onCreate")

         super.onCreate(savedInstanceState)
     }

     override fun onStart() {
         Log.i("sagar video poc","onStart")

         super.onStart()
     }

     override fun onResume() {
         Log.i("sagar video poc","onResume")

         super.onResume()
     }

     override fun onPause() {
         Log.i("sagar video poc","onPause")

         super.onPause()
     }

     override fun onStop() {
         Log.i("sagar video poc","onStop")

         super.onStop()
     }

     override fun onDestroy() {
         Log.i("sagar video poc","onDestroy")

         super.onDestroy()
     }

     override fun onDetach() {
         Log.i("sagar video poc","onDetach")

         super.onDetach()
     }

}