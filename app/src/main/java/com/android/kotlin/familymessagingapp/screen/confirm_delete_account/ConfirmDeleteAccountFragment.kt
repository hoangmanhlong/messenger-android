package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.kotlin.familymessagingapp.databinding.FragmentConfirmDeleteAccountBinding
import com.android.kotlin.familymessagingapp.screen.profile.ProfileFragment
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmDeleteAccountFragment(
    private val fragment: ProfileFragment
) : BottomSheetDialogFragment() {

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
        binding.checkboxAgree.addOnCheckedStateChangedListener { checkbox, _ ->
            binding.btConfirmDeleteAccount.isEnabled = checkbox.isChecked
        }

        binding.btConfirmDeleteAccount.setOnClickListener {
            activity?.let {
                NetworkChecker.checkNetwork(it) {
                    fragment.deleteAccount()
                    this@ConfirmDeleteAccountFragment.dismiss()
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}