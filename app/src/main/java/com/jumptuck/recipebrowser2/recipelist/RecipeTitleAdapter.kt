package com.jumptuck.recipebrowser2.recipelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.databinding.ListviewItemBinding

class RecipeTitleAdapter(private val clickListener: RecipeTitleListener): ListAdapter<Recipe, RecipeTitleAdapter.ViewHolder>(RecipeListDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListviewItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Recipe,
            clickListener: RecipeTitleListener
        ) {
            binding.recipe = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListviewItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class RecipeListDiffCallback : DiffUtil.ItemCallback<Recipe>() {
    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem.recipeID == newItem.recipeID
    }

    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem == newItem
    }
}

class RecipeTitleListener(val clickListener: (recipeID: Long) -> Unit) {
    fun onClick(recipe: Recipe) = clickListener(recipe.recipeID)
}
