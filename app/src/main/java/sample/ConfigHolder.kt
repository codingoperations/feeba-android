package sample

private val prodConfig = ServerConfig(
    "https://api.feeba.io",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjMzNDk4NjUsInBheWxvYWQiOnsidXNlcklkIjoiNjNmNDM5NGYzZDhmOTg3NjI1M2RhODU2IiwicHJvamVjdE5hbWUiOiJteS10YXhpIn0sImlhdCI6MTY3Njk0OTg2NX0.1LHodPBVt-sCB1A9xATkEidW7tQBBVOKzYoXR9NDx8Y"
)

private val devConfig = ServerConfig(
    "https://dev-api.feeba.io",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjIxMTQ4NDQsInBheWxvYWQiOnsidXNlcklkIjoiNjNlMGIzNDQ1MzdkNzg2YWY0Yzc4OGMyIiwicHJvamVjdE5hbWUiOiJmZWViYSJ9LCJpYXQiOjE2NzU3MTQ4NDR9.crhC6URHc0NH5D3x-7GKgHLV8CNFeP4wpnzjbiiAgzI" // for tests
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