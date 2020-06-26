package com.jumptuck.recipebrowser2.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_table")
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var recipeID: Long = 0L,
    @ColumnInfo(name = "title")
    var title: String = "",
    @ColumnInfo(name = "body")
    var body: String = "",
    @ColumnInfo(name = "url")
    var link: String = "",
    @ColumnInfo(name = "cat")
    var category: String = "",
    @ColumnInfo(name = "date")
    var date: String = "",
    @ColumnInfo(name = "fav")
    var favorite: Boolean = false
)