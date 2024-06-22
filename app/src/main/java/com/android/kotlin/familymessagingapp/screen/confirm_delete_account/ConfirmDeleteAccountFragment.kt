package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.kotlin.familymessagingapp.databinding.FragmentConfirmDeleteAccountBinding
import com.android.kotlin.familymessagingapp.screen.profile.ProfileFragment
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmDeleteAccountFragment(
    private val fragment: ProfileFragment
) : BottomSheetDialogFragment() {

    private val _viewModel: ConfirmDeleteAccountViewModel by viewModels()

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

        binding.btConfirmDeleteAccount.setOnClickListener {
            activity?.let {
                NetworkChecker.checkNetwork(it) {
                    fragment.deleteAccount()
                    this@ConfirmDeleteAccountFragment.dismiss()
                }
            }
        }
    }
}