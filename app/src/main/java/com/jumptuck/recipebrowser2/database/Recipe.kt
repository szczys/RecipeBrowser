package com.jumptuck.recipebrowser2.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_table")
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var recipeID: Long = 0L,
    @ColumnInfo(name = "title")
    var title: String = "No Title Found",
    @ColumnInfo(name = "body")
    var body: String = "No Body Found",
    @ColumnInfo(name = "url")
    var link: String = "No URL Found",
    @ColumnInfo(name = "dir")
    var directory: String = "No Directory Found",
    @ColumnInfo(name = "updated")
    var date: String = "No Date Found",
    @ColumnInfo(name = "fav")
    var favorite: Boolean = false
)