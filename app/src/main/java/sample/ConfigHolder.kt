package sample

val prodBaseUrl = "https://api.feeba.io"
val devBaseUrl = "https://dev-api.feeba.io"
enum class Environment {
    PRODUCTION, DEVELOPMENT
}
object ConfigHolder {
    var hostUrl: String = prodBaseUrl
    var jwtToken = ""

    fun setEnv(env: Environment) {
        hostUrl = if (env == Environment.PRODUCTION) {
            prodBaseUrl
        } else {
            devBaseUrl
        }
    }
}

private data class ServerConfig(
    val hostUrl: String,
    val jwtToken: String,
)