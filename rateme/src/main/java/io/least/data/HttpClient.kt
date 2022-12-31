package io.least.data

import retrofit2.Response
import retrofit2.http.*

interface HttpClient {

    @GET("/v1/rating/sdk/config")
    suspend fun fetchRateExperienceConfig(@Query("lang") langCode: String): Response<RateExperienceConfig>

    @POST("/v1/rating/sdk/user_rating")
    suspend fun publishResult(
        @Body result: RateExperienceResult,
        @Query("lang") langCode: String
    ): Response<Unit>

    @PUT("/v1/rating/sdk/tag-selection")
    suspend fun tagSelected(@Body tagUpdate: TagUpdate, @Query("lang") langCode: String): Response<TagUpdate>
}
