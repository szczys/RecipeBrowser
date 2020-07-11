package com.jumptuck.recipebrowser2.singlerecipe

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import java.lang.IllegalArgumentException

class SingleRecipeViewModelFactory(private val recipeID: Long, private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SingleRecipeViewModel::class.java)) {
            return SingleRecipeViewModel(
                recipeID, application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}