package com.websarva.wings.android.maskapp.screens.detection

import android.media.Image
import android.view.View
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetectionViewModel(
    val language: String
) : ViewModel() {

    private val _childCount = MutableLiveData<Int>()
    val childCount: LiveData<Int>
    get() = _childCount

    private val _viewFinder = MutableLiveData<PreviewView>()
    val viewFinder: LiveData<PreviewView>
    get() = _viewFinder

    fun removeALlView() {
        _viewFinder.value?.removeAllViews()
    }

    fun addView(view: View) {
        _viewFinder.value?.addView(view)
    }



}