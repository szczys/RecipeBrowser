//package com.jumptuck.recipebrowser2
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.Cursor
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.util.Log
//
//class RecipeData(var context: Context) {
//
//    var recipeDB: SQLiteDatabase? = null
//    var dbHelper = DbHelper()
//
//    internal inner class DbHelper :
//        SQLiteOpenHelper(context, DB_NAME, null, 4) {
//        override fun onCreate(db: SQLiteDatabase) {
//            db.execSQL(makeTable())
//        }
//
//        override fun onUpgrade(
//            db: SQLiteDatabase,
//            oldVersion: Int,
//            newVersion: Int
//        ) {
//            db.execSQL("drop table if exists recipes")
//            onCreate(db)
//        }
//    }
//
//    fun insert(recipe: Recipe) {
//        recipeDB = dbHelper.writableDatabase
//        val values = ContentValues()
//        values.put(C_TITLE, recipe.title)
//        values.put(C_LINK, recipe.link)
//        values.put(C_DIR, recipe.directory)
//        values.put(C_DATE, recipe.date)
//        values.put(C_BODY, recipe.body)
//        values.put(C_FAV, Integer.valueOf(recipe.favorite))
//        recipeDB.insert(TABLE, null, values)
//    }
//
//    fun setFavorite(id: String, favBit: Int) {
//        val sql = String.format(
//            "update %s set favorite=%s where _id=%s",
//            *arrayOf<Any>(TABLE, Integer.valueOf(favBit), id)
//        )
//        Log.d(TAG, "setFavorite sql: $sql")
//        recipeDB = dbHelper.writableDatabase
//        recipeDB.execSQL(sql)
//    }
//
//    val favoriteCount: Int
//        get() {
//            val sql = String.format(
//                "select count() from %s where %s=1",
//                *arrayOf<Any>(TABLE, C_FAV)
//            )
//            Log.d(TAG, "getFavoriteCount sql: $sql")
//            recipeDB = dbHelper.writableDatabase
//            val cursor = recipeDB.rawQuery(sql, null)
//            return if (cursor.moveToFirst()) {
//                cursor.getInt(0)
//            } else 0
//        }
//
//    fun isIn(title: String, date: String): Boolean {
//        recipeDB = dbHelper.readableDatabase
//        val cursor = recipeDB.query(
//            true,
//            TABLE,
//            arrayOf(C_TITLE, C_DATE),
//            "title=? AND date=?",
//            arrayOf(title, date),
//            null,
//            null,
//            null,
//            null
//        )
//        var returnValue = true
//        if (!cursor.moveToFirst() || cursor.count == 0) {
//            returnValue = false
//        }
//        cursor.close()
//        return returnValue
//    }
//
//    fun getRecipe(id: String): Recipe {
//        recipeDB = dbHelper.readableDatabase
//        val cursor = recipeDB.query(
//            true,
//            TABLE,
//            null,
//            "_id=?",
//            arrayOf(id),
//            null,
//            null,
//            null,
//            null
//        )
//        cursor.moveToNext()
//        val recipe = Recipe(
//            cursor.getString(cursor.getColumnIndex(C_TITLE)),
//            cursor.getString(cursor.getColumnIndex(C_LINK)),
//            cursor.getString(cursor.getColumnIndex(C_DIR)),
//            cursor.getString(cursor.getColumnIndex(C_DATE))
//        )
//        recipe.f2id = id
//        recipe.body = cursor.getString(cursor.getColumnIndex(C_BODY))
//        recipe.favorite = cursor.getInt(cursor.getColumnIndex(C_FAV))
//        cursor.close()
//        return recipe
//    }
//
//    fun query(): Cursor {
//        Log.d(TAG, "Getting all categories")
//        recipeDB = dbHelper.readableDatabase
//        return recipeDB.query(
//            TABLE,
//            null,
//            null,
//            null,
//            null,
//            null,
//            C_TITLE
//        )
//    }
//
//    fun query(dir: String): Cursor {
//        Log.d(TAG, "Polling for category: $dir")
//        recipeDB = dbHelper.readableDatabase
//        return recipeDB.query(
//            TABLE,
//            null,
//            "directory = ?",
//            arrayOf(dir),
//            null,
//            null,
//            C_TITLE
//        )
//    }
//
//    val favorites: Cursor
//        get() {
//            Log.d(TAG, "Polling for Favorites")
//            recipeDB = dbHelper.readableDatabase
//            return recipeDB.query(
//                TABLE,
//                null,
//                "favorite = 1",
//                null,
//                null,
//                null,
//                C_TITLE
//            )
//        }
//
//    fun makeTable(): String {
//        val sql = String.format(
//            "create table %s (_id integer primary key autoincrement, %s text, %s text, %s text, %s text, %s text, %s integer)",
//            *arrayOf<Any>(
//                TABLE,
//                C_TITLE,
//                C_LINK,
//                C_DIR,
//                C_DATE,
//                C_BODY,
//                C_FAV
//            )
//        )
//        Log.d(TAG, "onCreate with SQL: $sql")
//        return sql
//    }
//
//    fun deleteAllRecipes() {
//        Log.d(TAG, "deleteAllRecipes")
//        recipeDB = dbHelper.writableDatabase
//        recipeDB.execSQL("drop table if exists recipes")
//        recipeDB.execSQL(makeTable())
//    }
//
//    val categories: Cursor
//        get() {
//            recipeDB = dbHelper.readableDatabase
//            return recipeDB.rawQuery("select distinct directory from recipes", null)
//        }
//
//    companion object {
//        const val C_BODY = "body"
//        const val C_DATE = "date"
//        const val C_DIR = "directory"
//        const val C_FAV = "favorite"
//        const val C_LINK = "link"
//        const val C_TITLE = "title"
//        const val DB_NAME = "recipe_collection.db"
//        const val DB_VERSION = 4
//        const val TABLE = "recipes"
//        const val TAG = "RecipeData"
//    }
//
//    init {
//        Log.d(TAG, "Constructed")
//    }
//}