package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MessageOptionsFragment(
    private val isMessageOfMe: Boolean
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "MessageOptionsFragment"
    }

    private var _binding: FragmentMessageOptionsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageOptionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isMessageOfMe = isMessageOfMe

    }

}