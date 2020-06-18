package com.jumptuck.recipebrowser2.database

data class Recipe (
    var recipeID: Long = 0L
    var title: String = "No Title Found",
    var body: String = "No Body Found",
    var link: String = "No URL Found",
    var directory: String = "No Directory Found",
    var date: String = "No Date Found",
    var favorite: Boolean = false
)