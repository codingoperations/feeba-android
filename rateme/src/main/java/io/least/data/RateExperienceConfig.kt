package io.least.data

import android.os.Parcelable
import io.least.core.collector.CommonContext
import io.least.core.collector.UserSpecificContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Tag(val id: String, val text: String) : Parcelable

@Serializable
data class RateExperienceConfig(
    val tags: List<Tag>,
    val numberOfStars: Int,
    val valueReaction: List<LabelValue>,
    val title: String,
    val postSubmitTitle: String,
    val postSubmitText: String,
    val autoClosePostSubmission: Boolean = true,
)

@Serializable
data class LabelValue(val value: Int, val label: String)

@Serializable
data class RateExperienceResult(
    val tags: List<Tag>,
    val rate: Int,
    val totalStars: Int,
    val feedback: String,
    val userContext: UserSpecificContext,
    var commonContext: CommonContext? = null,
)