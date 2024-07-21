package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MessageOptionsFragment(
    private var parentFragment: ChatRoomFragment? = null,
    private var isMessageOfMe: Boolean? = null
) : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = MessageOptionsFragment::class.java.simpleName
    }

    private var _binding: FragmentMessageOptionsBinding? = null

    private var emojiRecyclerView: RecyclerView? = null

    private var emojiAdapter: EmojiAdapter? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageOptionsBinding.inflate(inflater, container, false)
        emojiRecyclerView = binding.emojiRecyclerView
        emojiAdapter = EmojiAdapter { emoji ->
            parentFragment?.updateMessageEmoji(emoji)
            dismiss()
        }
        emojiRecyclerView?.adapter = emojiAdapter
        emojiAdapter?.submitList(defaultEmoji)

        binding.copyMessageView.setOnClickListener {
            parentFragment?.copyMessage()
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isMessageOfMe = isMessageOfMe
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragment = null
    }
}