package io.least.core.collector

import android.os.Build

class DeviceDataCollector {
    fun collect(moduleVersion: String): CommonContext {
        return CommonContext(
            "android",
            moduleVersion,
            Build.VERSION.CODENAME,
            Build.VERSION.SDK_INT.toString(),
            Build.MODEL.toString(),
        )
    }
}