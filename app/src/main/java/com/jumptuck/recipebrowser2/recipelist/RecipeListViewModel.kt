package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import timber.log.Timber

class RecipeListViewModel(
    val database: RecipeDatabaseDao,
    application: Application) : AndroidViewModel(application) {



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