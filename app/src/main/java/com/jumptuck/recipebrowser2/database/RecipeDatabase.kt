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

package com.jumptuck.recipebrowser2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {

    abstract val recipeDatabaseDao: RecipeDatabaseDao

    companion object{

        @Volatile
        private lateinit var INSTANCE: RecipeDatabase

        fun getInstance(context: Context): RecipeDatabase {
            synchronized (RecipeDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            RecipeDatabase::class.java,
                            "recipe_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                }
                return INSTANCE
            }

        }
    }
}