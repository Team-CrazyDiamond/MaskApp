package com.websarva.wings.android.maskapp.screens.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StartViewModel : ViewModel() {

    private val _language = MutableLiveData<String>()
    val language: LiveData<String>
    get() = _language

    fun onJapanButtonClick() {
        _language.value = "Japanese"
    }

    fun onEnglishButtonClick() {
        _language.value = "English"
    }

    fun onNavigateDetection() {
        _language.value = null
    }
}