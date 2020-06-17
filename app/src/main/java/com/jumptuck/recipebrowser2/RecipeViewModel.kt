package com.jumptuck.recipebrowser2

import android.util.Log
import androidx.lifecycle.ViewModel
import timber.log.Timber

class RecipeViewModel : ViewModel() {

    val titleArray = arrayOf("Whisky Sour", "Bee's Knees", "Aperol Spritz")

    init {
        Timber.i("RecipeViewModel created")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i( "RecipeViewModel destroyed")
    }
}