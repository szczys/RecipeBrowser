package com.jumptuck.recipebrowser2

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class RecipeViewModel : ViewModel() {

    private val _titleArray = MutableLiveData<ArrayList<String>>()
    val titleArray : LiveData<ArrayList<String>>
        get() = _titleArray

    init {
        Timber.i("RecipeViewModel created")
        _titleArray.value = ArrayList<String>()
        (_titleArray.value)?.add("Whiskey Sour")
        (_titleArray.value)?.add("Bee's Knees")
        (_titleArray.value)?.add("Aperol Spritz")
    }

    fun addMenuItem() {
        _titleArray.value?.add("Success!!")
        _titleArray.value = titleArray.value //Force update for observers
    }
}