package com.android.kotlin.familymessagingapp.components.select_language

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.android.kotlin.familymessagingapp.databinding.FragmentSwitchLanguageBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLanguageBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SelectLanguageBottomSheetDialogFragment"
    }

    private val _viewModel: SelectLanguageViewModel by viewModels()

    private var _binding: FragmentSwitchLanguageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwitchLanguageBinding.inflate(inflater, container, false)
        binding.fragment = this@SelectLanguageBottomSheetDialogFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _viewModel.isTheEnglishLanguageDisplayed.observe(this.viewLifecycleOwner) { isTheEnglishLanguageDisplayed ->
            Log.d(TAG, "system language: $isTheEnglishLanguageDisplayed")
            isTheEnglishLanguageDisplayed?.let {
                _viewModel.isTheEnglishLanguageSelected(it)
                binding.englishMaterialRadioButton.isChecked = it
                binding.vietnameseMaterialRadioButton.isChecked = !it
            }
        }

        _viewModel.cancelFragment.observe(this.viewLifecycleOwner) {
            if (it) this@SelectLanguageBottomSheetDialogFragment.dismiss()
        }
    }

    fun onSaveButtonClick() = _viewModel.changeLanguage()

    fun isTheEnglishLanguageSelected(isTheEnglishLanguageSelected: Boolean) {
        Log.d(TAG, "selected language: $isTheEnglishLanguageSelected")
        _viewModel.isTheEnglishLanguageSelected(isTheEnglishLanguageSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}