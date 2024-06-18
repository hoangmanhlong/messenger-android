package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.kotlin.familymessagingapp.databinding.FragmentConfirmDeleteAccountBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ConfirmDeleteAccountFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ConfirmDeleteAccountFragment"
    }

    private var _binding: FragmentConfirmDeleteAccountBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmDeleteAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkboxAgree.addOnCheckedStateChangedListener { checkbox, _ ->
            binding.btConfirmDeleteAccount.isEnabled = checkbox.isChecked
        }
    }

}