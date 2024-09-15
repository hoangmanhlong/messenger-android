package com.android.kotlin.familymessagingapp.screen.my_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainViewModel
import com.android.kotlin.familymessagingapp.databinding.FragmentMyQrBinding
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyQRFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentMyQrBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyQrBinding.inflate(inflater, container, false)

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        binding.downloadQrView.setOnClickListener { viewModel.saveQrCode(binding.qrCodeView) }

        binding.shareQrView.setOnClickListener {
            activity?.let {
                viewModel.shareMyQRCode(context = it, qrView = binding.qrCodeView)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.currentUserLiveData.observe(viewLifecycleOwner) { userData ->
            val uid = userData?.uid
            if (uid.isNullOrEmpty()) {
                binding.ivQR.setImageResource(R.drawable.ic_broken_image)
                binding.tvUsername.visibility = View.GONE
                binding.qrActionView.visibility = View.GONE
            } else {
                binding.qrActionView.visibility = View.VISIBLE
                binding.ivQR.setImageBitmap(MediaUtils.getQRCodeBitmapFromString(uid))
                val username = userData.username
                if (username.isNullOrEmpty()) {
                    binding.tvUsername.visibility = View.GONE
                } else {
                    binding.tvUsername.text = userData.username
                    binding.tvUsername.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}