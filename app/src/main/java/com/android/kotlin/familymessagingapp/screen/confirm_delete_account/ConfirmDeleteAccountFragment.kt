package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.android.kotlin.familymessagingapp.databinding.FragmentConfirmDeleteAccountBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmDeleteAccountFragment : BottomSheetDialogFragment() {

    private val _viewModel: ConfirmDeleteAccountViewModel by viewModels()

    companion object {
        const val TAG = "ConfirmDeleteAccountFragment"
    }

    private var _binding: FragmentConfirmDeleteAccountBinding? = null

    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmDeleteAccountBinding.inflate(inflater, container, false)
        activity?.let { dialog = DialogUtils.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkboxAgree.addOnCheckedStateChangedListener { checkbox, _ ->
            binding.btConfirmDeleteAccount.isEnabled = checkbox.isChecked
        }

        _viewModel.deleteSuccess.observe(this.viewLifecycleOwner) {
            if (it) this.dismiss()
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            showLoadingDialog(it)
        }


        binding.btConfirmDeleteAccount.setOnClickListener {
            activity?.let {
                NetworkChecker.checkNetwork(it) {_viewModel.deleteAccount() }
            }
        }
    }

    private fun showLoadingDialog(isShow: Boolean) {
        dialog?.let {
            if (isShow && !it.isShowing) it.show()
            else it.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        dialog = null
        _binding = null
    }
}