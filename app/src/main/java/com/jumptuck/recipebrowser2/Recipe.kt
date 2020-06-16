package com.jumptuck.recipebrowser2

class Recipe(
    var title: String,
    var link: String,
    var directory: String,
    var date: String
) {
    var body = BODY_DEFAULT
    var favorite = 0

    /* renamed from: id */
    var recipeID: String? = null

    companion object {
        const val BODY_DEFAULT = "Recipe body not found"
    }
}