package io.least.case_management.data

import retrofit2.Response
import retrofit2.http.Url


interface CaseListRepository {
    suspend fun fetchMyCases(@Url url: String): Response<List<CaseItem>>
}
