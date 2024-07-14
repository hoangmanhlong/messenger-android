package com.android.kotlin.familymessagingapp.screen.video_call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.kotlin.familymessagingapp.databinding.FragmentCallBinding
import com.android.kotlin.familymessagingapp.model.ChatRoom


class CallFragment : Fragment() {

//    @OptIn(ExperimentalUnsignedTypes::class)
//    private var agView: AgoraVideoViewer? = null

    private var chatRoom: ChatRoom? = null

    companion object {
        val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.RECORD_AUDIO) // Recording permission
                add(Manifest.permission.CAMERA) // Camera permission
                add(Manifest.permission.READ_PHONE_STATE) // Permission to read phone status
                add(Manifest.permission.BLUETOOTH_CONNECT) // Bluetooth connection permission
            }
        }.toTypedArray()
    }

    private var _binding: FragmentCallBinding? = null

    private val binding get() = _binding!!

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {

            } else {
                initializeAndJoinChannel()
            }
        }

    private val arg: CallFragmentArgs by navArgs()

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRoom = arg.chatroom
        if (chatRoom == null) findNavController().navigateUp()


        if (allPermissionsGranted()) {
            initializeAndJoinChannel();
        } else {
            requestPermissions()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun initializeAndJoinChannel() {
//        try {
//            val settings = AgoraSettings()
////            settings.tokenURL = serverUrl
//            context?.let {
//                agView = AgoraVideoViewer(
//                    it,
//                    AgoraConnectionData(BuildConfig.agoraAppId, null),
//                    AgoraVideoViewer.Style.FLOATING,
//                    AgoraSettings(),
//                    null
//                )
//                agView!!.join(chatRoom!!.chatRoomId.toString());
//            }
//        } catch (e: Exception) {
//            Log.e(
//                "AgoraVideoViewer",
//                "Could not initialize AgoraVideoViewer. Check that your app Id is valid."
//            )
//            Log.e("Exception", e.toString())
//            return
//        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        context?.let {context ->
            ContextCompat.checkSelfPermission(
                context, it) == PackageManager.PERMISSION_GRANTED
        } ?: false

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onDestroyView() {
        super.onDestroyView()
//        agView?.leaveChannel()
        _binding = null
    }
}