package io.feeba.data

import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.ServerConfig
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class FeebaResponse(
    val surveyPlans: List<SurveyPlan>,
    val sdkConfig: SdkConfig,
)

@Serializable
data class SurveyPlan(
    val id: String,
    val surveyPresentation: SurveyPresentation,
    val triggerConditions: List<List<TriggerCondition>>,
)

@Serializable
data class TriggerCondition(
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class SdkConfig    (
    val refreshIntervalSec: Int,
    val baseServerUrl: String? = null,
)


@Serializable
data class UserData(
    val userId: String,
    val email: String?,
    val phoneNumber: String?,
)

@Serializable
data class LocalState(
    val numberOfLaunches: Int,
    val totalSessionDurationSec: Int,
    val lastSessionDurationSec: Int,
    val firstSessionDate: Int, // epoch in seconds
    val userData: UserData,
    val events: List<String>,
    val pages: List<String>,
)

data class FeebaConfig(
    val serviceConfig : ServerConfig,
)

@Serializable
data class SurveyPresentation(
    val surveyWebAppUrl: String,
    val useHeightMargin: Boolean,
    val useWidthMargin: Boolean,
    val isFullBleed: Boolean,
    // The following properties are populated from Javascript events
    val displayLocation: Position,
    val displayDuration: Double,
    val maxWidgetHeightInPercent: Int = 70, // between 0 to 100
    val maxWidgetWidthInPercent: Int = 90, // between 0 to 100
)

enum class Position {
    TOP_BANNER, BOTTOM_BANNER, CENTER_MODAL, FULL_SCREEN;

    fun isBanner(): Boolean =
        when (this) {
            TOP_BANNER, BOTTOM_BANNER -> true
            else -> false
        }
}

object Defaults {
    val localState = LocalState(
        numberOfLaunches = 0,
        totalSessionDurationSec = 0,
        lastSessionDurationSec = 0,
        firstSessionDate = Date().time.toInt() / 100,
        userData = UserData(
            userId = "",
            email = "",
            phoneNumber = ""
        ),
        events = listOf(),
        pages = listOf()
    )
}
