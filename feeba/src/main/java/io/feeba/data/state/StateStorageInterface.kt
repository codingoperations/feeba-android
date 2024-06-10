package io.feeba.data.state

import io.feeba.data.FeebaResponse

interface StateStorageInterface {

    var state: AppHistoryState
    var feebaResponse: FeebaResponse
    // Followings are likely to be backed by a database
    fun addPageOpenRecord(pageName: String, value: String, triggeredSurveyId: String)
    fun readPageEvenLogs(surveyId: String): List<SurveyExecutionLogs>
    fun addEventRecord(eventName: String, value: String, triggeredSurveyId: String)
     fun readEventLogs(surveyId: String): List<SurveyExecutionLogs>
    fun trimData()
    fun eraseEventAndPageLogs()
}

data class SurveyExecutionLogs(val triggerName: String, val payload: String, val surveyId: String, val type: String,  val createdAt: Long)
