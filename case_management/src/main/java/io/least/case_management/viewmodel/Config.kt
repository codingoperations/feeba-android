package io.least.case_management.viewmodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaseListConfig(val serverUrl: String, val profileId: String) : Parcelable