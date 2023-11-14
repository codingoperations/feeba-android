package io.feeba.data.state

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val userId: String,
    val email: String?,
    val phoneNumber: String?,
)

@Serializable
data class AppHistoryState(
    val numberOfLaunches: Int,
    val totalSessionDurationSec: Long,

    val lastTimeAppOpened: Long,
    val lastTimeSurveyTriggered: Map<String, Long>,

    val userData: UserData,
)

object Defaults {
    val appHistoryState = AppHistoryState(
        numberOfLaunches = 0,
        totalSessionDurationSec = 0,
        lastTimeAppOpened = 0,
        lastTimeSurveyTriggered = mapOf(),
        userData = UserData(
            userId = "",
            email = "",
            phoneNumber = ""
        ),
    )
}
