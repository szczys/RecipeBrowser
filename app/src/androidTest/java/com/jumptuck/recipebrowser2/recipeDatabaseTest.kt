/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumptuck.recipebrowser2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class recipeDatabaseTest {

    private lateinit var recipeDao: RecipeDatabaseDao
    private lateinit var db: RecipeDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        recipeDao = db.recipeDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testDatabase() {
        var recipe = Recipe()
        var retrievedRecipe = Recipe()

        //Test insert and getRecipe
        recipe.title = "Marmite"
        recipe.body = "Body"
        recipe.link = "recipe.com"
        recipe.category = "Poison"
        recipe.date = "2020-06-19"
        recipeDao.insert(recipe)
        retrievedRecipe = recipeDao.getRecipe(1)
        assertEquals(retrievedRecipe.title, "Marmite")
        assertEquals(retrievedRecipe.body,"Body")
        assertEquals(retrievedRecipe.link,"recipe.com")
        assertEquals(retrievedRecipe.category,"Poison")
        assertEquals(retrievedRecipe.date,"2020-06-19")
        assertEquals(retrievedRecipe.favorite,false)

        //Test setFavorite
        recipeDao.setFavorite(1,true)
        retrievedRecipe = recipeDao.getRecipe(1)
        assertEquals(retrievedRecipe.favorite, true)

        //Test favoriteCount
        recipe = Recipe()
        recipe.title = "Apple Pie"
        recipe.category = "Food"
        recipe.favorite = true
        recipeDao.insert(recipe)
        assertEquals(recipeDao.favoriteCount(),2)

        //Test isIn
        assertEquals(recipeDao.isIn("Motor Oil", ""),0)
        retrievedRecipe = recipeDao.getRecipe(1)
        assertNotEquals(recipeDao.isIn("Marmite","2020-06-19"),0)

        //Test getAll
        recipe = Recipe()
        recipe.title = "Chowder"
        recipe.category = "Food"
        recipeDao.insert(recipe)

        //Test getAll
        recipeDao.getAll().observeOnce {
            assertEquals(it[0].title,"Apple Pie")
            assertEquals(it[1].title,"Chowder")
            assertEquals(it[2].title,"Marmite")
        }

        //Test getCategory
        recipeDao.update(retrievedRecipe)
        recipeDao.getCategory("Food").observeOnce {
            assertEquals(it.size, 2)
            assertEquals(it[0].title, "Apple Pie")
            assertEquals(it[1].title, "Chowder")
        }

        //Test getFavorites
        recipeDao.getFavorites().observeOnce {
            assertEquals(it.size, 2)
            assertEquals(it[0].title, "Apple Pie")
            assertEquals(it[1].title, "Marmite")
        }

        //Test deleteAllRecipes
        recipeDao.getAll().observeOnce {
            assertEquals(it.size, 3)
        }
        recipeDao.deleteAllRecipes()
        recipeDao.getAll().observeOnce {
            assertEquals(it.size, 0)
        }
    }

    fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
        val observer = OneTimeObserver(handler = onChangeHandler)
        observe(observer, observer)
    }
}