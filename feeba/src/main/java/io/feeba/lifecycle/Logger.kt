package io.feeba.lifecycle

import android.util.Log

object Logger {
    private const val TAG = "Feeba"
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
}

enum class LogLevel {
    NONE, ERROR, WARN, DEBUG
}