package sample.data

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import sample.ConfigHolder
import sample.Environment
import java.util.Date

object RestClient {
    private lateinit var service: FeebaService
    private lateinit var jwt: String
    val networkJson = Json { ignoreUnknownKeys = true }

    fun updateEnvironment() {
        val baseUrl = ConfigHolder.hostUrl
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType())
            )
            .build()

        service = retrofit.create(FeebaService::class.java)
    }

    suspend fun login(username: String, password: String): LoginResponse {
        val response = service.login(LoginRequest(password, username))
        jwt = response.jwt
        return response
    }

    suspend fun getProjects(): List<Project> {
        val jwtToken = "Bearer $jwt"
        return service.getProjects(jwtToken)
    }
}

interface FeebaService {
    @POST("v1/dashboard/auth/login")
    suspend fun login(@Body login: LoginRequest): LoginResponse

    @GET("/v1/admin/project")
    suspend fun getProjects(@Header("Authorization") authToken: String): List<Project>
}

@Serializable
data class LoginRequest(
    val password: String, val username: String
)

@Serializable
data class LoginResponse(
    val jwt: String
)

@Serializable
data class Project(
    val id: String,
    val name: String,
    val tokens: List<Token>,
    val created: String,
    val updated: String
)

@Serializable
data class Token(
    val userId: String,
    val jwtToken: String,
    val created: String,
)
