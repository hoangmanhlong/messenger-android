package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.defaultEmoji
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessageOptionsFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = MessageOptionsFragment::class.java.simpleName
    }

    private val viewModel: ChatRoomViewModel by activityViewModels()

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
            viewModel.updateMessageEmoji(emoji)
            dismiss()
        }
        emojiRecyclerView?.adapter = emojiAdapter
        emojiAdapter?.submitList(defaultEmoji)

        binding.copyMessageView.setOnClickListener {
            viewModel.copyMessage(requireActivity())
            dismiss()
        }

        binding.pinMessageView.setOnClickListener {
            viewModel.pinMessage()
            dismiss()
        }

        binding.deleteMessageView.setOnClickListener {
            viewModel.deleteMessage()
            dismiss()
        }

        binding.replyMessageView.setOnClickListener {
            viewModel.setReplyingMessage(true)
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isMessageOfMe = viewModel.selectedMessage.value?.senderId == Firebase.auth.uid
        binding.tvPin.text =
            if (viewModel.selectedMessageIsPinnedMessage == true)
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