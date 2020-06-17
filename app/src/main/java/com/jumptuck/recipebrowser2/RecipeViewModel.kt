package com.jumptuck.recipebrowser2

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class RecipeViewModel : ViewModel() {

    val titleArray = MutableLiveData<ArrayList<String>>()

    init {
        Timber.i("RecipeViewModel created")
        titleArray.value = ArrayList<String>()
        (titleArray.value)?.add("Whiskey Sour")
        (titleArray.value)?.add("Bee's Knees")
        (titleArray.value)?.add("Aperol Spritz")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i( "RecipeViewModel destroyed")
    }

    fun addMenuItem() {
        titleArray.value?.add("Success!!")
        titleArray.value = titleArray.value //Force update for observers
    }
}