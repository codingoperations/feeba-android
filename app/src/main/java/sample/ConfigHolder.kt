package sample

enum class Environment {
    PRODUCTION, DEVELOPMENT
}
object ConfigHolder {
    var jwtToken = ""
}

private data class ServerConfig(
    val jwtToken: String,
)