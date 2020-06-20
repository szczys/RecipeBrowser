package com.jumptuck.recipebrowser2.recipelist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.TextItemViewHolder
import com.jumptuck.recipebrowser2.database.Recipe
import timber.log.Timber

class RecipeTitleAdapter: RecyclerView.Adapter<TextItemViewHolder>(){
    var data = listOf<Recipe>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount()= data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = data[position]
        Timber.i("holder.textView.text = %s", item.title)
        holder.textView.text = item.title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.listview_item, parent, false) as TextView
        return TextItemViewHolder(view)
    }
}
