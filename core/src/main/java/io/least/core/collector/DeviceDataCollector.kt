package io.least.core.collector

class DeviceDataCollector {
    fun collect(moduleVersion: String): CommonContext {
        return CommonContext(
            "android",
            moduleVersion,
            android.os.Build.VERSION.RELEASE_OR_CODENAME,
            android.os.Build.VERSION.SDK_INT.toString(),
            android.os.Build.MODEL.toString(),
        )
    }
}