package io.feeba.data.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger

object Contract {
    // Table contents are grouped together in an anonymous object.
    object PagesEntry : BaseColumns {
        const val TABLE_NAME = "pages"
        const val COL_PAGE_NAME = "page_name"
        const val COL_VALUE = "value"
        const val COL_CREATED = "created_at"
    }

    object EventsEntry : BaseColumns {
        const val TABLE_NAME = "events"
        const val COL_EVENT_NAME = "event_name"
        const val COL_VALUE = "value"
        const val COL_CREATED = "created_at"
    }
}

interface BaseColumns {

    companion object {
        const val _ID = "_id"
        const val _COUNT = "_count"
    }
}

class FeebaDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Logger.log(LogLevel.DEBUG, "FeebaDbHelper::onCreate")
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${Contract.PagesEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Contract.PagesEntry.COL_PAGE_NAME} TEXT," +
                    "${Contract.PagesEntry.COL_VALUE} TEXT)"
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Contract.PagesEntry.TABLE_NAME}"

        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "feeba.db"
    }
}