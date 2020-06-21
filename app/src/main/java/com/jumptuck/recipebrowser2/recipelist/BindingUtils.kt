package com.jumptuck.recipebrowser2.recipelist

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.jumptuck.recipebrowser2.database.Recipe

@BindingAdapter("recipeListTitle")
fun TextView.setRecipeListTitle(item: Recipe?){
    item?.let {
        text = item.title
    }
}

@BindingAdapter("singleRecipeBody")
fun TextView.setSingleRecipeBody(item: Recipe?){
    item?.let {
        text = item.body
    }
}