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
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessageOptionsFragment : BottomSheetDialogFragment() {

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val viewmodel: ChatRoomViewModel by activityViewModels()

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
            viewmodel.updateMessageEmoji(emoji)
            dismiss()
        }
        emojiRecyclerView?.adapter = emojiAdapter
        emojiAdapter?.submitList(defaultEmoji)

        binding.copyMessageView.setOnClickListener {
            activity?.let {
                viewmodel.copyMessage(requireActivity())
            }
            dismiss()
        }

        binding.pinMessageView.setOnClickListener {
            viewmodel.pinMessage()
            dismiss()
        }

        binding.deleteMessageView.setOnClickListener {
            viewmodel.deleteMessage()
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isMessageOfMe = viewmodel.selectedMessageIsMessageOfMe
        binding.ivPin.setImageResource(if (viewmodel.selectedMessageIsPinnedMessage == true) R.drawable.ic_duo else R.drawable.ic_push_pin)
        binding.tvPin.text =
            if (viewmodel.selectedMessageIsPinnedMessage == true)
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