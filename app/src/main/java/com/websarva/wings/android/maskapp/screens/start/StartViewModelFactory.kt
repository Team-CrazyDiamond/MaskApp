package com.websarva.wings.android.maskapp.screens.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StartViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("unchecked_cast")
        if (modelClass.isAssignableFrom(StartViewModel::class.java)) {
            return StartViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}