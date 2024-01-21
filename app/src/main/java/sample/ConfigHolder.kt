package sample

private val prodConfig = ServerConfig(
    "https://api.feeba.io",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjIxMzMyNjEsInBheWxvYWQiOnsidXNlcklkIjoiNjNlMTY1NmI1YzQ2NWM4Y2U1NGE1NjY2IiwicHJvamVjdE5hbWUiOiJmZWViYSJ9LCJpYXQiOjE2NzU3MzMyNjF9.u1QKFr15wJQcEgyVjaLd0aU5A-XZoOaFGqFnGOSIXhE"
)

private val devConfig = ServerConfig(
    "https://dev-api.feeba.io",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NTUwNTg2NjAsInBheWxvYWQiOnsidXNlcklkIjoiNjM3NWIzYTFiODMyZDE3ZmI2YmE1NTFjIiwicHJvamVjdE5hbWUiOiJ6YWZhci10ZXN0In0sImlhdCI6MTY2ODY1ODY2MH0.qpCgz57-rkWfjI3NiAH_8sihpGyfScOixN20XlyDDMc" // for tests
)

object ConfigHolder {
    private var currentConfig = prodConfig

    val hostUrl: String get() = currentConfig.hostUrl
    val jwtToken: String get() = currentConfig.jwtToken

    fun setEnv(isProd: Boolean) {
        currentConfig = if (isProd) {
            prodConfig
        } else {
            devConfig
        }
    }
}

private data class ServerConfig(
    val hostUrl: String,
    val jwtToken: String,
)