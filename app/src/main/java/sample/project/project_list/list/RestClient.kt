package sample.project.project_list.list

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.Date

enum class Environment {
    PRODUCTION, DEVELOPMENT
}

fun getFeebaService(env: Environment): FeebaService {
    val baseUrl = when (env) {
        Environment.PRODUCTION -> "https://api.github.com/"
        Environment.DEVELOPMENT -> "https://dev-api.github.com/"
    }
    val retrofit = Retrofit.Builder().baseUrl(baseUrl).build()
    return retrofit.create(FeebaService::class.java)
}

interface FeebaService {
    @POST("v1/dashboard/auth/login")
    fun login(@Body login: LoginRequest): Call<LoginResponse>

    @GET("/v1/admin/project")
    fun getProjects(): Call<List<Project>>
}

data class LoginRequest(
    val password: String, val username: String
)

data class LoginResponse(
    val jwt: String
)

data class Project(
    val id: String, val name: String, val tokens: List<Token>, val created: Date, val updated: Date
)

data class Token(
    val userId: String,
    val jwtToken: String,
    val created: Date,
)