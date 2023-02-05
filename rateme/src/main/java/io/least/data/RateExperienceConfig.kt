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
    val valueReactions: List<LabelValue>,
    val title: String,
    val postSubmitTitle: String,
    val postSubmitText: String,
    val autoClosePostSubmission: Boolean = true,
    val isPremium: Boolean? = false, // this is a signaling to avoid unnecessary backend calls. Server will run the validation without depending on this field
): java.io.Serializable

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

@Serializable
data class TagUpdate(val tags: List<Tag>, val selectionHistory: List<String>)
