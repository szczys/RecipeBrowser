package com.jumptuck.recipebrowser2.recipelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.Recipe

class RecipeTitleAdapter/*(val clickListener: RecipeTitleListener)*/: RecyclerView.Adapter<RecipeTitleAdapter.ViewHolder>(){
    var data = listOf<Recipe>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount()= data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val receipTitle: TextView = itemView.findViewById(R.id.recipe_list_title)

        fun bind(item: Recipe) {
            val res = itemView.context.resources
            receipTitle.text = item.title
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.listview_item, parent, false)
                return ViewHolder(view)
            }
        }
    }
}

/*
class RecipeTitleListener(val clickListener: (recipeID: Int) -> Unit) {
    fun onClick(recipe: Recipe) = clickListener(recipe.recipeID)
}*/
