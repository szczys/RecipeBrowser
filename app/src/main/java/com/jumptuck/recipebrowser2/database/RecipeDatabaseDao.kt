package com.jumptuck.recipebrowser2.database

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDatabaseDao {
    @Insert
    fun insert(recipe: Recipe)

    @Query("UPDATE recipe_table SET fav=:fav WHERE recipeID=:key")
    fun setFavorite(key: Long, fav: Boolean)

    @Query("SELECT count() FROM recipe_table WHERE fav=1")
    fun favoriteCount(): Int

    fun isIn(title: String, date: String): Boolean

    fun getRecipe(id: String): Recipe

    fun getAll(): Cursor

    fun getCategory(dir: String): Cursor

    val favorites: Cursor

    fun deleteAllRecipes()
}