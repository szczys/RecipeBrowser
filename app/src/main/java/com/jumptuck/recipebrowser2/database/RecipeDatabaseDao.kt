package com.jumptuck.recipebrowser2.database

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecipeDatabaseDao {
    @Insert
    fun insert(recipe: Recipe)

    @Update
    fun update(recipe: Recipe)

    @Query("UPDATE recipe_table SET fav=:fav WHERE recipeID = :key")
    fun setFavorite(key: Long, fav: Boolean)

    @Query("SELECT count() FROM recipe_table WHERE fav=1")
    fun favoriteCount(): Int

    @Query("SELECT count() FROM recipe_table WHERE title = :title AND lastUpdate = :date")
    fun isIn(title: String, date: String): Int

    @Query("SELECT * from recipe_table WHERE recipeID = :id")
    fun getRecipe(id: Int): Recipe

    @Query("SELECT * from recipe_table ORDER BY title ASC")
    fun getAll(): LiveData<List<Recipe>>
    //OLD CODE: fun getAll(): Cursor

    @Query("SELECT * from recipe_table WHERE cat = :cat ORDER BY title ASC")
    fun getCategory(cat: String): LiveData<List<Recipe>>
    //OLD CODE: fun getCategory(dir: String): Cursor

    @Query("SELECT * from recipe_table WHERE fav = 1 ORDER BY title ASC")
    fun getFavorites(): LiveData<List<Recipe>>
    //OLD CODE: val favorites: Cursor

    @Query("DELETE from recipe_table")
    fun deleteAllRecipes()
}