package com.android.kotlin.familymessagingapp.screen.my_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentMyQrBinding
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyQRFragment : Fragment() {

    private val viewModel: MyQRViewModel by viewModels()

    private var _binding: FragmentMyQrBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyQrBinding.inflate(inflater, container, false)
        binding.btNavigateUp.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.currentUserData.observe(viewLifecycleOwner) {
            if (it?.uid != null) {
                binding.ivQR.setImageBitmap(MediaUtils.getQRCodeBitmapFromString(it.uid))
            } else {
                binding.ivQR.setImageResource(R.drawable.ic_broken_image)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}