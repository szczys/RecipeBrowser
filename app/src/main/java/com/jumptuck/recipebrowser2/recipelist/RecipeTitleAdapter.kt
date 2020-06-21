package com.jumptuck.recipebrowser2.recipelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.TextItemViewHolder
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
        val res = holder.itemView.resources
        holder.receipTitle.text = item.title
        //holder.bind(item)
    }

    //private fun TextItemViewHolder.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listview_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val receipTitle: TextView = itemView.findViewById(R.id.recipe_list_title)
    }
}

/*
class RecipeTitleListener(val clickListener: (recipeID: Int) -> Unit) {
    fun onClick(recipe: Recipe) = clickListener(recipe.recipeID)
}*/
