package com.jumptuck.recipebrowser2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class SingleRecipeViewModelFactory(private val recipeID: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SingleRecipeViewModel::class.java)) {
            return SingleRecipeViewModel(recipeID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}