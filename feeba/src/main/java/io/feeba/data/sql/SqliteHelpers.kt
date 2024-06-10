package io.feeba.data.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger

const val ExecutedSurveyTypeEvent = "event"
const val ExecutedSurveyTypePage = "page"

object Contract {
    // Table contents are grouped together in an anonymous object.
    object SurveyExecutionRecords : BaseColumns {
        const val TABLE_NAME = "executed_surveys"
        const val COL_TRIGGERED_SURVEY_ID = "survey_id"
        const val COL_TRIGGER_VALUE = "triggered_value"
        const val COL_PAYLOAD = "payload"
        const val COL_TYPE = "type"
        const val COL_CREATED = "created_at"
    }
}

interface BaseColumns {

    companion object {
        const val _ID = "_id"
        const val _COUNT = "_count"
    }
}

class FeebaDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Logger.log(LogLevel.DEBUG, "FeebaDbHelper::onCreate")
        val SQL_CREATE_PAGES_TABLE =
            "CREATE TABLE ${Contract.SurveyExecutionRecords.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Contract.SurveyExecutionRecords.COL_TRIGGERED_SURVEY_ID} TEXT," +
                    "${Contract.SurveyExecutionRecords.COL_TRIGGER_VALUE} TEXT," +
                    "${Contract.SurveyExecutionRecords.COL_PAYLOAD} TEXT," +
                    "${Contract.SurveyExecutionRecords.COL_TYPE} TEXT," +
                    "${Contract.SurveyExecutionRecords.COL_CREATED} INTEGER)"
        db.execSQL(SQL_CREATE_PAGES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL( "DROP TABLE IF EXISTS ${Contract.SurveyExecutionRecords.TABLE_NAME}")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "feeba.db"
    }
}