package io.feeba.lifecycle

import android.util.Log

object Logger {
    private const val TAG = "Feeba"
    var minLevel: LogLevel = LogLevel.DEBUG
    fun log(level: LogLevel, message: String) = log(level, message, null)

    fun log(level: LogLevel, message: String, throwable: Throwable?) {

        if (level.compareTo(level) < 1) {
            when (level) {
                LogLevel.DEBUG -> Log.d(TAG, message, throwable)
                LogLevel.WARN -> Log.w(TAG, message, throwable)
                LogLevel.ERROR -> Log.e(TAG, message, throwable)
                LogLevel.NONE -> Unit
            };
        }
    }

    fun d(message: String) {
        if (minLevel.compareTo(LogLevel.DEBUG) < 1) {
            Log.d(TAG, message)
        }
    }

    fun w(message: String) {
        if (minLevel.compareTo(LogLevel.WARN) < 1) {
            Log.w(TAG, message)
        }
    }

    fun e(message: String) {
        if (minLevel.compareTo(LogLevel.ERROR) < 1) {
            Log.e(TAG, message)
        }
    }
}

enum class LogLevel {
    NONE, ERROR, WARN, DEBUG
}