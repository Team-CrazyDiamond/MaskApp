package com.websarva.wings.android.maskapp.screens.detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DetectionViewModelFactory(
    private val language: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetectionViewModel::class.java)) {
            return DetectionViewModel(language) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}