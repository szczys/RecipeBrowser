package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecipeListViewModelFactory(
    private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RecipeListViewModel::class.java)) {
            return RecipeListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}