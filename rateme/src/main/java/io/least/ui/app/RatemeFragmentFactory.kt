package io.least.ui.app

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.least.viewmodel.RateAppConfig

class RatemeFragmentFactory(
    private val config: RateAppConfig,
//    private val connector: Connector<String>?
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        Log.d("FragmentFactory", "instantiate(...)")
        return when (className) {
            RateAppFragment::class.java.name -> RateAppFragment(
                config,
//                connector
            )
            else -> super.instantiate(classLoader, className)
        }
    }
}