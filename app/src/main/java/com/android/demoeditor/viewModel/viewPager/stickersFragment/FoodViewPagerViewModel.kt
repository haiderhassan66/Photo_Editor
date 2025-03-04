package com.android.demoeditor.viewModel.viewPager.stickersFragment

import android.app.Application
import android.content.res.TypedArray
import com.android.demoeditor.R
import com.android.demoeditor.data.StickerData
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class FoodViewPagerViewModel(application: Application): AndroidViewModel(application)  {

    private val TAG = this::class.java.simpleName

    private var stickerList: TypedArray = application.resources.obtainTypedArray(R.array.foods_photo)

    private val _recyclerViewData = MutableLiveData(getRecyclerViewData())
    val recyclerViewData: LiveData<MutableList<StickerData>>
        get() = _recyclerViewData

    private fun getRecyclerViewData(): MutableList<StickerData> {

        val list = mutableListOf<StickerData>()

        var i = 0
        while (i < stickerList.length()) {
            try {
                val drawable = stickerList.getDrawable(i)!!
                val bitmap = (drawable as BitmapDrawable).bitmap!!
               // val data = StickerData(bitmap)
                val data = StickerData(drawable)
                list.add(data)
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
            i++
        }

        return list
    }


}