package io.least.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HttpClient {

    @GET("/v1/rating/sdk/config")
    suspend fun fetchRateExperienceConfig(@Query("lang") langCode: String): Response<RateExperienceConfig>

    @POST("/v1/rating/sdk/user_rating")
    suspend fun publishResult(
        @Body result: RateExperienceResult,
        @Query("lang") langCode: String
    ): Response<Unit>
}
