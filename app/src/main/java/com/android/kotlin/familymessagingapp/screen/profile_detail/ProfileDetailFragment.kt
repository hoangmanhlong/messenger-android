package com.android.kotlin.familymessagingapp.screen.profile_detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentProfileDetailBinding
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.loadImageFollowImageViewSize
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileDetailFragment : Fragment() {

    // Hold a reference to the current animator so that it can be canceled
    // midway.
    private var currentAnimator: Animator? = null

    // The system "short" animation time duration in milliseconds. This duration
    // is ideal for subtle animations or animations that occur frequently.
    private var shortAnimationDuration: Int = 0

    private val _viewModel: ProfileDetailViewModel by viewModels()

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                loadImageFollowImageViewSize(binding.ivAvatar, it)
                _viewModel.setImageUri(it)
            }
        }

    private var _binding: FragmentProfileDetailBinding? = null

    private val binding get() = _binding!!

    private val args: ProfileDetailFragmentArgs by navArgs()

    private var etPhoneNumber: EditText? = null

    private var etDisplayName: EditText? = null

    private var nestedScrollView: NestedScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        binding.fragment = this@ProfileDetailFragment
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        etPhoneNumber = binding.etPhoneNumber
        etDisplayName = binding.etDisplayName
        nestedScrollView = binding.nestedScrollView

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        etDisplayName?.addTextChangedListener {
            _viewModel.setDisplayName(it.toString().trim())
        }

        etPhoneNumber?.addTextChangedListener {
            _viewModel.setPhoneNumber(it.toString().trim())
        }

        binding.ivAvatar.setOnClickListener {
            // Hook up taps on the thumbnail views.
            zoomImageFromThumb(binding.ivAvatar, binding.ivAvatar.drawable)
        }

        etPhoneNumber?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // ScrollView will scroll to the bottom when focused
                nestedScrollView?.post {
                    nestedScrollView?.smoothScrollTo(0, nestedScrollView!!.bottom)
                }
            }
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.let { KeyBoardUtils.setupHideKeyboard(view, it) }
        val userdata = args.userdata
        _viewModel.setUserData(userdata)
        binding.userData = userdata

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        _viewModel.isEditingStatus.observe(this.viewLifecycleOwner) {
            binding.isEditing = it
            if (it) {
                etPhoneNumber?.hint = getString(R.string.phone_number_optional)
                binding.tvUnchangeable.visibility = View.VISIBLE
                etPhoneNumber?.inputType = InputType.TYPE_CLASS_PHONE
                etDisplayName?.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                 activity?.let { KeyBoardUtils.hideSoftKeyboard(requireActivity()) }
                etPhoneNumber?.hint = ""
                binding.tvUnchangeable.visibility = View.GONE
                etPhoneNumber?.clearFocus()
                etDisplayName?.clearFocus()
                etPhoneNumber?.inputType = InputType.TYPE_NULL
                etDisplayName?.inputType = InputType.TYPE_NULL
            }
        }

        _viewModel.saveButtonStatus.observe(this.viewLifecycleOwner) {
            binding.saveButton.isEnabled = it
        }
    }

    fun isEditing(isEditing: Boolean) {
        _viewModel.setEditingStatus(isEditing)
    }

    fun onEditImageButtonClick() {
        binding.etPhoneNumber.clearFocus()
        binding.etDisplayName.clearFocus()
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun onSaveButtonClick() {
        activity?.let {
            NetworkChecker.checkNetwork(it) { _viewModel.saveUserData() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).isShowLoadingDialog(false)
        etPhoneNumber = null
        etDisplayName = null
        nestedScrollView = null
        _binding = null
    }

    private fun zoomImageFromThumb(thumbView: View, imageDrawable: Drawable) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        if (activity == null) return

        // Load the high-resolution "zoomed-in" image.
        MediaUtils.loadImageFollowImageViewSize(
            imageView = binding.expandedImage,
            photo = imageDrawable,
            placeholder = R.drawable.image_placeholder
        )

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the
        // container view. Set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        binding.container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Using the "center crop" technique, adjust the start bounds to be the
        // same aspect ratio as the final bounds. This prevents unwanted
        // stretching during the animation. Calculate the start scaling factor.
        // The end scaling factor is always 1.0.
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally.
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically.
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it positions the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f

        animateZoomToLargeImage(startBounds, finalBounds, startScale)

        setDismissLargeImageAnimation(thumbView, startBounds, startScale)
    }

    private fun animateZoomToLargeImage(startBounds: RectF, finalBounds: RectF, startScale: Float) {
        binding.expandedImage.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of the zoomed-in view. The default is the center of
        // the view.
        binding.expandedImage.pivotX = 0f
        binding.expandedImage.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties: X, Y, SCALE_X, and SCALE_Y.
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    binding.expandedImage,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.Y,
                        startBounds.top,
                        finalBounds.top
                    )
                )
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
    }

    private fun setDismissLargeImageAnimation(
        thumbView: View,
        startBounds: RectF,
        startScale: Float
    ) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        binding.expandedImage.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.X,
                        startBounds.left
                    )
                ).apply {
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }

}