package com.android.kotlin.familymessagingapp.screen.message_options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MessageOptionsFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "MessageOptionsFragment"
    }

    private var _binding: FragmentMessageOptionsBinding? = null

    private val binding get() =  _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageOptionsBinding.inflate(inflater, container, false)
        binding?.tvRemoveMessage?.setOnClickListener {

        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}