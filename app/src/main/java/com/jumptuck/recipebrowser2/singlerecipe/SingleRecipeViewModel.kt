package com.jumptuck.recipebrowser2.singlerecipe

import androidx.lifecycle.ViewModel
import timber.log.Timber

class SingleRecipeViewModel(recipeID: Long) : ViewModel() {
    init {
        Timber.i("Recipe Index is $recipeID")
    }
}