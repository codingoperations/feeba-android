package sample

import android.app.Application
import io.feeba.Feeba
import io.least.core.ServerConfig
import sample.utils.PreferenceWrapper

class DemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceWrapper.init(this)
        ConfigHolder.setEnv(PreferenceWrapper.isProd)

        // This is how Feeba is initialized
        Feeba.init(
            this, ServerConfig(
                hostUrl = ConfigHolder.hostUrl,
                langCode = "en", // Whatever your default language is
                apiToken = ConfigHolder.jwtToken
            )
        )
    }
}