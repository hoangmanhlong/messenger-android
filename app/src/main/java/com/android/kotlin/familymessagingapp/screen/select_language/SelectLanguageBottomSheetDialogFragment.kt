package com.android.kotlin.familymessagingapp.screen.select_language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentSwitchLanguageBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLanguageBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = SelectLanguageBottomSheetDialogFragment::class.java.simpleName
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
            isTheEnglishLanguageDisplayed?.let {
                (activity as MainActivity).isTheEnglishLanguageSelected(it)
                binding.englishMaterialRadioButton.isChecked = it
                binding.vietnameseMaterialRadioButton.isChecked = !it
            }
        }
    }

    fun onSaveButtonClick() {
        (activity as MainActivity).changeLanguage()
        this.dismiss()
    }

    fun isTheEnglishLanguageSelected(isTheEnglishLanguageSelected: Boolean) {
        (activity as MainActivity).isTheEnglishLanguageSelected(isTheEnglishLanguageSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}