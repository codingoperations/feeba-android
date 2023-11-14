package io.feeba.data

import io.least.core.ServerConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeebaResponse(
    val surveyPlans: List<SurveyPlan>,
    val sdkConfig: SdkConfig? = null,
)

@Serializable
data class SurveyPlan(
    val id: String,
    val surveyPresentation: SurveyPresentation,
    val triggerConditions: List<List<TriggerCondition>>,
)

@Serializable
enum class RuleType () {
    @SerialName("event") EVENT,
    @SerialName("session_duration") SESSION_DURATION,
    @SerialName("since_last") SINCE_LAST,
    @SerialName("screen") SCREEN,
    @SerialName("app_open") APP_OPEN
}

@Serializable
data class TriggerCondition(
    val type: RuleType,
    val property: String,
    val operator: String,
    val value: String
)

@Serializable
data class SdkConfig    (
    val refreshIntervalSec: Int,
    val baseServerUrl: String? = null,
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
    val displayLocation: Position = Position.BOTTOM_BANNER,
    val displayDuration: Double,
    val maxWidgetHeightInPercent: Int = 70, // between 0 to 100
    val maxWidgetWidthInPercent: Int = 90, // between 0 to 100
)

@Serializable
enum class Position() {
    @SerialName("top_banner") TOP_BANNER,
    @SerialName("bottom_banner") BOTTOM_BANNER,
    @SerialName("center_modal") CENTER_MODAL,
    @SerialName("full_screen") FULL_SCREEN;
}

fun isBanner(position: Position): Boolean =
    when (position) {
        Position.TOP_BANNER, Position.BOTTOM_BANNER -> true
        else -> false
    }
