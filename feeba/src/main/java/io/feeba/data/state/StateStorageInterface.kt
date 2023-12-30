package io.feeba.data.state

import io.feeba.data.FeebaResponse

interface StateStorageInterface {

    var state: AppHistoryState
    var feebaResponse: FeebaResponse
    // Followings are likely to be backed by a database
    fun addPageOpenRecord(pageName: String, value: String)
    fun readPageEvenLogs(pageName: String): List<PageEventLog>
    fun addEventRecord(eventName: String, value: String)
    fun readEventLogs(eventName: String): List<EventLog>
    fun trimData()
    fun eraseEventAndPageLogs()
}

data class PageEventLog(val pageName: String, val value: String, val createdAt: Long)

data class EventLog(val eventName: String, val value: String, val createdAt: Long)
