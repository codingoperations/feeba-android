package sample

import android.app.Application
import io.feeba.Feeba
import io.least.core.ServerConfig
import sample.utils.PreferenceWrapper

class DemoApplication: Application() {
    companion object {
        lateinit var instance: DemoApplication
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        PreferenceWrapper.init(this)
        Feeba.init(this, ServerConfig("en", "YOU_API_TOKEN"))
    }
}