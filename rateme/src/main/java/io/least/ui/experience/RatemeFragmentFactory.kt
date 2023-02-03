package io.least.ui.experience

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.least.core.collector.UserSpecificContext
import io.least.core.ServerConfig
import io.least.data.RateExperienceConfig

class RateExperienceFragmentFactory(
    private val config: RateExperienceConfig?,
    private val serverConfig: ServerConfig,
    private val usersContext: UserSpecificContext,
    private val customView: View?
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        Log.d("FragmentFactory", "instantiate(...)")
        return when(className) {
            RateExperienceFragment::class.java.name -> RateExperienceFragment(config, serverConfig, usersContext, customView)
            else -> super.instantiate(classLoader, className)
        }
    }
}