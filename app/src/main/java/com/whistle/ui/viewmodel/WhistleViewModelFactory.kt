package com.whistle.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

private const val LOG_TAG = "Whistle.WhistleViewModelFactory"

class WhistleViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    fun getViewModelClass() = WhistleViewModel::class.java

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d(LOG_TAG, "Creating $modelClass")

        if (modelClass.isAssignableFrom((getViewModelClass())))
            return modelClass
                .getConstructor()
                .newInstance()
        throw IllegalArgumentException("Unknown ViewModel")
    }
}