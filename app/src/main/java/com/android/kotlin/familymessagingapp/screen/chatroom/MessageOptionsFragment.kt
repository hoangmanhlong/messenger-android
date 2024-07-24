package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.defaultEmoji
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessageOptionsFragment(
    private val listener: MessageOptionsEventListener? = null,
    private val isMessageOfMe: Boolean? = null,
    private val isPinnedMessage: Boolean? = null
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
            listener?.updateMessageEmoji(emoji)
            dismiss()
        }
        emojiRecyclerView?.adapter = emojiAdapter
        emojiAdapter?.submitList(defaultEmoji)

        binding.copyMessageView.setOnClickListener {
            listener?.onCopyMessage()
            dismiss()
        }

        binding.pinMessageView.setOnClickListener {
            listener?.onPinMessage()
            dismiss()
        }

        binding.deleteMessageView.setOnClickListener {
            listener?.onDeleteMessage()
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Khi cấu hình hệ thống thay đổi Fragment sẽ tạo lại, điều này sẽ làm các thuộc
        // tính null => Giao diện sẽ hiển thị không đúng
        // Đóng Fragment này bằng viêc kiểm tra listener == null
        if (listener == null) dismiss()
        binding.isMessageOfMe = isMessageOfMe
        binding.ivPin.setImageResource(if (isPinnedMessage == true) R.drawable.ic_duo else R.drawable.ic_push_pin)
        binding.tvPin.text =
            if (isPinnedMessage == true)
                getString(R.string.unpin)
            else getString(R.string.pin)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        emojiRecyclerView = null
        emojiAdapter = null
    }
}