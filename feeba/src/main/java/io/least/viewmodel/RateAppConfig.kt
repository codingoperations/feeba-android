package io.least.viewmodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RateAppConfig(
    val minPositiveRate: Float = 3.0f,
): Parcelable