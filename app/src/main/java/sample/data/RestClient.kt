package sample.data

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

val prodBaseUrl = "https://api.feeba.io"
val devBaseUrl = "https://dev-api.feeba.io"

object RestClient {
    private lateinit var service: FeebaService
    lateinit var jwt: String
    lateinit var baseUrl: String
    private val networkJson = Json { ignoreUnknownKeys = true }

    fun initLoggedUser(jwt: String, env: Environment) {
        switchEnvironment(env)
        // ensure desired jwt is set since switchEnvironment resets jwt to empty string
        this.jwt = jwt
    }

    fun switchEnvironment(env: Environment) {
        this.baseUrl = if (env == Environment.PRODUCTION) prodBaseUrl else devBaseUrl
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType())
            )
            .build()
        service = retrofit.create(FeebaService::class.java)
        this.jwt = ""

        ConfigHolder.baseUrl = baseUrl
    }

    suspend fun login(username: String, password: String): LoginResponse {
        val response = service.login(LoginRequest(password, username))
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
) : java.io.Serializable

@Serializable
data class Token(
    val userId: String,
    val jwtToken: String,
    val created: String,
)
