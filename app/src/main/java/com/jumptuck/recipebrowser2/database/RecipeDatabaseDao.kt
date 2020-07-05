package com.jumptuck.recipebrowser2.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecipeDatabaseDao {
    @Insert
    fun insert(recipe: Recipe): Long

    @Update
    fun update(recipe: Recipe)

    @Query("UPDATE recipe_table SET fav=:fav WHERE recipeID = :key")
    fun setFavorite(key: Long, fav: Boolean)

    @Query("SELECT count() from recipe_table")
    fun recipeCount(): Int

    @Query("SELECT count() FROM recipe_table WHERE fav=1")
    fun favoriteCount(): LiveData<Int>

    @Query("SELECT EXISTS(SELECT * FROM recipe_table WHERE title = :title)")
    fun isIn(title: String): Boolean

    @Query("SELECT * from recipe_table WHERE title = :title LIMIT 1")
    fun findRecipeByTitle(title: String): Recipe?

    @Query("SELECT * from recipe_table WHERE recipeID = :id")
    fun getRecipe(id: Long): LiveData<Recipe>

    @Query("SELECT * from recipe_table ORDER BY title ASC")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * from recipe_table WHERE cat = :cat ORDER BY title ASC")
    fun getRecipesFromCategory(cat: String): LiveData<List<Recipe>>

    @Query("SELECT distinct(cat) from recipe_table ORDER BY cat ASC")
    fun getCategoryList(): LiveData<List<String>>

    @Query("SELECT * from recipe_table WHERE fav = 1 ORDER BY title ASC")
    fun getFavorites(): LiveData<List<Recipe>>

    @Query("DELETE from recipe_table")
    fun deleteAllRecipes()
}