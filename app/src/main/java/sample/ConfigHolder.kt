package sample

enum class Environment {
    PRODUCTION, DEVELOPMENT
}
object ConfigHolder {
    var appToken = ""
    var baseUrl = ""
    var projectJwt = ""
}