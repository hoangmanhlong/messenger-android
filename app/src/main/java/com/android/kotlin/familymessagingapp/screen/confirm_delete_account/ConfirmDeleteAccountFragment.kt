package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.android.kotlin.familymessagingapp.databinding.FragmentConfirmDeleteAccountBinding
import com.android.kotlin.familymessagingapp.screen.profile.ProfileViewModel
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmDeleteAccountFragment : BottomSheetDialogFragment() {

    private val viewmodel: ProfileViewModel by activityViewModels()

    companion object {
        val TAG: String = ConfirmDeleteAccountFragment::class.java.simpleName
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
                    viewmodel.deleteAccount()
                    dismiss()
                }
            }
        }
        return binding.root
    }

    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}