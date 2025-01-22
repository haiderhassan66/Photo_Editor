package com.android.demoeditor.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.demoeditor.R
import com.android.demoeditor.customClass.BitmapHelper
import com.android.demoeditor.customView.blurViews.GaussianBlur
import com.android.demoeditor.customView.blurViews.ShapeLayout
import com.android.demoeditor.data.parcelData.CommonParcelData
import com.android.demoeditor.databinding.FragmentBlurBinding
import com.android.demoeditor.enums.ActiveNavArgsData
import com.android.demoeditor.viewModel.BlurScreenViewModel
import com.bumptech.glide.Glide
import com.isseiaoki.simplecropview.callback.CropCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlurFragment : Fragment() {
    private val TAG = BlurFragment::class.java.simpleName
    private var _binding:FragmentBlurBinding? = null
    private val binding get() = _binding!!

    private val navArgs by navArgs<BlurFragmentArgs>()
    private val blurViewModel:BlurScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBlurBinding.inflate(layoutInflater)

        // This for Inflating menu inside Fragment Toolbar
        setHasOptionsMenu(true)

        setupToolbar()

        // Specify the current activity as the lifecycle owner.
        with(_binding!!){
            lifecycleOwner = this@BlurFragment
            blurViewModel = blurViewModel
        }

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Setting Bitmap inside CropImageView
         */

        blurViewModel.imgSrcBitmap.observe(viewLifecycleOwner, {
//            val bitmap = uriToBitmap(it)
            GaussianBlur.with(context)
                .size(GaussianBlur.MAX_SIZE.toFloat())
                .radius(GaussianBlur.MAX_RADIUS)
                .put(it, binding.blurImageView)

            binding.shapeLayoutOverlay.setTypeOfShape(ShapeLayout.ShapeType.RECTANGLE)
            binding.actualImage.setImageBitmap(it)
            binding.blurImageView.setImageBitmap(it)

        })
    }

    /**
     * Inflating menu inside Fragment Toolbar
     */
    private fun setupToolbar() {

        binding.toolbar.apply {
            setNavigationOnClickListener {
                it.findNavController().navigateUp()
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.tick_id -> {

                        saveAsBitmap()

                        true
                    }
                    else -> super.onOptionsItemSelected(it)
                }
            }


        }


    }

    /**
     * Cropped-Photo into save as Bitmap and cropped Bitmap set inside [mainViewModel]
     */

    private fun saveAsBitmap() {
        val bitmap = navArgs.imgData.uri?.let { uriToBitmap(it) }
        GaussianBlur.with(context)
            .size(GaussianBlur.MAX_SIZE.toFloat())
            .radius(GaussianBlur.MAX_RADIUS)
            .put(bitmap, binding.blurImageView)

        binding.shapeLayoutOverlay.setTypeOfShape(ShapeLayout.ShapeType.CIRCLE)

//        val data = getNavArgData(bitmap = bitmap)
//
//        val action = CropFragmentDirections.cropFragmentToMainEditScreenFragment(data,false)
//
//        findNavController().navigate(action)

//        binding.blurImageView.crop(binding.blurImageView.sourceUri).execute(object : CropCallback {
//            override fun onError(e: Throwable?) {
//                Log.i(TAG, e?.message.toString())
//                toastFun("Failed to save as Bitmap")
//            }
//
//            override fun onSuccess(croppedImg: Bitmap?) {
//
//                val bitmap = binding.blurImageView.croppedBitmap
//                val data = getNavArgData(bitmap = bitmap)
//
//                val action = CropFragmentDirections.cropFragmentToMainEditScreenFragment(data,false)
//
//                findNavController().navigate(action)
//
//            }
//        })
    }

    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close() // Close the stream to prevent memory leaks
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun toastFun(str: String) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    /**
     * [getNavArgData] for get the nav arg data
     */
    private fun getNavArgData(uri: Uri? = null, bitmap: Bitmap? = null): CommonParcelData {

        bitmap?.also {
            BitmapHelper.setBitmap(it, true)
        }

        return CommonParcelData(
            uri = uri,
            availableData = if (uri != null) ActiveNavArgsData.URI else ActiveNavArgsData.BITMAP
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}