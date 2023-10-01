package io.feeba.data

import kotlinx.serialization.Serializable

@Serializable
data class FeebaResponse(
    val surveyPlans: List<SurveyPlan>,
    val sdkConfig: SdkConfig,
)

@Serializable
data class SurveyPlan(
    val id: String,
    val surveyUrl: String,
    val triggers: List<List<Trigger>>,
)

@Serializable
data class Trigger(
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class SdkConfig    (
    val refreshInterval: Int,
    val baseServerUrl: String?,
)


@Serializable
data class UserData(
    val userId: String,
    val email: String,
    val phoneNumber: String,
)

@Serializable
data class LocalState(
    val numberOfLaunches: Int,
    val totalSessionTimeSec: Int,
    val lastSessionTimeSec: Int,
    val userData: UserData,
    val events: List<String>,
    val pages: List<String>,
)


object Defaults {
    val localState = LocalState(
        numberOfLaunches = 0,
        totalSessionTimeSec = 0,
        lastSessionTimeSec = 0,
        userData = UserData(
            userId = "",
            email = "",
            phoneNumber = ""
        ),
        events = listOf(),
        pages = listOf()
    )
}