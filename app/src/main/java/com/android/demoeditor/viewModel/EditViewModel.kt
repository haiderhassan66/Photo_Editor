package com.android.demoeditor.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.demoeditor.customClass.BitmapHelper
import com.android.demoeditor.customClass.ResizePhoto
import com.android.demoeditor.customClass.ResizePhoto.RESOLUTION
import com.android.demoeditor.customClass.SaveBitmapInStorage
import com.android.demoeditor.customClass.UriBitmapUtils
import com.android.demoeditor.data.parcelData.CommonParcelData
import com.android.demoeditor.enums.ActiveNavArgsData
import com.android.demoeditor.repository.MainScreenRepository
import com.android.demoeditor.screens.EditScreenFragmentArgs
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EditViewModel(
    application: Application,
    navArgs: EditScreenFragmentArgs,
    activity: Activity ,
    private val redirectToScreen:()->Unit ,
) :
    AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext


    private val repository = MainScreenRepository(context)


    @SuppressLint("StaticFieldLeak")
    private var activity: Activity = activity

    companion object {
        private val TAG by lazy { EditViewModel::class.java.simpleName }
    }


    private val backgroundScope = CoroutineScope(Dispatchers.IO + Job())


    private val _imgSrc = MutableLiveData<Bitmap>()

    val imgSrc: LiveData<Bitmap>
        get() = _imgSrc


    /**
     * is Img Loading
     */
    private val _isImgLoading = MutableLiveData<Boolean>(false)
    val isImgLoading: LiveData<Boolean>
        get() = _isImgLoading


    val dataForMainScreenNavBarData = MutableLiveData(repository.dataForMainNavBar)


    private lateinit var uriBitmapUtils: UriBitmapUtils

    init {

        setUriOrBitmap(navArgs)

    }


    fun setUriOrBitmap(navArgs: EditScreenFragmentArgs) {
        when (navArgs.imgData.availableData) {
            ActiveNavArgsData.BITMAP -> {
                Log.e(TAG, "setUriOrBitmap: BITMAP", )

                if (BitmapHelper.bitmap != null) {

                    _isImgLoading.value = true

                    /**
                     * if [BitmapHelper.isResized] is [true] don't resize
                     * else [BitmapHelper.isResized] is [false] do resize
                     */
                    if (BitmapHelper.isResized) {
                        _imgSrc.value = BitmapHelper.bitmap
                        _isImgLoading.value = false
                    } else {

                        val resizePhoto =
                            ResizePhoto(context, BitmapHelper.bitmap!!, true, RESOLUTION.HD_720P)
                        backgroundScope.launch {
                            val resizeBitmap = resizePhoto.execute()
                            withContext(Dispatchers.Main) {
                                _imgSrc.value = resizeBitmap
                                _isImgLoading.value = false
                            }

                        }

                    }

                } else {
                    // Navigate to another screen
                    redirectToScreen()

                }

            }

            ActiveNavArgsData.URI -> {
                Log.e(TAG, "setUriOrBitmap: URI ${navArgs.imgData}", )
                convertUriToBitmap(navArgs.imgData,navArgs.isDraft)


            }

            ActiveNavArgsData.BYTE_ARRAY -> {
                Log.e(TAG, "setUriOrBitmap: BYTE_ARRAY", )
//                TODO()
            }
        }

    }


    private fun convertUriToBitmap(imgData: CommonParcelData, draft: Boolean) {
        if (imgData.uri != null) {

            _isImgLoading.value = true

            uriBitmapUtils = UriBitmapUtils(imgData.uri, context, viewModelScope,draft) {
                Log.d(TAG, "URI --> Bitmap converted ")


                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Bitmap --> Resized-Bitmap converted ")

                    _isImgLoading.value = false

                    _imgSrc.value = it

                }

            }


        } else {
            Log.d(TAG, "URI --> null")
            throw Exception("Uri not available")
        }
    }


    /**
     * this fun change value of [_isImgLoading]
     */
    fun setLoadingState(value: Boolean) {
        _isImgLoading.value = value
    }


    fun savePhotoInStorage(result: (resultUri: String) -> Unit) {

        val saveBitmapInStorage = SaveBitmapInStorage(context)
        backgroundScope.launch {
            val date = Date()
            val simpleDateFormat =
                SimpleDateFormat("yy-MM-dd  HH:mm:ss", Locale.US)
            SaveBitmapInStorage.imagePath = simpleDateFormat.format(date) + ".png"
//            val uri = saveBitmapInStorage.save(_imgSrc.value!!)
            val uri = saveBitmapInStorage.saveImage(activity,_imgSrc.value!!,true,100)
            delay(3000)
            withContext(Dispatchers.Main) {
                result(uri)
            }
        }

    }


}